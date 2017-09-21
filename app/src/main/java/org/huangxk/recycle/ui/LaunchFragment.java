package org.huangxk.recycle.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.huangxk.recycle.CountDown;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;

public class LaunchFragment extends FragmentBase implements View.OnClickListener, CountDown.CountListener {
    private static final String LOG_TAG = "LaunchFragment";
    private TextView mStartText;
    private TextView mSettingText;
    private TextView mTick;

    private CountDown mCountDown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_launch_fragment, container, false);

        mStartText = (TextView) view.findViewById(R.id.start);
        mSettingText = (TextView)view.findViewById(R.id.setting);
        mTick = (TextView)view.findViewById(R.id.tick);

        mStartText.setOnClickListener(this);
        mSettingText.setOnClickListener(this);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        int timeout = this.getActivity().getResources().getInteger(R.integer.launch_timeout_second);
        mCountDown = new CountDown(timeout, this);

        super.onResume();
    }

    @Override
    public void onPause() {
        mCountDown.cancelCount();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.start) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
        } else if (id == R.id.setting) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT2);
            //PosWriter.getInstance().testWrite();
        }
    }

    @Override
    public void onCountChanged(int count) {
        final int time = count;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTick.setText(String.format("%d", time));
            }
        });

        if (count == 0) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
        }
    }
}
