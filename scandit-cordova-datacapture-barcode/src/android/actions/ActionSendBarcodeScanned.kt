/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */

package com.scandit.datacapture.cordova.barcode.actions

import com.scandit.datacapture.cordova.core.actions.ActionBlocking
import com.scandit.datacapture.cordova.core.data.SerializableCallbackAction
import org.apache.cordova.CallbackContext
import org.json.JSONArray
import org.json.JSONObject

class ActionSendBarcodeScanned(
        private val listener: ResultListener
) : ActionBlocking {

    override fun run(args: JSONArray, callbackContext: CallbackContext): Boolean {
        val message = SerializableCallbackAction(
                ACTION_NAME,
                args.getJSONObject(0),
                shouldNotifyWhenFinished = true
        ).toJson()
        listener.onSendBarcodeScannedActionExecuted(message, callbackContext)
        return true
    }

    companion object {
        private const val ACTION_NAME = "didScanInBarcodeCapture"
    }

    interface ResultListener {
        fun onSendBarcodeScannedActionExecuted(
                message: JSONObject, callbackContext: CallbackContext
        )
    }
}
