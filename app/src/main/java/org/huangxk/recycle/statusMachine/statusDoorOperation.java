package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.DoorOperationFragment;
import org.huangxk.recycle.ui.FragmentBase;

public class statusDoorOperation extends statusBase {
    @Override
    protected StatusEnum acceptEvent(int event) {
        if (event == EVENT_NEXT1) {
            return StatusEnum.STATUS_USERPRINT;
        }
        return null;
    }

    @Override
    public FragmentBase getFragment() {
        if (mFragment == null) {
            mFragment = new DoorOperationFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_DOOROPERATION;
    }
}
