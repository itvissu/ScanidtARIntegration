/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */

package com.scandit.datacapture.cordova.barcode.callbacks

class BarcodeCallbackContainer {

    var barcodeCaptureCallback: BarcodeCaptureCallback? = null
        private set

    fun setBarcodeCaptureCallback(barcodeCaptureCallback: BarcodeCaptureCallback) {
        disposeBarcodeCaptureCallback()
        this.barcodeCaptureCallback = barcodeCaptureCallback
    }

    fun disposeAll() {
        disposeBarcodeCaptureCallback()
    }

    fun forceRelease() {
        barcodeCaptureCallback?.forceRelease()
    }

    private fun disposeBarcodeCaptureCallback() {
        barcodeCaptureCallback?.dispose()
        barcodeCaptureCallback = null
    }
}