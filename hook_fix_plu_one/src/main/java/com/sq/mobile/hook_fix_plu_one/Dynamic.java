package com.sq.mobile.hook_fix_plu_one;

import android.content.Context;

import com.sq.mobile.hook_fix_lib_one.IDynamic;

public class Dynamic implements IDynamic {

    @Override
    public String getStringForResId(Context context) {
        return context.getResources().getString(R.string.string_plu_hello);
    }

}
