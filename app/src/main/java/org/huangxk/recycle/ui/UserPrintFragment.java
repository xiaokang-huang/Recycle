package org.huangxk.recycle.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.huangxk.recycle.CountDown;
import org.huangxk.recycle.PosWriter;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.TaskData;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class UserPrintFragment extends FragmentBase implements CountDown.CountListener, View.OnClickListener {
    private CountDown mCountDown;
    private TextView mTick;
    private ImageView mNext;
    private ImageView mStop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_userprint_fragment, container, false);

        mTick = (TextView) view.findViewById(R.id.tick);
        mNext = (ImageView) view.findViewById(R.id.next);
        mStop = (ImageView) view.findViewById(R.id.stop);

        mNext.setOnClickListener(this);
        mStop.setOnClickListener(this);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        int timeout = this.getActivity().getResources().getInteger(R.integer.userprint_timeout_second);
        checkUserUI();
        mTick.setText(String.format("%d", timeout));
        mCountDown = new CountDown(timeout, this);
        Speeker.getInstance().startSpeak(Speeker.SOUND_PRINT, 10000);
        try {
            //PosWriter.getInstance().writeUser(getActivity(), TaskData.getInstance().getUserInfo());
            PosWriter.getInstance().writeTask(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mCountDown.cancelCount();
        super.onPause();
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
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_TIMEOUT);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.next) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
        } else if (id == R.id.stop) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_TIMEOUT);
        }
    }

    private void checkUserUI() {
        TaskData.UserInfo userInfo = TaskData.getInstance().getUserInfo();
        if (userInfo.isCollector()) {
            mNext.setVisibility(View.GONE);
        }
    }
}
