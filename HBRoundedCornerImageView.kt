package com.max.hbcustomview.roundedview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.pow
import com.max.hbcustomview.R

class HBRoundedCornerImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {
    private val mDrawableRect = RectF()
    private val mBorderRect = RectF()
    private val mShaderMatrix = Matrix()
    private val mBitmapPaint: Paint = Paint()
    private val mBorderPaint = Paint()
    private val mBackgroundPaint = Paint()
    private var mBorderColor = DEFAULT_BORDER_COLOR
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    private var mIsCircleMode = true
    private var mBackgroundColor = DEFAULT_CIRCLE_BACKGROUND_COLOR
    private var mBitmap: Bitmap? = null
    private var mBitmapShader: BitmapShader? = null
    private var mBitmapWidth = 0
    private var mBitmapHeight = 0
    private var mRoundedCornerRadius = 0
    private var mDrawableRadius = 0f
    private var mBorderRadius = 0f
    private var mColorFilter: ColorFilter? = null
    private var mReady = false
    private var mSetupPending = false
    private var mBorderOverlay = false

    init {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.HBRoundedCornerImageView, defStyle, 0)
        mBorderWidth = a.getDimensionPixelSize(
            R.styleable.HBRoundedCornerImageView_hb_border_width,
            DEFAULT_BORDER_WIDTH
        )
        mBorderColor = a.getColor(
            R.styleable.HBRoundedCornerImageView_hb_border_color,
            DEFAULT_BORDER_COLOR
        )
        mBorderOverlay = a.getBoolean(
            R.styleable.HBRoundedCornerImageView_hb_border_overlay,
            DEFAULT_BORDER_OVERLAY
        )
        mBackgroundColor = a.getColor(
            R.styleable.HBRoundedCornerImageView_hb_background_color,
            DEFAULT_CIRCLE_BACKGROUND_COLOR
        )
        mIsCircleMode = a.getBoolean(
            R.styleable.HBRoundedCornerImageView_hb_circle_mode,
            DEFAULT_CIRCLE_MODE
        )
        mRoundedCornerRadius = a.getDimensionPixelSize(
            R.styleable.HBRoundedCornerImageView_hb_rounded_corner,
            DEFAULT_ROUNDER_CORNER
        )
        a.recycle()
        init()
    }

    private fun init() {
        super.setScaleType(SCALE_TYPE)
        mReady = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = OutlineProvider()
        }
        if (mSetupPending) {
            setup()
            mSetupPending = false
        }
    }

    override fun getScaleType(): ScaleType {
        return SCALE_TYPE
    }

    override fun setScaleType(scaleType: ScaleType) {
        require(scaleType == SCALE_TYPE) { String.format("ScaleType %s not supported.", scaleType) }
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { "adjustViewBounds not supported." }
    }

    override fun onDraw(canvas: Canvas) {
        mBitmap ?: return
        if (mIsCircleMode) {
            drawCircleImageView(canvas)
        } else {
            drawRoundedCornerImageView(canvas)
        }
    }

    private fun drawRoundedCornerImageView(canvas: Canvas) {
        if (mBackgroundColor != Color.TRANSPARENT) {
            drawRoundRect(canvas, mBackgroundPaint)
        }
        drawRoundRect(canvas, mBitmapPaint)
        if (mBorderWidth > 0) {
            drawRoundRect(canvas, mBorderPaint)
        }
    }

    private fun drawCircleImageView(canvas: Canvas) {
        if (mBackgroundColor != Color.TRANSPARENT) {
            drawCircle(canvas, mBackgroundPaint)
        }
        drawCircle(canvas, mBitmapPaint)
        if (mBorderWidth > 0) {
            drawCircle(canvas, mBorderPaint)
        }
    }

    private fun drawCircle(canvas: Canvas, paint: Paint) {
        canvas.drawCircle(
            mDrawableRect.centerX(),
            mDrawableRect.centerY(),
            mDrawableRadius,
            paint
        )
    }

    private fun drawRoundRect(canvas: Canvas, paint: Paint) {
        canvas.drawRoundRect(
            mDrawableRect,
            mRoundedCornerRadius.toFloat(),
            mRoundedCornerRadius.toFloat(),
            paint
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        setup()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        setup()
    }

    var borderColor: Int
        get() = mBorderColor
        set(borderColor) {
            if (borderColor == mBorderColor) {
                return
            }
            mBorderColor = borderColor
            mBorderPaint.color = mBorderColor
            invalidate()
        }
    var circleBackgroundColor: Int
        get() = mBackgroundColor
        set(circleBackgroundColor) {
            if (circleBackgroundColor == mBackgroundColor) {
                return
            }
            mBackgroundColor = circleBackgroundColor
            mBackgroundPaint.color = circleBackgroundColor
            invalidate()
        }

    fun setCircleBackgroundColorResource(@ColorRes circleBackgroundRes: Int) {
        circleBackgroundColor = context.resources.getColor(circleBackgroundRes)
    }

    var borderWidth: Int
        get() = mBorderWidth
        set(borderWidth) {
            if (borderWidth == mBorderWidth) {
                return
            }
            mBorderWidth = borderWidth
            setup()
        }
    var isBorderOverlay: Boolean
        get() = mBorderOverlay
        set(borderOverlay) {
            if (borderOverlay == mBorderOverlay) {
                return
            }
            mBorderOverlay = borderOverlay
            setup()
        }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        initializeBitmap()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initializeBitmap()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        initializeBitmap()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initializeBitmap()
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (cf === mColorFilter) {
            return
        }
        mColorFilter = cf
        applyColorFilter()
        invalidate()
    }

    override fun getColorFilter(): ColorFilter? {
        return mColorFilter
    }

    private fun applyColorFilter() {
        // This might be called from setColorFilter during ImageView construction
        // before member initialization has finished on API level <= 19.
        mBitmapPaint.colorFilter = mColorFilter
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else try {
            val bitmap: Bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(
                    COLOR_DRAWABLE_DIMENSION,
                    COLOR_DRAWABLE_DIMENSION,
                    BITMAP_CONFIG
                )
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    BITMAP_CONFIG
                )
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun initializeBitmap() {
        mBitmap = getBitmapFromDrawable(drawable)
        setup()
    }

    private fun setup() {
        if (!mReady) {
            mSetupPending = true
            return
        }
        if (width == 0 && height == 0) {
            return
        }
        if (mBitmap == null) {
            invalidate()
            return
        }
        mBitmapShader = BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mBitmapPaint.let {
            it.isAntiAlias = true
            it.isDither = true
            it.isFilterBitmap = true
            it.shader = mBitmapShader
        }
        mBorderPaint.let {
            it.style = Paint.Style.STROKE
            it.color = mBorderColor
            it.strokeWidth = mBorderWidth.toFloat()
        }
        mBackgroundPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = mBackgroundColor
        }

        mBitmapHeight = mBitmap?.height ?: 0
        mBitmapWidth = mBitmap?.width ?: 0
        mBorderRect.set(calculateBounds())
        mBorderRadius =
            ((mBorderRect.height() - mBorderWidth) / 2.0f).coerceAtMost((mBorderRect.width() - mBorderWidth) / 2.0f)
        mDrawableRect.set(mBorderRect)
        if (!mBorderOverlay && mBorderWidth > 0) {
            mDrawableRect.inset(mBorderWidth - 1.0f, mBorderWidth - 1.0f)
        }
        mDrawableRadius = (mDrawableRect.height() / 2.0f).coerceAtMost(mDrawableRect.width() / 2.0f)
        applyColorFilter()
        updateShaderMatrix()
        invalidate()
    }

    private fun calculateBounds(): RectF {
        return if (mIsCircleMode) {
            val availableWidth = width - paddingLeft - paddingRight
            val availableHeight = height - paddingTop - paddingBottom
            val sideLength = availableWidth.coerceAtMost(availableHeight)
            val left = paddingLeft + (availableWidth - sideLength) / 2f
            val top = paddingTop + (availableHeight - sideLength) / 2f
            RectF(left, top, left + sideLength, top + sideLength)
        } else {
            val availableWidth = width - paddingLeft - paddingRight
            val availableHeight = height - paddingTop - paddingBottom
            val left = paddingLeft.toFloat()
            val top = paddingTop.toFloat()
            return RectF(left, top, left + availableWidth, top + availableHeight)
        }
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f
        mShaderMatrix.set(null)
        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / mBitmapHeight.toFloat()
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f
        } else {
            scale = mDrawableRect.width() / mBitmapWidth.toFloat()
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
        }
        if (mIsCircleMode) {
            mShaderMatrix.setScale(scale, scale)
            dx = ((dx + 0.5f).toInt() + mDrawableRect.left).coerceAtMost(MAX_BITMAP_WIDTH.toFloat())
            dy = ((dy + 0.5f).toInt() + mDrawableRect.top).coerceAtMost(MAX_BITMAP_WIDTH.toFloat())

            mShaderMatrix.postTranslate(dx, dy)
            mBitmapShader?.setLocalMatrix(mShaderMatrix)
        } else {
            mShaderMatrix.setScale(scale, scale)
            mShaderMatrix.postTranslate(
                (dx + 0.5f).toInt() + mDrawableRect.left,
                (dy + 0.5f).toInt() + mDrawableRect.top
            )
            mBitmapShader?.setLocalMatrix(mShaderMatrix)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return inTouchableArea(event.x, event.y) && super.onTouchEvent(event)
    }

    private fun inTouchableArea(x: Float, y: Float): Boolean {
        return if (mBorderRect.isEmpty) {
            true
        } else (x - mBorderRect.centerX()).toDouble()
            .pow(2.0) + (y - mBorderRect.centerY()).toDouble()
            .pow(2.0) <= mBorderRadius.toDouble()
            .pow(2.0)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private inner class OutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val bounds = Rect()
            mBorderRect.roundOut(bounds)
            outline.setRoundRect(bounds, bounds.width() / 2.0f)
        }
    }

    companion object {
        private val SCALE_TYPE = ScaleType.CENTER_CROP
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private const val COLOR_DRAWABLE_DIMENSION = 2
        private const val DEFAULT_BORDER_WIDTH = 0
        private const val DEFAULT_ROUNDER_CORNER = 0
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
        private const val DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.TRANSPARENT
        private const val DEFAULT_BORDER_OVERLAY = false
        private const val DEFAULT_CIRCLE_MODE = false
        private const val MAX_BITMAP_WIDTH = 50
    }
}