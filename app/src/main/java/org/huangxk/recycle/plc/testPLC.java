package org.huangxk.recycle.plc;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.webkit.CookieManager;

import com.jiagu.utils.serialUtil;

public class testPLC {
    private static final String LOG_TAG = "testPLC";
    private static final int MSG_START = 184;
    private static Handler sHandler;
    public static void test() {
        Log.d(LOG_TAG, "==================================================");
        if (Connection.getInstance().connect("192.168.2.1") == true) {
            byte[] db = Connection.getInstance().testReadDB(800, 32);
            Log.d(LOG_TAG, String.format("weight = %f, length = %f", Float.intBitsToFloat(byte2Int(db, 0)), Float.intBitsToFloat(byte2Int(db, 4))));

            Connection.getInstance().doOperation(Connection.OP_FRONTDOOR_OPEN, sHandler);

            //Connection.getInstance().doOperation(Connection.OP_FRONTDOOR_CLOSE, 10000);
            Connection.AnimalData animal = Connection.getInstance().getAnimalData();
            Log.d(LOG_TAG, String.format("Animal[%f, %f, %f, %f, %f, %f]", animal.mWeight, animal.mLength, animal.mLiaoDouWeight, animal.mLiaoDouAnimalNum, animal.mTotalWeight, animal.mTotalWeight2));
            Connection.getInstance().close();
        } else {
            Log.d(LOG_TAG, "connect failed");
        }
    }

    public static void startTest(Looper looper) {
        sHandler = new MyHandler(looper);
        //sHandler.sendEmptyMessage(MSG_START);
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                test();
            }
        }).start();
        */
    }

    private static int byte2Int(byte[] data, int start) {
        int value = ((data[start + 0] & 0xFF) << 24) | ((data[start + 1] & 0xFF) << 16) | ((data[start + 2] & 0xFF) << 8) | ((data[start + 3] & 0xFF) << 0);
        //Log.d(LOG_TAG, String.format("byte2int %x%x%x%x %x", data[0 + start], data[1 + start], data[2 + start], data[3 + start], value));
        return value;
    }

    private static class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            /*
            if (msg.what == MSG_START && Connection.getInstance().connect("192.168.2.1") == true) {
                Connection.getInstance().doOperation(Connection.OP_FRONTDOOR_OPEN, sHandler);
            } else if (msg.what == Connection.MSG_OPERATION_FINISHED) {
                int opcode = msg.arg1;
                if (opcode == Connection.OP_FRONTDOOR_OPEN) {
                    Connection.getInstance().doOperation(Connection.OP_STARTTRANS, sHandler);
                } else if (opcode == Connection.OP_STARTTRANS) {
                    Connection.getInstance().doOperation(Connection.OP_FRONTDOOR_CLOSE, sHandler);
                } else if (opcode == Connection.OP_FRONTDOOR_CLOSE) {
                    Connection.getInstance().close();
                }
            }
            */
            if (msg.what == MSG_START && Connection.getInstance().connect("192.168.2.1") == true) {
                Connection.getInstance().doOperation(Connection.OP_BACKDOOR_OPEN, sHandler);
            } else if (msg.what == Connection.MSG_OPERATION_FINISHED) {
                int opcode = msg.arg1;
                if (opcode == Connection.OP_BACKDOOR_OPEN) {
                    Connection.getInstance().doOperation(Connection.OP_BACKDOOR_CLOSE, sHandler);
                } else if (opcode == Connection.OP_LEVEL1) {
                    Connection.getInstance().doOperation(Connection.OP_LEVEL2, sHandler);
                } else if (opcode == Connection.OP_LEVEL2) {
                    Connection.getInstance().doOperation(Connection.OP_LEVEL3, sHandler);
                } else if (opcode == Connection.OP_LEVEL3) {
                    Connection.getInstance().doOperation(Connection.OP_BACKDOOR_CLOSE, sHandler);
                } else if (opcode == Connection.OP_BACKDOOR_CLOSE) {
                    Connection.getInstance().close();
                }
            }
            super.handleMessage(msg);
        }
    }
}
