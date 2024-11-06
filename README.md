# Karoo Extensions

Karoo Extensions (karoo-ext library) is an [Android library](https://developer.android.com/studio/projects/android-library) for use on [Hammerhead](https://www.hammerhead.io/)
cycling computers.

Like other Android libraries, it can be included as a gradle dependency to provide APIs for use in Kotlin and Java code.

## Documentation

Comprehensive documentation for karoo-ext is available [here](https://hammerheadnav.github.io/karoo-ext/index.html).

## Getting started

### Setting up the dependency

The first step is to include karoo-ext into your project, for example, as a gradle compile dependency:

```kotlin
implementation("io.hammerhead:karoo-ext:1.x.y")
```

(Replace `x` and `y` with the latest version numbers: [Latest karoo-ext](https://github.com/hammerheadnav/karoo-ext/packages/2175616))

Add the package repository to your buildscript. karoo-ext is publicly available however Github Packages always requires authentication.
Ultimately, it'll look something like this:

```kotlin
dependencyResolutionManagement {
    // ...
    repositories {
        // ...
        // karoo-ext from Github Packages
        maven {
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("USERNAME"))
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("TOKEN"))
            }
        }
    }
}
```

You can store `gpr.user` and `gpr.key` in your `local.properties` or use environment variables to provide your credentials.

Full instructions from [Github](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package).

### Hello Karoo

With the dependency in-place, all of the library symbols will be available within your Android application after a gradle sync.

To do a simple "hello world" to Karoo, add this code to your Android activity:

```kotlin
import io.hammerhead.karooext.KarooSystemService

class HelloActivity : Activity() {
    private val karooSystem by lazy { KarooSystemService(this) }

    override fun onStart() {
        super.onStart()
        karooSystem.connect {
            println("karoo system connected")
        }
    }

    override fun onStop() {
        karooSystem.disconnect()
        super.onStop()
    }
}
```

This example demonstrates the minimal code to connecting to your application to Karoo. Once connected, you can use `karooSystem` to
consume events or dispatch effects.

Register to consume events with:

```kotlin
val consumerId = karooSystem.addConsumer { rideState: RideState ->
    println("Ride state is now $rideState")
}
```

Unregistering and cleanup can then be done with:

```kotlin
karooSystem.removeConsumer(consumerId)
```

Dispatch effects to the system with:

```kotlin
karooSystem.dispatch(PerformHardwareAction.ControlCenterComboPress)
```

Read the docs for more details about the [events](https://hammerheadnav.github.io/karoo-ext/karoo-ext/io.hammerhead.karooext.models/-karoo-event/index.html) and 
[effects](https://hammerheadnav.github.io/karoo-ext/karoo-ext/io.hammerhead.karooext.models/-karoo-effect/index.html) exposed by karoo-ext as well
as the documentation of [KarooSystemService](https://hammerheadnav.github.io/karoo-ext/karoo-ext/io.hammerhead.karooext/-karoo-system-service/index.html) itself.

### Hello Extension

Interaction with Karoo from your application is the starting point but the real value provided by Karoo Extensions is,
by definition, extending the capabilities and experience of Karoo.

To do this, Karoo OS (the apps and services on the device out-of-the-box) need to know about and communicate with your application.

First, create a class that is derived from [KarooExtension](https://hammerheadnav.github.io/karoo-ext/karoo-ext/io.hammerhead.karooext.extension/-karoo-extension/index.html) and
defining the extension ID and version:

```kotlin
class HelloExtension : KarooExtension("hello", "5")
```

Next, register your extension with the Karoo System, by including it as a `<service>` in your `AndroidManifest.xml` like this:

```xml

<service android:name=".HelloExtension">
    <intent-filter>
        <action android:name="io.hammerhead.karooext.KAROO_EXTENSION" />
    </intent-filter>
    <meta-data
        android:name="io.hammerhead.karooext.EXTENSION_INFO"
        android:resource="@xml/extension_info" />
</service>
```

Importantly, the intent-filter for "io.hammerhead.karooext.KAROO_EXTENSION" identifies this service as a Karoo Extension.

Secondly, the `extension_info` referenced points to an XML resources defining the capabilities your extension provides.

Extension Info XML resource (`extension_info.xml`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<ExtensionInfo
    id="hello"
    displayName="@string/extension_name"
    icon="@drawable/X"
    scansDevices="true">
    <DataType
        typeId="<type id>"
        description="@string/X"
        displayName="@string/X"
        graphical="true"
        icon="@drawable/X" />
    ...
    <DataType... />
</ExtensionInfo>
```

### Template

If you're starting from scratch, to create a new Android app with necessary dependencies
and stubbed extension, start from the template repository: [karoo-ext-template](https://github.com/hammerheadnav/karoo-ext-template).

1. **Use the Template**: Go to the template repository, click **"Use this template"**, and create your new repository.
2. **Customize**: Clone your new repository and start writing your app and extensions.

## Sample App

Within [app/](app/), a sample application with Karoo Extensions is provided with examples for various integrations with Karoo. While this sample app uses
Jetpack Compose, Hilt, ViewModels, and Glance, these are not strict dependencies or requirements to be able to use the karoo-ext library.

Main activity demonstrates:

1. HW actions
2. Beeper control
3. Subscribing to system events

The functionality demonstrated in [MainActivity](app/src/main/kotlin/io/hammerhead/sampleext/MainActivity.kt) can also
be used in extensions to create more advanced in-ride behavior.

To install the basic sample app:

```bash
./gradlew app:installDebug
```

The `app` depends on the `lib` module directly, allowing for quick testing of library changes. A prebuilt sample app can also be found in release artifacts.

### Sample Extension

The sample app includes an [Extension](app/src/main/kotlin/io/hammerhead/sampleext/extension)
This is a good starting point or reference if you have an existing Android app written in Kotlin.

The sample extension demonstrates:

1. Defining the extension info in XML to match the extension service
2. Scanning for devices (fake HR sensors)
3. Connecting to and updating a device
4. Defining a data type (Power-HR and Custom Speed)
5. Streaming data for a data type
6. Adding a custom view for a data type (Custom Speed view)
7. Publishing system notifications
8. Pushing in-ride alerts based on streaming ride data

## Methodologies

Karoo Extensions aim to make developing for Karoo and K2 seamless. While some hardware differences exist, this library
largely abstracts them. If an implementation requires distinguishing products, [KarooInfo](https://hammerheadnav.github.io/karoo-ext/karoo-ext/io.hammerhead.karooext.models/-karoo-info/index.html)
can be used to switch on the available hardware types.

This library exists separately from [karoo-sdk](https://github.com/hammerheadnav/karoo-sdk) in order to allow the legacy SDK to
continue to be supported and included alongside karoo-ext without class conflicts. While the ideal is for all Karoo development to move
toward karoo-ext usage, there may be time where including both karoo-sdk and karoo-ext is necessary.

In order to provide a robust and stable experience, unlike the legacy SDK, all third-party code is now run within the third-party's own application process.
This separation provides a clear API and singular touch-points from third-party to Karoo OS and vice-versus.

The large majority of interactions with `KarooSystem` are done through consumers and dispatching. This allows the API to remain stable and make it
easier to add new events and effects. While most operations are encapsulated in `@Serializable` data structures, views and resources can't easily be captured.
Where third-party views are needed by Karoo OS, [RemoteViews](https://developer.android.com/reference/android/widget/RemoteViews), used by Android platform for
widgets, allow describing a view hierarchy that can safely be displayed in another process.