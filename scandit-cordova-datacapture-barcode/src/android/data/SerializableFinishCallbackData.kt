/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */
package com.scandit.datacapture.cordova.barcode.data

import android.util.Log
import org.json.JSONObject

class SerializableFinishCallbackData(json: JSONObject) {
    val enabled: Boolean = if (json.has("enabled")) {
        json.getBoolean("enabled")
    } else {
        Log.i("Scandit Barcode Capture", "No enabled field in finishCallback action $json")
        false
    }
}