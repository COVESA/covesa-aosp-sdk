![Status - Incubating](https://img.shields.io/static/v1?label=Status&message=Incubating&color=FEFF3A&style=for-the-badge)

# COVESA AOSP SDK

## Objective of this SDK
The main goal of this SDK is to provide a set of libraries which allow third-party applications to access some automotive features in a way that is agnostic of manufacturer and model.

Examples of such features include:
1. Car features such as control over interior ambient lights or car data such as mileage which are not exposed by OEMs via [CarPropertyManager](https://developer.android.com/reference/android/car/hardware/property/CarPropertyManager)
2. Common APIs for functions which are not supported by AOSP, such as the push notification API.

## Standardization
Standardization of different car APIs is an ongoing effort: https://wiki.covesa.global/display/WIK4/Automotive+AOSP+App+Framework+Standardization+Expert+Group

Push notifications is the first standardized API within this group.

This SDK also provides an example of an AIDL-based communication between an OEM-implementable service and a third-party application. The example implements an ambient light API.

## Versioning
The versioning strategy and stability promises are yet to be established.

## SDK components
The SDK puts into communication 3 components:
* **Client app**: the third party-app that wants to interact with manufacturers cars.
* **Server app**: the service that each manufacturer needs to implement on their cars.
* **AIDL**: it’s the interface used for remote communication between the client and server apps.

The AIDL definition for the SDK is contained in the [api/aidl](./api/aidl/) gradle module.
The official Google documentation about the AIDL format can be found [here](https://developer.android.com/develop/background-work/services/aidl).

While client apps developers can directly use the AIDL to establish a connection with the SDK service, an utility layer is defined in the [api/client](./api/client/) gradle module. This layer hides most of the complexity of the AIDL connection by exposing a modern, more developer-friendly interface.

The same layer is used to expose APIs which do not depend on AIDL, such as push notifications.

## Push notifications
Third-party apps should link to the same single library `implementation(project(":api:client"))`. See [samples/client](./samples/client) for details. 

There is no AIDL layer for push notifications. Instead of an AIDL-based system service, OEMs implement their own [distributor](https://unifiedpush.org/users/distributors/) which is reacting to a specific broadcast.

In order to send/receive push notifications a distributor is needed. In this case, we are using Sunup in order to be able to test. Sunup is already doing some work for us, since Mozzilla push services are set by default. We also need some kind of service that handles the Web Pushes: send, receive, register a push service and unregister push service.

Add the following dependency to your `build.gradle`:
```
(TBD)
```

Create a class that implements `PushService` from our SDK:
```kotlin
class PushServiceImpl: PushService() {
    class NewRegistrationState(val registered: Boolean)

    /**
     * A new endpoint is to be used for sending push messages. The new endpoint
     * should be send to the application server, and the app should sync for
     * missing notifications.
     */
    override suspend fun onNewEndpoint(endpoint: PushEndpoint, instance: String) {
        // TODO: Send the new endpoint to your web push server
        updateRegistrationState(true)
    }

   /**
     * A new message is received. The message contains the decrypted content of the push message
     * for the instance
     */
    override suspend fun onMessage(message: PushMessage, instance: String) {
        // TODO: Create and show a notification with the message received
        updateRegistrationState(true)
    }

    /**
     * The registration is not possible, eg. no network, depending on the reason,
     * you can try to register again directly.
     */
    override suspend fun onRegistrationFailed(reason: FailedReason, instance: String) {
      // TODO: Inform the user that the registration failed and therefore notifications
      // are not available
    }

    /**
     * This application is unregistered by the distributor from receiving push messages
     */
    override suspend fun onUnregistered(instance: String) {
        // TODO: Send an unregister action to your web push server, removing the endpoint
        updateRegistrationState(false)
    }

    /**
     * Update the UI
     */
    private suspend fun updateRegistrationState(registered: Boolean) {
        _events.emit(NewRegistrationState(registered))
    }

    companion object {
        private const val TAG = "PushServiceImpl"
        private val _events = MutableSharedFlow<NewRegistrationState>()
        val events = _events.asSharedFlow()
    }
}
```

Add your `PushService` implementation to your manifest:
```xml
<service android:name="global.covesa.sdk.client.push.PushServiceImpl"
    android:exported="true">
    <intent-filter>
        <action android:name="global.covesa.sdk.PUSH_EVENT"/>
        <action android:name="org.unifiedpush.android.connector.RAISE_TO_FOREGROUND"/>
    </intent-filter>
</service>
```

### Working with PushManager
The PushManager, as the name says, manages your app interactions with the push distributor. If you skip this step, your PushServiceImpl will never work.

#### Register push service
In order to register you need to get the distributor first, so your app knows who distributes the push you receive/send.
To do that user the `tryUseCurrentOrDefaultDistributor` method from the `PushManager`:
```kotlin
PushManager.tryUseCurrentOrDefaultDistributor(activityContext) { success ->
    // TODO: Next step
}
```
With this method the SDK tries to use the saved distributor else, use the default distributor opening the deeplink "unifiedpush://link"
It can be used on application startup to register to the distributor.
If you had already registered to a distributor, this ensure the connection is working.
If the previous distributor has been uninstalled, it will fallback to the user's default.
If you register for the first time, it will use the user's default Distributor or the OS will ask what it should use.
When a distributor is picked, you can then register:

```kotlin
PushManager.tryUseCurrentOrDefaultDistributor(activityContext) { success ->
  if (success) {
    val vapidPubKey = <Your vapid pub key>
    try {
        PushManager.register(
            activity,
            vapid = vapidPubKey
        )
        Log.w(TAG, "UnifiedPush registered with vapid $vapidPubKey")
    } catch (e: PushManager.VapidNotValidException) {
        Log.w(TAG, "UnifiedPush failed to register with vapid $vapidPubKey. With exception $e")
    }
  }
}
```

#### Get systems available distributors
```kotlin
PushManager.getDistributors(context: Context) : List<String>
```

#### Save a new distributor
```kotlin
PushManager.saveDistributor(context: Context, distributor: String)
```

#### Get registered distributor
Get the distributor registered by the user, but the distributor may not have respond yet to our requests. Most of the time getAckDistributor is preferred.
```kotlin
PushManager.getSavedDistributor(context: Context): String?
```

The preferred way:
```kotlin
PushManager.getAckDistributor(context: Context): String?
```

#### Remove distributor
If you don't want to receive any more push notification, unregister the distributor
```kotlin
PushManager.removeDistributor(context: Context)
```

## API Artifacts
Both API libraries artifacts can be generated by using the following commands:

```
./gradlew :api:aidl:assemble
./gradlew :api:client:assemble
```

The generated `aar` library files can then be found in the `build/output/aar` folders for each module.

## Integration with third-party apps
The generated `aar` libraries can be added as dependency for a third-party app by copying them in a folder in the app project tree and adding the following in the app module `build.gradle.kts`:

```
dependencies {
    implementation(files("../libs/aidl-debug.aar"))
    implementation(files("../libs/client-debug.aar"))
    ...
}
```

## Samples
Example implementations for both the client and server libraries integrations can be found in the [samples](./samples) modules of this repo.
Each sample contains its own README.md with more detailed information.

## Client testing
Third party app developers can test the library correct integration by installing the [server sample](./samples/server) in any Android emulator (even a non-automotive one) running a minimum Android SDK version as defined in the [library configuration](./gradle-plugins/src/main/kotlin/global/covesa/gradle/AndroidPlugin.kt#L11).

The server sample acts both as a reference for car manufacturers but also it logs every request received by client apps and responds updating each registered listener in a realistic way. That can be used to test third party apps behavior.

### Maintainers
* Juhani Lehtimäki - Snapp Automotive
* Nuno Palma - Appning
* Rodrigo Fernandes - Appning
* Viktor Mukha - Paradox Cat
