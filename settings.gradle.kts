rootProject.name = "Clevent"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":app",

    ":ui:core",
    ":domain:core",
    ":data:core",

    ":ui:order",

    ":ui:settings",
    ":domain:settings",

    ":ui:receipt",
    ":domain:receipt",

    ":ui:recharge"
)
