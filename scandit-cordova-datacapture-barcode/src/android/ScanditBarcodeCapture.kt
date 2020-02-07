/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */

package com.scandit.datacapture.cordova.barcode

import android.Manifest
import com.scandit.datacapture.barcode.capture.BarcodeCapture
import com.scandit.datacapture.barcode.capture.BarcodeCaptureDeserializer
import com.scandit.datacapture.barcode.capture.BarcodeCaptureDeserializerListener
import com.scandit.datacapture.barcode.capture.BarcodeCaptureListener
import com.scandit.datacapture.barcode.capture.BarcodeCaptureSession
import com.scandit.datacapture.cordova.barcode.actions.ActionFinishCallback
import com.scandit.datacapture.cordova.barcode.actions.ActionInjectDefaults
import com.scandit.datacapture.cordova.barcode.actions.ActionSendBarcodeScanned
import com.scandit.datacapture.cordova.barcode.actions.ActionSendSessionUpdated
import com.scandit.datacapture.cordova.barcode.actions.ActionSubscribeBarcodeCapture
import com.scandit.datacapture.cordova.barcode.callbacks.BarcodeCallbackContainer
import com.scandit.datacapture.cordova.barcode.callbacks.BarcodeCaptureCallback
import com.scandit.datacapture.cordova.barcode.data.SerializableFinishCallbackData
import com.scandit.datacapture.cordova.barcode.factories.BarcodeCaptureActionFactory
import com.scandit.datacapture.cordova.barcode.handlers.BarcodeCaptureHandler
import com.scandit.datacapture.cordova.core.communication.CameraPermissionGrantedListener
import com.scandit.datacapture.cordova.core.communication.ModeDeserializersProvider
import com.scandit.datacapture.cordova.core.errors.InvalidActionNameError
import com.scandit.datacapture.cordova.core.errors.JsonParseError
import com.scandit.datacapture.cordova.core.factories.ActionFactory
import com.scandit.datacapture.cordova.core.handlers.ActionsHandler
import com.scandit.datacapture.core.capture.serialization.DataCaptureModeDeserializer
import com.scandit.datacapture.core.data.FrameData
import com.scandit.datacapture.core.json.JsonValue
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONObject

class ScanditBarcodeCapture : CordovaPlugin(),
        BarcodeCaptureListener,
        BarcodeActionsListeners,
        BarcodeCaptureDeserializerListener,
        CameraPermissionGrantedListener,
        ModeDeserializersProvider {

    private val actionFactory: ActionFactory = BarcodeCaptureActionFactory(this)
    private val actionsHandler: ActionsHandler = ActionsHandler(actionFactory)

    private val barcodeCallbacks: BarcodeCallbackContainer = BarcodeCallbackContainer()
    private val barcodeCaptureHandler: BarcodeCaptureHandler = BarcodeCaptureHandler(this)

    private var lastBarcodeCaptureEnabledState: Boolean = false

    override fun pluginInitialize() {
        super.pluginInitialize()

        if (cordova.hasPermission(Manifest.permission.CAMERA)) {
            onCameraPermissionGranted()
        }
    }

    override fun onStop() {
        barcodeCallbacks.forceRelease()
        lastBarcodeCaptureEnabledState = barcodeCaptureHandler.barcodeCapture?.isEnabled ?: false
        barcodeCaptureHandler.barcodeCapture?.isEnabled = false
    }

    override fun onStart() {
        barcodeCaptureHandler.barcodeCapture?.isEnabled = lastBarcodeCaptureEnabledState
    }

    override fun onReset() {
        barcodeCaptureHandler.disposeCurrent()
        barcodeCallbacks.disposeAll()
    }

    override fun execute(
            action: String, args: JSONArray, callbackContext: CallbackContext
    ): Boolean {
        return try {
            actionsHandler.addAction(action, args, callbackContext)
        } catch (e: InvalidActionNameError) {
            false
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    //region BarcodeCaptureCallbackListener
    override fun onSessionUpdated(
            barcodeCapture: BarcodeCapture, session: BarcodeCaptureSession, data: FrameData
    ) {
        barcodeCallbacks.barcodeCaptureCallback?.onSessionUpdated(barcodeCapture, session, data)
    }

    override fun onBarcodeScanned(
            barcodeCapture: BarcodeCapture, session: BarcodeCaptureSession, data: FrameData
    ) {
        barcodeCallbacks.barcodeCaptureCallback?.onBarcodeScanned(barcodeCapture, session, data)
    }
    //endregion

    //region CameraPermissionGrantedListener
    override fun onCameraPermissionGranted() {
        actionsHandler.onCameraPermissionGranted()
    }
    //endregion

    //region ModeDeserializersProvider
    override fun provideModeDeserializers(): List<DataCaptureModeDeserializer> = listOf(
            BarcodeCaptureDeserializer().also {
                it.listener = this
            }
    )
    //endregion

    //region BarcodeCaptureDeserializerListener
    override fun onModeDeserializationFinished(
            deserializer: BarcodeCaptureDeserializer, mode: BarcodeCapture, json: JsonValue
    ) {
        mode.updateFromJson(json.jsonString())
        mode.isEnabled = json.getByKeyAsBoolean("enabled", false)
        barcodeCaptureHandler.attachBarcodeCapture(mode)
    }
    //endregion

    //region Action callbacks
    override fun onJsonParseError(error: Throwable, callbackContext: CallbackContext) {
        JsonParseError(error.message).sendResult(callbackContext)
    }

    //region ActionInjectDefaults.ResultListener
    override fun onInjectDefaultsActionExecuted(
            default: JSONObject, callbackContext: CallbackContext
    ) {
        callbackContext.success(default)
    }
    //endregion

    //region ActionSubscribeBarcodeCapture.ResultListener
    override fun onBarcodeCaptureSubscribeActionExecuted(callbackContext: CallbackContext) {
        barcodeCallbacks.setBarcodeCaptureCallback(
                BarcodeCaptureCallback(actionsHandler, callbackContext)
        )
        callbackContext.sendPluginResult(// We notify the callback context to keep it alive.
                PluginResult(PluginResult.Status.OK).apply {
                    keepCallback = true
                }
        )
    }
    //endregion

    //region ActionSendBarcodeScanned.ResultListener
    override fun onSendBarcodeScannedActionExecuted(
            message: JSONObject, callbackContext: CallbackContext
    ) {
        callbackContext.sendPluginResult(
                PluginResult(PluginResult.Status.OK, message).apply {
                    keepCallback = true
                }
        )
    }
    //endregion

    //region ActionSendSessionUpdated.ResultListener
    override fun onSendSessionUpdatedActionExecuted(
            message: JSONObject, callbackContext: CallbackContext
    ) {
        callbackContext.sendPluginResult(
                PluginResult(PluginResult.Status.OK, message).apply {
                    keepCallback = true
                }
        )
    }
    //endregion

    //region ActionFinishCallback.ResultListener
    override fun onFinishCallbackActionExecuted(
            finishData: SerializableFinishCallbackData, callbackContext: CallbackContext
    ) {
        barcodeCallbacks.barcodeCaptureCallback?.onFinishCallback(finishData.enabled)
    }
    //endregion
    //endregion
}

interface BarcodeActionsListeners : ActionInjectDefaults.ResultListener,
        ActionSubscribeBarcodeCapture.ResultListener,
        ActionSendSessionUpdated.ResultListener,
        ActionSendBarcodeScanned.ResultListener,
        ActionFinishCallback.ResultListener
