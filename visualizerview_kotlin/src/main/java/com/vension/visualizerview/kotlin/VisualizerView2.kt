package com.vension.visualizerview.kotlin


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot


/**
 * 自定义View——随音乐频谱跳动的线条
 */
class VisualizerView2(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private val mColor: Int// 主色调
    private val mLineWidth: Int// 频谱线条宽度
    private var mSpeceNum: Int = 0// 空隙个数(不设置自己计算)
    private val mSpeceWidth: Int// 空隙宽度
    private val mBaseHeight: Int// 基础高度
    private val mLineIsSingleColor: Boolean // 线条只有一种颜色
    private val mFirstPartColor: Int // 频谱线条支持多种颜色
    private val mSecondPartColor: Int
    private val mThirdPartColor: Int
    private val mFourthPartColor: Int

    private var mBytes: ByteArray? = null
    private var mPoints: FloatArray? = null
    private val mRect = Rect()

    private val mPaint = Paint()
    private var mMinPoint: Int = 0
    private var mhalfPoint: Int = 0

    init {
        val t = context.obtainStyledAttributes(attributeSet, R.styleable.VisualizerView2, 0, 0)
        mColor = t.getColor(R.styleable.VisualizerView2_lineColor, Color.parseColor("#FFFFFF"))
        mSpeceNum = t.getInteger(R.styleable.VisualizerView2_spaceNum, 0)
        mSpeceWidth = t.getDimensionPixelSize(R.styleable.VisualizerView2_spaceWidth, 0)
        mLineWidth = t.getDimensionPixelSize(R.styleable.VisualizerView2_lineWidth, 5)
        mBaseHeight = t.getDimensionPixelSize(R.styleable.VisualizerView2_baseHeight, 1)
        mLineIsSingleColor = t.getBoolean(R.styleable.VisualizerView2_lineIsSingleColor, true)
        mFirstPartColor = t.getColor(R.styleable.VisualizerView2_firstPartColor, Color.parseColor("#FFFFFF"))
        mSecondPartColor = t.getColor(R.styleable.VisualizerView2_secondPartColor, Color.parseColor("#FFFFFF"))
        mThirdPartColor = t.getColor(R.styleable.VisualizerView2_thirdPartColor, Color.parseColor("#FFFFFF"))
        mFourthPartColor = t.getColor(R.styleable.VisualizerView2_fourthPartColor, Color.parseColor("#FFFFFF"))
        t.recycle()
        init()
    }

    private fun init() {
        mMinPoint = dip2px(context, 2f)
        mhalfPoint = dip2px(context, 1f)
        mBytes = null
        mPaint.strokeWidth = mLineWidth.toFloat()
        mPaint.isAntiAlias = true
        if (mLineIsSingleColor) {
            mPaint.color = mColor
        } else {
            mPaint.color = mColor
        }
    }

    fun updateVisualizer(fft: ByteArray) {
        val model = ByteArray(fft.size / 2 + 1)
        model[0] = abs(fft[0].toInt()).toByte()
        if (mSpeceNum == 0) {
            val width = width
            mSpeceNum = (width + mLineWidth) / (mSpeceWidth + mLineWidth)
        }
        var i = 2
        var j = 1
        while (j < mSpeceNum) {
            model[j] = hypot(fft[i].toDouble(), fft[i + 1].toDouble()).toByte()
            i += 2
            j++
        }
        mBytes = model
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        // 设置wrap_content的默认宽 / 高值
        val mWidth = dip2px(context, 200f)
        val mHeight = dip2px(context, 100f)

        if (widthMode == View.MeasureSpec.AT_MOST && heightMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(mWidth, mHeight)
        } else if (widthMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(mWidth, heightSize)
        } else if (heightMode == View.MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, mHeight)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBytes == null) {
            return
        }
        if (mPoints == null || mPoints!!.size < mBytes!!.size * 4) {
            mPoints = FloatArray(mBytes!!.size * 4)
        }
        mRect.set(0, 0, width, height)
        val baseX = mRect.width() / mSpeceNum
        val baseLine = mRect.height()
        for (i in 0 until mSpeceNum){
            if (mBytes!![i] < 0) {
                mBytes!![i] = 127
            }
            val xi = baseX * i + baseX
            val xi2 = baseX * i
            if (i != mSpeceNum - 1) {
                mPoints!![i * 4] = xi.toFloat()
                mPoints!![i * 4 + 2] = xi.toFloat()
            } else {
                mPoints!![i * 4] = xi2.toFloat()
                mPoints!![i * 4 + 2] = xi2.toFloat()
            }
            val offset = mBytes!![i] * 3f + mBaseHeight
            if (offset <= mMinPoint) {
                mPoints!![i * 4 + 1] = (baseLine + mhalfPoint).toFloat()
                mPoints!![i * 4 + 3] = (baseLine - mhalfPoint).toFloat()
            } else {
                mPoints!![i * 4 + 1] = baseLine + offset
                mPoints!![i * 4 + 3] = baseLine - offset
            }
            if (!mLineIsSingleColor) {
                mPaint.shader = LinearGradient(
                    mPoints!![i * 4], mPoints!![i * 4 + 1], mPoints!![i * 4 + 2], mPoints!![i * 4 + 3],
                    intArrayOf(
                        mFirstPartColor,
                        mFirstPartColor,
                        mSecondPartColor,
                        mSecondPartColor,
                        mThirdPartColor,
                        mThirdPartColor,
                        mFourthPartColor,
                        mFourthPartColor
                    ),
                    floatArrayOf(0f, 0.65f, 0.64f, 0.75f, 0.74f, 0.85f, 0.84f, 1f), Shader.TileMode.CLAMP
                )

                canvas.drawLine(
                    mPoints!![i * 4],
                    mPoints!![i * 4 + 1],
                    mPoints!![i * 4 + 2],
                    mPoints!![i * 4 + 3],
                    mPaint
                )
            }
        }
        if (mLineIsSingleColor) {
            canvas.drawLines(mPoints!!, mPaint)
        }
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


}
