package deckyfx.reactnative.printer

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import deckyfx.reactnative.printer.devicescan.DeviceScanner

class RNPrinterPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf<NativeModule>(
      DeviceScanner(reactContext),
      RNPrinter(reactContext),
    )
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return emptyList()
  }
}
