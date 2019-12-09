buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    id("kotlin-multiplatform") version "1.3.60"
    id("kotlinx-serialization") version "1.3.60"
    id("org.jetbrains.kotlin.frontend") version "0.0.45"
    id("com.github.node-gradle.node") version "1.3.0"
}

group = "fuzzer"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    maven("https://dl.bintray.com/kotlin/ktor")
    maven("https://dl.bintray.com/kotlin/kotlin-js-wrappers")
    maven("https://dl.bintray.com/kotlin/kotlinx")
    mavenCentral()
    google()
}

apply("plugin" to "com.github.node-gradle.node")


val kotlin_version = "1.3.60"
val ktor_version = "1.1.4"
val logback_version = "1.2.3"

val configureNodePlugin: Project.() -> Unit = {

    node {
        // Version of node to use
        version = "10.15.3"

        // Base URL for fetching node distributions (change if you have a mirror)
        distBaseUrl = "https://nodejs.org/dist"

        // If true, it will download node using above parameters.
        // If false, it will try to use globally installed node.
        download = true

        // Set the work directory for unpacking node
        workDir = file("${projectDir}/build/nodejs")

        // Set the work directory for NPM
        npmWorkDir = file("${projectDir}/build/npm")

        // Set the work directory where node_modules should be located
        nodeModulesDir = file("${projectDir}/build")
    }
}

ext["configureNodePlugin"] = configureNodePlugin

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    js {
        compilations.all {
            kotlinOptions {
                languageVersion = "1.3"
                moduleKind = "commonjs"
                sourceMap = true
                sourceMapEmbedSources = "always"
                metaInfo = true
            }
        }

        compilations.getByName("main") {
            kotlinOptions.main = "call"
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.14.0")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        getByName("jvmMain") {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("io.ktor:ktor-client-apache:$ktor_version")
                implementation("io.ktor:ktor-jackson:$ktor_version")
                implementation("io.ktor:ktor-html-builder:$ktor_version")
                implementation("ch.qos.logback:logback-classic:$logback_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
                implementation("org.jetbrains.exposed:exposed:0.13.2")
                implementation("org.apache.commons:commons-csv:1.1")
                implementation("org.apache.commons:commons-text:1.8")
                implementation("org.jsoup:jsoup:1.11.3")
                implementation("log4j:log4j:1.2.17")
                implementation("org.slf4j:slf4j-simple:1.7.29")
                implementation("org.apache.commons:commons-csv:1.1")
                implementation("org.kodein.di:kodein-di-generic-jvm:6.4.1")
//                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
//              implementation group: "com.android.tools.external.com-intellij", name: "kotlin-compiler", version: "26.5.0"
                implementation("org.jetbrains.kotlin:kotlin-compiler:$kotlin_version")
            }
        }
        getByName("jvmTest") {
            dependencies {
                //                implementation(kotlin("test"))
//                implementation(kotlin("test-junit"))
            }
        }
        getByName("jsMain") {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("io.ktor:ktor-client-js:$ktor_version")
                implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.69-kotlin-1.3.21")
                implementation("org.jetbrains:kotlin-css-js:1.0.0-pre.69-kotlin-1.3.21")
                implementation("org.jetbrains:kotlin-react:16.6.0-pre.69-kotlin-1.3.21")
                implementation("org.jetbrains:kotlin-react-dom:16.6.0-pre.69-kotlin-1.3.21")
                implementation("org.jetbrains:kotlin-styled:1.0.0-pre.69-kotlin-1.3.21")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.14.0")
            }
        }
        getByName("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}


kotlinFrontend {
    npm {
        devDependency("mocha", "6.0.2")
        devDependency("karma", "4.0.1")
        devDependency("karma-mocha", "1.3.0")
        devDependency("karma-chrome-launcher", "2.2.0")
        devDependency("karma-sourcemap-loader", "0.3.7")
        devDependency("karma-viewport", "1.0.4")
        devDependency("karma-webpack", "3.0.5")
        devDependency("webpack", "^4.0.0")
        devDependency("enzyme", "3.9.0")
        devDependency("enzyme-adapter-react-16", "1.12.1")

        dependency("core-js", "^2.0.0")
        dependency("svg-inline-loader", "0.8.0")

        dependency("text-encoding")

        dependency("@jetbrains/kotlin-extensions", "1.0.1-pre.67")
        dependency("@jetbrains/kotlin-css", "1.0.0-pre.67")
        dependency("@jetbrains/kotlin-css-js", "1.0.0-pre.67")

        dependency("react", "16.8.3")
        dependency("react-dom", "16.8.3")
        dependency("inline-style-prefixer", "5.0.4")
        dependency("styled-components", "3.4.10")

        dependency("@jetbrains/logos", "1.1.4")
        dependency("@jetbrains/ring-ui", "2.0.0-beta.11")
    }

    sourceMaps = false

    bundle<org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension>("webpack") {
        this as org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension
        port = 8080
        bundleName = "main"
        proxyUrl = "http://0.0.0.0:8081"
        sourceMapEnabled = true
        mode = "development"
        mode = "production"
    }
}

println("CONFIG SSS")
project.afterEvaluate {
    configurations.all {
        try {
            println(name)
        } catch (ex: Throwable) {
            //
        }
    }
//    println(configurations.getByName("jvmMainApi").toList())
    val jars = configurations.getByName("jvmCompileClasspath").toList()
    val jars2 = configurations.getByName("jvmRuntimeClasspath").toList()
    val jars3 = configurations.getByName("compileClasspath").toList()
    val jars4 = configurations.getByName("runtimeClasspath").toList()
    val filtered = (jars + jars2 + jars3 + jars4).filterNot {
        "core-1.1.1.jar" in it.name
                || "common-1.3.2" in it.name
//                || "coroutines-io-0.1.16" in it.name
                || "coroutines-core" in it.name
//                || "coroutines" in it.name
//        false
    }
    filtered.forEach {
        println(it)
    }
    val deps = files(*filtered.toSet().toTypedArray())
    val runMain by tasks.creating(JavaExec::class) {
        dependsOn(tasks.getByName("jvmJar"))
        classpath = deps + files("$buildDir/classes/kotlin/jvm/main")

        main = "com.github.recognized.Main"
    }
}


