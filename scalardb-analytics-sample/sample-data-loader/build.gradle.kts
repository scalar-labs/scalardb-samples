plugins {
    application
    id("com.diffplug.spotless") version "6.24.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.scalar-labs:scalardb:3.16.1")
    implementation("com.scalar-labs:scalardb-schema-loader:3.16.1")
    implementation("org.apache.commons:commons-csv:1.10.0")

    implementation("io.netty:netty-transport-native-epoll:4.1.99.Final:linux-x86_64")
    implementation("io.netty:netty-transport-native-epoll:4.1.99.Final:linux-aarch_64")
    implementation("io.netty:netty-transport-native-kqueue:4.1.99.Final:osx-x86_64")
    implementation("io.netty:netty-transport-native-kqueue:4.1.99.Final:osx-aarch_64")
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
