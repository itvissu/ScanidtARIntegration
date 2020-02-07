/*
 * This file is part of the Scandit Data Capture SDK
 *
 * Copyright (C) 2019- Scandit AG. All rights reserved.
 */

package com.scandit.datacapture.cordova.barcode.factories

import com.scandit.datacapture.cordova.barcode.BarcodeActionsListeners
import com.scandit.datacapture.cordova.barcode.actions.ActionFinishCallback
import com.scandit.datacapture.cordova.barcode.actions.ActionInjectDefaults
import com.scandit.datacapture.cordova.barcode.actions.ActionSendBarcodeScanned
import com.scandit.datacapture.cordova.barcode.actions.ActionSendSessionUpdated
import com.scandit.datacapture.cordova.barcode.actions.ActionSubscribeBarcodeCapture
import com.scandit.datacapture.cordova.core.actions.Action
import com.scandit.datacapture.cordova.core.errors.InvalidActionNameError
import com.scandit.datacapture.cordova.core.factories.ActionFactory

class BarcodeCaptureActionFactory(
        private val listener: BarcodeActionsListeners
) : ActionFactory {

    @Throws(InvalidActionNameError::class)
    override fun provideAction(actionName: String): Action {
        return when (actionName) {
            INJECT_DEFAULTS -> createActionInjectDefaults()
            SUBSCRIBE_BARCODE_CAPTURE -> createActionSubscribeBarcodeCapture()
            SEND_SESSION_UPDATED_EVENT -> createActionSessionUpdated()
            SEND_BARCODE_SCANNED_EVENT -> createActionBarcodeScanned()
            FINISH_BLOCKING_ACTION -> createActionFinishBlocking()
            else -> throw InvalidActionNameError(actionName)
        }
    }

    private fun createActionInjectDefaults(): Action = ActionInjectDefaults(listener)

    private fun createActionSubscribeBarcodeCapture(): Action = ActionSubscribeBarcodeCapture(
            listener
    )

    private fun createActionSessionUpdated(): Action = ActionSendSessionUpdated(
            listener
    )

    private fun createActionBarcodeScanned(): Action = ActionSendBarcodeScanned(
            listener
    )

    private fun createActionFinishBlocking(): Action = ActionFinishCallback(
            listener
    )

    override fun doesNotDependOnCameraPermission(actionName: String): Boolean {
        return ACTIONS_NOT_DEPENDING_ON_CAMERA.contains(actionName)
    }

    companion object {
        private const val INJECT_DEFAULTS = "getDefaults"
        private const val SUBSCRIBE_BARCODE_CAPTURE = "subscribeBarcodeCaptureListener"
        private const val FINISH_BLOCKING_ACTION = "finishCallback"
        const val SEND_SESSION_UPDATED_EVENT = "sendSessionUpdateEvent"
        const val SEND_BARCODE_SCANNED_EVENT = "sendBarcodeScannedEvent"

        private val ACTIONS_NOT_DEPENDING_ON_CAMERA = arrayOf(INJECT_DEFAULTS)
    }
}
