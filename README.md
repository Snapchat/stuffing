# Stuffing

Stuffing is a small library that allows multiple [Applications](https://developer.android.com/reference/android/app/Application) to be _stuffed_ into a single [APK](https://en.wikipedia.org/wiki/Android_application_package). Since the Android
operating system only supports a single `Application` declaration in each installed APK, this library uses a common
top-level application and delegates to multiple ```ApplicationLike``` types. The ```ApplicationLike``` types adhere to same general
interface as the [Application](https://developer.android.com/reference/android/app/Application) type, and are responsible for implementing all functionality that would otherwise but common
for an ```Application``` class.

There are two complications to hosting multiple applications in a single APK:

1. The Android operating system does not directly launch _Applications_, it launches Manifest Components: ```Activities```, ```Services```, ```Broadcast Receivers``` which are bound to a specific ```Application``` in the Manifest file.
When a _Component_ is launched (via the Launcher, intent receiver, etc) the OS will create the parent ```Application``` and then the needed _Component_. There are no hooks available in this process.
_This means that there are entry-points that need to be faked to simulate multiple applications 1) the application itself, 2) the manifest components._

1. Android [APKs](https://en.wikipedia.org/wiki/Android```Application```package) are configured using a single [Manifest](https://developer.android.com/guide/topics/manifest/manifest-intro) file, which assumes the presence of a single ```<Application>``` tag.
All of the manifest components are child nodes of this ```Application```. When packaging an [APKs](https://en.wikipedia.org/wiki/Android```Application```package), the Android build tools merge the manifests of the libraries that the application depends on into a single top-level manifest.
This causes the manifest components for **all** applications to be included in the single ```<Application>``` tag, regardless if they are relevant for the active application.
_A system is needed to disable manifest components based on the active application_.

## Diving into code

If you are eager to jump into the code without reading the docs. Here is quick overview of the modules:
- `:api` - various API classes of the Stuffing library. See below.
- `:lib` - internal implementation of Stuffing
- `:sample` - sample app that provides an example for using Stuffing.

To build the sample, open the project in Android Studio or run `./gradlew :sample:installDebug`.

## How Stuffing Works

### The Structure of a Stuffed APK

In a _stuffed_ APK, each child application is called an app family. An app family includes all AndroidManifest components that correspond to a given child application. Given that the APK is only allowed to have a single Application class, 
the *Stuffing* library uses a common top-level Application class or AppShell , which delegates to multiple ApplicationLike classes that represent individual app families. This concept is inspired by the [Exopackage](https://buck.build/article/exopackage.html) functionality from Facebook’s [buck build tool](https://buck.build/). 

![Stuffing Architecture](stuffing.png)

An `ApplicationLike` is type that adheres to roughly the same interface as the Android framework [Application](https://developer.android.com/reference/android/app/Application) type. It acts as a delegate for any application-related functionality in the application lifecycle and manages the global state for an app family.

`DynamicLaunchActivity` serves as the primary entry point for the *stuffed* application. Given that the Android system doesn’t know which app family is active when launching the app, S*tuffing* provides a layer of indirection/routing via this special activity. When this activity starts, it uses the `DynamicAppManager` to determine which child Activity should be launched

### Managing The App Families

`DynamicAppManager` is the heart of stuffing as it’s responsible for managing the multiple application stuffed into the APK:

* It maintains the state of which application is currently active (via shared preferences in `DynamicAppManagerPrefs`)
* It provides the hooks for switching between different app families. 
* It enables/disables AndroidManifest components depending on which app is active. 
    * It does this at app start (to ensure any newly added components are in the right state) and when the app family is switched manually

There are two discrete implementations of this interface:

* `MultiDynamicAppMananger` - manages multiple applications 
* `SingleDynamicAppManager` - manages a single application

The primary reason for have two implementation is provide an easy way to build multiple flavors of the app. For instance, while the primary flavor of the app could include both apps, it is useful to have an additional build flavor for testing that only include your new app. Swapping different implementations of DynamicAppManager makes this easy.

### Switching between app families 

When `DynamicAppManager.switchToAppFamily` is called, the class will iterate through all manifest components using `PackageManager` and disable/enable the right set of components depending on the target app family. Switching between app families can take a while if your app has a lot of components. While the switch is in progress, a special `AppSwitchActivity` is displayed to the user. 

This activity runs in a separate process, and it waits to receive a `Intent.ACTION_PACKAGE_CHANGED]` before allowing the transition to continue. This `Intent` signals that the package manager has finished updating with the app switch changes. If we don't wait for this signal to be received before switching to the new application, the OS might close that application once it receives that signal since it thinks the app has changed.
 
`AppSwitchActivity` works around that by waiting for this signal, and then kicking off the launch of the new intended `Activity` once it has been processed.

## Using Stuffing in an Existing App

#### Preparing your codebase

Let’s assume that you have two `android-application` modules in your Gradle project: `:app` and `:new-app`. 
    - `:new-app` contains the code for your new Android application that you would like to stuff into your primary application `:app`. 

You start by moving the code from from both of apps into separate `android-library` modules. 
    - Code from `:app` is moved into `:old`
    - Code from `:new-app` is moved into `:new`.
    - `new-app` can now be deleted, and `:app` will contain no code for now, but it eventually serve as the binding layer between two apps. 

The next step would be to annotate all components in the `AndroidManifests` of both apps with `appFamiles` metadata.
    - The value will be one of two strings: `old` or `new`,  depending on which app the component belongs to.
    - Side note: if had a module with some components that are being shared between both apps, the appFamilies metadata can be omitted, which would automatically include the Component into both application.
    - Example:
```xml
    <service android:name=".SomeService" android:exported="false">
        <meta-data android:name="appFamilies" android:value="old" />
    </service>
```    

#### Adding new launcher `Activity`

To create a new entry point for both apps, add `DynamicLaunchActivity` in the top level manifest of `:app`:
 > This will cause the `DynamicLaunchActivity` to be launched when the app's launcher button is tapped. The `DynamicLaunchActivity` then needs to route to the default activity for the active `appFamily` (see below).

```xml
    <activity android:name="com.snap.stuffing.lib.DynamicLaunchActivity">
        <!-- Allow launch from the launcher. -->
        <intent-filter>
            <action android:name="android.intent.action.MAIN"/>
            <category android:name="android.intent.category.LAUNCHER"/>
        </intent-filter>
    </activity>
```

Once `DynamicLaunchActivity` is added, define a default Activity for each app family in thier respective manifests:

```xml
    <activity android:name=".first.FirstActivity">
        <meta-data android:name="mainForAppFamilies" android:value="old"/>
    </activity>
```

#### Adding new launcher `Application`

Create a new top level application in `:app`, which is responsible for creating and initializing `MultiDynamicAppManager` that will delegate execution to the right application.
> In order to make the DynamicAppManager injectable with Dagger, some special treatment is needed. Since it is created very early in the application lifecycle, even before the Dagger application component is created, it needs to be manually bound to the DI graph

```java
public class SampleDelegatingApplicationLike extends DelegatingApplicationLike {

    @NonNull
    @Override
    protected ApplicationLike createApplication() {
        final DynamicAppModule dynamicAppModule =
                DynamicAppModule.makeMultiAppModule(mApplication, "old");

        dynamicAppModule.dynamicAppManager().initialize();

        final ApplicationLike applicationLike;
        if ("new".equals(dynamicAppModule.dynamicAppManager().getApplicationFamily())) {
            applicationLike = new SecondApplication(mApplication);
        } else {
            applicationLike = new FirstApplication(mApplication);
        }

        if (applicationLike instanceof ApplicationComponentOwner) {
            ((ApplicationComponentOwner) applicationLike).attachDynamicAppModule(dynamicAppModule);
        }

        return applicationLike;
    }
}
```


#### Change existing apps into Stuffing plugins

Update the `Application` classes in the `:old` and `:new` apps to extend from `DefaultApplicationLike` and `ApplicationComponentOwner` in order in order to bind the `DynamicAppModule` to the Application's Dagger graph.

```java
public class FirstApplication extends DefaultApplicationLike implements HasActivityInjector, ApplicationComponentOwner {
    private final Application app;

    private FirstApplicationComponent appComponent;
    private DynamicAppModule dynamicAppModule;

    @Inject DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    public FirstApplication(Application app) {
        this.app = app;
    }

    @Override
    public void onCreate() {
        appComponent = DaggerFirstApplicationComponent
                .builder()
                .dynamicAppModule(dynamicAppModule)
                .build();
        appComponent.inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public void attachDynamicAppModule(@NonNull DynamicAppModule dynamicAppModule) {
        this.dynamicAppModule = dynamicAppModule;
    }
}
```

#### Add a switcher for Applications

Now that `DynamicAppManager` is available in your application’s dagger graph, it can be used to provide a way to manually switch from your old application to your new application. For instance, you can trigger this code when a use clicks on the “Try New App” somewhere in settings:

```java
    @Inject DynamicAppManager dynamicAppManager;

    ...

    dynamicAppMamager.switchToAppFamily("new")
```