import java.util.Properties

// Módulo único :app — ver plan.md § Project Structure (Principio VII:
// se descartó multi-módulo Gradle por no estar justificado para el
// tamaño de este MVP).

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

// T003 — secretos locales leídos de local.properties (NUNCA committeado,
// ver .gitignore y quickstart.md §3). Si el archivo o alguna clave no
// existen, se usa cadena vacía: el proyecto sigue compilando (p. ej. en
// CI o para desarrolladores que aún no trabajan en la integración con
// EcoGPT), pero las llamadas de red fallarán en runtime hasta configurarla.
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun localProperty(key: String, default: String = ""): String =
    localProperties.getProperty(key, default)

android {
    namespace = "com.ecosmart.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ecosmart.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // T003 — ver contracts/openapi.yaml (servers) y quickstart.md §3
        buildConfigField(
            "String",
            "ECOGPT_API_KEY",
            "\"${localProperty("ECOGPT_API_KEY")}\"",
        )
        buildConfigField(
            "String",
            "ECOGPT_BASE_URL",
            // Retrofit exige que baseUrl termine en "/" (IllegalArgumentException si no) — RNF-008 bug post-QA.
            "\"${localProperty("ECOGPT_BASE_URL", "https://api.ecogpt.example/v1/")}\"",
        )
        buildConfigField(
            "String",
            "PUNTOS_VERDES_BASE_URL",
            "\"${localProperty(
                "PUNTOS_VERDES_BASE_URL",
                "https://datos.buenosaires.gob.ar/api/ecosmart-puntos-verdes/v1/",
            )}\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.useJUnitPlatform() }
        }
    }
}

// Room: exportar el esquema para poder versionar migraciones (data-model.md § Migraciones)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Core / Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose (T002)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Hilt (T002)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Hilt + WorkManager (T059) — permite @HiltWorker en PuntosVerdesSyncWorker
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Room (T002) — ver data-model.md
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Retrofit + OkHttp (T002) — ver research.md §2 (cliente EcoGPT)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    // WorkManager (T002) — sincronización de Puntos Verdes, research.md §5
    implementation(libs.androidx.work.runtime.ktx)

    // CameraX + Play Services Location (T002) — research.md §1
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.play.services.location)
    // Puente Task -> suspend fun para FusedLocationProviderClient (T060, UbicacionProvider)
    implementation(libs.kotlinx.coroutines.play.services)

    // Mapa de Puntos Verdes (T106, research.md §6) — OpenStreetMap, sin API key ni facturación
    implementation(libs.osmdroid.android)

    // Seguridad: JWK/JWE + Android Keystore (T002) — research.md §4
    implementation(libs.nimbus.jose.jwt)
    implementation(libs.androidx.security.crypto)

    // Tests unitarios (dominio/aplicación — sin Android)
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.okhttp.mockwebserver)

    // Tests instrumentados (integración/UI)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.okhttp.mockwebserver)
}

// T004 — ktlint/detekt aplicados a este módulo
ktlint {
    version.set("1.3.1")
    android.set(true)
    ignoreFailures.set(false)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}
