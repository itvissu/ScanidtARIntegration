/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */

package com.scandit.datacapture.cordova.core.data.defaults

import com.scandit.datacapture.cordova.core.data.SerializableData
import com.scandit.datacapture.cordova.core.utils.camelCaseName
import com.scandit.datacapture.core.source.CameraPosition
import org.json.JSONObject

data class SerializableTorchAvailability(
        private val availability: Map<CameraPosition, Boolean>
) : SerializableData {
    override fun toJson(): JSONObject = JSONObject(availability.mapKeys { it.key.camelCaseName })
}
