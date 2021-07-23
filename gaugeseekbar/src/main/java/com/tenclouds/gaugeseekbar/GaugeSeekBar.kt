package com.tenclouds.gaugeseekbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable


class GaugeSeekBar : View {

    private companion object {
        private const val DEFAULT_START_ANGLE_DEG = 30f
        private const val DEFAULT_THUMB_RADIUS_DP = 11
        private const val DEFAULT_TRACK_WIDTH_DP = 8

        private const val DEGREE_TO_RADIAN_RATIO = 0.0174533
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        applyAttributes(context.theme.obtainStyledAttributes(attrs, R.styleable.GaugeSeekBar, 0, 0))
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        applyAttributes(context.theme.obtainStyledAttributes(attrs, R.styleable.GaugeSeekBar, 0, 0))
    }

    private var thumbRadius = DEFAULT_THUMB_RADIUS_DP * resources.displayMetrics.density
    private var trackWidth = DEFAULT_TRACK_WIDTH_DP * resources.displayMetrics.density
    private var progressWidth = DEFAULT_TRACK_WIDTH_DP * resources.displayMetrics.density
    private var trackGradientArray: IntArray = context.resources.getIntArray(R.array.default_track_gradient)
    private var progressGradientArray = context.resources.getIntArray(R.array.default_index_gradient)
    private var progressGradientArrayPositions: FloatArray? = null
    private var thumbColor: Int = ContextCompat.getColor(context, R.color.default_thumb_color)
    private var thumbOuterColor: Int = ContextCompat.getColor(context, android.R.color.white)
    private var startAngle = DEFAULT_START_ANGLE_DEG
    private var centerPosition = PointF(0f, 0f)
    private var thumbDrawableId: Int = 0

    private var trackDrawable: TrackDrawable? = null
    private var progressDrawable: ProgressDrawable? = null
    private var thumbEntity: ThumbEntity? = null
    private var lottieDrawableId: Int = 0
    private var lottieDrawable: LottieDrawable? = null

    private var showThumb: Boolean = true
    private var showProgress: Boolean = true

    private var progress: Float = 0f

    var interactive: Boolean = true

    var progressChangedCallback: (progress: Float) -> Unit = {}

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    /**
     * Set whether thumb is visible. To update the view call invalidate().
     *
     * @param showThumb When set to false yhumb will not be drawn
     */
    fun setShowThumb(showThumb: Boolean) {
        this.showThumb = showThumb
    }

    /**
     * Set wheter progress bar is visible. To update the view call invalidate().
     *
     * @param showProgress When set to false progress bar will not be drawn
     */
    fun setShowProgress(showProgress: Boolean) {
        this.showProgress = showProgress
    }

    /**
     * @return boolean indicating whether progress bar is visible
     */
    fun getShowProgress() = showProgress

    /**
     * Set track width in dp. To update the view call invalidate().
     *
     * @param trackWidthDp Track width in dp
     */
    fun setTrackWidthDp(trackWidthDp: Int) {
        trackWidth = trackWidthDp * resources.displayMetrics.density
    }

    /**
     * Set track width in pixels. To update the view call invalidate().
     *
     * @param trackWidth Track width in pixels
     */
    fun setTrackWidth(trackWidth: Float) {
        this.trackWidth = trackWidth
    }

    /**
     * Set track width from dimension resource. To update the view call invalidate().
     *
     * @param widthDimensId Dimension resource id
     */
    fun setTrackWidth(@DimenRes widthDimensId: Int) {
        trackWidth = context.resources.getDimension(widthDimensId)
    }

    /**
     * Set progress width in dp. To update the view call invalidate().
     *
     * @param progressWidthDp Progress bar width in dp
     */
    fun setProgressWidthDp(progressWidthDp: Float) {
        this.progressWidth = progressWidthDp
    }

    /**
     * Set progress width in pixels. To update the view call invalidate().
     *
     * @param progressWidth Progress bar width in pixels
     */
    fun setProgressWidth(progressWidth: Float) {
        this.progressWidth = progressWidth
    }

    /**
     * Set progress width from dimension resource. To update the view call invalidate().
     *
     * @param progressWidthResourceId Progress bar width dimension resource
     */
    fun setProgressWidth(@DimenRes progressWidthResourceId: Int) {
        progressWidth = context.resources.getDimension(progressWidthResourceId)
    }

    /**
     * Set progress and invalidate the view
     *
     * @param progress Progress from 0.0 to 1.0
     */
    fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        this.progress = when {
            progress in 0f..1f -> progress
            progress > 1f -> 1f
            else -> 0f
        }
        invalidate()
    }

    /**
     * @return Progress as float in range from 0.0 to 1.0
     */
    fun getProgress() = progress

    private fun applyAttributes(attributes: TypedArray) {
        try {
            startAngle = attributes.getFloat(R.styleable.GaugeSeekBar_startAngleDegrees, startAngle)
            thumbRadius = attributes.getDimension(R.styleable.GaugeSeekBar_thumbRadius, thumbRadius)
            thumbColor = attributes.getColor(R.styleable.GaugeSeekBar_thumbColor, thumbColor)
            thumbOuterColor = attributes.getColor(R.styleable.GaugeSeekBar_thumbOuterColor, thumbOuterColor)
            val trackGradientArrayId = attributes.getResourceId(R.styleable.GaugeSeekBar_trackGradient, 0)
            if (trackGradientArrayId != 0) {
                trackGradientArray = resources.getIntArray(trackGradientArrayId)
            }

            val trackGradientArrayPositionsResourceId = attributes.getResourceId(R.styleable.GaugeSeekBar_trackGradientPositions, 0)
            if (trackGradientArrayPositionsResourceId != 0) {
                val positionsIntArray = resources.getIntArray(trackGradientArrayPositionsResourceId)
                progressGradientArrayPositions = FloatArray(positionsIntArray.size) { positionsIntArray[it].div(100f) }
            }

            showThumb = attributes.getBoolean(R.styleable.GaugeSeekBar_showThumb, showThumb)
            progressWidth = attributes.getDimension(R.styleable.GaugeSeekBar_progressWidth, progressWidth)
            trackWidth = attributes.getDimension(R.styleable.GaugeSeekBar_trackWidth, trackWidth)
            progress = attributes.getFloat(R.styleable.GaugeSeekBar_progress, 0f)

            val gradientArrayResourceId = attributes.getResourceId(R.styleable.GaugeSeekBar_progressGradient, 0)
            if (gradientArrayResourceId != 0) {
                progressGradientArray = resources.getIntArray(gradientArrayResourceId)
            }
            val gradientArrayPositionsResourceId = attributes.getResourceId(R.styleable.GaugeSeekBar_progressGradientPositions, 0)
            if (gradientArrayPositionsResourceId != 0) {
                val positionsIntArray = resources.getIntArray(gradientArrayPositionsResourceId)
                progressGradientArrayPositions = FloatArray(positionsIntArray.size) { positionsIntArray[it].div(100f) }
            }

            interactive = attributes.getBoolean(R.styleable.GaugeSeekBar_interactive, interactive)
            thumbDrawableId = attributes.getResourceId(R.styleable.GaugeSeekBar_thumbDrawable, 0)
            lottieDrawableId = attributes.getResourceId(R.styleable.GaugeSeekBar_lottieDrawable, 0)
            showProgress = attributes.getBoolean(R.styleable.GaugeSeekBar_showProgress, showProgress)
        } finally {
            attributes.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        init(measuredWidth / 2f, measuredHeight / 2f)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean =
        if (interactive) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    performClick()
                    handleMotionEvent(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    handleMotionEvent(event)
                }
            }
            true
        } else {
            super.onTouchEvent(event)
        }

    private fun handleMotionEvent(event: MotionEvent) {
        val relativeX = measuredWidth / 2f - event.x
        val relativeY = event.y - measuredHeight / 2f
        val angle = Math.toDegrees(Math.atan2(relativeX.toDouble(), relativeY.toDouble()))
        setProgress(angleToProgress(if (angle > 0) angle else angle + 360f))
        progressChangedCallback.invoke(progress)
    }

    private fun angleToProgress(angle: Double): Float {
        val availableAngle = 360 - 2 * startAngle
        val relativeAngle = angle - startAngle
        return (relativeAngle / availableAngle).toFloat()
    }

    private fun init(centerX: Float, centerY: Float) {
        centerPosition = PointF(centerX, centerY)
        val radiusPx = Math.min(centerX, centerY)
        val margin = Math.max(thumbRadius, trackWidth / 2f)
        trackDrawable = TrackDrawable(centerPosition, radiusPx, margin, trackGradientArray, startAngle, trackWidth)

        if (showProgress) {
            progressDrawable = ProgressDrawable(centerPosition, progress, radiusPx, margin, progressGradientArray, startAngle, progressWidth, progressGradientArrayPositions)
        }

        if (showThumb) {
            val bitmap = getBitmapFromVectorDrawable(context, thumbDrawableId)
            val thumbDrawable = ThumbDrawable(thumbColor, thumbOuterColor, bitmap)

            if (lottieDrawableId > 0) {
                lottieDrawable = LottieDrawable()
                lottieDrawable?.let {
                    it.enableMergePathsForKitKatAndAbove(true)
                    it.callback = this
                    val result = LottieCompositionFactory.fromRawResSync(context.applicationContext, lottieDrawableId)
                    it.composition = result.value
                    it.repeatCount = 0
                    it.addAnimatorUpdateListener { invalidate() }
                }
            }
            thumbEntity = ThumbEntity(centerPosition, progress, startAngle, thumbRadius, thumbDrawable)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            trackDrawable?.draw(this)
            progressDrawable?.draw(this, progress)
            thumbEntity?.draw(this, progress)
            if (lottieDrawableId > 0) {
                canvas.updateCanvasPositionForLottie(progress)
                lottieDrawable?.draw(this)
            }
        }
    }

    fun runCompleteAnimation() {
        lottieDrawable?.start()
    }

    private fun Canvas.updateCanvasPositionForLottie(progress: Float) {
        lottieDrawable?.let {
            val seekbarRadius = Math.min(centerPosition.x, centerPosition.y) - thumbRadius

            val angle = (startAngle + (360 - 2 * startAngle) * progress) * DEGREE_TO_RADIAN_RATIO

            val indicatorX = centerPosition.x - Math.sin(angle) * seekbarRadius
            val indicatorY = Math.cos(angle) * seekbarRadius + centerPosition.y

            val lottiWidth = it.intrinsicWidth / 2
            translate((indicatorX - lottiWidth).toFloat(), (indicatorY - lottiWidth).toFloat())
        }
    }


    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = if (drawableId != 0) ContextCompat.getDrawable(context, drawableId) else null
        return drawable?.let {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    public fun updateProgressColor(colors: Int){
        progressGradientArray = context.resources.getIntArray(colors)
        init(measuredWidth / 2f, measuredHeight / 2f)
    }
}
