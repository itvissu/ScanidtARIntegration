/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */

package com.scandit.datacapture.cordova.barcode.actions

import com.scandit.datacapture.barcode.capture.BarcodeCapture
import com.scandit.datacapture.barcode.capture.BarcodeCaptureSettings
import com.scandit.datacapture.barcode.data.SymbologyDescription
import com.scandit.datacapture.barcode.ui.overlay.BarcodeCaptureOverlay
import com.scandit.datacapture.cordova.barcode.data.defaults.SerializableBarcodeCaptureOverlayDefaults
import com.scandit.datacapture.cordova.barcode.data.defaults.SerializableBarcodeCaptureSettingsDefaults
import com.scandit.datacapture.cordova.barcode.data.defaults.SerializableBarcodeDefaults
import com.scandit.datacapture.cordova.barcode.data.defaults.SerializableSymbologySettingsDefaults
import com.scandit.datacapture.cordova.core.actions.Action
import com.scandit.datacapture.cordova.core.data.defaults.SerializableBrushDefaults
import com.scandit.datacapture.cordova.core.data.defaults.SerializableCameraSettingsDefault
import com.scandit.datacapture.cordova.core.utils.hexString
import com.scandit.datacapture.core.source.toJson
import org.apache.cordova.CallbackContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ActionInjectDefaults(
        private val listener: ResultListener
) : Action {

    override fun run(args: JSONArray, callbackContext: CallbackContext): Boolean {
        try {
            val settings = BarcodeCaptureSettings()
            val brush = BarcodeCaptureOverlay.defaultBrush()
            val symbologyDescriptions = SymbologyDescription.all()
            val cameraSettings = BarcodeCapture.createRecommendedCameraSettings()
            val defaults = SerializableBarcodeDefaults(
                    barcodeCaptureOverlayDefaults = SerializableBarcodeCaptureOverlayDefaults(
                            brushDefaults = SerializableBrushDefaults(
                                    fillColor = brush.fillColor.hexString,
                                    strokeColor = brush.strokeColor.hexString,
                                    strokeWidth = brush.strokeWidth
                            )
                    ),
                    barcodeCaptureSettingsDefaults = SerializableBarcodeCaptureSettingsDefaults(
                            codeDuplicateFilter = settings.codeDuplicateFilter.asSeconds()
                    ),
                    symbologySettingsDefaults = SerializableSymbologySettingsDefaults(
                            barcodeCaptureSettings = settings
                    ),
                    symbologyDescriptionsDefaults = JSONArray(
                            symbologyDescriptions.map { it.toJson() }
                    ),
                    recommendedCameraSettings = SerializableCameraSettingsDefault(
                            prefResolution = cameraSettings.preferredResolution.toJson(),
                            maxFrameRate = cameraSettings.maxFrameRate,
                            zoomFactor = cameraSettings.zoomFactor,
                            focusRange = "full"
                    )
            ).toJson()
            listener.onInjectDefaultsActionExecuted(defaults, callbackContext)
        } catch (e: JSONException) {
            e.printStackTrace()
            listener.onJsonParseError(e, callbackContext)
        }
        return true
    }

    interface ResultListener {
        fun onInjectDefaultsActionExecuted(default: JSONObject, callbackContext: CallbackContext)
        fun onJsonParseError(error: Throwable, callbackContext: CallbackContext)
    }
}
