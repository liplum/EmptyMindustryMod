import java.io.ByteArrayOutputStream
plugins {
    kotlin("jvm") version "1.6.10"
    groovy
    java
    scala
}
sourceSets {
    main {
        java.srcDir("src")
        resources.srcDir("resources")
        groovy.srcDir("src")
        scala.srcDir("src")
    }
    test {
        java.srcDir("test")
        resources.srcDir("resources")
        groovy.srcDir("src")
        scala.srcDir("src")
    }
}

group = "net.liplum"
version = "1.0"
val sdkRoot: String? by extra(System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT"))
val outputJarName = "EmptyMod"

repositories {
    mavenCentral()
    maven {
        url = uri("https://www.jitpack.io")
    }
}

dependencies {
    compileOnly("com.github.Anuken.Mindustry:core:v135")
    implementation(kotlin("stdlib"))
    implementation("org.scala-lang:scala-library:2.13.8")
    implementation("org.codehaus.groovy:groovy-all:3.0.10")
    testImplementation("org.scalatest:scalatest_2.11:3.2.12")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${outputJarName}Desktop.jar")
    includeEmptyDirs = false
    exclude("**/**/*.java")
    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )

    from(rootDir) {
        include("mod.hjson")
        include("icon.png")
    }

    from("$rootDir/assets") {
        include("**")
    }
}

tasks {
    register("jarAndroid") {
        group = "build"
        dependsOn("jar")

        doLast {
            val sdkRoot = sdkRoot
            if (sdkRoot == null || !File(sdkRoot).exists())
                throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
            val platformRoot = File("$sdkRoot/platforms/").listFiles()!!.sorted().reversed()
                .find { f -> File(f, "android.jar").exists() }
                ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")
            //collect dependencies needed for desugaring
            val allDependencies = configurations.compileClasspath.get().toList() +
                    configurations.runtimeClasspath.get().toList() +
                    listOf(File(platformRoot, "android.jar"))
            val dependencies = allDependencies.joinToString(" ") { "--classpath ${it.path}" }
            //dex and desugar files - this requires d8 in your PATH
            val paras = "$dependencies --min-api 14 --output ${outputJarName}Android.jar ${outputJarName}Desktop.jar"
            try {
                exec {
                    commandLine = "d8 $paras".split(' ')
                    workingDir = File("$buildDir/libs")
                    standardOutput = System.out
                    errorOutput = System.err
                }
            } catch (_: Exception) {
                val cmdOutput = ByteArrayOutputStream()
                logger.lifecycle("d8 cannot be found in your PATH, so trying to use an absolute path.")
                exec {
                    commandLine = listOf("where", "d8")
                    standardOutput = cmdOutput
                    errorOutput = System.err
                }
                val d8FullPath = cmdOutput.toString().replace("\r", "").replace("\n", "")
                exec {
                    commandLine = "$d8FullPath $paras".split(' ')
                    workingDir = File("$buildDir/libs")
                    standardOutput = System.out
                    errorOutput = System.err
                }
            }
        }
    }
    register<Jar>("deploy") {
        group = "build"
        dependsOn("jarAndroid")
        archiveFileName.set("${outputJarName}.jar")

        from(
            zipTree("$buildDir/libs/${outputJarName}Desktop.jar"),
            zipTree("$buildDir/libs/${outputJarName}Android.jar")
        )

        doLast {
            delete {
                delete(
                    "$buildDir/libs/${outputJarName}Desktop.jar",
                    "$buildDir/libs/${outputJarName}Android.jar"
                )
            }
        }
    }
}