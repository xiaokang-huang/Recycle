package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.FragmentBase;
import org.huangxk.recycle.ui.IdCardFragment;

public class statusIdCard extends statusBase {
    @Override
    protected StatusEnum acceptEvent(int event) {
        if (event == EVENT_NEXT1) {
            return StatusEnum.STATUS_USERINFO;
        } else if (event == EVENT_FAILED) {
            return StatusEnum.STATUS_IDFAIL;
        }
        return null;
    }

    @Override
    public FragmentBase getFragment() {
        if (mFragment == null) {
            mFragment = new IdCardFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_IDCARD;
    }

    @Override
    public boolean needNfc() {
        return true;
    }
}
