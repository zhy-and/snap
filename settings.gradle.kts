@file:Suppress("UnstableApiUsage")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        //TU(Core)
        maven {
            setUrl("https://jfrog.anythinktech.com/artifactory/overseas_sdk")
        }
        //Ironsource
        maven {
            setUrl("https://android-sdk.is.com/")
        }
        //Pangle
        maven {
            setUrl("https://artifact.bytedance.com/repository/pangle")
        }
        //Mintegral
        maven {
            setUrl("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        }
        //Bidmachine
        maven {
            setUrl("https://artifactory.bidmachine.io/bidmachine")
        }
        //Chartboost
        maven {
            setUrl("https://cboost.jfrog.io/artifactory/chartboost-ads")
        }
        maven {
            setUrl("https://cboost.jfrog.io/artifactory/chartboost-mediation")
        }
        maven {
            setUrl("https://cboost.jfrog.io/artifactory/chartboost-core")
        }
        //Ogury
        maven {
            setUrl("https://maven.ogury.co")
        }
    }
}

rootProject.name = "MusicSnap"
include(":app")
include(":innertube")
include(":kugou")
include(":kizzy")
