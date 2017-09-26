package org.huangxk.recycle.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.huangxk.recycle.DataBase;
import org.huangxk.recycle.R;
import org.huangxk.recycle.plc.Connection;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class DoorOperationFragment extends FragmentBase implements View.OnClickListener {
    private static final String LOG_TAG = "DoorOperationFragment";
    private TextView mLevel1;
    private TextView mLevel2;
    private TextView mLevel3;
    private TextView mShutDoor;

    private ImageView mDebugImg;
    private TextView mDebugText;
    private boolean mShowDebug;
    private boolean mConnected = false;

    private static final int MSG_START_OPEN = 1234;
    private static final int MSG_START_LV1 = 1235;
    private static final int MSG_START_LV2 = 1236;
    private static final int MSG_START_LV3 = 1237;
    private static final int MSG_START_CLOSE = 1238;
    private static final int MSG_UPDATE_DEBUG = 1345;

    private MyHandler mHandler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_dooroperation_fragment, container, false);

        mLevel1 = (TextView) view.findViewById(R.id.level1);
        mLevel2 = (TextView) view.findViewById(R.id.level2);
        mLevel3 = (TextView) view.findViewById(R.id.level3);
        mShutDoor = (TextView) view.findViewById(R.id.shutdoor);

        setBtnEnable(true, false, false, false);

        mLevel1.setOnClickListener(this);
        mLevel2.setOnClickListener(this);
        mLevel3.setOnClickListener(this);
        mShutDoor.setOnClickListener(this);

        mDebugImg = (ImageView) view.findViewById(R.id.debugImg);
        mDebugText = (TextView) view.findViewById(R.id.debugText);

        mShowDebug = false;
        mDebugText.setVisibility(View.INVISIBLE);
        mDebugImg.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        mHandler = new MyHandler(getActivity().getMainLooper());
        mHandler.sendEmptyMessage(MSG_START_OPEN);
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.level1) {
            setBtnEnable(false, true, false, false);
            mHandler.sendEmptyMessage(MSG_START_LV1);
        } else if (id == R.id.level2) {
            setBtnEnable(false, false, true, false);
            mHandler.sendEmptyMessage(MSG_START_LV2);
        } else if (id == R.id.level3) {
            setBtnEnable(false, false, false, true);
            mHandler.sendEmptyMessage(MSG_START_LV3);
        } else if (id == R.id.shutdoor) {
            mHandler.sendEmptyMessage(MSG_START_CLOSE);
        } else if (id == R.id.debugImg) {
            if (mShowDebug) {
                mShowDebug = false;
                mDebugText.setVisibility(View.INVISIBLE);
                mHandler.removeMessages(MSG_UPDATE_DEBUG);
            } else {
                mShowDebug = true;
                mDebugText.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEBUG, 500);
            }
        }
    }

    private void setBtnEnable(boolean level1, boolean level2, boolean level3, boolean shutdoor) {
        mLevel1.setEnabled(true);
        mLevel2.setEnabled(true);
        mLevel3.setEnabled(true);
        mShutDoor.setEnabled(true);
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_START_OPEN) {
                DoorOperationFragment.this.mConnected = Connection.getInstance().connect("192.168.2.1");
                if (DoorOperationFragment.this.mConnected) {
                    Connection.getInstance().doOperation(Connection.OP_BACKDOOR_OPEN, null);
                }
            } else if (msg.what == MSG_START_LV1) {
                Connection.getInstance().doOperation(Connection.OP_LEVEL1, null);
            } else if (msg.what == MSG_START_LV2) {
                Connection.getInstance().doOperation(Connection.OP_LEVEL2, null);
            } else if (msg.what == MSG_START_LV3) {
                Connection.getInstance().doOperation(Connection.OP_LEVEL3, null);
            } else if (msg.what == MSG_START_CLOSE) {
                Connection.getInstance().doOperation(Connection.OP_BACKDOOR_CLOSE, null);
                Connection.getInstance().doOperation(Connection.OP_BACKDOOR_CLEAR, null);
                Connection.getInstance().close();
                DoorOperationFragment.this.mConnected = false;
                Log.d(LOG_TAG, "save current task " + DataBase.getInstance().saveCollectTask());
                statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
            } else if (msg.what == MSG_UPDATE_DEBUG) {
                if (DoorOperationFragment.this.mConnected) {
                    DoorOperationFragment.this.mDebugText.setText(Connection.getInstance().getInput());
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEBUG, 500);
                }
            }
            super.handleMessage(msg);
        }
    }
}
