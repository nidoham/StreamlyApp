plugins {
    id("com.android.application")
}

android {
    namespace = "com.nidoham.streamly"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nidoham.streamly"
        minSdk = 26
        targetSdk = 33
        versionCode = 2
        versionName = "0.2025.07.15"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Java 17 এর জন্য Multidex সাপোর্ট
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
        buildConfig = true
    }
}

dependencies {

    //<|*---------- Androidx ---------- *|>//
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    //<|*---------- OkHttp ---------- *|>//
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    //<|*---------- Admob SDK ---------- *|>//
    implementation("com.google.android.gms:play-services-ads:23.1.0")

    //<|*---------- NewPipe Extractor ---------- *|>//
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.24.6")

    //<|*---------- OkHttp ---------- *|>//
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //<|*---------- RxJava ---------- *|>//
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    //<|*---------- Media3 ExoPlayer ---------- *|>//
    val media3Version = "1.3.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-exoplayer-hls:$media3Version")
    implementation("androidx.media3:media3-exoplayer-dash:$media3Version")
    
    //<|*---------- Image Processing ---------- *|>//
    val imageVersion = "4.16.0"
    implementation("com.github.bumptech.glide:glide:$imageVersion")
    annotationProcessor("com.github.bumptech.glide:compiler:$imageVersion")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    //<|*---------- TimeZone ---------- *|>//
    implementation("org.ocpsoft.prettytime:prettytime:5.0.7.Final")
}