package org.huangxk.recycle.statusMachine;

import org.huangxk.recycle.ui.AnimalSelectFragment;
import org.huangxk.recycle.ui.FragmentBase;

public class statusAnimalSel extends statusBase {
    @Override
    protected StatusEnum acceptEvent(int event) {
        if (event == EVENT_NEXT1) {
            return StatusEnum.STATUS_WAITUSERSTORE;
        } else if (event == EVENT_TIMEOUT) {
            return StatusEnum.STATUS_IDCARD;
        }
        return null;
    }

    @Override
    public FragmentBase getFragment() {
        if (mFragment == null) {
            mFragment = new AnimalSelectFragment();
        }
        return mFragment;
    }

    @Override
    public StatusEnum getStatusId() {
        return StatusEnum.STATUS_ANIMALSEL;
    }
}
