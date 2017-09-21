package org.huangxk.recycle.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.huangxk.recycle.CountDown;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class IdFailFragment extends FragmentBase implements CountDown.CountListener {
    private TextView mText;
    private CountDown mCountDown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_idfail_fragment, container, false);

        mText = (TextView) view.findViewById(R.id.idfail_txt);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        int timeout = this.getActivity().getResources().getInteger(R.integer.idfail_timeout_second);
        mText.setText(String.format(getResources().getString(R.string.idfail_msg), timeout));
        mCountDown = new CountDown(timeout, this);
        Speeker.getInstance().startSpeak(Speeker.SOUND_IDFAIL, 1000);
        super.onResume();
    }

    @Override
    public void onPause() {
        mCountDown.cancelCount();
        Speeker.getInstance().stopSpeak();
        super.onPause();
    }

    @Override
    public void onCountChanged(int count) {
        final int time = count;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText.setText(String.format(getResources().getString(R.string.idfail_msg), time));
            }
        });

        if (count == 0) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_TIMEOUT);
        }
    }
}