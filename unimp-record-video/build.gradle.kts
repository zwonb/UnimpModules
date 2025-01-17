import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.yidont.unimp.record.video"
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
}

dependencies {
    compileOnly(files("../app/libs/uniapp-v8-release.aar"))
    compileOnly(libs.fastjson)

//    implementation("io.github.zwonb:record-video:0.1")
}

mavenPublishing {
    val version = "1.3"
//    val version = "0.1-SNAPSHOT"
    coordinates("io.github.zwonb", "unimp-record-video", version)

    pom {
        name.set("unimp-record-video")
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
