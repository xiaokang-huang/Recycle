package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.FragmentBase;
import org.huangxk.recycle.ui.LaunchFragment;

/**
 * Created by huangxk on 17-7-20.
 */

public class statusLauncher extends statusBase {
    @Override
    protected StatusEnum acceptEvent(int event) {
        if (event == EVENT_NEXT1) {
            return StatusEnum.STATUS_IDCARD;
        } else if (event == EVENT_NEXT2) {
            return StatusEnum.STATUS_SETTING;
        }
        return null;
    }

    @Override
    public FragmentBase getFragment() {
        if (mFragment == null) {
            mFragment = new LaunchFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_LAUNCHER;
    }
}
