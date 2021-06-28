package cn.ghzn.player.receiver;

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
import static cn.ghzn.player.MyApplication.util;
import static cn.ghzn.player.util.AuthorityUtils.digest;
import static cn.ghzn.player.util.FileUtils.getFilePath;
import static java.lang.Thread.sleep;

/**
 * 此Usb接收器实现对设备接入的监听，获取绝对路径以方法对授权文件，资源文件进行处理。
 */

public class UsbReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbReceiverActivity";

    @Override
    public void onReceive(Context context, Intent intent) {
        //广播有则接受，但需要特定信息才进行特定操作
        //1.监听是否得到U盘目录(是否存在文件)(true跳转import，false返回mainActivity；做出提示)->
        //2.进入import，加入加载页面，执行复制U盘程序，复制完成后判断是否成功复制(true跳转playerActivity，false跳转mainActivity；做出提示)
        //3.进入playerActivity，执行分屏逻辑和动态imageView逻辑；执行完后才退出加载页面，；
        //先执行完程序，如果没问题才跳转到对应的player布局界面
        try {
            if(!app.isReadDeviceState()){//来自ReadDevice的广播就屏蔽，且默认为false；
                util.infoLog(TAG,"检查USB设备是否为U盘的方法","up");
                UsbUtils.checkUsb(context);
            }
        } catch (Exception e) {
            util.varyLog(TAG,e,"Exception e");
        }
        //LogUtils.e(String.valueOf(app));
        if(!"null".equals(String.valueOf(app))){//拔U盘可能回出现莫名全局变量app为null的情况，故屏蔽
            if (app.isImportState()) {//导入USB类型是的是U盘情况下
                util.varyLog(TAG,intent.getAction(),"action ===");
                if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")) {
                    String path = intent.getDataString();
                    util.infoLog(TAG,"U盘接入",null);
                    util.varyLog(TAG,path,"path");
                    //todo：屏蔽来自UsbHelper的readDevice()方法带来的重复挂载广播。判断是来自ReadDevice的广播就屏蔽。
                    if(!app.isReadDeviceState()){
                        //广播真正执行的地方
                        UsbUtils.checkUsbFileForm(context,path);//检查U盘存放的授权文件是否符合格式，符合则跳转
                        app.setUpdateOnceSource(true);//U盘进来，恢复为可更新资源状态，更新一次资源或拔出后置false
                    }
                    app.setReadDeviceState(false);//恢复默认的非屏蔽状态
                }else if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED")) {//U盘拔出
                    if(!app.isReadDeviceState()){
                        //广播真正执行的地方
                        app.setImportState(false);
                        app.setUpdateOnceSource(false);
                    }
                    util.infoLog(TAG,"U盘拔出",null);
                }else if (intent.getAction().equals("android.intent.action.MEDIA_REMOVED")){ // 完全拔出
                    if(!app.isReadDeviceState()){
                        //广播真正执行的地方
                        app.setImportState(false);
                        app.setUpdateOnceSource(false);
                    }
                    util.infoLog(TAG,"U盘完全拔出",null);
                }
            }
        }
    }
}
