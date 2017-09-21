package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.FragmentBase;
import org.huangxk.recycle.ui.TaskInfoFragment;

public class statusTaskInfo extends statusBase {
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
            mFragment = new TaskInfoFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_TASKINFO;
    }
}
