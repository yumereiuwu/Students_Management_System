plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.students_system"
    compileSdk = 35 // Sử dụng 34 (ổn định) thay vì 35 (preview) trừ khi bạn có lý do cụ thể

    defaultConfig {
        applicationId = "com.example.students_system"
        minSdk = 24
        targetSdk = 35 // Nên target SDK ổn định
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true // Giữ nguyên dòng này
    }
}

dependencies {

    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Core Android & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx) // Sử dụng activity-ktx
    implementation(libs.androidx.constraintlayout)

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Thêm version cụ thể nếu không có trong libs

    // CardView (Optional)
    implementation("androidx.cardview:cardview:1.0.0") // Thêm version cụ thể nếu không có trong libs

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7") // Thêm version cụ thể nếu không có trong libs
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7") // Thêm version cụ thể nếu không có trong libs

    // Glide (Image Loading)
    implementation("com.github.bumptech.glide:glide:4.16.0") // Thêm version cụ thể nếu không có trong libs
    // ksp("com.github.bumptech.glide:ksp:4.16.0") // << --- THÊM DÒNG NÀY nếu dùng KSP
    // Hoặc: kapt("com.github.bumptech.glide:compiler:4.16.0") // << --- THÊM DÒNG NÀY nếu dùng KAPT

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}