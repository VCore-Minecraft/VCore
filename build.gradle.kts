plugins {
    `java-library`
    application
    `maven-publish`
    id("io.ktor.plugin") version "2.2.1"
}

group = "de.verdox"
version = "1.0"
description = "VCore"

repositories {
    mavenCentral()
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withJavadocJar()
    withSourcesJar()
}

application{
    mainClass.set("de.verdox.vcore.impl.gameserver.paper.VCorePaper")
}

ktor{
    fatJar {
        archiveFileName.set("fat.jar")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    implementation("de.verdox.vpipeline:VPipeline:1.0")
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
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

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "com.mkyong.DateUtils"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
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

    build {
        dependsOn(fatJar)
    }
/*    jar {
        manifest.attributes["Main-Class"] = "com.example.MyMainClass"
        val dependencies = configurations
            .runtimeClasspath
            .get()
            .map(::zipTree) // OR .map { zipTree(it) }
        from(dependencies)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }*/

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}



publishing {
    publications.create<MavenPublication>("maven").from(components["java"]);
    publications {
        create<MavenPublication>("lib") {
            artifact("/build/libs/$description-$version.jar")
        }
    }
    repositories.maven(repositories.mavenLocal())
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}