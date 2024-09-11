plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.dokka.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    `maven-publish`
}

val moduleName = "karoo-ext"
val libVersion = "1.0.4"

android {
    namespace = "io.hammerhead.karooext"
    compileSdk = 28

    defaultConfig {
        minSdk = 23
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        aidl = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

tasks.dokkaHtml.configure {
    moduleName = "karoo-ext"
    moduleVersion = libVersion
    outputDirectory.set(rootDir.resolve("docs"))

    dokkaSourceSets {
        configureEach {
            skipEmptyPackages.set(true)
            includeNonPublic.set(false)
            includes.from("Module.md")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}

// To build an publish locally: gradle lib:assemblerelease lib:publishtomavenlocal
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("karoo-ext") {
            artifactId = moduleName
            groupId = "io.hammerhead"
            version = libVersion

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
