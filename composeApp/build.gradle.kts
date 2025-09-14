import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.sqlDelight)
}


// Systematic .env loading for BuildKonfig (single source of truth for all targets)
// Layers (earlier can be overridden by later): .env -> .env.<env> -> .env.local
// Select environment via -Penv=<name> (default: development). Optional -PstrictEnv=true to fail on missing required keys.
val envName: String = (project.findProperty("env") as? String)?.lowercase() ?: "development"

fun loadProps(fileName: String): Properties = Properties().apply {
    val f = rootProject.file(fileName)
    if (f.exists()) f.inputStream().use { load(it) }
}

fun loadLayeredEnv(): Properties {
    val base = loadProps(".env")
    val env = loadProps(".env.$envName")
    val local = loadProps(".env.local")
    // Merge in order: base <- env <- local
    base.putAll(env)
    base.putAll(local)
    // Allow OS environment variables to fill in missing keys (do not override explicitly set ones)
    System.getenv().forEach { (k, v) ->
        if (!base.containsKey(k) && !v.isNullOrEmpty()) base[k] = v
    }
    return base
}

val envProps: Properties = loadLayeredEnv()

buildkonfig {
    packageName = "eu.vvoleman.gympal"
    objectName = "BuildKonfig"
    defaultConfigs {
        // Dynamically expose any env keys starting with GP_ as STRING constants
        envProps.stringPropertyNames()
            .filter { it.startsWith("GP_") }
            .sorted()
            .forEach { key ->
                val value = envProps.getProperty(key) ?: ""
                buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, key, value)
            }
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.kotlinx.coroutines.android)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.sqldelight.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.kotlinx.serialization.json)
            // Voyager navigation for KMP
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.touchlab.kermit)

//            implementation(libs.material.icons.core)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.core)
            api(libs.androidx.datastore)
            api(libs.androidx.datastore.preferences)

            // Supabase (modules)
            implementation(libs.supabase.gotrue)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.storage)
            implementation(libs.supabase.realtime)

            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.jvm)

            // JavaCV pro implementaci kamery na JVM platformě
            implementation("org.bytedeco:javacv-platform:1.5.9")
            implementation("org.bytedeco:opencv:4.7.0-1.5.9")
        }
        nativeMain.dependencies {
            implementation(libs.sqldelight.native)
        }
    }

    sqldelight {
        databases {
            create("GymPalDatabase") {
                packageName.set("eu.vvoleman.gympal.database")
            }
        }
    }
}

android {
    namespace = "eu.vvoleman.gympal"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "eu.vvoleman.gympal"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "eu.vvoleman.gympal.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "eu.vvoleman.gympal"
            packageVersion = "1.0.0"
        }
    }
}
