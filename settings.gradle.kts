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
    ":core:ui",
    ":core:domain",
    ":core:data",
    ":crypto",
    ":feature:order:ui",
    ":feature:receipt:ui",
    ":feature:receipt:domain",
    ":feature:recharge:ui",
    ":feature:settings:ui",
    ":feature:settings:domain",
    ":nfc",
    ":notification"
)
