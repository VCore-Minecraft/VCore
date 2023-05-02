plugins {
    `java-library`
    application
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.0" apply true
}

group = "de.verdox.vcore"
version = "1.0"
description = "VCore"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation("de.verdox.vpipeline:VPipeline:1.2")
    implementation("de.tr7zw:item-nbt-api:2.11.2")
    implementation("com.jeff_media:CustomBlockData:2.2.0")
    implementation(project(":PaperUtil"))
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.github.waterfallmc:waterfall-api:1.18-R0.1-SNAPSHOT")
}

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    repositories {
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    }
}


tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
        options.forkOptions.jvmArgs?.add("--add-opens java.base/java.time=ALL-UNNAMED");
        options.forkOptions.jvmArgs?.add("--add-exports java.base/jdk.internal.misc=ALL-UNNAMED");
    }

    shadowJar {
        archiveBaseName.set("VCore")
        archiveVersion.set(version.toString())

        relocate("de.tr7zw.changeme.nbtapi", "de.verdox.vcore.impl.gameserver.paper.nbt")
        relocate("com.jeff_media.customblockdata", "de.verdox.vcore.impl.gameserver.paper.customblockdata")
        project.setProperty("mainClassName", "com.your.MainClass")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}



publishing {

    publications.create<MavenPublication>("shadow").from(components["java"]);
    /*    publications.create<MavenPublication>("maven").artifact("/build/libs/$description-$version.jar")*/

    repositories.maven(repositories.mavenLocal())
}