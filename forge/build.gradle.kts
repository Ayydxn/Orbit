plugins {
    id("com.gradleup.shadow")
}

loom {
    forge {
        mixinConfig("${rootProject.extra["mod_id"]}.mixins.json")
    }
}

architectury {
    platformSetupLoomIde()
    forge()
}

val shadowJar = tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar")

configurations {
    val common by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    configurations["compileClasspath"].extendsFrom(common)
    configurations["runtimeClasspath"].extendsFrom(common)
    configurations["developmentForge"].extendsFrom(common)

    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

dependencies {
    /* -- Core Dependencies -- */
    "forge"("net.minecraftforge:forge:${rootProject.extra["forge_version"]}")
    modImplementation("dev.architectury:architectury-forge:${rootProject.extra["architectury_api_version"]}")

    add("common", project(mapOf("path" to ":common", "configuration" to "namedElements")))
    add("shadowBundle", project(mapOf("path" to ":common", "configuration" to "transformProductionForge")))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    configurations = listOf(project.configurations["shadowBundle"])
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    dependsOn("shadowJar")

    inputFile.set(shadowJar.flatMap { it.archiveFile })
}
