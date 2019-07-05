package com.vension.visualizerview.demo

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_kotlin.*

/**
 * ========================================================
 * 作 者：Vension
 * 日 期：2019/7/5 16:03
 * 更 新：2019/7/5 16:03
 * 描 述：
 * ========================================================
 */

class KotlinActivity : AppCompatActivity() {

    private var mMediaPlayer: MediaPlayer? = null//音频
    private var mVisualizer: Visualizer? = null//频谱器
    private var mEqualizer: Equalizer? = null //均衡器


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContentView(R.layout.activity_kotlin)

        initView()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            init()
        }
    }

    private fun initView() {
        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbt_1 -> {//无倒影
                    if (mVisualizerView.visibility == View.GONE) {
                        mVisualizerView.visibility = View.VISIBLE
                    }
                    if (mVisualizerView2.visibility == View.VISIBLE) {
                        mVisualizerView2.visibility = View.GONE
                    }
                    mVisualizerView.setHasShadow(false)
                    //设置允许波形表示，并且捕获它
                    mVisualizerView.setVisualizer(mVisualizer)//先设置可视化工具
                    mVisualizer?.enabled = true//开启可视化工具
                }
                R.id.rbt_2 -> {//有倒影
                    if (mVisualizerView.visibility == View.GONE) {
                        mVisualizerView.visibility = View.VISIBLE
                    }
                    if (mVisualizerView2.visibility == View.VISIBLE) {
                        mVisualizerView2.visibility = View.GONE
                    }
                    mVisualizerView.setHasShadow(true)
                    //设置允许波形表示，并且捕获它
                    mVisualizerView.setVisualizer(mVisualizer)//先设置可视化工具
                    mVisualizer?.enabled = true//开启可视化工具
                }
                R.id.rbt_3 -> {//颜色渐变
                    if (mVisualizerView.visibility == View.GONE) {
                        mVisualizerView.visibility = View.VISIBLE
                    }
                    if (mVisualizerView2.visibility == View.VISIBLE) {
                        mVisualizerView2.visibility = View.GONE
                    }
                    mVisualizerView.setGradient(true)
                    mVisualizerView.setHasShadow(true)
                    //设置允许波形表示，并且捕获它
                    mVisualizerView.setVisualizer(mVisualizer)//先设置可视化工具
                    mVisualizer?.enabled = true//开启可视化工具
                }
                R.id.rbt_4//颜色分块
                -> {
                    if (mVisualizerView2!!.visibility == View.GONE) {
                        mVisualizerView2!!.visibility = View.VISIBLE
                    }
                    if (mVisualizerView!!.visibility == View.VISIBLE) {
                        mVisualizerView!!.visibility = View.GONE
                    }
                    mVisualizer?.let {
                        it.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(visualizer: Visualizer, waveform: ByteArray, samplingRate: Int) {

                            }

                            override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {
                                mVisualizerView2.updateVisualizer(fft)
                            }
                        }, Visualizer.getMaxCaptureRate() / 2, true, true)
                    }
                }
            }
        }
        iv_play.setOnClickListener {
            mMediaPlayer?.let {
                if (mMediaPlayer!!.isPlaying) {
                    iv_play.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                    it.pause()
                } else {
                    iv_play.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp)
                    it.start()
                }
            }
        }
    }

    private fun init() {
        initMediaPlayer()
        setVisualizerOnUi()
        setupEqualizeFxAndUi()
        mMediaPlayer!!.start()
    }

    private fun initMediaPlayer() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.videodemo)
        mMediaPlayer?.isLooping = true
        mMediaPlayer?.setOnCompletionListener {
            //				mVisualizer.setEnabled(false);
        }
    }

    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private fun setVisualizerOnUi() {
        if (mMediaPlayer != null) {
            //实例化Visualizer，参数SessionId可以通过MediaPlayer的对象获得
            mVisualizer = Visualizer(mMediaPlayer!!.audioSessionId)
            //采样 - 参数内必须是2的位数 - 如64,128,256,512,1024
            mVisualizer?.captureSize = Visualizer.getCaptureSizeRange()[1]
            //设置允许波形表示，并且捕获它
            mVisualizerView.setVisualizer(mVisualizer)//先设置可视化工具
            mVisualizer?.enabled = true//开启可视化工具
        }
    }


    /**
     * 通过mMediaPlayer返回的AudioSessionId创建一个优先级为0均衡器对象 并且通过频谱生成相应的UI和对应的事件
     */
    private fun setupEqualizeFxAndUi() {
        if (mMediaPlayer != null) {
            mEqualizer = Equalizer(0, mMediaPlayer!!.audioSessionId)
            mEqualizer?.enabled = true// 启用均衡器
            // 通过均衡器得到其支持的频谱引擎
            val bands = mEqualizer?.numberOfBands

            // getBandLevelRange 是一个数组，返回一组频谱等级数组，
            // 第一个下标为最低的限度范围
            // 第二个下标为最大的上限,依次取出
            val minEqualizer = mEqualizer!!.bandLevelRange[0]
            val maxEqualizer = mEqualizer!!.bandLevelRange[1]
            for (i in 0 until bands!!.toShort()) {

                val freqTextView = TextView(this)
                freqTextView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                freqTextView.gravity = Gravity.CENTER_HORIZONTAL
                // 取出中心频率
                freqTextView.text = (mEqualizer!!.getCenterFreq(i.toShort()) / 1000).toString() + "HZ"
                layout_Equalize.addView(freqTextView)

                val row = LinearLayout(this)
                row.orientation = LinearLayout.HORIZONTAL

                val minDbTextView = TextView(this)
                minDbTextView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                minDbTextView.text = (minEqualizer / 100).toString() + " dB"

                val maxDbTextView = TextView(this)
                maxDbTextView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                maxDbTextView.text = (maxEqualizer / 100).toString() + " dB"

                val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.weight = 1f

                val seekbar = SeekBar(this)
                seekbar.layoutParams = layoutParams
                seekbar.max = maxEqualizer - minEqualizer
                seekbar.progress = mEqualizer!!.getBandLevel(i.toShort()).toInt()

                seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        // TODO Auto-generated method stub
                        mEqualizer?.setBandLevel(i.toShort(), (progress + minEqualizer).toShort())
                    }
                })
                row.addView(minDbTextView)
                row.addView(seekbar)
                row.addView(maxDbTextView)
                layout_Equalize.addView(row)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer!!.release()
        mEqualizer!!.release()
        mVisualizer!!.release()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                // Permission Denied
                Toast.makeText(this@KotlinActivity, "请打开录音权限", Toast.LENGTH_SHORT).show()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
