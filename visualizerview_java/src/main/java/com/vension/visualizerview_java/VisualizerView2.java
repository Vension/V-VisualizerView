package com.vension.visualizerview_java;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;


/**
 * 自定义View——随音乐频谱跳动的线条
 */
public class VisualizerView2 extends View {
  private int mColor;// 主色调
  private int mLineWidth;// 频谱线条宽度
  private int mSpeceNum;// 空隙个数(不设置自己计算)
  private int mSpeceWidth;// 空隙宽度
  private int mBaseHeight;// 基础高度
  private boolean mLineIsSingleColor; // 线条只有一种颜色
  private int mFirstPartColor; // 频谱线条支持多种颜色
  private int mSecondPartColor;
  private int mThirdPartColor;
  private int mFourthPartColor;

  private byte[] mBytes;
  private float[] mPoints;
  private Rect mRect = new Rect();

  private Paint mPaint = new Paint();
  private int mMinPoint;
  private int mhalfPoint;

  public VisualizerView2(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    TypedArray t = context.obtainStyledAttributes(attributeSet, R.styleable.VisualizerView2, 0, 0);
    mColor = t.getColor(R.styleable.VisualizerView2_lineColor, Color.parseColor("#FFFFFF"));
    mSpeceNum = t.getInteger(R.styleable.VisualizerView2_spaceNum, 0);
    mSpeceWidth = t.getDimensionPixelSize(R.styleable.VisualizerView2_spaceWidth, 0);
    mLineWidth = t.getDimensionPixelSize(R.styleable.VisualizerView2_lineWidth, 5);
    mBaseHeight = t.getDimensionPixelSize(R.styleable.VisualizerView2_baseHeight, 1);
    mLineIsSingleColor = t.getBoolean(R.styleable.VisualizerView2_lineIsSingleColor, true);
    mFirstPartColor = t.getColor(R.styleable.VisualizerView2_firstPartColor, Color.parseColor("#FFFFFF"));
    mSecondPartColor = t.getColor(R.styleable.VisualizerView2_secondPartColor, Color.parseColor("#FFFFFF"));
    mThirdPartColor = t.getColor(R.styleable.VisualizerView2_thirdPartColor, Color.parseColor("#FFFFFF"));
    mFourthPartColor = t.getColor(R.styleable.VisualizerView2_fourthPartColor, Color.parseColor("#FFFFFF"));
    t.recycle();
    init();
  }

  private void init() {
    mMinPoint = dip2px(getContext(), 2);
    mhalfPoint = dip2px(getContext(), 1);
    mBytes = null;
    mPaint.setStrokeWidth(mLineWidth);
    mPaint.setAntiAlias(true);
    if (mLineIsSingleColor) {
      mPaint.setColor(mColor);
    } else {
      mPaint.setColor(mColor);
    }
  }

  public void updateVisualizer(byte[] fft) {
    byte[] model = new byte[fft.length / 2 + 1];
    model[0] = (byte) Math.abs(fft[0]);
    if (mSpeceNum == 0) {
      int width = getWidth();
      mSpeceNum = (width + mLineWidth) / (mSpeceWidth + mLineWidth);
    }
    for (int i = 2, j = 1; j < mSpeceNum; ) {
      model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
      i += 2;
      j++;
    }
    mBytes = model;
    invalidate();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);

    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    // 设置wrap_content的默认宽 / 高值
    int mWidth = dip2px(getContext(), 200);
    int mHeight = dip2px(getContext(), 100);

    if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
      setMeasuredDimension(mWidth, mHeight);
    } else if (widthMode == MeasureSpec.AT_MOST) {
      setMeasuredDimension(mWidth, heightSize);
    } else if (heightMode == MeasureSpec.AT_MOST) {
      setMeasuredDimension(widthSize, mHeight);
    }
  }

  @SuppressLint("DrawAllocation")
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (mBytes == null) {
      return;
    }
    if (mPoints == null || mPoints.length < mBytes.length * 4) {
      mPoints = new float[mBytes.length * 4];
    }
    mRect.set(0, 0, getWidth(), getHeight());
    final int baseX = mRect.width() / mSpeceNum;
    final int baseLine = mRect.height();

    for (int i = 0; i < mSpeceNum; i++) {
      if (mBytes[i] < 0) {
        mBytes[i] = 127;
      }
      final int xi = baseX * i + baseX;
      final int xi2 = baseX * i;
      if (i != mSpeceNum - 1) {
        mPoints[i * 4] = xi;
        mPoints[i * 4 + 2] = xi;
      } else {
        mPoints[i * 4] = xi2;
        mPoints[i * 4 + 2] = xi2;
      }
      float offset = mBytes[i] * 3f + mBaseHeight;
      if (offset <= mMinPoint) {
        mPoints[i * 4 + 1] = baseLine + mhalfPoint;
        mPoints[i * 4 + 3] = baseLine - mhalfPoint;
      } else {
        mPoints[i * 4 + 1] = baseLine + offset;
        mPoints[i * 4 + 3] = baseLine - offset;
      }
      if (!mLineIsSingleColor) {
        mPaint.setShader(new LinearGradient(mPoints[i * 4], mPoints[i * 4 + 1], mPoints[i * 4 + 2], mPoints[i * 4 + 3],
            new int[]{mFirstPartColor, mFirstPartColor,
                mSecondPartColor, mSecondPartColor,
                mThirdPartColor, mThirdPartColor,
                mFourthPartColor, mFourthPartColor},
            new float[]{0f, 0.65f, 0.64f, 0.75f, 0.74f, 0.85f, 0.84f, 1f}, Shader.TileMode.CLAMP));

        canvas.drawLine(mPoints[i * 4], mPoints[i * 4 + 1], mPoints[i * 4 + 2], mPoints[i * 4 + 3], mPaint);
      }
    }
    if (mLineIsSingleColor) {
      canvas.drawLines(mPoints, mPaint);
    }
  }


  /**
   * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
   */
  private int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }


}
