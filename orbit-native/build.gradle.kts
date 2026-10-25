import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.nativeplatform.platform.internal.DefaultOperatingSystem
import java.util.Properties

plugins {
    id("java")
    id("c")
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
            "\n[ultralight-jni] WARNING: 'ultralight_sdk_location' is not set.\n" +
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

        val produced = file("$cmakeBuildDirectory/Release/$libName")
        if (produced.exists()) {
            copy {
                from(produced)
                into(layout.buildDirectory.dir("resources/main/natives/$platform"))
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 17
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
