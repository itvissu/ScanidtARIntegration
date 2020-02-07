/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */

package com.scandit.datacapture.cordova.barcode.callbacks

import com.scandit.datacapture.barcode.capture.BarcodeCapture
import com.scandit.datacapture.barcode.capture.BarcodeCaptureSession
import com.scandit.datacapture.cordova.barcode.factories.BarcodeCaptureActionFactory
import com.scandit.datacapture.cordova.core.callbacks.Callback
import com.scandit.datacapture.cordova.core.handlers.ActionsHandler
import com.scandit.datacapture.core.data.FrameData
import org.apache.cordova.CallbackContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeCaptureCallback(
        private val actionsHandler: ActionsHandler,
        callbackContext: CallbackContext
) : Callback(callbackContext) {

    private val semaphore = Semaphore(0)

    private val latestEnabledState: AtomicBoolean = AtomicBoolean(false)

    fun onSessionUpdated(
            barcodeCapture: BarcodeCapture,
            session: BarcodeCaptureSession,
            frameData: FrameData
    ) {
        if (disposed.get()) return
        actionsHandler.addAction(
                BarcodeCaptureActionFactory.SEND_SESSION_UPDATED_EVENT,
                JSONArray().apply {
                    put(
                            JSONObject(
                                    mapOf(
                                            FIELD_SESSION to session.toJson(),
                                            FIELD_FRAME_DATA to serializeFrameData(
                                                    frameData
                                            ).toString()
                                    )
                            )
                    )
                },
                callbackContext
        )
        lockAndWait()
        barcodeCapture.isEnabled = latestEnabledState.get()
    }

    fun onBarcodeScanned(
            barcodeCapture: BarcodeCapture,
            session: BarcodeCaptureSession,
            frameData: FrameData
    ) {
        if (disposed.get()) return
        actionsHandler.addAction(
                BarcodeCaptureActionFactory.SEND_BARCODE_SCANNED_EVENT,
                JSONArray().apply {
                    put(
                            JSONObject(
                                    mapOf(
                                            FIELD_SESSION to session.toJson(),
                                            FIELD_FRAME_DATA to serializeFrameData(
                                                    frameData
                                            ).toString()
                                    )
                            )
                    )
                },
                callbackContext
        )
        lockAndWait()
        barcodeCapture.isEnabled = latestEnabledState.get()
    }

    private fun lockAndWait() {
        semaphore.acquire()
    }

    fun onFinishCallback(enabledState: Boolean) {
        latestEnabledState.set(enabledState)
        unlock()
    }

    fun forceRelease() {
        unlock()
    }

    private fun unlock() {
        semaphore.release()
    }

    private fun serializeFrameData(frameData: FrameData): JSONObject = JSONObject(
            mapOf(
                    FIELD_FRAME_DATA to JSONObject()
            )
    )

    override fun dispose() {
        super.dispose()
        unlock()
    }

    companion object {
        private const val FIELD_SESSION = "session"
        private const val FIELD_FRAME_DATA = "frameData"
    }
}
