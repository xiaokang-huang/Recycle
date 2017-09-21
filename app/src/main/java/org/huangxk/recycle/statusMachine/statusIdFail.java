package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.FragmentBase;
import org.huangxk.recycle.ui.IdFailFragment;

public class statusIdFail extends statusBase {
    @Override
    protected StatusEnum acceptEvent(int event) {
        if (event == EVENT_TIMEOUT) {
            return StatusEnum.STATUS_IDCARD;
        }
        return null;
    }

    @Override
    public FragmentBase getFragment() {
        if (mFragment == null) {
            mFragment = new IdFailFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_IDFAIL;
    }
}
