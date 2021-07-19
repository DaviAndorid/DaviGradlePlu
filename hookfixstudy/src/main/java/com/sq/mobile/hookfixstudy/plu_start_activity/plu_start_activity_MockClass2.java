package com.sq.mobile.hookfixstudy.plu_start_activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.sq.mobile.hookfixstudy.RefInvoke;

public class plu_start_activity_MockClass2 implements Handler.Callback {

    Handler mBase;

    public plu_start_activity_MockClass2(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case 100:
                handleLaunchActivity(msg);
                break;

        }

        mBase.handleMessage(msg);
        return true;
    }


    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Object obj = msg.obj;

        // 把替身恢复成真身
        Intent intent = (Intent) RefInvoke.getFieldObject(obj, "intent");

        Intent targetIntent = intent.getParcelableExtra(plu_start_activity_AMSHookHelper.EXTRA_TARGET_INTENT);
        intent.setComponent(targetIntent.getComponent());
    }


}
