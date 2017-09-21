package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.FragmentBase;

public abstract class statusBase {
    public enum StatusEnum {
        STATUS_LAUNCHER,
        STATUS_SETTING,
        STATUS_IDCARD,
        STATUS_IDFAIL,
        STATUS_USERINFO,
        STATUS_ANIMALSEL,
        STATUS_WAITUSERSTORE,
        STATUS_WAITSYSSTORE,
        STATUS_DOOROPERATION,
        STATUS_TASKINFO,
        STATUS_USERPRINT,

        STATUS_NUM
    };

    public static final int EVENT_NEXT1 = 0;
    public static final int EVENT_NEXT2 = 1;
    public static final int EVENT_TIMEOUT = 2;
    public static final int EVENT_FAILED = 3;
    public static final int EVENT_EXIT = 4;

    protected abstract StatusEnum acceptEvent(int event);
    public abstract FragmentBase getFragment();
    public abstract StatusEnum getStatusId();

    protected FragmentBase mFragment;

    public boolean needNfc() {
        return false;
    }

    public void updateStatus(int event) {
        statusManager.getInstance().updateStatus(acceptEvent(event));
    }
}
