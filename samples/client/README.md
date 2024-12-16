# COVESA SDK Client sample

This first version of the COVESA SDK client sample shows how to use the COVESA Lights Service to update the values of internal lights and receive and updated state of all the lights.
It also demonstrates how to use push notifications.

## Client SDK artifacts
The client SDK library is composed (at the moment) of two AAR files: `aidl-debug.aar` and `client-debug.aar`.

These need to be added as dependencies for third-party applications. Please refer to the samples/client module for an example of setup.

## Services Catalog API
The main API entry point is `global.covesa.sdk.api.client.ServicesCatalog`. 
It is the service that informs a client at runtime about the other installed COVESA services.

It can be created with a Context constructor parameter and it exposes the following:
- `getInstalledServices()` returns a list of String, each one representing the action needed to connect to a supported COVESA Service via Intent.
  Note that `PushService` for push notifications is a service implemented by applications and it is hence not exposed here.
- `getServiceApiVersion()` returns a nullable Int specifying the API level exposed by the service or `null` if the service is not installed

## Lights Client API

### LightsService
The main API entry point is `global.covesa.sdk.api.client.LightsService`. 
It can be created with a Context constructor parameter and it exposes the following:

- `lightsStates`: it's a [kotlin flow](https://kotlinlang.org/docs/flow.html) of LightState. Clients should collect from this flow to get reactive updates about the current state of all the lights.
- `getServiceApiVersion()` returns a nullable Int specifying the API level exposed by the service or `null` if the service is not installed
- `setInternalLight(lightState: LightState)` which should be used to update the value of a specific zone lighting.

Due to the remote client-service communication at the base of the SDK, all methods are `suspend` and for that reason should be invoked from a [CoroutineScope](https://kotlinlang.org/docs/composing-suspending-functions.html).

### LightState
`global.covesa.sdk.api.lights.LightState` is a Parcelable which describes a single light.

It specifies the zone relative to the light, the RGB color (in the form of `global.covesa.sdk.api.lights.LightColor`) and the brightness.

The zone can be one of the following:

- ALL_ZONES = 0
- ZONE_DRIVER = 1
- ZONE_PASSENGER = 2
- ZONE_REAR_LEFT = 3
- ZONE_REAR_CENTER = 4
- ZONE_REAR_RIGHT = 5

Brightness can be a value between 0 and 255.

### LightColor
`global.covesa.sdk.api.lights.LightColor` is a Parcelable which describes athe color for a light.
It has 3 fields, for the red, green and blue values, each one can be a value between 0 and 255.

## Prerequisites for testing
- A device (physical or emulators, doesn't need to be Automotive) running Android 10 (API 29)
- Install on that device the semple server APK (`server-debug.apk`)

## Client sample
Open the client project in Android Studio and run it on the device.

The UI of the client will show if the service is installed or not, which API version and the status for all the light zones.

When tapping on the "Set internal lights" button, the SDK will connect to the lights service (if installed) and set a random color for the "All zones" light.
This will be reflected in the Toast triggered by the service and by a callback updating the client UI.

Note that selecting `ALL_ZONES` when invoking the method `setInternalLight()` of Light Service, will override all other zones lights.

### Push notifications
In order to test your push notifications on a non-automotive emulator, you need to install a testing [distributor](https://unifiedpush.org/users/distributors/) app.
For example, you can install [ntfy](https://unifiedpush.org/users/distributors/ntfy/) (also from Google Play Store). Simply open it and then run this client sample.
