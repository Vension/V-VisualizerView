package com.vension.visualizerview.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.vension.visualizerview_java.VisualizerView;
import com.vension.visualizerview_java.VisualizerView2;

/**
 * ========================================================
 * 作 者：Vension
 * 日 期：2019/7/5 16:03
 * 更 新：2019/7/5 16:03
 * 描 述：
 * ========================================================
 */

public class JavaActivity extends AppCompatActivity {

  private MediaPlayer mMediaPlayer;//音频
  private Visualizer mVisualizer;//频谱器
  private Equalizer mEqualizer; //均衡器

  private LinearLayout layoutEqualize;//均衡器布局
  private VisualizerView mBaseVisualizerView;
  private VisualizerView2 mVisualizerView;
  private RadioGroup mRadioGroup;
  private ImageView ivPlay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    setContentView(R.layout.activity_java);

    initView();
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
    } else {
      init();
    }
  }

  private void initView() {
    mBaseVisualizerView = findViewById(R.id.visualizerView1);
    mVisualizerView = findViewById(R.id.visualizerView2);
    layoutEqualize = findViewById(R.id.layout_Equalize);
    mRadioGroup = findViewById(R.id.mRadioGroup);
    ivPlay = findViewById(R.id.iv_play);
    mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
         switch (checkedId){
           case R.id.rbt_1://无倒影
             if (mBaseVisualizerView.getVisibility() == View.GONE){
               mBaseVisualizerView.setVisibility(View.VISIBLE);
             }
             if (mVisualizerView.getVisibility() == View.VISIBLE){
               mVisualizerView.setVisibility(View.GONE);
             }
             mBaseVisualizerView.setHasShadow(false);
             //设置允许波形表示，并且捕获它
             mBaseVisualizerView.setVisualizer(mVisualizer);//先设置可视化工具
             mVisualizer.setEnabled(true);//开启可视化工具
             break;
           case R.id.rbt_2://有倒影
             if (mBaseVisualizerView.getVisibility() == View.GONE){
               mBaseVisualizerView.setVisibility(View.VISIBLE);
             }
             if (mVisualizerView.getVisibility() == View.VISIBLE){
               mVisualizerView.setVisibility(View.GONE);
             }
             mBaseVisualizerView.setHasShadow(true);
             //设置允许波形表示，并且捕获它
             mBaseVisualizerView.setVisualizer(mVisualizer);//先设置可视化工具
             mVisualizer.setEnabled(true);//开启可视化工具
             break;
           case R.id.rbt_3://颜色渐变
             if (mBaseVisualizerView.getVisibility() == View.GONE){
               mBaseVisualizerView.setVisibility(View.VISIBLE);
             }
             if (mVisualizerView.getVisibility() == View.VISIBLE){
               mVisualizerView.setVisibility(View.GONE);
             }
             mBaseVisualizerView.setGradient(true);
             mBaseVisualizerView.setHasShadow(true);
             //设置允许波形表示，并且捕获它
             mBaseVisualizerView.setVisualizer(mVisualizer);//先设置可视化工具
             mVisualizer.setEnabled(true);//开启可视化工具
             break;
           case R.id.rbt_4://颜色分块
             if (mVisualizerView.getVisibility() == View.GONE){
               mVisualizerView.setVisibility(View.VISIBLE);
             }
             if (mBaseVisualizerView.getVisibility() == View.VISIBLE){
               mBaseVisualizerView.setVisibility(View.GONE);
             }
             if(mVisualizer  != null){
               mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                 @Override
                 public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

                 }

                 @Override
                 public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                   mVisualizerView.updateVisualizer(fft);
                 }
               }, Visualizer.getMaxCaptureRate() / 2, true, true);
             }
             break;
         }
      }
    });
    ivPlay.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(mMediaPlayer != null){
          if (mMediaPlayer.isPlaying()) {
            ivPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);

            mMediaPlayer.pause();
          } else {
            ivPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            mMediaPlayer.start();
          }
        }
      }
    });
  }

  private void init() {
    initMediaPlayer();
    setVisualizerOnUi();
    setupEqualizeFxAndUi();
    if(mMediaPlayer != null){
      mMediaPlayer.start();
    }
  }

  private void initMediaPlayer() {
    mMediaPlayer = MediaPlayer.create(this, R.raw.videodemo);
    mMediaPlayer.setLooping(true);
    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

      @Override
      public void onCompletion(MediaPlayer mp) {
//				mVisualizer.setEnabled(false);
      }
    });
  }

  /**
   * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
   */
  private void setVisualizerOnUi() {
    if(mMediaPlayer != null){
      //实例化Visualizer，参数SessionId可以通过MediaPlayer的对象获得
      mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
      //采样 - 参数内必须是2的位数 - 如64,128,256,512,1024
      mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
      //设置允许波形表示，并且捕获它
      mBaseVisualizerView.setVisualizer(mVisualizer);//先设置可视化工具
      mVisualizer.setEnabled(true);//开启可视化工具
    }
  }


  /**
   * 通过mMediaPlayer返回的AudioSessionId创建一个优先级为0均衡器对象 并且通过频谱生成相应的UI和对应的事件
   */
  private void setupEqualizeFxAndUi() {
    if(mMediaPlayer != null){
      mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
      mEqualizer.setEnabled(true);// 启用均衡器
      // 通过均衡器得到其支持的频谱引擎
      short bands = mEqualizer.getNumberOfBands();

      // getBandLevelRange 是一个数组，返回一组频谱等级数组，
      // 第一个下标为最低的限度范围
      // 第二个下标为最大的上限,依次取出
      final short minEqualizer = mEqualizer.getBandLevelRange()[0];
      final short maxEqualizer = mEqualizer.getBandLevelRange()[1];

      for (short i = 0; i < bands; i++) {
        final short band = i;

        TextView freqTextView = new TextView(this);
        freqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        // 取出中心频率
        freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + "HZ");
        layoutEqualize.addView(freqTextView);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView minDbTextView = new TextView(this);
        minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        minDbTextView.setText((minEqualizer / 100) + " dB");

        TextView maxDbTextView = new TextView(this);
        maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        maxDbTextView.setText((maxEqualizer / 100) + " dB");

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.weight = 1;

        SeekBar seekbar = new SeekBar(this);
        seekbar.setLayoutParams(layoutParams);
        seekbar.setMax(maxEqualizer - minEqualizer);
        seekbar.setProgress(mEqualizer.getBandLevel(band));

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
          }

          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // TODO Auto-generated method stub
            mEqualizer.setBandLevel(band, (short) (progress + minEqualizer));
          }
        });
        row.addView(minDbTextView);
        row.addView(seekbar);
        row.addView(maxDbTextView);
        layoutEqualize.addView(row);
      }
    }
  }



  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mMediaPlayer != null) {
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
    if (mEqualizer != null) {
      mEqualizer.release();
      mEqualizer = null;
    }
    if (mVisualizer != null) {
      mVisualizer.release();
      mVisualizer = null;
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == 1) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        init();
      } else {
        // Permission Denied
        Toast.makeText(JavaActivity.this, "请打开录音权限", Toast.LENGTH_SHORT).show();
      }
      return;
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

}
