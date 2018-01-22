package org.huangxk.recycle.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.huangxk.recycle.DataBase;
import org.huangxk.recycle.PosWriter;
import org.huangxk.recycle.R;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

public class SettingFragment extends FragmentBase implements View.OnClickListener {
    private TextView mExitText;
    private TextView mSaveText;
    private TextView mSystemSetting;
    private TextView mTestPrint;
    private EditText mStationId;

    private String mErrorStr;
    private Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_setting, container, false);

        mExitText = (TextView) view.findViewById(R.id.exit);
        mSaveText = (TextView) view.findViewById(R.id.save);
        mSystemSetting = (TextView) view.findViewById(R.id.system_setting);
        mTestPrint = (TextView) view.findViewById(R.id.test_print);
        mStationId = (EditText) view.findViewById(R.id.station_id);

        mExitText.setOnClickListener(this);
        mSaveText.setOnClickListener(this);
        mSystemSetting.setOnClickListener(this);
        mTestPrint.setOnClickListener(this);

        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        update_settings();
        mErrorStr = PosWriter.getInstance().checkConnection();
        if (mErrorStr != null) {
            mTestPrint.setEnabled(false);
            showPostError();
        }
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.exit) {
            statusManager.getInstance().getCurrentStatus().updateStatus(statusBase.EVENT_EXIT);
        } else if (id == R.id.save) {
            saveSettings();
            Toast.makeText(getActivity(), R.string.toast_save_ok, Toast.LENGTH_LONG).show();
        } else if (id == R.id.system_setting) {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            getActivity().startActivity(intent);
        } else if (id == R.id.test_print) {
            PosWriter.getInstance().testWrite();
        }
    }

    private void saveSettings() {
        int station_id = DataBase.INVALID_STATIONID;
        try {
            station_id = Integer.parseInt(mStationId.getText().toString());
        } catch (Exception ex) {}
        DataBase.getInstance().setStationId(station_id);
    }

    private void update_settings() {
        int station_id = DataBase.getInstance().getStationId();
        if (station_id != DataBase.INVALID_STATIONID) {
            mStationId.setText(String.format("%s", station_id));
        }
    }

    private void showPostError() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder ab = new AlertDialog.Builder(SettingFragment.this.getActivity());
                ab.setTitle(mErrorStr);
                ab.setIcon(android.R.drawable.stat_sys_warning);
                ab.create().show();
            }
        });

    }
}
