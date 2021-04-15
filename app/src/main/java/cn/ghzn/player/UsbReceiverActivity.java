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
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.Constants.LICENCE_NAME;
import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.util.AuthorityUtils.digest;
import static cn.ghzn.player.util.FileUtils.getFilePath;
import static java.lang.Thread.sleep;

public class UsbReceiverActivity extends BroadcastReceiver {//此处命名错误，非activity作用，请注意！！！
    private static final String TAG = "UsbReceiverActivity";
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
                app.setImportState(true);

                Toast.makeText(context,"U盘接入，路径为：",Toast.LENGTH_SHORT).show();
                Toast.makeText(context,path,Toast.LENGTH_SHORT).show();
                Log.d(TAG, path);
                String extraPath = path.replace("file://", "");//去除uri前缀，得到文件路径(绝对路径)
                app.setExtraPath(extraPath + "/Android/data/cn.ghzn.player/files/");//记录U盘中我们新建ghznPlayer的绝对地址
                Log.d(TAG,"this is extraPath" + app.getExtraPath());

                //todo:对搜寻授权码进行更新检查，保持最新的授权码
                File updateLicence = new File(app.getExtraPath() + LICENCE_NAME);
                if (updateLicence.exists()) {
                    Log.d(TAG, "this is 授权码存在 :" + updateLicence.getAbsolutePath());

                    File deleteLicence = new File(app.getLicenceDir() + LICENCE_NAME);
                    if (deleteLicence.exists()) {
                        deleteLicence.delete();
                        Log.d(TAG, "this is 原机器码存在，进行删除 ：" + deleteLicence.delete());
                    }
                    //存在app.getLicenceDir()为null的情况//在重复U盘考入文件播放时会出现
                    if (app.getLicenceDir() == null) {
                        app.setLicenceDir(getFilePath(context, Constants.STOREPATH) + "/");
                        Log.d(TAG,"this is app.getLicenceDir() :" + app.getLicenceDir());
                    }
                    FileUtils.copyFile(updateLicence.getAbsolutePath(), app.getLicenceDir() + LICENCE_NAME);

                    //todo：U盘导入后，先搜寻授权文件-验证mac-分析内容，符合则真状态，不符则假状态;授权文件以","为区分
                    if (FileUtils.readTxt(app.getLicenceDir() + LICENCE_NAME).contains(",")) {
                        Log.d(TAG, "this is 合法授权文件");
                        Toast.makeText(context,"this is 非法授权文件",Toast.LENGTH_SHORT).show();

                        String[] macStrings = FileUtils.readTxt(app.getLicenceDir() + LICENCE_NAME).split(",");
                        if (macStrings[0].equals(digest(MacUtils.getMac(context)))) {//合法文件中mac验证身份正确--正确且符合的授权文件
                            Log.d(TAG, "this is 合法文件中mac验证身份正确");
                            Toast.makeText(context,"this is 合法文件中mac验证身份正确",Toast.LENGTH_SHORT).show();
                            app.setMap(AuthorityUtils.getAuthInfo(macStrings[1]));
//                            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//                            df.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                            app.setStart_time((long) app.getMap().get("startTime"));//存储授权时间信息；暂时不设定Date显示格式
                            app.setEnd_time((long) app.getMap().get("endTime"));

                            //todo:设置显示授权时间和授权失效时间
                            LogUtils.e(app.getAuthority_time());
                            if (app.getAuthority_time().equals("无")) {//第一次初始化--数据库的授权时间默认为无
                                app.setFirst_time(System.currentTimeMillis());//记录第一次导入时本地的时间
                                app.setTime_difference(app.getEnd_time() - app.getStart_time());//记录两时间戳的差值
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                                //todo：这里是授权文件的时间，如果电脑的时间不是准确的，则本地加时间差。
                                if (app.getStart_time() < app.getCreateTime()) {//获取本地时间为准还是以服务器时间为准
                                    Log.d(TAG,"this is 本地时间是服务器时间");
                                    app.setAuthority_time(df.format(new Date(app.getStart_time())));
                                    app.setAuthority_expired(df.format(new Date(app.getEnd_time())));
                                    app.setRelative_time(app.getEnd_time());//将服务器时间设为设为授权到期时间
                                } else {
                                    Log.d(TAG,"this is 本地时间不是服务器时间");
                                    app.setAuthority_time(df.format(new Date(app.getCreateTime())));
                                    app.setAuthority_expired(df.format(new Date(app.getCreateTime() + app.getTime_difference())));
                                    app.setRelative_time(app.getCreateTime() + app.getTime_difference());//将本地时间设为授权到期时间
                                }
                                Log.d(TAG,"this is app.getAuthority_time() :" + app.getAuthority_time());
                                Log.d(TAG,"this is app.getEnd_time() :" + app.getAuthority_expired());
                            }

                            //todo:获取并存储授权信息的内容，再进行对内容的取出，用于判断授权状态以限制其他操作---嵌入跳转功能
                            if (app.getCreate_time() == 0) {//数据库的当前时间默认为0，用于记录每次成功播放资源时的时间
                                Toast.makeText(context,"this is 第一次资源导入",Toast.LENGTH_SHORT).show();
                                app.setAuthority_state(true);//此次已获取授权状态，完成授权文件的更新

                                usbTurnActivity(context, path);
                            } else {////正常情况下，本次导入的节目时间一定比上一次时间大；授权时间一定比当前时间大；避免修改安卓本地时间简易破解授权
                                //app.getRelative_time() > app.getCreate_time() ||多余的相对时间判断
                                LogUtils.e(app.getRelative_time() > app.getCreate_time());//1.防止本地或服务器时间大于授权到期相对时间；
                                if (((app.getCreateTime()-app.getFirst_time()) < app.getTime_difference()) && app.getCreateTime() > app.getSource().getCreate_time()) {//2.第一次导入资源时间与当前时间差<授权时间段；3.当前时间一定大于上一次的当前时间
                                    Toast.makeText(context,"this is 后续资源导入",Toast.LENGTH_SHORT).show();
                                    app.setAuthority_state(true);

                                    usbTurnActivity(context, path);
                                } else {
                                    Log.d(TAG, "this is 后续导入资源时，不在有效授权时间内");
                                    Toast.makeText(context,"this is 后续导入资源时，不在有效授权时间内",Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Log.d(TAG, "this is 非法身份验证或逗号过多的非法授权文件");
                            Toast.makeText(context,"this is 非法身份验证或逗号过多的非法授权文件",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "this is 非法授权文件");
                        Toast.makeText(context,"this is 非法授权文件",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG,"this is 无授权文件，检查授权状态");
                    if (app.getDevice().getAuthority_state()) {//数据库中的授权状态为真才能执行资源读取
                        Log.d(TAG,"this is 无授权文件，存在授权状态");

                        usbTurnActivity(context, path);
                    }
                }
            } else {
                Toast.makeText(context,"path为空，未接入U盘",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "U盘未接入");
            }

//            usbTurnActivity(context, path);

        }else if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED")) {//U盘拔出
            // doSomething
            Log.d(TAG,"U盘拔出");
            app.setImportState(false);
        }else if (intent.getAction().equals("android.intent.action.MEDIA_REMOVED")){ // 完全拔出
            Log.d(TAG,"U盘完全拔出");
            app.setImportState(false);
        }
    }


    public void usbTurnActivity(Context context, String path) {
        app.setCreate_time(app.getCreateTime());//获取当前时间赋值给与数据库相关数据的"当前时间"--作为上次时导入时间
        Intent i = new Intent(context, ImportActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString("extra_path", path);
        i.putExtras(bundle);
        if (path == null) {
            Toast.makeText(context, "path为null,无法跳转",Toast.LENGTH_SHORT).show();
        } else {
            context.startActivity(i);
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
