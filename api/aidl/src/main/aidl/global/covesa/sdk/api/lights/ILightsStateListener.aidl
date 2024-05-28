package global.covesa.sdk.api.lights;

import global.covesa.sdk.api.lights.LightState;

interface ILightsStateListener {

    void onLightsStateUpdate(in List<LightState> states);
}
