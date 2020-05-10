package com.estzhe.timer.activities.utility

import android.os.Bundle
import androidx.wear.ambient.AmbientModeSupport

internal class AmbientCallback(private val onAmbientListener: OnAmbientListener)
    : AmbientModeSupport.AmbientCallback()
{
    override fun onEnterAmbient(ambientDetails: Bundle) {
        super.onEnterAmbient(ambientDetails)
        onAmbientListener.onEnterAmbient(ambientDetails)
    }

    override fun onExitAmbient() {
        super.onExitAmbient()
        onAmbientListener.onExitAmbient()
    }

    interface OnAmbientListener {
        fun onEnterAmbient(ambientDetails: Bundle)
        fun onExitAmbient()
    }
}