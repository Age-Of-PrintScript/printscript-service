pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Age-Of-PrintScript/gradle-conventions")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull
                password = System.getenv("GITHUB_TOKEN")
                    ?: providers.gradleProperty("gpr.token").orNull
                            ?: providers.gradleProperty("gpr.key").orNull
            }
        }
    }
}
rootProject.name = "printscript-service"

// Permite usar la version de printscript que este local en mi maquina, en el ci
// no existe asi que usa la libreria publicada en maven
if (file("../printscript").isDirectory) {
    includeBuild("../printscript")
}
