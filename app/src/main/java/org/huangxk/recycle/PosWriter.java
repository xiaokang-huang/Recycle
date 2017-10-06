package org.huangxk.recycle;

import android.content.Context;
import android.util.Log;

import com.jiagu.utils.serialUtil;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Locale;

public class PosWriter {
    private static final String LOG_TAG = "PosWriter";
    private static final String PORT = "/dev/ttyUSB0";
    private static final int BAUD = 9600;

    private static final Charset CHARSET = Charset.forName("GB18030");

    private static final byte[] LF = new byte[] {   0x0A    };
    private static final byte[] INIT_CODE = new byte[] { 27, 64, 0x1C, 0x26 };

    private FileDescriptor mFd;
    private OutputStream mOst;

    public void writeUser(Context context, TaskData.UserInfo userInfo) throws Exception {
        Log.d(LOG_TAG, "write user");
        writeChinese(context.getResources().getString(R.string.user_info_cardnum));
        writeChinese(userInfo.mCardNum);
        writeChinese(context.getResources().getString(R.string.user_info_name));
        writeChinese(userInfo.mUserName);
        writeChinese(context.getResources().getString(R.string.user_info_usertype));
        writeChinese(userInfo.getUserTypeStr(context));
        writeChinese(context.getResources().getString(R.string.user_info_registerdate));
        writeChinese(userInfo.mRegisterDate);
        writeChinese(context.getResources().getString(R.string.user_info_userinfo));
        writeChinese(userInfo.mInfo);
    }

    public void writeAnimal(Context context, TaskData.AnimalInfo animalInfo) throws Exception {
        Log.d(LOG_TAG, "write animal");
        writeChinese(context.getResources().getString(R.string.anim_type));
        writeChinese(getAnimalType(context, animalInfo));
        writeChinese(context.getResources().getString(R.string.anim_length));
        writeChinese(String.format("%5.2f", animalInfo.mLengthMM / 1000.0f));
        writeChinese(context.getResources().getString(R.string.anim_weight));
        writeChinese(String.format("%5.2f", animalInfo.mWeightGRAM / 1000.0f));
    }

    public void writeTask(Context context) throws Exception {
        TaskData.UserInfo userInfo = TaskData.getInstance().getUserInfo();
        TaskData.AnimalInfo animalInfo = TaskData.getInstance().getAnimalInfo();
        writeChinese("------------------");
        writeUser(context, userInfo);
        if (!userInfo.isCollector() && animalInfo != null && animalInfo.isValid()) {
            writeChinese("------------------");
            writeAnimal(context, animalInfo);
        }
        for (int i = 0; i < 5; ++i) writeLine("");
    }

    public void testWrite() {
        try {
            writeLine("hello world");
            writeChinese("据外媒报道，当地时间8月17日傍晚，在巴西东南部小城Teixeira de Freitas，一团犹如“上帝之手”的粉尘云悬挂在半空，当地众多居民目睹了这罕见一幕。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeChinese(String data) throws Exception {
        Log.d(LOG_TAG, "writeLine [" + dumpByte(data.getBytes(CHARSET)) + "]");
        mOst.write(data.getBytes(CHARSET));
        mOst.write(LF);
        mOst.flush();
    }

    public void writeLine(String data) throws Exception {
        mOst.write(data.getBytes());
        mOst.write(LF);
        mOst.flush();
    }

    private PosWriter() {
        mFd = serialUtil.uartOpen(PORT, BAUD, 0);
        Log.d(LOG_TAG, "mFd = " + mFd);
        if (mFd != null) {
            mOst = new FileOutputStream(mFd);
        }

    }

    public byte[] toAreaCode(String word) throws Exception {
        byte[] bs = word.getBytes("GB2312");
        for (int i = 0; i < bs.length; ++i) {
            bs[i] = (byte)(bs[i] + 0x80);
        }
        return bs;
    }

    private static PosWriter sInstance = null;
    public static PosWriter getInstance() {
        if (sInstance == null) {
            sInstance = new PosWriter();
        }
        return sInstance;
    }

    private String dumpByte(byte[] data) {
        return dumpByte(data, 0, data.length);
    }

    private String dumpByte(byte[] data, int offset, int len) {
        if (data == null)   return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            sb.append(String.format("0x%x ", data[offset + i]));
        }
        return sb.toString();
    }

    private String getAnimalType(Context context, TaskData.AnimalInfo info) {
        if (info.mAnimalType == TaskData.ANIMALINFO_TYPE_PIG) {
            return context.getResources().getString(R.string.anim_sel_pig);
        } else if  (info.mAnimalType == TaskData.ANIMALINFO_TYPE_CHICKEN) {
            return context.getResources().getString(R.string.anim_sel_chicken);
        } else if  (info.mAnimalType == TaskData.ANIMALINFO_TYPE_DUCK) {
            return context.getResources().getString(R.string.anim_sel_duck);
        }
        return context.getResources().getString(R.string.anim_sel_other);
    }
}
