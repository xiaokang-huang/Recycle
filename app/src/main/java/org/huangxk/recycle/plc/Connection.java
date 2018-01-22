package org.huangxk.recycle.plc;

import android.os.Handler;
import android.util.Log;

public class Connection {
    private static final String LOG_TAG = "PLCConnection";
    private static final int CHECK_FINISH_DELAY = 500;

    private static native boolean connectToPLC(String ip);
    private static native void closeConnection();
    private static native byte[] readMerker(int start, int count);
    private static native byte[] readDB(int start, int count);
    private static native boolean setBit(int addrByte, int addrBit, boolean on);
    private static native byte[] readArea(int area, int dbNum, int start, int count);

    public static final int OP_FRONTDOOR_OPEN = 2 * 8 + 2;
    public static final int OP_STARTTRANS = 2 * 8 + 3;
    public static final int OP_FRONTDOOR_CLOSE = 3 * 8 + 4;

    public static final int OP_BACKDOOR_OPEN = 7 * 8 + 0;
    public static final int OP_BACKDOOR_CLOSE = 7 * 8 + 1;
    public static final int OP_BACKDOOR_CLEAR = 7 * 8 + 3;

    public static final int OP_LEVEL1 = 3 * 8 + 0;
    public static final int OP_LEVEL2 = 3 * 8 + 1;
    public static final int OP_LEVEL3 = 3 * 8 + 2;

    public static final int MSG_OPERATION_FINISHED = 0x1001;

    public class AnimalData {
        public float mWeight;
        public float mLength;
        public float mLiaoDouWeight;
        public float mLiaoDouAnimalNum;
        public float mTotalWeight;
        public float mTotalWeight2;
    }

    public boolean connect(String ip) {
        boolean ret = connectToPLC(ip);
        if (ret) {
            initPLCOutout();
        }
        return ret;
    }

    public void close() {
        closeConnection();
    }

    public byte[] testReadMerker(int start, int count) {
        return readMerker(start, count);
    }

    public byte[] testReadDB(int start, int count) {
        return readDB(start, count);
    }

    public AnimalData getAnimalData() {
        updateDBData();
        mAnimalData.mWeight = byte2Float(mDBData, DB_ADDR_WEIGHT);
        mAnimalData.mLength = byte2Float(mDBData, DB_ADDR_LENGTH);
        mAnimalData.mLiaoDouWeight = byte2Float(mDBData, DB_ADDR_LIAODOU_WEIGHT);
        mAnimalData.mLiaoDouAnimalNum = byte2Float(mDBData, DB_ADDR_LIAODOU_ANIMALNUM);
        mAnimalData.mTotalWeight = byte2Float(mDBData, DB_ADDR_TOTAL_WEIGHT);
        mAnimalData.mTotalWeight2 = byte2Float(mDBData, DB_ADDR_TOTAL_WEIGHT2);

        return mAnimalData;
    }

    public void initPLCOutout() {
        finishOperation(OP_FRONTDOOR_OPEN);
        finishOperation(OP_FRONTDOOR_CLOSE);
        finishOperation(OP_STARTTRANS);
        finishOperation(OP_BACKDOOR_OPEN);
        finishOperation(OP_BACKDOOR_CLOSE);
        finishOperation(OP_LEVEL1);
        finishOperation(OP_LEVEL2);
        finishOperation(OP_LEVEL3);
    }

    public void doOperation(int opCode, Handler handler) {
        finishOperation(opCode);
        updateMerkerData();
        int addr_byte = opCode >> 3;
        int addr_bit = opCode & 7;
        Log.d(LOG_TAG, String.format("doOperation, set byte = %d, bit = %d", addr_byte, addr_bit));
        setBit(addr_byte, addr_bit, true);
        if (handler != null) {
            handler.postAtTime(new FinishHandler(opCode, handler), CHECK_FINISH_DELAY);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finishOperation(opCode);

    }

    private void finishOperation(int opCode) {
        updateMerkerData();
        int addr_byte = opCode >> 3;
        int addr_bit = opCode & 7;
        setBit(addr_byte, addr_bit, false);
    }

    private class FinishHandler implements Runnable {
        private int mOpCode = 0;
        private Handler mHandler;

        public FinishHandler(int opCode, Handler handler) {
            mOpCode = opCode;
            mHandler = handler;
        }

        @Override
        public void run() {
            Log.d(LOG_TAG, String.format("RUN OPCODE = %d", mOpCode));
            if (mOpCode == OP_FRONTDOOR_OPEN) {
                if (!isFrontdoorOpenFinish()) {
                    mHandler.postDelayed(this, CHECK_FINISH_DELAY);
                    Log.d(LOG_TAG, String.format("\tnot finished"));
                } else {
                    finishOperation(mOpCode);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_FINISHED, mOpCode, 0));
                    Log.d(LOG_TAG, String.format("\tfinished"));
                }
            } else if (mOpCode == OP_FRONTDOOR_CLOSE) {
                if (!isFrontdoorCloseFinish()) {
                    mHandler.postDelayed(this, CHECK_FINISH_DELAY);
                    Log.d(LOG_TAG, String.format("\tnot finished"));
                } else {
                    finishOperation(mOpCode);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_FINISHED, mOpCode, 0));
                    Log.d(LOG_TAG, String.format("\tfinished"));
                }
            } else if (mOpCode == OP_STARTTRANS) {
                if (!isTransFinish()) {
                    mHandler.postDelayed(this, CHECK_FINISH_DELAY);
                    Log.d(LOG_TAG, String.format("\tnot finished"));
                } else {
                    finishOperation(mOpCode);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_FINISHED, mOpCode, 0));
                    Log.d(LOG_TAG, String.format("\tfinished"));
                }
            } else if (mOpCode == OP_BACKDOOR_OPEN) {
                if (!isBackdoorOpenFinish()) {
                    mHandler.postDelayed(this, CHECK_FINISH_DELAY);
                    Log.d(LOG_TAG, String.format("\tnot finished"));
                } else {
                    finishOperation(mOpCode);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_FINISHED, mOpCode, 0));
                    Log.d(LOG_TAG, String.format("\tfinished"));
                }
            } else if (mOpCode == OP_BACKDOOR_CLOSE) {
                if (!isBackdoorCloseFinish()) {
                    mHandler.postDelayed(this, CHECK_FINISH_DELAY);
                    Log.d(LOG_TAG, String.format("\tnot finished"));
                } else {
                    finishOperation(mOpCode);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_FINISHED, mOpCode, 0));
                    Log.d(LOG_TAG, String.format("\tfinished"));
                }
            } else if (mOpCode == OP_LEVEL1 || mOpCode == OP_LEVEL2 || mOpCode == OP_LEVEL3) {
                if (!isLevelFinish()) {
                    mHandler.postDelayed(this, CHECK_FINISH_DELAY);
                    Log.d(LOG_TAG, String.format("\tnot finished"));
                } else {
                    finishOperation(mOpCode);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_FINISHED, mOpCode, 0));
                    Log.d(LOG_TAG, String.format("\tfinished"));
                }
            }
        }
    }

    public boolean isFrontdoorOpenFinish() {
        updateInputData();
        updateOutputData();
        boolean frontDoorOpenOK = checkBit(mInputData[3], 1);
        boolean frontDoorStopOpen = !checkBit(mOutputData[0], 0);
        return frontDoorOpenOK && frontDoorStopOpen;
    }

    public boolean isFrontdoorCloseFinish() {
        updateInputData();
        updateOutputData();
        boolean frontDoorCloseOK = checkBit(mInputData[3], 2);
        boolean frontDoorStopClose = !checkBit(mOutputData[0], 1);
        return frontDoorCloseOK && frontDoorStopClose;
    }

    public boolean isTransFinish() {
        updateInputData();
        return checkBit(mInputData[12], 1);
    }

    public boolean isBackdoorOpenFinish() {
        updateInputData();
        updateOutputData();
        boolean backDoorOpenOK = checkBit(mInputData[3], 3);
        boolean backDoorStopOpen = !checkBit(mOutputData[0], 2);
        return backDoorOpenOK && backDoorStopOpen;
    }

    public boolean isBackdoorCloseFinish() {
        updateInputData();
        updateOutputData();
        boolean backDoorCloseOK = checkBit(mInputData[3], 4);
        boolean backDoorStopClose = !checkBit(mOutputData[0], 3);
        return backDoorCloseOK && backDoorStopClose;
    }

    public boolean isLevelFinish() {
        updateInputData();
        return checkBit(mInputData[0], 5);
    }

    public String getInput() {
        updateInputData();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mInputData.length; ++i) {
            int data = mInputData[i] & 0xFF;
            if ((i & 1) == 0) {
                sb.append("\n");
            }
            sb.append("I").append(i).append("=[ ");
            for (int bit = 0; bit < 8; ++bit) {
                sb.append(((data & (1 << bit)) != 0)? "1 ":"0 ");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public String getOutput() {
        updateInputData();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mOutputData.length; ++i) {
            int data = mOutputData[i] & 0xFF;
            if ((i & 1) == 0) {
                sb.append("\n");
            }
            sb.append("O").append(i).append("=[ ");
            for (int bit = 0; bit < 8; ++bit) {
                sb.append(((data & (1 << bit)) != 0)? "1 ":"0 ");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    private static final int S7AreaPE   =	0x81;
    private static final int S7AreaPA   =	0x82;
    private static final int S7AreaMK   =	0x83;
    private static final int S7AreaDB   =	0x84;
    private static final int S7AreaCT   =	0x1C;
    private static final int S7AreaTM   =	0x1D;

    private static final int DB_ADDR_BASE = 800;
    private static final int DB_ADDR_LENGTH = 30;
    private static final int DB_ADDR_WEIGHT = 34;
    private static final int DB_ADDR_LIAODOU_WEIGHT = 8;
    private static final int DB_ADDR_LIAODOU_ANIMALNUM  = 12;
    private static final int DB_ADDR_TOTAL_WEIGHT = 16;
    private static final int DB_ADDR_TOTAL_WEIGHT2 = 20;
    private static final int DB_READ_SIZE = 40;

    private static final int MK_ADDR_BASE = 0;
    private static final int MK_READ_SIZE = 16;

    private byte[] mMerkerData;
    private byte[] mDBData;
    private byte[] mInputData;
    private byte[] mOutputData;
    private AnimalData mAnimalData = new AnimalData();

    private void updateMerkerData() {
        mMerkerData = readMerker(MK_ADDR_BASE, MK_READ_SIZE);
    }

    private void updateDBData() {
        mDBData = readDB(DB_ADDR_BASE, DB_READ_SIZE);
    }

    private void updateInputData() {
        mInputData = readArea(S7AreaPE, 0, 0, 32);
    }

    private void updateOutputData() {
        mOutputData = readArea(S7AreaPA, 0, 0, 32);
    }

    private static boolean checkBit(byte data, int bit) {
        return (data & (1 << bit)) != 0;
    }

    private static float byte2Float(byte[] data, int start) {
        int value = ((data[start + 0] & 0xFF) << 24) | ((data[start + 1] & 0xFF) << 16) | ((data[start + 2] & 0xFF) << 8) | ((data[start + 3] & 0xFF) << 0);
        return Float.intBitsToFloat(value);
    }

    static {
        System.loadLibrary("lib-plc");
    }

    private static Connection sInstance = null;
    public static Connection getInstance() {
        if (sInstance == null) {
            sInstance = new Connection();
        }
        return sInstance;
    }
}
