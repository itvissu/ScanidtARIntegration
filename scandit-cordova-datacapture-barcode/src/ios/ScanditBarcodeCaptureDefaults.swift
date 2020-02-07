import ScanditBarcodeCapture

extension SymbologySettings {
    func toJSON() -> String {
        // TODO: use serialized SymbologySettings https://jira.scandit.com/browse/SDC-1002
        return String(data: try! JSONSerialization.data(withJSONObject: [
            "enabled": isEnabled,
            "colorInvertedEnabled": isColorInvertedEnabled,
            "activeSymbolCounts": Array(activeSymbolCounts),
            "extensions": Array(enabledExtensions),
            "checksums": [String]()
        ]), encoding: .utf8)!
    }
}

struct ScanditBarcodeCaptureDefaults: Encodable {
    typealias CameraSettingsDefaults = ScanditCaptureCoreDefaults.CameraSettingsDefaults

    struct BarcodeCaptureOverlayDefaults: Encodable {
        let Brush: ScanditCaptureCoreDefaults.BrushDefaults
    }

    struct BarcodeCaptureSettingsDefaults: Encodable {
        let codeDuplicateFilter: Double
    }

    typealias SymbologySettingsDefaults = [String: String]
    typealias SymbologyDescriptionsDefaults = [String]

    let BarcodeCaptureOverlay: BarcodeCaptureOverlayDefaults
    let BarcodeCaptureSettings: BarcodeCaptureSettingsDefaults
    let SymbologySettings: SymbologySettingsDefaults
    let SymbologyDescriptions: SymbologyDescriptionsDefaults
    let RecommendedCameraSettings: CameraSettingsDefaults

    init(barcodeCaptureSettings: BarcodeCaptureSettings, overlay: BarcodeCaptureOverlay) {
        self.BarcodeCaptureOverlay = BarcodeCaptureOverlayDefaults.from(overlay)
        self.BarcodeCaptureSettings = BarcodeCaptureSettingsDefaults.from(barcodeCaptureSettings)
        self.SymbologySettings = SymbologySettingsDefaults.from(barcodeCaptureSettings)
        self.SymbologyDescriptions = SymbologyDescription.all.map { $0.jsonString() }
        self.RecommendedCameraSettings = CameraSettingsDefaults.from(BarcodeCapture.recommendedCameraSettings)
    }
}

extension ScanditBarcodeCaptureDefaults.BarcodeCaptureOverlayDefaults {
    static func from(_ overlay: BarcodeCaptureOverlay) -> ScanditBarcodeCaptureDefaults.BarcodeCaptureOverlayDefaults {
        let brush = ScanditCaptureCoreDefaults.BrushDefaults.from(overlay.brush)
        return ScanditBarcodeCaptureDefaults.BarcodeCaptureOverlayDefaults(Brush: brush)
    }
}

extension ScanditBarcodeCaptureDefaults.BarcodeCaptureSettingsDefaults {
    static func from(_ settings: BarcodeCaptureSettings) ->
        ScanditBarcodeCaptureDefaults.BarcodeCaptureSettingsDefaults {
            return ScanditBarcodeCaptureDefaults.BarcodeCaptureSettingsDefaults(codeDuplicateFilter:
                settings.codeDuplicateFilter)
    }
}

extension ScanditBarcodeCaptureDefaults.SymbologySettingsDefaults {
    static func from(_ settings: BarcodeCaptureSettings) -> ScanditBarcodeCaptureDefaults.SymbologySettingsDefaults {
        return SymbologyDescription.all.reduce(
            into: [String: String](), {(result, symbologyDescription) in
                let symbology = SymbologyDescription.symbology(fromIdentifier: symbologyDescription.identifier)
                let settings = settings.settings(for: symbology)
                result[symbologyDescription.identifier] = settings.toJSON()
        })
    }
}
