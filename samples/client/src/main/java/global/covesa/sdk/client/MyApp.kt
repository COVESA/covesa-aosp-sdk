package global.covesa.sdk.client

import android.app.Application
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Security.addProvider(BouncyCastleProvider())
    }
}
