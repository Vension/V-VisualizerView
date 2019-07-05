package com.vension.visualizerview_java;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View implements Visualizer.OnDataCaptureListener{

    private static final int DN_W = 480;//view宽度与单个音频块占比 - 正常480 需微调
    private static final int DN_H = 360;//view高度与单个音频块占比
    private static final int DN_SL = 15;//单个音频块宽度
    private static final int DN_SW = 5;//单个音频块高度
    protected final static int MAX_LEVEL = 30;//音量柱·音频块 - 最大个数
    protected final static int CYLINDER_NUM = 26;//音量柱 - 最大个数

    private boolean hasShadow = false;//是否有倒影
    private int shadowNum = 5;//倒影个数
    private int shadowColor = Color.GRAY;//倒影颜色
    private int visualColor = Color.RED;//频块颜色
    private boolean isGradient = false;//音频块颜色是否渐变
    private int colorStart = Color.parseColor("#A47586");
    private int colorCenter = Color.parseColor("#C36084");
    private int colorEnd = Color.parseColor("#F14380");

    private int hgap = 0;
    private int vgap = 0;
    private int levelStep = 0;
    private float strokeWidth = 0;
    private float strokeLength = 0;


    /**
     * It is the visualizer.
     */
    protected Visualizer mVisualizer = null;//频谱器

    /**
     * It is the paint which is used to draw to visual effect.
     */
    protected Paint mPaint = null;//画笔

    /**
     * It is the buffer of fft.
     */
    protected byte[] mData = new byte[CYLINDER_NUM];//音量柱 数组

    boolean mDataEn = true;

    /**
     * It constructs the base visualizer view.
     * 构造函数初始化画笔
     * @param context It is the context of the view owner.
     */
    public VisualizerView(Context context) {
        this(context, null);

    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VisualizerView, 0, 0);
        hasShadow = ta.getBoolean(R.styleable.VisualizerView_kv_hasShadow,false);
        shadowNum = ta.getInteger(R.styleable.VisualizerView_kv_shadowNum,5);
        shadowColor = ta.getColor(R.styleable.VisualizerView_kv_shadowColor, Color.GRAY);
        visualColor = ta.getColor(R.styleable.VisualizerView_kv_visualColor, Color.RED);
        isGradient = ta.getBoolean(R.styleable.VisualizerView_kv_isGradient,false);
        colorStart = ta.getColor(R.styleable.VisualizerView_kv_colorStart, Color.parseColor("#A47586"));
        colorCenter = ta.getColor(R.styleable.VisualizerView_kv_colorCenter, Color.parseColor("#C36084"));
        colorEnd = ta.getColor(R.styleable.VisualizerView_kv_colorEnd, Color.parseColor("#F14380"));
        ta.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint();//初始化画笔工具
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setColor(0xFFd60d25);//画笔颜色
        mPaint.setStrokeJoin(Join.ROUND);//频块圆角
        mPaint.setStrokeCap(Cap.ROUND);//频块圆角
    }

    //执行 Layout 操作
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        float w, h, xr, yr;
        w = right - left;
        h = bottom - top;
        xr = w / (float)DN_W;
        yr = h / (float)DN_H;

        strokeWidth = DN_SW * yr;
        strokeLength = DN_SL * xr;
        hgap = (int)((w - strokeLength * CYLINDER_NUM) / (CYLINDER_NUM + 1) );
        vgap = (int)(h / (MAX_LEVEL + 2));//频谱块高度

        mPaint.setStrokeWidth(strokeWidth); //设置频谱块宽度
    }


    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < CYLINDER_NUM; i ++) {
            drawCylinder(canvas, strokeWidth / 2 + hgap + i * (hgap + strokeLength), mData[i]);
        }

//		int j=-4;
//		for (int i = 0; i < CYLINDER_NUM/2-4; i++) { //绘制25个能量柱
//
//			drawCylinder(canvas, strokeWidth / 2 + hgap + i * (hgap + strokeLength), mData[i]);
//		}
//		for(int i =CYLINDER_NUM; i>=CYLINDER_NUM/2-4; i--){
//			j++;
//			drawCylinder(canvas, strokeWidth / 2 + hgap + (CYLINDER_NUM/2+j-1 )* (hgap + strokeLength), mData[i-1]);
//		}
    }


	/**
	 *  绘制频谱块和倒影
	 */
	protected void drawCylinder(Canvas canvas, float x, byte value) {
		float y;
		if (value <= 0) value = 1;
		for (int i = 0; i < value; i++) {//每个能量柱绘制value个能量块
			if(hasShadow){//是否有倒影
				y = (getHeight()/2 - i * vgap - vgap);//计算y轴坐标
				float y1=(getHeight()/2+i * vgap + vgap);
				//绘制音量柱倒影
				if (i <= shadowNum && value > 0) {
					mPaint.setColor(shadowColor);//画笔颜色
					mPaint.setAlpha(100 - (100 / shadowNum * i));//倒影颜色
					canvas.drawLine(x, y1, (x + strokeLength), y1, mPaint);//绘制频谱块
				}
			}else{
				y = getHeight() - i * vgap - vgap;
			}
			//绘制频谱块
			mPaint.setColor(visualColor);//画笔颜色
            if(isGradient){
                LinearGradient backGradient = new LinearGradient(x, y, (x + strokeLength), y, new int[]{colorStart, colorCenter ,colorEnd}, null, Shader.TileMode.CLAMP);
                mPaint.setShader(backGradient);
            }
			canvas.drawLine(x, y, (x + strokeLength), y, mPaint);//绘制频谱块
		}
	}

    /**
     * It sets the visualizer of the view. DO set the viaulizer to null when exit the program.
     * @parma visualizer It is the visualizer to set.
     */
    public void setVisualizer(Visualizer visualizer) {
        if (visualizer != null) {
            if (!visualizer.getEnabled()) {
                visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
            }
            levelStep = 240 / MAX_LEVEL;
            visualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, false, true);

        } else {

            if (mVisualizer != null) {
                mVisualizer.setEnabled(false);
                mVisualizer.release();
            }
        }
        mVisualizer = visualizer;
    }


    //这个回调应该采集的是快速傅里叶变换有关的数据
    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        byte[] model = new byte[fft.length / 2 + 1];
        if (mDataEn) {
            model[0] = (byte) Math.abs(fft[1]);
            int j = 1;
            for (int i = 2; i < fft.length; ) {
                model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
                i += 2;
                j++;
            }
        } else {
            for (int i = 0; i < CYLINDER_NUM; i++) {
                model[i] = 0;
            }
        }
        for (int i = 0; i < CYLINDER_NUM; i++) {
            final byte a = (byte) (Math.abs(model[CYLINDER_NUM - i]) / levelStep);

            final byte b = mData[i];
            if (a > b) {
                mData[i] = a;
            } else {
                if (b > 0) {
                    mData[i]--;
                }
            }
        }
        postInvalidate();//刷新界面
    }

    //这个回调应该采集的是波形数据
    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        // Do nothing...
    }

    /**
     * It enables or disables the data processs.
     * @param en If this value is true it enables the data process..
     */
    public void enableDataProcess(boolean en) {
        mDataEn = en;
    }

    public boolean isHasShadow() {
        return hasShadow;
    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }

    public int getShadowNum() {
        return shadowNum;
    }

    public void setShadowNum(int shadowNum) {
        this.shadowNum = shadowNum;
    }

    public int getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    public int getVisualColor() {
        return visualColor;
    }

    public void setVisualColor(int visualColor) {
        this.visualColor = visualColor;
    }

    public boolean isGradient() {
        return isGradient;
    }

    public void setGradient(boolean gradient) {
        isGradient = gradient;
    }

    public int getColorStart() {
        return colorStart;
    }

    public void setColorStart(int colorStart) {
        this.colorStart = colorStart;
    }

    public int getColorCenter() {
        return colorCenter;
    }

    public void setColorCenter(int colorCenter) {
        this.colorCenter = colorCenter;
    }

    public int getColorEnd() {
        return colorEnd;
    }

    public void setColorEnd(int colorEnd) {
        this.colorEnd = colorEnd;
    }

}
