package org.huangxk.recycle.statusMachine;

public class statusManager {
    public interface onStatusChangeListener {
        void onStatusChanged(statusBase newStatus);
    };

    public void updateStatus(statusBase.StatusEnum newStatus) {
        if (mCurrentStatus != newStatus) {
            mCurrentStatus = newStatus;
            if (mListener != null) {
                mListener.onStatusChanged(mStatusArray[mCurrentStatus.ordinal()]);
            }
        }
    }

    public void setListener(onStatusChangeListener listner) {
        mListener = listner;
    }

    public statusBase getCurrentStatus() {
        return mStatusArray[mCurrentStatus.ordinal()];
    }

    private static statusManager sInstance = null;
    public static statusManager getInstance() {
        if (sInstance == null) {
            sInstance = new statusManager();
        }
        return sInstance;
    }

    private statusBase[] mStatusArray = new statusBase[] {
            new statusLauncher(),
            new statusSetting(),
            new statusIdCard(),
            new statusIdFail(),
            new statusUserInfo(),
            new statusAnimalSel(),
            new statusWaitUser(),
            new statusWaitSys(),
            new statusDoorOperation(),
            new statusTaskInfo(),
            new statusUserPrint()
    };
    private statusBase.StatusEnum mCurrentStatus = statusBase.StatusEnum.STATUS_LAUNCHER;
    private onStatusChangeListener mListener;
}
