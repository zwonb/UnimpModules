pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

rootProject.name = "uniapp-debug"
include(":app")
//include(":uts")
include(":compose")
include(":barcode-scan")
include(":record-video")
//include(":unimp-record-video")
