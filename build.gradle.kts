import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    `java-library`

    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
}

group = "pl.karoldronia"
version = "1.0-SNAPSHOT"

val mainPackage = "pl.karoldronia.artefacts"
val projectPrefix = "artefacts"

repositories {
    mavenCentral()

    maven("https://storehouse.okaeri.eu/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.eternalcode.pl/releases")
}

dependencies {
    // -- paper --
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    // -- configs --
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.6")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.6")
    implementation("eu.okaeri:okaeri-configs-serdes-commons:5.0.6")
    implementation("eu.okaeri:okaeri-configs-json-simple:5.0.6")

    // -- commons
    implementation("net.kyori:adventure-platform-bukkit:4.3.1")
    implementation("net.kyori:adventure-text-minimessage:4.22.0")

    // -- notifications --
    implementation("com.eternalcode:multification-bukkit:1.1.4")
    implementation("com.eternalcode:multification-okaeri:1.1.4")

    // -- lombok --
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    // -- database --
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("eu.okaeri:okaeri-persistence-jdbc:2.0.4")

    // -- expiring map --
    implementation("net.jodah:expiringmap:0.5.11")

    // -- super vanish --
    compileOnly("com.github.LeonMangler:SuperVanish:6.2.18-3")

    // -- commands --
    implementation("dev.rollczi:litecommands-bukkit:3.9.7")
}

paper {
    name = "FireballArtefacts"
    version = "${project.version}"
    author = "Karol Dronia"
    prefix = "FireballArtefacts"
    apiVersion = "1.21"

    main = "$mainPackage.ArtefactsPlugin"

    serverDependencies {
        register("ProtocolLib") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
            joinClasspath = true
        }
        register("SuperVanish") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
    }
}

tasks.shadowJar {
    archiveFileName.set("FireballArtefacts v${project.version}.jar")

    exclude(
        "org/intellij/lang/annotations/**",
        "org/jetbrains/annotations/**",
        "META-INF/**"
    )

    listOf(
        "dev.rollczi",
        "eu.okaeri",
        "panda",
        "org.yaml",
        "com.eternalcode.commons",
        "net.jodah",
    ).forEach { relocate(it, "$mainPackage.$it") }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.runServer {
    version("1.21.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}