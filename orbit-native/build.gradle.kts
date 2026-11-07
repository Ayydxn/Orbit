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

val osName: String = System.getProperty("os.name").lowercase()
val osArchitecture: String = System.getProperty("os.arch").lowercase()

val isWindows = osName.startsWith("windows")
val isMacOS = osName.contains("mac")
val isLinux = osName.contains("linux")

val platform: String = (findProperty("platform") as String?) ?: when {
    isWindows -> "windows-x64"
    isMacOS -> "macos-arm64"
    isLinux -> "linux-x64"
    else -> throw GradleException("Unsupported host operating system: $osName ($osArchitecture)")
}

val nativeLibraryName = when (platform) {
    "windows-x64" -> "OrbitJNIBridge.dll"
    "macos-arm64" -> "libOrbitJNIBridge.dylib"
    else -> "libOrbitJNIBridge.so"
}

val nativeChecksumDirectory = layout.buildDirectory.dir("native-checksums/$platform").get().asFile
val cmakeBuildDirectory = layout.buildDirectory.dir("cmake-build").get().asFile

val ffmJavaVersion = 22

// Multi-config generators like Visual Studio put the library in a per-configuration folder, single-config generators such as Makefiles don't.
fun findBuiltNativeLibrary(): File? = listOf(
    File(cmakeBuildDirectory, "Release/$nativeLibraryName"),
    File(cmakeBuildDirectory, nativeLibraryName)
).firstOrNull { it.exists() }

repositories {
    mavenCentral()
}

dependencies {
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

sourceSets {
    create("ffm") {
        java.srcDirs("src/ffm/java")

        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val configureCMake = tasks.register("configureCMake", Exec::class) {
    group = "native"
    description = "Runs CMake to configure the project files for our native build"

    onlyIf { ultralightSdkLocation.isNotBlank() }

    doFirst { cmakeBuildDirectory.mkdirs() }

    workingDir = cmakeBuildDirectory

    commandLine(
        "cmake",
        file("src/main/native").absolutePath,
        "-DULTRALIGHT_SDK=${ultralightSdkLocation}",
        "-DCMAKE_BUILD_TYPE=Release",
        "-G", if (isWindows) "Visual Studio 18 2026" else "Unix Makefiles"
    )
}

val compileNative = tasks.register("compileNative", Exec::class) {
    group = "native"
    description = "Compiles the native C++ code via CMake"

    dependsOn(configureCMake)

    onlyIf { ultralightSdkLocation.isNotBlank() }

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

    dependsOn(compileNative)

    onlyIf { ultralightSdkLocation.isNotBlank() }

    from(cmakeBuildDirectory) {
        include("Release/$nativeLibraryName", nativeLibraryName)
        eachFile { relativePath = RelativePath(true, "natives", platform, name) }

        includeEmptyDirs = false
    }

    into(layout.buildDirectory.dir("generated/orbit-natives"))

    logger.quiet("[orbit-native] Staged native library to ${layout.buildDirectory.dir("generated/orbit-natives").get().asFile.absolutePath}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<JavaCompile>("compileJava") {
    options.release = 17
}

tasks.named<JavaCompile>(sourceSets.getByName("ffm").compileJavaTaskName) {
    options.release = ffmJavaVersion

    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(ffmJavaVersion)
        vendor = JvmVendorSpec.AZUL
    }
}

tasks.jar {
    into("META-INF/versions/$ffmJavaVersion") {
        from(sourceSets.getByName("ffm").output)
    }

    manifest {
        attributes("Multi-Release" to true)
    }
}

tasks.processResources {
    from(stageJNIBridgeLibrary)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
