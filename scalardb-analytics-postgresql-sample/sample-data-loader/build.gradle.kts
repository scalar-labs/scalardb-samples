plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.diffplug.spotless") version "6.24.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.scalar-labs:scalardb:3.10.2")
    implementation("org.apache.commons:commons-csv:1.10.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("sample.data.Main")
}

spotless {
    java {
        target("src/*/java/**/*.java")
        importOrder()
        removeUnusedImports()
        googleJavaFormat("1.17.0")
    }
}
