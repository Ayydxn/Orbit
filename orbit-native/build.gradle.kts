plugins {
    id("java")
    id("c")
}

version = "${rootProject.extra["mod_version"]}"

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

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
