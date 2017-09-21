package org.huangxk.recycle;

import java.util.Timer;
import java.util.TimerTask;

public class CountDown extends TimerTask {
    private int mTotal;
    private Timer mTimer = new Timer();
    private CountListener mListener;

    public interface CountListener {
        void onCountChanged(int count);
    }

    public CountDown(int second, CountListener listener) {
        mTotal = second;
        mListener = listener;
        mTimer.schedule(this, 0, 1000);
    }

    public void cancelCount() {
        mTimer.cancel();
    }

    @Override
    public void run() {
        if (mTotal < 0) {
            mTimer.cancel();
        } else if (mListener != null) {
            mListener.onCountChanged(mTotal);
        }
        -- mTotal;
    }
}
