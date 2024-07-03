import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val keystorePropertiesFile: File = rootProject.file("${rootDir.parent}/key.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    namespace = "com.yidont.uniapp.debug"
    compileSdk = 34

    signingConfigs {
        create("config") {
            keyAlias = keystoreProperties["RELEASE_KEY_ALIAS"] as String
            keyPassword = keystoreProperties["RELEASE_KEY_PASSWORD"] as String
            storeFile = file(keystoreProperties["RELEASE_STORE_FILE"] as String)
            storePassword = keystoreProperties["RELEASE_STORE_PASSWORD"] as String
        }
    }

    defaultConfig {
        applicationId = "com.yidont.uniapp.debug"
        minSdk = 26
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 31
        versionCode = 654
        versionName = "9.9.9"

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += setOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("config")
        }
        debug {
            signingConfig = signingConfigs.getByName("config")

            applicationVariants.all {
                outputs.map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach { output ->
                        val fileName = "android_debug.apk"
                        output.outputFileName = fileName
                    }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions.jniLibs.useLegacyPackaging = true

    androidResources {
        additionalParameters += "--auto-add-overlay"
        ignoreAssetsPattern = "!.svn:!.git:.*:!CVS:!thumbs.db:!picasa.ini:!*.scc:*~"
    }
}

dependencies {
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(fileTree("libs"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.facebook.fresco:fresco:2.6.0")
    implementation("com.facebook.fresco:animated-gif:2.6.0")
    implementation("com.github.bumptech.glide:glide:4.9.0")
    implementation("com.alibaba:fastjson:1.2.83")
    implementation("androidx.webkit:webkit:1.3.0")
    implementation("com.squareup.okhttp3:okhttp:3.12.12")

    implementation("io.github.zwonb:utils:latest.release")
    implementation("io.github.zwonb:unimp-components:latest.release")
    implementation("io.github.zwonb:unimp-modules:1.44")
    implementation("io.github.zwonb:compose:1.3")
    implementation("io.github.zwonb:unimp-scan:1.4")
    implementation("io.github.zwonb:unimp-record-video:1.3")
    implementation("io.github.zwonb:unimp-screen-flash:latest.release")
    implementation("io.github.zwonb:unimp-ble:1.10")
    implementation("io.github.zwonb:unimp-voice-call:latest.release")
}