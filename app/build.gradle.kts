plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.opencvdemo"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.opencvdemo"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        dataBinding {
            enable = true
        }
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.bumptech.glide:glide:5.0.0-rc01")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // open cv
    implementation(project(mapOf("path" to ":opencv")))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    // 图片选择库 ： https://github.com/LuckSiege/PictureSelector/blob/version_component/README_CN.md
    implementation("io.github.lucksiege:pictureselector:v3.11.2")
    // Glide
    implementation("com.github.bumptech.glide:glide:4.13.2")
    // 权限请求框架：GitHub - getActivity/XXPermissions: Android 权限请求框架，已适配 Android 13
    implementation("com.github.getActivity:XXPermissions:16.6")
}