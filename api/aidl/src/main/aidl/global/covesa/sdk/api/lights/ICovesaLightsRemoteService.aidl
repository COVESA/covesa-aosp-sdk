package global.covesa.sdk.api.lights;

import global.covesa.sdk.api.lights.LightColor;
import global.covesa.sdk.api.lights.LightState;
import global.covesa.sdk.api.lights.ILightsStateListener;

interface ICovesaLightsRemoteService {

    const int API_VERSION = 1;

    int getApiVersion();

    void setInternalLight(in LightState state);

    void registerLightsStateListener(in ILightsStateListener listener);

    void unregisterLightsStateListener(in ILightsStateListener listener);
}
