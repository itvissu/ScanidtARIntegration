/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */

package com.scandit.datacapture.cordova.core.data.defaults

import com.scandit.datacapture.cordova.core.data.SerializableData
import org.json.JSONObject

data class SerializableBrushDefaults(
        private val fillColor: String,
        private val strokeColor: String,
        private val strokeWidth: Float
) : SerializableData {

    override fun toJson(): JSONObject = JSONObject(
            mapOf(
                    FIELD_FILL_COLOR to fillColor,
                    FIELD_STROKE_COLOR to strokeColor,
                    FIELD_STROKE_WIDTH to strokeWidth
            )
    )

    companion object {
        private const val FIELD_FILL_COLOR = "fillColor"
        private const val FIELD_STROKE_COLOR = "strokeColor"
        private const val FIELD_STROKE_WIDTH = "strokeWidth"
    }
}
