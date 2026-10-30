plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.ksp)
    id ("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

android {
    namespace = "com.snap.intelligence.musics"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.snap.intelligence.musics"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false
            // Use non-optimize rules: R8 optimize can break AppsFlyer JSONException handlers
            // (response body "ok" → JSONObject), wrapping into fatal java.lang.Error on worker threads.
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        jvmTarget = "17"
    }
    // avoid DEPENDENCY_INFO_BLOCK for IzzyOnDroid
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Moloco SDK pulls ktor-client-android:3.2.3 which removes HttpTimeout (Ktor 2 API)
// used by ktor-client-okhttp:2.x — force a single Ktor 2 line and exclude Moloco's copy.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.ktor") {
            useVersion(libs.versions.ktor.get())
        }
    }
}

dependencies {
    implementation(libs.guava)
    implementation(libs.coroutines.guava)
    implementation(libs.concurrent.futures)

    implementation(libs.activity)

    implementation(libs.datastore)

    implementation(libs.viewmodel)


    implementation(libs.coil)


    implementation(libs.media3)
    implementation(libs.media3.session)
    implementation(libs.media3.okhttp)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.apache.lang3)

    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    implementation(projects.innertube)

    implementation(projects.kizzy) {
        // Android already provides org.json. Packaging org.json:json causes release R8
        // to rewrite JSONArray while JSONObject is loaded from bootclasspath.
        exclude(group = "org.json", module = "json")
    }

    implementation(libs.ktor.client.core)

    coreLibraryDesugaring(libs.desugaring)

    implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.9.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        testImplementation("junit:junit:4.13.2")
    implementation ("com.github.bumptech.glide:glide:4.15.1")// 使用最新稳定版�?    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(libs.timber)
    implementation ("com.tencent:mmkv:1.3.14")
        implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("io.supercharge:shimmerlayout:2.1.0")
    implementation("com.github.getActivity:XXPermissions:20.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation ("com.blankj:utilcodex:1.31.1")
    implementation ("com.geyifeng.immersionbar:immersionbar:3.2.2")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    // admob todo
    implementation("com.google.android.gms:play-services-ads:22.4.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("org.greenrobot:eventbus:3.3.1")
    implementation ("com.google.android.ump:user-messaging-platform:2.2.0")
    implementation ("com.android.installreferrer:installreferrer:2.2")

    implementation ("com.appsflyer:af-android-sdk:6.17.5")
    //fb todo
    implementation (platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-crashlytics")
    implementation ("com.facebook.android:facebook-android-sdk:11.3.0")
//    implementation ("com.alibaba:fastjson:1.2.83")
    implementation("androidx.webkit:webkit:1.14.0")
    implementation(libs.ktor.serialization.json)

    //TU (Necessary)
    implementation("com.thinkup.sdk:core-tpn:6.5.73.3")

    //Androidx (Necessary)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.browser:browser:1.4.0")

    //Moloco (exclude Ktor 3 — conflicts with app's Ktor 2 / HttpTimeout)
    implementation("com.thinkup.sdk:adapter-tpn-moloco:4.8.1.1.0")
    implementation("com.moloco.sdk:moloco-sdk:4.8.1") {
        exclude(group = "io.ktor")
    }

    //Vungle
    implementation("com.thinkup.sdk:adapter-tpn-vungle:7.7.3.1.0")
    implementation("com.vungle:vungle-ads:7.7.3")
    implementation("com.google.android.gms:play-services-basement:18.1.0")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")

    //UnityAds
    implementation("com.thinkup.sdk:adapter-tpn-unityads:4.17.0.1.1")
    implementation("com.unity3d.ads:unity-ads:4.17.0")

    //Ironsource
    implementation("com.thinkup.sdk:adapter-tpn-ironsource:9.2.0.1.1")
    implementation("com.unity3d.ads-mediation:mediation-sdk:9.2.0")
    implementation("com.google.android.gms:play-services-appset:16.0.2")

    //Bigo
    implementation("com.thinkup.sdk:adapter-tpn-bigo:5.7.1.1.1")
    implementation("com.bigossp:bigo-ads:5.7.1")

    //Pangle
    implementation("com.thinkup.sdk:adapter-tpn-pangle:8.1.0.3.1.0")
    implementation("com.pangle.global:pag-sdk:8.1.0.3")

    //Kwai (skip media3 alpha; project already uses Media3 1.4.1)
    implementation("com.thinkup.sdk:adapter-tpn-kwai:1.2.21.1.0")
    implementation("io.github.kwainetwork:adApi:1.2.21")
    implementation("io.github.kwainetwork:adImpl:1.2.21")

    //Inmobi
    implementation("com.thinkup.sdk:adapter-tpn-inmobi:11.1.1.1.1")
    implementation("com.inmobi.monetization:inmobi-ads-kotlin:11.1.1")

    //TU Adx SDK(Necessary)
    implementation("com.thinkup.sdk:adapter-tpn-sdm:6.5.68.1.1")
    implementation("com.smartdigimkttech.sdk:smartdigimkttech-sdk:6.5.68")

    //AppLovin
    implementation("com.thinkup.sdk:adapter-tpn-applovin:13.6.3.1.0")
    implementation("com.applovin:applovin-sdk:13.6.3")

    //Mintegral
    implementation("com.thinkup.sdk:adapter-tpn-mintegral:17.0.91.1.0")
    implementation("com.mbridge.msdk.oversea:mbridge_android_sdk:17.0.91")
    implementation("androidx.recyclerview:recyclerview:1.1.0")

    //Bidmachine
    implementation("com.thinkup.sdk:adapter-tpn-bidmachine:3.7.0.1.0")
    implementation("io.bidmachine:ads:3.7.0")

    //Chartboost
    implementation("com.thinkup.sdk:adapter-tpn-chartboost:9.12.0.1.0")
    implementation("com.chartboost:chartboost-sdk:9.12.0")
    implementation("com.chartboost:chartboost-mediation-sdk:5.3.0")
    implementation("com.chartboost:chartboost-core-sdk:1.1.0")
    implementation("com.chartboost:chartboost-mediation-adapter-chartboost:5.9.11.0.0")

    //Fyber
    implementation("com.thinkup.sdk:adapter-tpn-fyber:8.4.2.1.1")
    implementation("com.fyber:marketplace-sdk:8.4.2")

    //Ogury
    implementation("com.thinkup.sdk:adapter-tpn-ogury:5.2.0.1.0")
    implementation("co.ogury:ogury-sdk:5.2.0")

    //Tramini
    implementation("com.thinkup.sdk:tramini-plugin-tpn:6.5.73")
}
