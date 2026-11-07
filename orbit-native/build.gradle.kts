import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.nativeplatform.platform.internal.DefaultOperatingSystem
import java.security.MessageDigest
import java.util.Properties

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
            "\n[orbit-native] WARNING: 'ultralight_sdk_location' is not set.\n" +
                    "  Native compilation will be skipped.\n" +
                    "  Set it in local.properties or pass -Pultralight_sdk_location=<path>.\n"
        )
    }
}

val operatingSystem: DefaultOperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
val osArchitecture = DefaultNativePlatform.getCurrentArchitecture().name

val platform: String = (findProperty("platform") as String?) ?: when {
    operatingSystem.isWindows -> "windows-x64"
    operatingSystem.isMacOsX ->"macos-arm64"
    operatingSystem.isLinux -> "linux-x64"
    else -> throw GradleException("Unsupported host operating system: ${operatingSystem.name} ($osArchitecture)")
}

val cmakeBuildDirectory = layout.buildDirectory.dir("cmake-build").get().asFile
val ffmJavaVersion = 22

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
        "-G", if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) "Visual Studio 18 2026" else "Unix Makefiles"
    )
}

val compileNative = tasks.register("compileNative", Exec::class) {
    group = "native"
    description = "Compiles the native C++ code via CMake"

    dependsOn(configureCMake)

    onlyIf { ultralightSdkLocation.isNotBlank() }

    workingDir = cmakeBuildDirectory
    commandLine("cmake", "--build", ".", "--config", "Release", "--parallel")

    // Copy the produced shared lib into resources so it's on the classpath
    doLast {
        val libName = when (platform) {
            "windows-x64" -> "OrbitJNIBridge.dll"
            "macos-arm64" -> "libOrbitJNIBridge.dylib"
            else -> "libOrbitJNIBridge.so"
        }

        val produced = listOf(
            File(cmakeBuildDirectory, "Release/$libName"),
            File(cmakeBuildDirectory, libName)
        ).firstOrNull { it.exists() }
            ?: throw GradleException("CMake finished, but $libName was not found in $cmakeBuildDirectory (checked Release/ and the build root).")
        val nativesDirectory = layout.buildDirectory.dir("resources/main/natives/$platform").get().asFile

        copy {
            from(produced)
            into(nativesDirectory)
        }

        val checksumDirectory = layout.buildDirectory.dir("native-checksums/$platform").get().asFile
        checksumDirectory.mkdirs()

        val checksum = MessageDigest.getInstance("SHA-256")
            .digest(produced.readBytes())
            .joinToString("") { "%02x".format(it) }

        val checksumFile = File(checksumDirectory, "$libName.sha256")
        checksumFile.writeText(checksum)

        logger.quiet("[orbit-native] Copied $libName to ${nativesDirectory.absolutePath} and wrote checksum to ${checksumFile.absolutePath}")
    }
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
    dependsOn(compileNative)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
