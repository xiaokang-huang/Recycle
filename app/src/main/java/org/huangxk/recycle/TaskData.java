package org.huangxk.recycle;

import android.content.Context;
import android.media.Image;

import java.nio.ByteBuffer;

public class TaskData {
    public static final int INVALID_DATA = -1;

    public static final int USERINFO_TYPE_COLLECTOR = 0x1001;
    public static final int USERINFO_TYPE_REGISTED_FARMER = 0x1002;
    public static final int USERINFO_TYPE_INDIVIDUAL = 0x1003;

    public static final int ANIMALINFO_TYPE_PIG = 0xFC01;
    public static final int ANIMALINFO_TYPE_CHICKEN = 0xFC02;
    public static final int ANIMALINFO_TYPE_DUCK = 0xFC03;
    public static final int ANIMALINFO_TYPE_OTHER = 0xFC04;

    public class UserInfo {
        public String mCardNum;
        public int mUserType;
        public String mUserName;
        public String mRegisterDate;
        public String mInfo;

        public UserInfo() {
            clear();
        }

        public void clear() {
            mUserType = INVALID_DATA;
            mCardNum = null;
            mUserName = null;
            mRegisterDate = null;
            mInfo = null;
        }

        public boolean isValid() {
            return mUserType != INVALID_DATA;
        }
        public boolean isFarmer() {
            return (mUserType == USERINFO_TYPE_REGISTED_FARMER || mUserType == USERINFO_TYPE_INDIVIDUAL);
        }
        public boolean isCollector() {
            return (mUserType == USERINFO_TYPE_COLLECTOR);
        }
        public String getUserTypeStr(Context context) {
            String output;
            if (mUserType == USERINFO_TYPE_COLLECTOR) {
                output = context.getResources().getString(R.string.user_type_collector);
            } else if (mUserType == USERINFO_TYPE_REGISTED_FARMER) {
                output = context.getResources().getString(R.string.user_type_registedfarmer);
            } else if (mUserType == USERINFO_TYPE_INDIVIDUAL) {
                output = context.getResources().getString(R.string.user_type_individualfarmer);
            } else {
                output = context.getResources().getString(R.string.user_type_invalid);
            }

            return output;
        }
    }

    public class AnimalInfo {
        public int mAnimalType;
        public int mLengthMM;
        public int mWeightGRAM;
        public Image mJpegPic;

        public AnimalInfo() {
            clear();
        }

        public void clear() {
            mAnimalType = INVALID_DATA;
            mLengthMM = INVALID_DATA;
            mWeightGRAM = INVALID_DATA;
            mJpegPic = null;
        }

        public byte[] jpegPicToByte() {
            if (mJpegPic == null)   return null;
            ByteBuffer buffer = mJpegPic.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        }

        public boolean isValid() {
            return mAnimalType != INVALID_DATA;
        }
    }

    public class CollectorInfo {
        public String mTrackId;
        public String mTrackInfo;
        public String mRegisterDate;
    }

    private UserInfo mUserInfo = new UserInfo();
    private AnimalInfo mAnimalInfo = new AnimalInfo();
    private CollectorInfo mCollectorInfo = new CollectorInfo();
    private long mTimestamp = INVALID_DATA;

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public AnimalInfo getAnimalInfo() {
        return mAnimalInfo;
    }

    public void clearAll() {
        mUserInfo.clear();
        mAnimalInfo.clear();
    }

    private static TaskData sInstance = null;
    public static TaskData getInstance() {
        if (sInstance == null) {
            sInstance = new TaskData();
        }
        return sInstance;
    }
}
