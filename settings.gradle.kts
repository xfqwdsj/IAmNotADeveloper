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
        maven("https://androidx.dev/snapshots/builds/13945475/artifacts/repository")
        maven("https://api.xposed.info/")
        maven("https://jitpack.io")
        maven("https://maven.kr328.app/releases")
    }
}

include(":app")
