package org.huangxk.recycle.ui;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.huangxk.recycle.CardOperator;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.TaskData;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class IdCardFragment extends FragmentBase implements View.OnClickListener, View.OnLongClickListener {
    TextView mSwipe;

    public IdCardFragment() {
        mCanReadNfc = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_idcard_fragment, container, false);

        mSwipe = (TextView) view.findViewById(R.id.swipe);
        mSwipe.setOnClickListener(this);
        mSwipe.setOnLongClickListener(this);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        TaskData.getInstance().clearAll();
        Speeker.getInstance().startSpeak(Speeker.SOUND_IDCARD, 1000);
        super.onResume();
    }

    @Override
    public void onPause() {
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
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT2);
        }
    }


}
