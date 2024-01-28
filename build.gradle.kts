plugins {
    java
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.bdayprob"
version = "1.4.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(files("lib/big-math-2.3.2.jar"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

repositories {
    mavenCentral()
}

tasks.jar {
    manifest.attributes.apply {
        put("Implementation-Title", "BirthdayProblem Solver thin jar")
        put("Implementation-Version", version)
        put("Main-Class", "com.bdayprob.BirthdayProblem\$CLISolver")
    }
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")
    manifest.attributes.apply {
        put("Implementation-Title", "BirthdayProblem Solver fat jar")
        put("Implementation-Version", version)
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
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

application {
    mainClass.set("com.bdayprob.BirthdayProblem\$CLISolver")
}
