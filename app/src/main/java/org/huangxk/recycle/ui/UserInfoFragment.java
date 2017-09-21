package org.huangxk.recycle.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.huangxk.recycle.CountDown;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.TaskData;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class UserInfoFragment extends FragmentBase implements CountDown.CountListener, View.OnClickListener {


    private TextView mUserInfo;
    private ImageView mUserImg;
    private ImageView mNextBtn;
    private TextView mTimeout;
    private CountDown mCountDown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_userinfo_fragment, container, false);

        mUserInfo = (TextView)view.findViewById(R.id.user_info);
        mUserImg = (ImageView)view.findViewById(R.id.user_img);
        mNextBtn = (ImageView)view.findViewById(R.id.next);
        mTimeout = (TextView)view.findViewById(R.id.tick);

        mNextBtn.setOnClickListener(this);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        int timeout = this.getActivity().getResources().getInteger(R.integer.userinfo_timeout_second);
        mTimeout.setText(String.format("%d", timeout));
        mCountDown = new CountDown(timeout, this);
        updateUserInfo();
        Speeker.getInstance().startSpeak(Speeker.SOUND_USERINFO, 1000);
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
                mTimeout.setText(String.format("%d", time));
            }
        });

        if (count == 0) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_TIMEOUT);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.next &&  TaskData.getInstance().getUserInfo().isFarmer()) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
        } else if (id == R.id.next &&  TaskData.getInstance().getUserInfo().isCollector()) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT2);
        }
    }

    private String getUserType(int type) {
        String STR_COLLECTOR = getResources().getString(R.string.user_type_collector);
        String STR_REGISTERFARMER = getResources().getString(R.string.user_type_registedfarmer);
        String STR_INDIVIDUALFARMER = getResources().getString(R.string.user_type_individualfarmer);
        if (type == TaskData.USERINFO_TYPE_COLLECTOR) {
            return STR_COLLECTOR;
        } else if (type == TaskData.USERINFO_TYPE_REGISTED_FARMER) {
            return STR_REGISTERFARMER;
        } else if (type == TaskData.USERINFO_TYPE_INDIVIDUAL) {
            return STR_INDIVIDUALFARMER;
        }
        return null;
    }

    private void updateUserInfo() {
        TaskData.UserInfo userInfo = TaskData.getInstance().getUserInfo();
        if (userInfo.isValid()) {
            mUserInfo.setText(String.format(getResources().getString(R.string.user_info),
                    userInfo.mCardNum,
                    getUserType(userInfo.mUserType),
                    userInfo.mUserName, userInfo.mInfo)
            );
        }
    }
}
