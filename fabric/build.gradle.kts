plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val shadowJar = tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar")

configurations {
    val common by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }

    configurations["compileClasspath"].extendsFrom(common)
    configurations["runtimeClasspath"].extendsFrom(common)
    configurations["developmentFabric"].extendsFrom(common)

    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

dependencies {
    /* -- Core Dependencies -- */
    modImplementation("net.fabricmc:fabric-loader:${rootProject.extra["fabric_loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.extra["fabric_api_version"]}")
    modImplementation("dev.architectury:architectury-fabric:${rootProject.extra["architectury_api_version"]}")

    /* -- Mod Dependencies -- */
    modImplementation("com.terraformersmc:modmenu:${rootProject.extra["modmenu_version"]}")

    add("common", project(mapOf("path" to ":common", "configuration" to "namedElements")))
    add("shadowBundle", project(mapOf("path" to ":common", "configuration" to "transformProductionFabric")))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    configurations = listOf(project.configurations["shadowBundle"])
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    dependsOn("shadowJar")

    inputFile.set(shadowJar.flatMap { it.archiveFile })
}
