package com.sq.mobile.hook_fix_host_one;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sq.mobile.hook_fix_lib_one.IBean;
import com.sq.mobile.hook_fix_lib_one.ICallback;
import com.sq.mobile.hook_fix_lib_one.IDynamic;
import com.sq.mobile.hook_fix_lib_one.RefInvoke;

import java.lang.reflect.Method;

public class MainActivity extends BaseActivity {

    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //testLoadPlu();
        //loadBeanInterfaceTest();
        //readResFromPlu();
        changeSkinFromPlu();
    }


    /*********************************
     * 换肤【插件2】
     **********************************/
    void changeSkinFromPlu() {
        Button btn_7 = (Button) findViewById(R.id.btn_7);
        btn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PluginInfo info = plugins.get(PLU_APK_NAME_LIST[1]);
                if (info == null) {
                    return;
                }
                //资源加载，构建AssetManager/Resources等
                loadResources(info.getDexPath());

                //使用资源
                doSomething(info.getClassLoader());
            }
        });
    }

    private void doSomething(ClassLoader cl) {
        try {
            /**
             * 方式1：插件中封装接口方式，如：UIUtil
             * */
            Class clazz = cl.loadClass("com.sq.mobile.hook_fix_plu_second.UIUtil");
            String str = (String) RefInvoke.invokeStaticMethod(clazz, "getTextString", Context.class, this);
            //Toast.makeText(getApplicationContext(), str + "", Toast.LENGTH_LONG).show();
            //textV.setText(str);

            /***
             * 方式2：直接访问插件的资源id的值，不需要接口封装
             * */
            Class stringClass = cl.loadClass("com.sq.mobile.hook_fix_plu_second.R$string");
            int resId1 = (int) RefInvoke.getStaticFieldObject(stringClass, "app_name");
            Toast.makeText(getApplicationContext(), getResources().getString(resId1) + "", Toast.LENGTH_LONG).show();

            //Drawable drawable = (Drawable) RefInvoke.invokeStaticMethod(clazz, "getImageDrawable", Context.class, this);
            //imgV.setBackground(drawable);

            //layout.removeAllViews();
            //View view = (View) RefInvoke.invokeStaticMethod(clazz, "getLayout", Context.class, this);
            //layout.addView(view);

        } catch (Exception e) {
            Log.e(TAG, "【doSomething】msg : " + e.getMessage());
        }
    }


    /*********************************
     * 读取插件里的一个字符串资源【插件1】
     **********************************/
    void readResFromPlu() {
        final String classPlu = "com.sq.mobile.hook_fix_plu_one.Dynamic";

        Button btn_6 = (Button) findViewById(R.id.btn_6);
        final TextView textView = (TextView) findViewById(R.id.tv);

        //带资源文件的调用
        btn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loadResources(plugins.get(PLU_APK_NAME_LIST[0]).getDexPath());
                Class mLoadClassDynamic = null;

                try {
                    mLoadClassDynamic = classLoader.loadClass(classPlu);
                    Object dynamicObject = mLoadClassDynamic.newInstance();

                    IDynamic dynamic = (IDynamic) dynamicObject;
                    String content = dynamic.getStringForResId(MainActivity.this);
                    textView.setText(content);
                    Toast.makeText(getApplicationContext(), content + "", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "readResFromPlu err, msg : --> " + e.getMessage());
                }
            }
        });
    }


    /*********************
     * 面向接口方式加载
     ********************/
    void loadBeanInterfaceTest() {

        Button btn_1 = (Button) findViewById(R.id.btn_1);
        Button btn_2 = (Button) findViewById(R.id.btn_2);
        Button btn_3 = (Button) findViewById(R.id.btn_3);

        tv = (TextView) findViewById(R.id.tv);

        //普通调用，反射的方式
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Class mLoadClassBean;
                try {
                    mLoadClassBean = classLoader.loadClass("jianqiang.com.plugin1.Bean");
                    Object beanObject = mLoadClassBean.newInstance();

                    Method getNameMethod = mLoadClassBean.getMethod("getName");
                    getNameMethod.setAccessible(true);
                    String name = (String) getNameMethod.invoke(beanObject);

                    tv.setText(name);
                    Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Log.e("DEMO", "msg:" + e.getMessage());
                }
            }
        });

        //带参数调用
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Class mLoadClassBean = classLoader.loadClass("jianqiang.com.plugin1.Bean");
                    Object beanObject = mLoadClassBean.newInstance();

                    IBean bean = (IBean) beanObject;
                    bean.setName("Hello");
                    tv.setText(bean.getName());
                } catch (Exception e) {
                    Log.e("DEMO", "msg:" + e.getMessage());
                }

            }
        });

        //带回调函数的调用
        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Class mLoadClassBean = classLoader.loadClass("jianqiang.com.plugin1.Bean");
                    Object beanObject = mLoadClassBean.newInstance();

                    IBean bean = (IBean) beanObject;

                    ICallback callback = new ICallback() {
                        @Override
                        public void sendResult(String result) {
                            tv.setText(result);
                        }
                    };
                    bean.register(callback);
                } catch (Exception e) {
                    Log.e("DEMO", "msg:" + e.getMessage());
                }

            }
        });


    }


    /**********************
     * 简单的测试加载
     ********************
     */
    void loadBeanTest() {
        Button btn_1 = (Button) findViewById(R.id.btn_1);
        tv = (TextView) findViewById(R.id.tv);
        //普通调用，反射的方式
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //loadBeanTest();

                Class mLoadClassBean;
                try {
                    //获取插件的bean和属性
                    mLoadClassBean = classLoader.loadClass("jianqiang.com.plugin1.Bean");
                    Object beanObject = mLoadClassBean.newInstance();
                    Method getNameMethod = mLoadClassBean.getMethod("getName");
                    getNameMethod.setAccessible(true);
                    String name = (String) getNameMethod.invoke(beanObject);
                    //打印
                    tv.setText(name);
                    Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("DEMO", "msg:" + e.getMessage());
                }
            }
        });

    }


}
