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
                app.setExtraPath(extraPath + "/Android/data/cn.ghzn.player/files/ghzn/");//记录U盘中我们新建ghznPlayer的绝对地址
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
                    FileUtils.copyFile(updateLicence.getAbsolutePath(), app.getLicenceDir() + LICENCE_NAME);

                    //todo：U盘导入后，先搜寻授权文件-验证mac-分析内容，符合则真状态，不符则假状态;授权文件以","为区分
                    if (FileUtils.readTxt(app.getLicenceDir() + LICENCE_NAME).contains(",")) {
                        Log.d(TAG, "this is 合法授权文件");
                        String[] macStrings = FileUtils.readTxt(app.getLicenceDir() + LICENCE_NAME).split(",");
                        if (macStrings[0].equals(digest(MacUtils.getMac(context)))) {//合法文件中mac验证身份正确
                            Log.d(TAG, "this is 合法文件中mac验证身份正确");
                            app.setMap(AuthorityUtils.getAuthInfo(macStrings[1]));

//                            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//                            df.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

                            app.setStart_time((long) app.getMap().get("startTime"));//存储授权时间信息；暂时不设定Date显示格式
                            app.setEnd_time((long) app.getMap().get("endTime"));
                            LogUtils.e(app.getStart_time());
                            LogUtils.e(app.getEnd_time());
                            LogUtils.e(app.getMap());

                            //todo:获取并存储授权信息的内容，再进行对内容的取出，用于判断授权状态以限制其他操作---嵌入跳转功能
                            if (app.getCreate_time() == 0) {//第一次导入资源时，数据库的当前时间为默认值为0
                                usbTurnActivity(context, path);
                            } else {////正常情况下，本次导入的节目时间一定比上一次时间大；授权时间一定比当前时间大；避免修改安卓本地时间简易破解授权
                                if ((Long) app.getMap().get("endTime") > app.getCreate_time() && app.getCreateTime() > app.getSource().getCreate_time()) {
                                    app.getSource().setCreate_time(app.getCreateTime());
                                    usbTurnActivity(context, path);
                                } else {
                                    Log.d(TAG, "this is 后续导入资源时，不在有效授权时间内");
                                }
                            }
                        } else {
                            Log.d(TAG, "this is 非法身份验证或逗号过多的非法授权文件");
                        }
                    } else {
                        Log.d(TAG, "this is 非法授权文件");
                    }
                } else {
                    Log.d(TAG,"updateLicence.exists() ：不存在");
                }
            } else {
                Toast.makeText(context,"path为空，未接入U盘",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "U盘未接入");
            }

            //获取U盘路径后，先把对U盘路径下的license文件进行检测，有效则进行替换覆盖，跳转之前先进行授权判断
//            LogUtils.e(app.getSource());
//            Log.d(TAG,"this is app.getLicenseDir() 》》》》》》》》》》》" + app.getLicenceDir());
//            Log.d(TAG,"this is FileUtils.readTxt(app.getLicenseDir()) 》》》》》》》》》》》" + FileUtils.readTxt(app.getLicenceDir()));
//            String[] strings = FileUtils.readTxt(app.getLicenceDir()).split(",");
//            app.setMap(AuthorityUtils.getAuthInfo(strings[1]));//手动替换：从路径的文件取出文件内容,再将内容拿给getAuth方法截取信息存储在map中
//            LogUtils.e(app.getMap());
//            if (strings[0].equals(digest(MacUtils.getMac(context)))) {//已授权，进行导入跳转
//                Log.d(TAG,"this is 已授权");
//
//            } else {
//                Log.d(TAG,"this is 未授权");
//                Log.d(TAG,"this is strings[0] :" + strings[0]);
//                Log.d(TAG,"this is  digest(MacUtils.getMac(context)) :" + digest(MacUtils.getMac(context)));
//            }
//            //todo：跳转部分只有在授权期内(真状态)才进行
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


    private void usbTurnActivity(Context context, String path) {
        Intent i = new Intent(context, ImportActivity.class);
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
