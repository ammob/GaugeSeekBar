package com.tenclouds.gaugeprogressbar

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_animated_progress.*
import kotlinx.android.synthetic.main.fragment_animated_progress.view.*

class FragmentAnimatedProgress : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_animated_progress, container, false)
        view.updateButton.setOnClickListener {
            val newProgress = seekbar.progress / seekbar.max.toFloat()
            val oldProgress = progress.getProgress()
            val valueAnimator = ValueAnimator.ofFloat(oldProgress, newProgress)
            valueAnimator.duration = 1000
            valueAnimator.addUpdateListener {
                progress.setProgress(it.animatedValue as Float)
                if (it.animatedValue as Float >= 1) {
                    progress.runCompleteAnimation()
                }
            }
            valueAnimator.start()
        }
        return view
    }
}