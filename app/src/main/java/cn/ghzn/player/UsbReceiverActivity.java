package cn.ghzn.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class UsbReceiverActivity extends BroadcastReceiver {
    private static final String TAG = "UsbReceiverActivity";
    //找到U盘的路径并打印出来
//    private static boolean mActionFlag = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        //1.监听是否得到U盘目录(是否存在文件)(true跳转import，false返回mainActivity；做出提示)->
        //2.进入import，加入加载页面，执行复制U盘程序，复制完成后判断是否成功复制(true跳转playerActivity，false跳转mainActivity；做出提示)
        //3.进入playerActivity，执行分屏逻辑和动态imageView逻辑；执行完后才退出加载页面，；
        //先执行完程序，如果没问题才跳转到对应的player布局界面

        Log.d(TAG,"action === " + intent.getAction());
        if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")) {
            String path = intent.getDataString();
            if (path != null) {
                Log.d(TAG, "U盘接入");
                Toast.makeText(context,"U盘接入，路径为：",Toast.LENGTH_SHORT).show();
                Toast.makeText(context,path,Toast.LENGTH_SHORT).show();
                Log.d(TAG, path);
            } else {
                Toast.makeText(context,"path为空，未接入U盘",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "U盘未接入");
            }
            Intent i = new Intent();
            i.setClass(context, ImportActivity.class);//此处是设置，未跳转
            Bundle bundle = new Bundle();
            bundle.putString("extra_path", path);
            i.putExtras(bundle);
            context.startActivity(i);

        }else if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED")) {//U盘拔出
            // doSomething
            Log.d(TAG,"U盘拔出");
        }else if (intent.getAction().equals("android.intent.action.MEDIA_REMOVED")){ // 完全拔出
            Log.d(TAG,"U盘完全拔出");
        }
    }



//    public static boolean isActionFlag() {
//        return mActionFlag;
//    }
//
//    public void setActionFlag(boolean actionFlag) {
//        mActionFlag = actionFlag;
//    }
}
