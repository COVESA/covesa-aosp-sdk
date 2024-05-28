package global.covesa.sdk.api;

import global.covesa.sdk.api.lights.LightColor;
import global.covesa.sdk.api.lights.LightState;
import global.covesa.sdk.api.lights.ILightsStateListener;

interface ICovesaCatalogRemoteService {

    const int API_VERSION = 1;

    const String CATALOG_SERVICE_ACTION = "global.covesa.sdk.server.CovesaCatalogService.BIND";
    const String LIGHT_SERVICE_ACTION = "global.covesa.sdk.server.CovesaLightService.BIND";

    int getApiVersion();

    List<String> getInstalledServices();

}
