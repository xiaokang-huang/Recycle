package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.FragmentBase;
import org.huangxk.recycle.ui.WaitUserStoreFragment;

/**
 * Created by huangxk on 17-7-20.
 */

public class statusWaitUser extends statusBase {
    @Override
    protected StatusEnum acceptEvent(int event) {
        if (event == EVENT_NEXT1) {
            return StatusEnum.STATUS_WAITSYSSTORE;
        } else if (event == EVENT_TIMEOUT) {
            return StatusEnum.STATUS_IDCARD;
        }
        return null;
    }

    @Override
    public FragmentBase getFragment() {
        if (mFragment == null) {
            mFragment = new WaitUserStoreFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_WAITUSERSTORE;
    }
}
