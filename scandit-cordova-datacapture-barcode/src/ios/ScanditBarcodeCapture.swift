import ScanditBarcodeCapture

// TODO: serialize frame data as argument (https://jira.scandit.com/browse/SDC-1014)
extension FrameData {
    func toJSON() -> CDVPluginResult.JSONMessage {
        return [:]
    }
}

class BarcodeCaptureCallbacks {
    var barcodeCaptureListener: Callback?
}

extension CDVPluginResult {
    /// Success result with defaults.
    static func success(message: ScanditBarcodeCaptureDefaults) -> CDVPluginResult {
        guard let data = try? JSONEncoder().encode(message),
            let object = try? JSONSerialization.jsonObject(with: data) as? JSONMessage else {
                return .failure(with: "Could not serialize message")
        }
        return CDVPluginResult(status: CDVCommandStatus_OK, messageAs: object)
    }
}

struct BlockingCallbackResult: Decodable {
    let enabled: Bool

    static func from(_ command: CDVInvokedUrlCommand) -> BlockingCallbackResult? {
        guard let data = command.defaultArgumentAsString?.data(using: .utf8) else {
            return nil
        }
        let decoder = JSONDecoder()
        return try? decoder.decode(BlockingCallbackResult.self, from: data)
    }
}

@objc(ScanditBarcodeCapture)
class ScanditBarcodeCapture: CDVPlugin, DataCapturePlugin {
    lazy var modeDeserializer: DataCaptureModeDeserializer = {
        let deserializer = BarcodeCaptureDeserializer()
        deserializer.delegate = self
        return deserializer
    }()

    lazy var callbacks = BarcodeCaptureCallbacks()

    private var condition = NSCondition()
    private var isCallbackFinished = true
    private var lastCallbackResult: BlockingCallbackResult?

    override func pluginInitialize() {
        super.pluginInitialize()
        ScanditCaptureCore.dataCapturePlugins.append(self)
    }

    override func onReset() {
        super.onReset()
        callbacks.barcodeCaptureListener = nil
        isCallbackFinished = true
        condition.signal()
    }

    @objc(getDefaults:)
    func getDefaults(command: CDVInvokedUrlCommand) {
        let settings = BarcodeCaptureSettings()
        let barcodeCapture = BarcodeCapture(context: DataCaptureContext(licenseKey: ""), settings: settings)
        let overlay = BarcodeCaptureOverlay(barcodeCapture: barcodeCapture)

        let defaults = ScanditBarcodeCaptureDefaults(barcodeCaptureSettings: settings,
                                                     overlay: overlay)

        commandDelegate.send(.success(message: defaults), callbackId: command.callbackId)
    }

    // MARK: Listeners

    @objc(subscribeBarcodeCaptureListener:)
    func subscribeBarcodeCaptureListener(command: CDVInvokedUrlCommand) {
        callbacks.barcodeCaptureListener?.dispose(by: commandDelegate)
        callbacks.barcodeCaptureListener = Callback(id: command.callbackId)
        commandDelegate.send(.keepCallback, callbackId: command.callbackId)
    }

    @objc(finishCallback:)
    func finishCallback(command: CDVInvokedUrlCommand) {
        lastCallbackResult = BlockingCallbackResult.from(command)
        isCallbackFinished = true
        condition.signal()
        commandDelegate.send(.success, callbackId: command.callbackId)
    }

    func waitForFinished(_ result: CDVPluginResult, callbackId: String) {
        condition.lock()
        isCallbackFinished = false
        commandDelegate.send(result, callbackId: callbackId)
        while !isCallbackFinished {
            condition.wait()
        }
        condition.unlock()
    }

    func finishBlockingCallback(with barcodeCapture: BarcodeCapture) {
        guard let result = lastCallbackResult else {
            return
        }
        if result.enabled != barcodeCapture.isEnabled {
            barcodeCapture.isEnabled = result.enabled
        }
        lastCallbackResult = nil
    }
}

extension ScanditBarcodeCapture: BarcodeCaptureListener {
    func barcodeCapture(_ barcodeCapture: BarcodeCapture,
                        didScanIn session: BarcodeCaptureSession,
                        frameData: FrameData) {
        guard let callback = callbacks.barcodeCaptureListener else {
            return
        }

        let event = ListenerEvent(name: .didScanInBarcodeCapture,
                                  argument: ["session": session.jsonString(), "frameData": frameData.toJSON()],
                                  shouldNotifyWhenFinished: true)
        waitForFinished(.listenerCallback(event), callbackId: callback.id)
        finishBlockingCallback(with: barcodeCapture)
    }

    func barcodeCapture(_ barcodeCapture: BarcodeCapture,
                        didUpdate session: BarcodeCaptureSession,
                        frameData: FrameData) {
        guard let callback = callbacks.barcodeCaptureListener else {
            return
        }

        let event = ListenerEvent(name: .didUpdateSessionInBarcodeCapture,
                                  argument: ["session": session.jsonString(), "frameData": frameData.toJSON()],
                                  shouldNotifyWhenFinished: true)
        waitForFinished(.listenerCallback(event), callbackId: callback.id)
        finishBlockingCallback(with: barcodeCapture)
    }

    func didStartObserving(_ barcodeCapture: BarcodeCapture) {
        // ignored in Cordova
    }

    func didStopObserving(_ barcodeCapture: BarcodeCapture) {
        // ignored in Cordova
    }
}

extension ScanditBarcodeCapture: BarcodeCaptureDeserializerDelegate {
    func barcodeCaptureDeserializer(_ deserializer: BarcodeCaptureDeserializer,
                                    didFinishDeserializingMode mode: BarcodeCapture,
                                    from JSONValue: JSONValue) {
        let JSONString = JSONValue.jsonString()

        guard let data = JSONString.data(using: .utf8),
            let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
            let enabled = json?["enabled"] as? Bool else {
                return
        }
        mode.isEnabled = enabled

        mode.addListener(self)
    }

    func barcodeCaptureDeserializer(_ deserializer: BarcodeCaptureDeserializer,
                                    didStartDeserializingMode mode: BarcodeCapture,
                                    from JSONValue: JSONValue) { }

    func barcodeCaptureDeserializer(_ deserializer: BarcodeCaptureDeserializer,
                                    didStartDeserializingSettings settings: BarcodeCaptureSettings,
                                    from JSONValue: JSONValue) { }

    func barcodeCaptureDeserializer(_ deserializer: BarcodeCaptureDeserializer,
                                    didFinishDeserializingSettings settings: BarcodeCaptureSettings,
                                    from JSONValue: JSONValue) { }

    func barcodeCaptureDeserializer(_ deserializer: BarcodeCaptureDeserializer,
                                    didStartDeserializingOverlay overlay: BarcodeCaptureOverlay,
                                    from JSONValue: JSONValue) { }

    func barcodeCaptureDeserializer(_ deserializer: BarcodeCaptureDeserializer,
                                    didFinishDeserializingOverlay overlay: BarcodeCaptureOverlay,
                                    from JSONValue: JSONValue) { }
}
