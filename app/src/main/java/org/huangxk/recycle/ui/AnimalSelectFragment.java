package org.huangxk.recycle.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.huangxk.recycle.CountDown;
import org.huangxk.recycle.R;
import org.huangxk.recycle.Speeker;
import org.huangxk.recycle.TaskData;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class AnimalSelectFragment extends FragmentBase implements View.OnClickListener, CountDown.CountListener {
    private TextView mPig;
    private TextView mChicken;
    private TextView mDuck;
    private TextView mOther;
    private TextView mTick;
    private CountDown mCountDown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_animalselect_fragment, container, false);

        mPig = (TextView) view.findViewById(R.id.pig);
        mChicken = (TextView) view.findViewById(R.id.chicken);
        mDuck = (TextView) view.findViewById(R.id.duck);
        mOther = (TextView) view.findViewById(R.id.other);
        mTick = (TextView) view.findViewById(R.id.tick);

        mPig.setOnClickListener(this);
        mChicken.setOnClickListener(this);
        mDuck.setOnClickListener(this);
        mOther.setOnClickListener(this);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        int timeout = this.getActivity().getResources().getInteger(R.integer.animalsel_timeout_second);
        mTick.setText(String.format("%d", timeout));
        mCountDown = new CountDown(timeout, this);
        Speeker.getInstance().startSpeak(Speeker.SOUND_ANIMALSEL, 1000);
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
        if (id == R.id.pig) {
            TaskData.getInstance().getAnimalInfo().mAnimalType = TaskData.ANIMALINFO_TYPE_PIG;
        } else if (id == R.id.chicken) {
            TaskData.getInstance().getAnimalInfo().mAnimalType = TaskData.ANIMALINFO_TYPE_CHICKEN;
        } else if (id == R.id.duck) {
            TaskData.getInstance().getAnimalInfo().mAnimalType = TaskData.ANIMALINFO_TYPE_DUCK;
        } else if (id == R.id.other) {
            TaskData.getInstance().getAnimalInfo().mAnimalType = TaskData.ANIMALINFO_TYPE_OTHER;
        }

        statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_NEXT1);
    }
}
