package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.FragmentBase;
import org.huangxk.recycle.ui.SettingFragment;

public class statusSetting extends statusBase {
    @Override
    protected StatusEnum acceptEvent(int event) {
        if (event == EVENT_EXIT) {
            return StatusEnum.STATUS_LAUNCHER;
        }
        return null;
    }

    @Override
    public FragmentBase getFragment() {
        if (mFragment == null) {
            mFragment = new SettingFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_SETTING;
    }
}
