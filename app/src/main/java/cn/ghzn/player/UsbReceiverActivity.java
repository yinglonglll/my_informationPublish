package cn.ghzn.player;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;
import cn.ghzn.player.util.AuthorityUtils;
import cn.ghzn.player.util.FileUtils;
import cn.ghzn.player.util.InfoUtils;
import cn.ghzn.player.util.MacUtils;
import cn.ghzn.player.util.UsbUtils;
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.Constants.LICENCE_NAME;
import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.util.AuthorityUtils.digest;
import static cn.ghzn.player.util.FileUtils.getFilePath;
import static java.lang.Thread.sleep;

public class UsbReceiverActivity extends BroadcastReceiver {//此处命名错误，非activity作用，请注意！！！
    private static final String TAG = "UsbReceiverActivity";

    @Override
    public void onReceive(Context context, Intent intent) {
        //广播有则接受，但需要特定信息才进行特定操作
        //1.监听是否得到U盘目录(是否存在文件)(true跳转import，false返回mainActivity；做出提示)->
        //2.进入import，加入加载页面，执行复制U盘程序，复制完成后判断是否成功复制(true跳转playerActivity，false跳转mainActivity；做出提示)
        //3.进入playerActivity，执行分屏逻辑和动态imageView逻辑；执行完后才退出加载页面，；
        //先执行完程序，如果没问题才跳转到对应的player布局界面
        try {
            UsbUtils.checkUsb(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG,"this is test for ImportState");
        LogUtils.e(String.valueOf(app));
        if(!"null".equals(String.valueOf(app))){
            if (app.isImportState()) {//导入USB类型是的是U盘情况下
                Log.d(TAG,"action === " + intent.getAction());
                if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")) {//系统广播，无法自行发送，权限不够

                    String path = intent.getDataString();
                    Log.d(TAG,"this is path" + path);
                    UsbUtils.checkUsbFileForm(context,path);//检查U盘存放的授权文件是否符合格式，符合则跳转
                }else if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED")) {//U盘拔出
                    Log.d(TAG,"U盘拔出");
                    app.setImportState(false);
                }else if (intent.getAction().equals("android.intent.action.MEDIA_REMOVED")){ // 完全拔出
                    Log.d(TAG,"U盘完全拔出");
                    app.setImportState(false);
                }
            }
        }
    }
}
