plugins {
    id("com.android.application")
}

android {
    namespace = "com.nidoham.streamly"
    compileSdk = 34 // 33 থেকে আপগ্রেড করুন

    defaultConfig {
        applicationId = "com.nidoham.streamly"
        minSdk = 26
        targetSdk = 33 // 33 is fixed
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }

        // Java 17 এর জন্য মাল্টিডেক্স সক্ষম করুন
        multiDexEnabled = true
    }
    
    compileOptions {
        // Java 17 এ আপগ্রেড করুন
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Java 17 ফিচারের জন্য ডিসুগারিং সক্ষম করুন
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // NewPipe Extractor এর সঠিক ভার্সন ব্যবহার করুন
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.24.6")
    
    // OkHttp এর সাম্প্রতিক ভার্সন যোগ করুন
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // ডিসুগারিং লাইব্রেরি
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // আপনার অন্যান্য ডিপেন্ডেন্সি
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    
    implementation("androidx.preference:preference-ktx:1.2.1")
}