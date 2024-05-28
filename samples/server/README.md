# COVESA SDK Server sample

This version of the COVESA SDK server sample shows how to implement the COVESA Lights Service to listen for lights update requests and Catalog Service to provide clients information about the installed services.

## Server SDK artifacts
The server SDK library is composed (at the moment) of an AAR file: `aidl-debug.aar.

These need to be added as dependencies for third-party applications. Please refer to the samples/client module for an example of setup.

## Services Catalog
This service needs to be exported and have an `intent filter` defined in its manifest with the action `global.covesa.sdk.server.CovesaCatalogService.BIND`.
It's possible to find a sample implementation in `global.covesa.sdk.server.CovesaCatalogService`.

The default implementation should list an action for each of the installed services among the known ones.
The list of all the known COVESA services is defined in the AIDL file `global.covesa.sdk.api.ICovesaCatalogRemoteService`.

## Lights Service 
This service needs to be exported and have an `intent filter` defined in its manifest with the action `global.covesa.sdk.server.CovesaLightService.BIND`.
It's possible to find a sample implementation in `global.covesa.sdk.server.CovesaLightService`.

The service must expose, in its binder, several methods defined in the AIDL:

- `registerLightsStateListener(...)`/`unregisterLightsStateListener(...)`: these allow clients to register for updates about the state of all the available lights zones
- `setInternalLight(lightState: LightState)`: this allows clients to update the light state for a given zone

The sample implementation correctly notifies the registered listeners every time a client invokes `setInternalLight(...)`, and internally logs and shows a Toast which can be used by client applications developers to test their client implementation.
A manufacturer implementing their version of the service should instead invoke the lower-level methods to interact with the hardware, where the sample app invokes `logReceivedState()`.
