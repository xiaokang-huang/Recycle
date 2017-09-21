package org.huangxk.recycle.ui;

import android.app.Fragment;
import android.content.Intent;

public abstract class FragmentBase extends Fragment {
    void handleNewIntent(Intent intent) {
        // do nothing
    }

    public boolean mCanReadNfc = false;
}
