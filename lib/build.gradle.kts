import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import java.net.URL
import java.time.LocalDateTime

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    `maven-publish`
}

val moduleName = "karoo-ext"
val libVersion = "1.1.6"

buildscript {
    dependencies {
        classpath(libs.jetbrains.dokka.android)
    }
}

android {
    namespace = "io.hammerhead.karooext"
    compileSdk = 34

    defaultConfig {
        minSdk = 23

        buildConfigField("String", "LIB_VERSION", "\"$libVersion\"")
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
        buildConfig = true
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
    suppressInheritedMembers = true

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        val assetsDir = rootDir.resolve("assets")
        homepageLink = "https://github.com/hammerheadnav/karoo-ext"

        footerMessage = "© ${LocalDateTime.now().year} SRAM LLC."
        customAssets = listOf(assetsDir.resolve("logo-icon.svg"))
        customStyleSheets = listOf(assetsDir.resolve("hammerhead-style.css"))
    }

    dokkaSourceSets {
        configureEach {
            // A bug exists in dokka for Android libraries that prevents this from being generated
            // https://github.com/Kotlin/dokka/issues/2876
            sourceLink {
                localDirectory.set(projectDir.resolve("lib/src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/hammerheadnav/karoo-ext/blob/${libVersion}/lib"))
                remoteLineSuffix.set("#L")
            }
            skipEmptyPackages.set(true)
            includeNonPublic.set(false)
            includes.from("Module.md")
            samples.from("src/test/kotlin/samples.kt")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    dokkaPlugin(libs.jetbrains.dokka.android)

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
