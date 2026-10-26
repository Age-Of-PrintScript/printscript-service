plugins {
    id("printscript.common-conventions") version "1.0.4"
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "3.4.3"
}

group = "age.of.printscript"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Age-Of-PrintScript/printscript")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull
            password = System.getenv("GITHUB_TOKEN")
                ?: providers.gradleProperty("gpr.token").orNull
                ?: providers.gradleProperty("gpr.key").orNull
        }
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.3"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("printscript.grupo1:printscript-jobs:1.0.1")
    implementation("printscript.grupo1:engine:1.0.3")
    implementation("printscript.grupo1:linter:1.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

val jacocoExclusions = listOf(
    "**/PrintscriptServiceApplication*",
    "**/configuration/**",
    "**/shared/infrastructure/http/**",
)

tasks.named<JacocoReport>("jacocoTestReport") {
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(jacocoExclusions)
                }
            },
        ),
    )
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(jacocoExclusions)
                }
            },
        ),
    )
}
