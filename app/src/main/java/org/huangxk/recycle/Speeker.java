package org.huangxk.recycle;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import java.io.IOException;

public class Speeker implements InitListener, SynthesizerListener, MediaPlayer.OnCompletionListener {
    public static final String SOUND_IDCARD = "请在刷卡区域刷卡";
    public static final String SOUND_IDFAIL = "身份验证失败，请重新刷卡";
    public static final String SOUND_USERINFO = "请点击右下角箭头继续操作";
    public static final String SOUND_ANIMALSEL = "请选择要存放的畜禽种类";
    public static final String SOUND_WAITUSER = "请将畜禽放在输送带上，然后点击屏幕中按钮开始存放";
    public static final String SOUND_WAITSYS = "存储中，请稍后";
    public static final String SOUND_TASKINFO = "请确认屏幕上信息";
    public static final String SOUND_PRINT = "本次存储结束";

    private static final String APPID = "59ba2f8b";
    private static final int MSG_WHAT_START = 143;
    private SpeechSynthesizer mTts;
    private String mData;
    private int mDelayMs = 1000;

    private MediaPlayer mPlayer = new MediaPlayer();

    private void setParam(){
        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        mTts.setParameter(SpeechConstant.SPEED, "50");
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "50");

        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
    }

    public void initialize(Context context) {
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=" + APPID);
        mTts = SpeechSynthesizer.createSynthesizer(context, this);
        setParam();
    }

    public void startSpeak(String data, int delayMs) {
        mData = data;
        mDelayMs = delayMs;

        String nativeFile = null;
        if (data.equals(SOUND_IDCARD)) {
            nativeFile = "/sdcard/1.m4a";
        } else if (data.equals(SOUND_USERINFO)) {
            nativeFile = "/sdcard/2.m4a";
        } else if (data.equals(SOUND_PRINT)) {
            nativeFile = "/sdcard/3.m4a";
        }
        if (nativeFile != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            try {
                mPlayer.reset();
                mPlayer.setDataSource(nativeFile);
                mPlayer.setOnCompletionListener(this);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (mTts.isSpeaking()) {
            stopSpeak();
        }
        mTts.startSpeaking(data, this);
    }

    public void stopSpeak() {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        mTts.stopSpeaking();
    }

    public static Speeker getInstance() {
        if(sInstance == null) {
            sInstance = new Speeker();
        }
        return sInstance;
    }
    private static Speeker sInstance = null;

    @Override
    public void onInit(int i) {

    }

    @Override
    public void onSpeakBegin() {

    }

    @Override
    public void onBufferProgress(int i, int i1, int i2, String s) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i1, int i2) {

    }

    @Override
    public void onCompleted(SpeechError speechError) {
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_START, mDelayMs);
    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_START) {
                startSpeak(mData, mDelayMs);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_START, mDelayMs);
    }
}
