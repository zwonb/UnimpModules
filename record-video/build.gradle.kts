import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.yidont.record.video"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // uts插件默认集成此库
    implementation(libs.getactivity.xxpermissions)

    api(libs.zwonb.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)
}

mavenPublishing {
    val version = "0.1"
    coordinates("io.github.zwonb", "record-video", version)

    pom {
        name.set("record-video")
        description.set("Android library")
        inceptionYear.set("2023")
        url.set("https://gitee.com/zwonb/app-modules/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("zwonb")
                name.set("zhou yuan bin")
                url.set("https://gitee.com/zwonb/")
            }
        }
        scm {
            url.set("https://gitee.com/zwonb/app-modules/")
            connection.set("scm:git:git://gitee.com/zwonb/app-modules.git")
            developerConnection.set("scm:git:ssh://git@gitee.com/zwonb/app-modules.git")
        }
    }

    if (version.endsWith("-SNAPSHOT")) {
        publishToMavenCentral(SonatypeHost.S01, false)
    } else {
        publishToMavenCentral(SonatypeHost.S01, true)
        signAllPublications()
    }
}
