package global.covesa.sdk.api.lights;

import global.covesa.sdk.api.lights.LightColor;

parcelable LightState {

    const int ALL_ZONES         = 0;
    const int ZONE_DRIVER       = 1;
    const int ZONE_PASSENGER    = 2;
    const int ZONE_REAR_LEFT    = 3;
    const int ZONE_REAR_CENTER  = 4;
    const int ZONE_REAR_RIGHT   = 5;

    int zone = ALL_ZONES;
    @nullable LightColor color;
    int brightness = 0;
}
