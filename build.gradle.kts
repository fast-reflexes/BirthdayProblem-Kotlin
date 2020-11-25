plugins {
    java
    kotlin("jvm") version "1.4.0"
    application
}

group = "com.bdayprob"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("lib/big-math-2.3.0.jar"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.+")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("org.assertj:assertj-core:3.16.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}

repositories {
    mavenCentral()
}

tasks.jar {
    manifest.attributes.apply {
        put("Implementation-Title", "BirthdayProblem Solver thin jar")
        put("Implementation-Version", "1.0")
        put("Main-Class", "com.bdayprob.BirthdayProblem\$CLISolver")
    }
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")
    manifest.attributes.apply {
        put("Implementation-Title", "BirthdayProblem Solver fat jar")
        put("Implementation-Version", "1.0")
        put("Main-Class", "com.bdayprob.BirthdayProblem\$CLISolver")
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") && !it.name.contains(Regex("(stdlib|reflect)")) }
            .map { zipTree(it) }
    })
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED, org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED, org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED)
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

application {
    mainClassName = "com.bdayprob.BirthdayProblem\$CLISolver"
}
