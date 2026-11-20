import java.security.MessageDigest
import java.util.*

plugins {
    id("java")
}

version = "${rootProject.extra["mod_version"]}"

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists())
        load(localPropertiesFile.inputStream())
}

val ultralightSdkLocation: String = (localProperties.getProperty("ultralight_sdk_location") ?: findProperty("ultralight_sdk_location") as String? ?: "").also {
    if (it.isBlank()) {
        logger.warn(
            "\n[orbit-native] WARNING: 'ultralight_sdk_location' is not set. JNI bridge library compilation will be skipped.\n" +
                    "  Set it in local.properties or pass -Pultralight_sdk_location=<path>.\n"
        )
    }
}

// When set, the native build is skipped, and this directory (laid out as <platform>/<library>) is bundled as-is instead.
// CI uses this, so the final mod JAR contains the exact JNI bridge binaries that were built and tested.
val prebuiltNativesLocation: String = (findProperty("orbit_prebuilt_natives") as String?) ?: ""
val usePrebuiltNatives = prebuiltNativesLocation.isNotBlank()

val osName: String = System.getProperty("os.name").lowercase()
val osArchitecture: String = System.getProperty("os.arch").lowercase()

val isWindows = osName.startsWith("windows")
val isMacOS = osName.contains("mac")
val isLinux = osName.contains("linux")

val isX64 = osArchitecture == "amd64" || osArchitecture == "x86_64"
val isArm64 = osArchitecture == "aarch64" || osArchitecture == "arm64"

// These platform ids and library file names must stay in sync with NativePlatform.java in common.
val platform: String = (findProperty("platform") as String?) ?: when {
    isWindows && isX64 -> "windows-x64"
    isMacOS && isArm64 -> "macos-arm64"
    isLinux && isX64 -> "linux-x64"
    else -> throw GradleException("Unsupported host platform: $osName ($osArchitecture). Supported platforms are windows-x64, macos-arm64 and linux-x64. Use  -Pplatform=<id> to override.")
}

val nativeLibraryName = when (platform) {
    "windows-x64" -> "OrbitJNIBridge.dll"
    "macos-arm64" -> "libOrbitJNIBridge.dylib"
    else -> "libOrbitJNIBridge.so"
}

val nativeChecksumDirectory = layout.buildDirectory.dir("native-checksums/$platform").get().asFile
val cmakeBuildDirectory = layout.buildDirectory.dir("cmake-build").get().asFile

val contractPackage = "me.ayydxn.orbit.natives.ultralight"
val contractDirectory = file("src/main/java/${contractPackage.replace('.', '/')}")

// Only contracts that opt in are generated, so hand-written ones like are left alone.
val contractClassNames: List<String> = contractDirectory.listFiles { file -> file.extension == "java" }
    .orEmpty()
    .filter { it.readText().contains("@NativeContract") }
    .map { "$contractPackage.${it.nameWithoutExtension}" }
    .sorted()

val generatedBindingsDirectory = layout.buildDirectory.dir("generated/bindings")
val generatedJavaDirectory = generatedBindingsDirectory.map { it.dir("java") }
val generatedNativeDirectory = generatedBindingsDirectory.map { it.dir("native") }

// Multi-config generators like Visual Studio put the library in a per-configuration folder, single-config generators such as Makefiles don't.
fun findBuiltNativeLibrary(): File? = listOf(
    File(cmakeBuildDirectory, "Release/$nativeLibraryName"),
    File(cmakeBuildDirectory, nativeLibraryName)
).firstOrNull { it.exists() }

fun testLibraryPath(): String
{
    val ultralightBin = file("$ultralightSdkLocation/bin").absolutePath
    val nativeLibraryBin = findBuiltNativeLibrary()?.parentFile?.absolutePath
        ?: throw GradleException("Unit tests unavailable because $nativeLibraryName was not found in $cmakeBuildDirectory (checked Release/ and the build root).")

    return "$nativeLibraryBin${File.pathSeparator}$ultralightBin"
}

repositories {
    mavenCentral()
}

sourceSets {
    create("generator") {
        java.srcDir("src/main/java")
        java.srcDir("src/generator/java")

        java.include("me/ayydxn/orbit/natives/annotations/**", "me/ayydxn/orbit/natives/generator/**")

        contractClassNames.forEach {
            java.include("${it.replace('.', '/')}.java")
        }
    }

    main {
        java.srcDir(generatedJavaDirectory)
    }

    test {
        compileClasspath += sourceSets.main.get().compileClasspath
    }
}

dependencies {
    compileOnly("com.ibm.icu:icu4j:${rootProject.property("icu4j_version").toString()}")
    compileOnly("org.jetbrains:annotations:${rootProject.extra["jetbrains_annotations_version"]}")
    compileOnly(sourceSets.named("generator").get().output)

    /*--------------------*/
    /* -- Unit Testing -- */
    /*--------------------*/

    testImplementation(platform("org.junit:junit-bom:${rootProject.extra["junit_version"]}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val generateNativeBindings = tasks.register<JavaExec>("generateBindings") {
    group = "native"
    description = "Generates the JNI Java classes and C++ glue from the @NativeContract interfaces."

    classpath = sourceSets.named("generator").get().runtimeClasspath
    mainClass = "me.ayydxn.orbit.natives.generator.NativeBindingGenerator"

    outputs.dir(generatedJavaDirectory)
    outputs.dir(generatedNativeDirectory)

    args(
        listOf(
            generatedJavaDirectory.get().asFile.absolutePath,
            generatedNativeDirectory.get().asFile.absolutePath
        ) + contractClassNames
    )

    // A contract that was removed or renamed shouldn't leave its old generated files behind.
    doFirst {
        generatedJavaDirectory.get().asFile.deleteRecursively()
        generatedNativeDirectory.get().asFile.deleteRecursively()
    }
}

val configureCMake = tasks.register("configureCMake", Exec::class) {
    group = "native"
    description = "Runs CMake to configure the project files for our native build"

    dependsOn(generateNativeBindings)

    onlyIf { !usePrebuiltNatives && ultralightSdkLocation.isNotBlank() }

    doFirst { cmakeBuildDirectory.mkdirs() }

    workingDir = cmakeBuildDirectory

    commandLine(
        "cmake",
        file("src/main/native").absolutePath,
        "-DULTRALIGHT_SDK=${ultralightSdkLocation}",
        "-DORBIT_GENERATED_DIR=${generatedNativeDirectory.get().asFile.absolutePath}",
        "-DCMAKE_BUILD_TYPE=Release",
        "-G", if (isWindows) "Visual Studio 18 2026" else "Unix Makefiles"
    )
}

val compileNative = tasks.register("compileNative", Exec::class) {
    group = "native"
    description = "Compiles the native C++ code via CMake"

    dependsOn(configureCMake)

    onlyIf { !usePrebuiltNatives && ultralightSdkLocation.isNotBlank() }

    workingDir = cmakeBuildDirectory
    commandLine("cmake", "--build", ".", "--config", "Release", "--parallel")

    doLast {
        val produced = findBuiltNativeLibrary()
            ?: throw GradleException("CMake finished, but $nativeLibraryName was not found in $cmakeBuildDirectory (checked Release/ and the build root).")

        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(produced.readBytes())
            .joinToString("") { "%02x".format(it) }

        // Written outside the bundled resources so it never ships inside the jar.
        nativeChecksumDirectory.mkdirs()

        val checksumFile = File(nativeChecksumDirectory, "$nativeLibraryName.sha256")
        checksumFile.writeText(checksum)

        logger.quiet("[orbit-native] Wrote native library checksum to ${checksumFile.absolutePath}")
    }
}

val stageJNIBridgeLibrary = tasks.register<Sync>("stageJNIBridgeLibrary") {
    group = "native"
    description = "Stages the compiled JNI bridge library so it's bundled into the jar's resources"

    if (usePrebuiltNatives)
    {
        val prebuiltNativesDirectory = rootProject.file(prebuiltNativesLocation)

        doFirst {
            if (!prebuiltNativesDirectory.isDirectory)
                throw GradleException("orbit_prebuilt_natives points to $prebuiltNativesDirectory which isn't a directory.")
        }

        from(prebuiltNativesDirectory) {
            into("natives")
        }
    }
    else
    {
        dependsOn(compileNative)

        onlyIf { ultralightSdkLocation.isNotBlank() }

        from(cmakeBuildDirectory) {
            include("Release/$nativeLibraryName", nativeLibraryName)
            eachFile { relativePath = RelativePath(true, "natives", platform, name) }

            includeEmptyDirs = false
        }
    }

    into(layout.buildDirectory.dir("generated/orbit-natives"))

    logger.quiet("[orbit-native] Staged native library to ${layout.buildDirectory.dir("generated/orbit-natives").get().asFile.absolutePath}")
}

listOf("compileJava", "sourcesJar", "javadoc").forEach { taskName ->
    tasks.named(taskName) { dependsOn(generateNativeBindings) }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    failOnNoDiscoveredTests = false

    // Every full run checks that each contract method was exercised.
    // The orbitSkipContractCoverage is passed when you only want to run part of the suite.
    systemProperty("orbit.test.enforceContractCoverage", (!project.hasProperty("orbit.SkipContractCoverage")).toString())

    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.named<JavaCompile>("compileJava") {
    options.release = 17
}

tasks.named<JavaCompile>(sourceSets.getByName("generator").compileJavaTaskName) {
    options.release = 17
    options.compilerArgs.add("-parameters")
}

tasks.processResources {
    from(stageJNIBridgeLibrary)
}

tasks.test {
    dependsOn(compileNative)

    doFirst {
        systemProperty("java.library.path", testLibraryPath())
    }
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:-missing", true)
}
