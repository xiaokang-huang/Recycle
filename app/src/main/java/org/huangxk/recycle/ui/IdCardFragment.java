package org.huangxk.recycle.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiagu.utils.netUtil;

import org.huangxk.recycle.CardOperator;
import org.huangxk.recycle.DataBase;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.TaskData;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class IdCardFragment extends FragmentBase implements View.OnClickListener, View.OnLongClickListener {
    private static final int MSG_CHECK_NETWORK = 0x100;
    private static final int MSG_CHECK_STORETIME = 0x101;
    private TextView mSwipe;
    private ImageView mNetStatus;
    private TextView mStoreTime;
    private TextView mVersion;

    public IdCardFragment() {
        mCanReadNfc = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_idcard_fragment, container, false);

        mSwipe = (TextView) view.findViewById(R.id.swipe);
        mSwipe.setOnClickListener(this);
        mSwipe.setOnLongClickListener(this);

        mNetStatus = (ImageView) view.findViewById(R.id.networkstate);
        mStoreTime = (TextView) view.findViewById(R.id.storetime);
        mVersion = (TextView) view.findViewById(R.id.version);



        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        TaskData.getInstance().clearAll();
        Speeker.getInstance().startSpeak(Speeker.SOUND_IDCARD, 10000);
        updateNetwork();
        checkStoreTime();
        updateVersion();
        super.onResume();
    }

    @Override
    public void onPause() {
        mHandler.removeMessages(MSG_CHECK_NETWORK);
        mHandler.removeMessages(MSG_CHECK_STORETIME);
        Speeker.getInstance().stopSpeak();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        TaskData.getInstance().getUserInfo().mCardNum = "123456";
        TaskData.getInstance().getUserInfo().mUserType = TaskData.USERINFO_TYPE_REGISTED_FARMER;
        TaskData.getInstance().getUserInfo().mUserName = "关云长";
        TaskData.getInstance().getUserInfo().mRegisterDate = "2017-01-01";
        TaskData.getInstance().getUserInfo().mInfo = "这就是个测试用户";
        statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
    }

    @Override
    public boolean onLongClick(View view) {
        TaskData.getInstance().getUserInfo().mCardNum = "234567";
        TaskData.getInstance().getUserInfo().mUserType = TaskData.USERINFO_TYPE_COLLECTOR;
        TaskData.getInstance().getUserInfo().mUserName = "刘玄德";
        TaskData.getInstance().getUserInfo().mRegisterDate = "2017-01-01";
        TaskData.getInstance().getUserInfo().mInfo = "这就是个测试用户";
        statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
        return true;
    }

    @Override
    public void handleNewIntent(Intent intent) {
        if (CardOperator.getInstance().readCardData(intent) == true) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
        } else {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_FAILED);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CHECK_NETWORK) {
                updateNetwork();
            } else if (msg.what == MSG_CHECK_STORETIME) {
                checkStoreTime();
            }
            super.handleMessage(msg);
        }
    };

    private void updateNetwork() {
        boolean connect = netUtil.isNetworkConnected(getActivity());
        int icon = (connect)? R.drawable.network : R.drawable.network_disconnect;
        mNetStatus.setImageResource(icon);
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_NETWORK, getResources().getInteger(R.integer.check_network_gap));
    }

    private void checkStoreTime() {
        long firstStoreTime = DataBase.getInstance().getFirstStoreTime();
        if (firstStoreTime == DataBase.INVALID_STORE_TIME) {
            mStoreTime.setText(String.format(getResources().getString(R.string.store_time), 0));
        } else {
            long currentTime = System.currentTimeMillis();
            long diff = (currentTime - firstStoreTime) / 1000 / 3600;
            if (diff < 0) diff = 0;
            mStoreTime.setText(String.format(getResources().getString(R.string.store_time), diff));
        }
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_STORETIME, 10000);
    }

    private void updateVersion() {
        PackageManager pm = getActivity().getPackageManager();//context为当前Activity上下文
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(getActivity().getPackageName(), 0);
            mVersion.setText(String.format("版本：V%s", pi.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}
