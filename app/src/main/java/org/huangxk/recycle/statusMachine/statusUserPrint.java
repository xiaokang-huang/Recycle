package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.FragmentBase;
import org.huangxk.recycle.ui.UserPrintFragment;

public class statusUserPrint extends statusBase {
    @Override
    protected StatusEnum acceptEvent(int event) {
        if (event == EVENT_NEXT1) {
            return StatusEnum.STATUS_ANIMALSEL;
        } else if (event == EVENT_TIMEOUT) {
            return StatusEnum.STATUS_IDCARD;
        }
        return null;
    }

    @Override
    public FragmentBase getFragment() {
        if (mFragment == null) {
            mFragment = new UserPrintFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_USERPRINT;
    }
}
