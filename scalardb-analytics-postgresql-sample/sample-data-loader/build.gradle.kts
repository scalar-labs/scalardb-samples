plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
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
