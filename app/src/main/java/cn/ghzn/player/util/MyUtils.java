package cn.ghzn.player.util;

import android.util.Log;

/**
 *
 */
public class MyUtils {
    private static final String TAG = "MyUtil";
    private static MyUtils mInstance;
    public MyUtils() {
    }
    public static MyUtils getInstance(){
        if(null == mInstance)
        {
            synchronized (MyUtils.class) {//保证异步处理安全操作
                if (null == mInstance) {
                    mInstance = new MyUtils();
                }
            }
        }
        return mInstance;
    }

    /*打印方法变量*/
    public void varyLog(String TAG,Object obj,String string){//以this is来检索调试信息
            Log.d(TAG,"this is "+ string + " : " + obj);
    }
    /*打印中文信息*/
    public void infoLog(String TAG,String info,String state){
        try {
            switch (state){
                case "up":
                    Log.i(TAG,"this is "+ "“"+ info +"”"+ "之前" );//用于方法执行之前
                    break;
                case "down":
                    Log.i(TAG,"this is "+ "“"+ info +"”"+ "之后" );//用于方法执行之后
                    break;
                case "middle":
                    Log.i(TAG,"this is "+ "“"+ info +"”"+ "之中" );//用于方法执行之中
                    break;
                default:
                    Log.i(TAG,"this is "+ "“"+ info +"”" );//默认null
                    break;
            }
        } catch (Exception e) {
            Log.i(TAG,"this is "+ "“"+ info +"”" );//默认null
        }
    }
}
