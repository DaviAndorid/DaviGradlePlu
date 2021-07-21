package com.sq.mobile.hook_fix_host_one;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sq.mobile.hook_fix_lib_one.IBean;
import com.sq.mobile.hook_fix_lib_one.ICallback;
import com.sq.mobile.hook_fix_lib_one.IDynamic;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "daviAndroid【host】mainActivity，";

    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;
    private String dexpath = null;    //apk文件地址
    private File fileRelease = null;  //释放目录
    private DexClassLoader classLoader = null;

    private String apkName = "plugin1-2.apk";    //apk名称

    TextView tv;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        copyFromAssets(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        loadPlu();

        //testLoadPlu();
        //loadBeanInterfaceTest();
        readResFromPlu();
    }

    void loadPlu() {
        //classLoader 的构建
        File extractFile = this.getFileStreamPath(apkName);
        dexpath = extractFile.getPath();
        fileRelease = getDir("dex", 0); //0 表示Context.MODE_PRIVATE
        Log.d(TAG, "【loadPlu】dexpath: " + dexpath);
        Log.d(TAG, "【loadPlu】fileRelease.getAbsolutePath(): " + fileRelease.getAbsolutePath());
        classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null, getClassLoader());
    }

    /*********************
     * 读取插件里的一个字符串资源
     ********************/
    void readResFromPlu() {
        final String classPlu = "com.sq.mobile.hook_fix_plu_one.Dynamic";

        Button btn_6 = (Button) findViewById(R.id.btn_6);
        final TextView textView = (TextView) findViewById(R.id.tv);

        //带资源文件的调用
        btn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loadResources();
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

    protected void loadResources() {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexpath);
            mAssetManager = assetManager;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "【loadResources】Exception --> " + e.getMessage());
        }

        mResources = new Resources(mAssetManager, super.getResources().getDisplayMetrics(),
                super.getResources().getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(super.getTheme());
    }

    @Override
    public AssetManager getAssets() {
        if (mAssetManager == null) {
            Log.e(TAG, "【getAssets】mAssetManager is null");
            return super.getAssets();
        }

        Log.e(TAG, "【getAssets】mAssetManager is not null");
        return mAssetManager;
    }

    @Override
    public Resources getResources() {
        if (mResources == null) {
            Log.e(TAG, "【getResources】mResources is null");
            return super.getResources();
        }

        Log.e(TAG, "【getResources】mResources is not null");
        return mResources;
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme == null) {
            Log.e(TAG, "【getTheme】Theme is null");
            return super.getTheme();
        }

        Log.e(TAG, "【getTheme】Theme is not null");
        return mTheme;
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


    void copyFromAssets(Context newBase) {
        try {
            /**
             * 把Assets里面得文件复制到 /data/data/files 目录下
             */
            Utils.extractAssets(newBase, apkName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
