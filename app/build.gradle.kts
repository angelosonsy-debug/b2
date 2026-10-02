import java.net.URL
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val hasilteePkg: String =
    (project.findProperty("hasilteePackage") as String?)
        ?.trim()
        .orEmpty()
        .ifEmpty { "com.example.hasiltee" }

// بينزّل React وقت البناء ويحطه جوه الـ APK عشان التطبيق يشتغل بدون إنترنت
val vendorDir: File = layout.projectDirectory.dir("src/main/assets/vendor").asFile

val downloadVendorJs by tasks.registering {
    description = "Download React UMD builds into assets/vendor for offline use"
    doLast {
        vendorDir.mkdirs()
        val libs = mapOf(
            "react.production.min.js" to
                "https://cdnjs.cloudflare.com/ajax/libs/react/18.2.0/umd/react.production.min.js",
            "react-dom.production.min.js" to
                "https://cdnjs.cloudflare.com/ajax/libs/react-dom/18.2.0/umd/react-dom.production.min.js"
        )
        for ((name, url) in libs) {
            val target = File(vendorDir, name)
            if (target.exists() && target.length() > 5000L) continue
            try {
                java.net.URL(url).openStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                if (target.length() <= 5000L) {
                    target.delete()
                    logger.warn("vendor: $name looks too small, removed")
                }
            } catch (e: Exception) {
                logger.warn("vendor: could not download $name (${e.message}) - app will fall back to the CDN")
            }
        }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(downloadVendorJs)
}

android {
    namespace = "com.b2tracker.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.b2tracker.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["hasilteePackage"] = hasilteePkg
        buildConfigField("String", "HASILTEE_PACKAGE", "\"$hasilteePkg\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.11.0")

    testImplementation("junit:junit:4.13.2")
}
