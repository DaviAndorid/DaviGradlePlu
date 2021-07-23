package com.sq.mobile.hook_fix_lib_one;

import android.app.Activity;
import android.content.res.Resources;

public class ZeusBaseActivity extends Activity {

    @Override
    public Resources getResources() {
        return PluginManager.mNowResources;
    }


}
