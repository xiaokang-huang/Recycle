package org.huangxk.recycle.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.huangxk.recycle.CountDown;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.TaskData;
import org.huangxk.recycle.plc.Connection;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class WaitSysStoreFragment extends FragmentBase implements View.OnClickListener {
    private boolean mConnected = false;
    private PLCHandler mHandler;
    private static final int MSG_START = 1395;
    private static final int MSG_UPDATE_DEBUG = 1396;

    private ImageView mDebugImg;
    private TextView mDebugText;
    private boolean mShowDebug = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_sysstore_fragment, container, false);

        mDebugImg = (ImageView) view.findViewById(R.id.debugImg);
        mDebugText = (TextView) view.findViewById(R.id.debugText);

        mShowDebug = false;
        mDebugText.setVisibility(View.INVISIBLE);
        mDebugImg.setOnClickListener(this);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        mConnected = Connection.getInstance().connect(getString(R.string.plc_ip));
        mHandler = new PLCHandler(getActivity().getMainLooper());
        if (mConnected == false) {
            showConnectionAlert();
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_FAILED);
        } else {
            mHandler.sendEmptyMessage(MSG_START);
        }
        Speeker.getInstance().startSpeak(Speeker.SOUND_WAITSYS, 1000);
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mConnected) {
            Connection.getInstance().close();
        }
        Speeker.getInstance().stopSpeak();
        super.onPause();
    }

    private void showConnectionAlert() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this.getActivity());
        ab.setTitle("Error When Connecting to PLC");
        ab.create().show();
    }

    @Override
    public void onClick(View view) {
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

    private class PLCHandler extends Handler {
        public PLCHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_START && Connection.getInstance().connect("192.168.2.1") == true) {
                Connection.getInstance().doOperation(Connection.OP_FRONTDOOR_OPEN, this);
                Toast.makeText(getActivity(), "开门中", Toast.LENGTH_LONG);
            } else if (msg.what == Connection.MSG_OPERATION_FINISHED) {
                int opcode = msg.arg1;
                if (opcode == Connection.OP_FRONTDOOR_OPEN) {
                    Connection.getInstance().doOperation(Connection.OP_STARTTRANS, this);
                    Toast.makeText(getActivity(), "开始传送", Toast.LENGTH_LONG);
                } else if (opcode == Connection.OP_STARTTRANS) {
                    TaskData.AnimalInfo animalInfo = TaskData.getInstance().getAnimalInfo();
                    Connection.AnimalData animal = Connection.getInstance().getAnimalData();
                    animalInfo.mWeightGRAM = (int)(animal.mWeight * 1000);
                    animalInfo.mLengthMM = (int)(animal.mLength * 1000);
                    Connection.getInstance().doOperation(Connection.OP_FRONTDOOR_CLOSE, this);
                    Toast.makeText(getActivity(), "关门中", Toast.LENGTH_LONG);
                } else if (opcode == Connection.OP_FRONTDOOR_CLOSE) {
                    Connection.getInstance().close();
                    statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
                }
            } else if (msg.what == MSG_UPDATE_DEBUG) {
                if (WaitSysStoreFragment.this.mConnected) {
                    WaitSysStoreFragment.this.mDebugText.setText(Connection.getInstance().getInput());
                }
            }
            super.handleMessage(msg);
        }
    }
}
