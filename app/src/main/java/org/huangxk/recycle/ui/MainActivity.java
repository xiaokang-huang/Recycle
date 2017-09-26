package org.huangxk.recycle.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import org.huangxk.recycle.CardOperator;
import org.huangxk.recycle.DataBase;
import org.huangxk.recycle.PosWriter;
import org.huangxk.recycle.R;
import org.huangxk.recycle.plc.Connection;
import org.huangxk.recycle.plc.testPLC;
import org.huangxk.recycle.statusMachine.statusBase;
import org.huangxk.recycle.statusMachine.statusManager;

import java.io.IOException;

public class MainActivity extends Activity implements statusManager.onStatusChangeListener {
    private static final String LOG_TAG = "MainActivity";
    private FragmentManager mFragmentMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main_activity);

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        mFragmentMgr = getFragmentManager();
        statusManager.getInstance().setListener(this);
        CardOperator.getInstance().initializeNfc(this);
        DataBase.createInstance(this);

        onNewIntent(getIntent());
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume");

        int uiOption = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(uiOption);

        updateFragment(statusManager.getInstance().getCurrentStatus());
        justForTest();
        super.onResume();
    }
    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        CardOperator.getInstance().stopListen();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(LOG_TAG, "onNewIntent " + intent.getAction());

        if (!intent.getAction().equals(Intent.ACTION_MAIN)) {
            statusBase currStatus = statusManager.getInstance().getCurrentStatus();
            if (currStatus.needNfc()) {
                currStatus.getFragment().handleNewIntent(intent);
            }
        }

        super.onNewIntent(intent);
    }

    private void updateFragment(statusBase newStatus) {
        FragmentTransaction transaction = mFragmentMgr.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.content, newStatus.getFragment());
        transaction.commit();

        if (newStatus.needNfc()) {
            CardOperator.getInstance().startListen();
        } else {
            CardOperator.getInstance().stopListen();
        }
    }

    @Override
    public void onStatusChanged(statusBase newStatus) {
        final statusBase tempStatus = newStatus;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateFragment(tempStatus);
            }
        });
    }

    private void justForTest() {
        testPLC.startTest(getMainLooper());
    }
}
