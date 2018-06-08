package io.nyris.camera

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * FocusMarkerLayout.kt - layout class for showing marker when user tap to use manual focus
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */

@TargetApi(14)
class FocusMarkerLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val mFocusMarkerContainer: FrameLayout
    private val mFill: ImageView

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_focus_marker, this)

        mFocusMarkerContainer = findViewById(R.id.focusMarkerContainer)
        mFill = findViewById(R.id.fill)

        mFocusMarkerContainer.alpha = 0f
    }

    fun focus(mx: Float, my: Float) {
        val x = (mx - mFocusMarkerContainer.width / 2).toInt()
        val y = (my - mFocusMarkerContainer.width / 2).toInt()

        mFocusMarkerContainer.translationX = x.toFloat()
        mFocusMarkerContainer.translationY = y.toFloat()

        mFocusMarkerContainer.animate().setListener(null).cancel()
        mFill.animate().setListener(null).cancel()

        mFill.scaleX = 0f
        mFill.scaleY = 0f
        mFill.alpha = 1f

        mFocusMarkerContainer.scaleX = 1.36f
        mFocusMarkerContainer.scaleY = 1.36f
        mFocusMarkerContainer.alpha = 1f

        mFocusMarkerContainer.animate().scaleX(1f).scaleY(1f).setStartDelay(0).setDuration(30)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mFocusMarkerContainer.animate().alpha(0f).setStartDelay(150).setDuration(200).setListener(null).start()
                    }
                }).start()

        mFill.animate().scaleX(1f).scaleY(1f).setDuration(30)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mFill.animate().alpha(0f).setDuration(200).setListener(null).start()
                    }
                }).start()

    }

    override fun performClick(): Boolean {
        // do what you want
        return true
    }
}
