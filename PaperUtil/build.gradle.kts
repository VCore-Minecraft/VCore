plugins {
    `java-library`
    `maven-publish`
}

group = "de.verdox.vcore"
description = "PaperUtil"
version = "1.0"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withJavadocJar()
    withSourcesJar()
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
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")
}