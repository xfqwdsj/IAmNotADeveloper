rootProject.name = "IAmNotADeveloper"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://androidx.dev/snapshots/builds/13944877/artifacts/repository")
        maven("https://api.xposed.info/")
        maven("https://maven.kr328.app/releases")
    }
}

include(":app")
