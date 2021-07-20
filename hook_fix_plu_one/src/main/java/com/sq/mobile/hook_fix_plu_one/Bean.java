package com.sq.mobile.hook_fix_plu_one;

import com.sq.mobile.hook_fix_lib_one.IBean;
import com.sq.mobile.hook_fix_lib_one.ICallback;

public class Bean implements IBean {

    private String name = "daviAndroid";


    private ICallback callback;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String paramString) {
        this.name = paramString;
    }

    @Override
    public void register(ICallback callback) {
        this.callback = callback;

        //clickButton();
    }

    public void clickButton() {
        callback.sendResult("Hello: " + this.name);
    }


}
