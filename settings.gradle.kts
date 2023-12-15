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
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            // Do not change the username below. It should always be "mapbox" (not your username).
            credentials.username = "mapbox"
            // Use the secret token stored in gradle.properties as the password
            credentials.password = "sk.eyJ1IjoiamRvZTI1IiwiYSI6ImNscHJvejlxajAxb3cybHBodjduNzk3dmYifQ.KWmaBAQlyZQH9OAGXbm-bQ"
            authentication.create<BasicAuthentication>("basic")
        }
    }
}

rootProject.name = "Rental-app"
include(":app")
 