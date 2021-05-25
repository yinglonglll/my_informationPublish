package cn.ghzn.player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;
import cn.ghzn.player.util.FileUtils;
import cn.ghzn.player.util.ViewImportUtils;

import static java.lang.Thread.sleep;

public class ImportActivity extends Activity {
    private static final String TAG = "ImportActivity";

    private static String mTarget ="";
    private static int filesCount = 0;
    private MyApplication app;
    private DaoManager daoManager = DaoManager.getInstance();//找到单例(唯一数据库对象)
    private static Runnable mRunnable;
    private static Map map1 = new HashMap();//存每次导入进来时里面的U盘文件，
    private boolean mMatch;
    private MainActivity mMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {//监听到U盘的插入，才会执行这个操作，否则和这所有功能等于没有
        super.onCreate(savedInstanceState);
        app = (MyApplication)getApplication();//全局变量池：
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_progress);

        Toast.makeText(this,"加载数据中，请稍等",Toast.LENGTH_LONG).show();
        Intent intent = getIntent();//获取意图
        String extraPath = intent.getExtras().getString("extra_path");
        Log.d(TAG,"extraPath的值为：" + extraPath);
        copyExtraFile(extraPath);//从U盘复制指定目标文件夹到U盘指定目录target；Intent.getdata()得到的uri为String型的filePath，现在将uri的前缀格式去除，则找到路径(用于new File(path))；

        //todo：经历一次分析授权与查询存储文件后，刷新主界面信息。

        Log.d(TAG,"this is renovate mainActivity");
        Intent RenovateIntent = new Intent("cn.ghzn.player.broadcast.RENOVATE_MAIN");
        //intent.setComponent(new ComponentName("cn.ghzn.player","cn.ghzn.player.receive.VarReceiver"));
        sendBroadcast(RenovateIntent);


        LogUtils.e(mMatch);
        if (mMatch) {//需找到有路径，路径为有效
            //fixme:逐个取消图片延迟线程，再finish()掉第一次分屏播放。即不会出现停止后的黑屏，不会出现第二次线程被取消
            if (app.getRunnable1() != null) {
                app.getHandler().removeCallbacks(app.getRunnable1());
            }
            if (app.getRunnable2() != null) {
                app.getHandler().removeCallbacks(app.getRunnable2());
            }
            if (app.getRunnable3() != null) {
                app.getHandler().removeCallbacks(app.getRunnable3());
            }
            if (app.getRunnable4() != null) {
                app.getHandler().removeCallbacks(app.getRunnable4());
            }
            app.setPlayFlag(0);//图片线程：导入新资源时优先触发onPause()，故避免取消线程导致未执行线程里的set true而带来playFlag一直为false的情况，一旦被暂停，即初始化该标志状态。永远保持true
            app.setMediaPlayState(false);//视频监听：不知如何取消视频监听，但通过状态量，使监听后执行无效功能

            if (app.getCurrentActivity() != null) {//即导入分屏资源成功
                LogUtils.e(TAG,app.getCurrentActivity());
                app.getCurrentActivity().finish();//关闭正在播放的资源，准备播放即将导入的资源
                Log.d(TAG,"this is kill curActivity");
            }
            turnActivity(mTarget);//对命名格式，文件夹数量进行检错才跳转
        } else {
            Log.d(TAG,"this is 您的ghznPlayer文件夹内格式不对或不存在ghznPlayer文件夹");//禁止从U盘导入的跳转，如果文件夹为空，那就意味着不存在不对的情况。
            Toast.makeText(this,"您的ghznPlayer文件夹内格式不对或不存在ghznPlayer文件夹",Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public static Runnable getRunnable() {
        return mRunnable;
    }

    public static int getFilesCount() {
        return filesCount;
    }

    public static Map getMap1() {
        return map1;
    }


    private void turnActivity(String mTarget) {
        Log.d(TAG,"this is turnActivity");
        File target = new File(mTarget);//创建复制后的ghznPlayer的对象

        if (!target.exists()) {
            target.mkdir();
        }
        if(target.isDirectory()) {
            Log.d(TAG,"this is target.isDirectory()");
            int turnFlag = 0;
            String fileName = "";
            File[] files = target.listFiles();
            if (files != null && files.length != 0) {
                filesCount = files.length;//循环次数
                Log.d(TAG,"this is filesCount:" + filesCount);
            }else{
                Log.d(TAG,"turnActivity--files:为null");
            }
            if (filesCount != 0 ) {
                switch (filesCount) {
                    case 1:
                        Log.d(TAG,"this is case1");
                        for (int i = 0; i < 1; i++) {
                            fileName = files[i].getName();
                            if (fileName.contains("1-")) {//通过检测A-前缀，判断是否符合分屏格式名，符合即累加符合分屏格式名文件数
                                turnFlag++;
                                Log.d(TAG,"turnFlag:"+ turnFlag );
                            } else {
                                Toast.makeText(this, "跳转一分屏失败，请按照教程方法的格式放入对应的文件", Toast.LENGTH_LONG).show();
                                turnFlag = 0;
                                break;//直接跳出循环
                            }
                        }
                        if (turnFlag == filesCount) {//根据累加的文件数，判断ghznPlayer内的文件是否都符合分屏格式名的格式；
                            Intent intent = new Intent(this, OneSplitViewActivity.class);
                            intent.putExtra("splitView", filesCount);//分屏样式传递
                            intent.putExtra("filesParent", mTarget);//直接将ghzn文件夹地址传递过去，以获取父类file类型
                            Log.d(TAG,"this is if (turnFlag == filesCount)");
                            app.setExtraState(true);
                            startActivity(intent);
                        }
                        break;
                    case 2:
                        for (int i = 0; i < 2; i++) {
                            fileName = files[i].getName();
                            if (fileName.contains("2-")) {//检查两个文件是否都符合命名格式
                                turnFlag++;
                            } else {
                                Toast.makeText(this, "跳转二分屏失败，请按照教程方法的格式放入对应的文件", Toast.LENGTH_LONG).show();
                                turnFlag = 0;
                                break;//直接跳出循环
                            }
                        }
                        if (turnFlag == filesCount) {//全部都是true才能跳转
                            Intent intent = new Intent(this, TwoSplitViewActivity.class);
                            intent.putExtra("splitView", filesCount);//将分屏样式传输过去
                            intent.putExtra("filesParent", mTarget);
                            Log.d(TAG,"this is if (turnFlag == filesCount)");
                            app.setExtraState(true);
                            startActivity(intent);
                        }
                        break;
                    case 3:
                        for (int i = 0; i < 3; i++) {
                            fileName = files[i].getName();
                            if (fileName.contains("3-")) {
                                turnFlag++;
                            } else {
                                Toast.makeText(this, "跳转三分屏失败，请按照教程方法的格式放入对应的文件", Toast.LENGTH_LONG).show();
                                turnFlag = 0;
                                break;
                            }
                        }
                        if (turnFlag == filesCount) {//全部都是true才能跳转
                            Intent intent = new Intent(this,ThreeSplitViewActivity.class);
                            intent.putExtra("splitView", filesCount);//将分屏样式传输过去
                            intent.putExtra("filesParent", mTarget);
                            Log.d(TAG,"this is if (turnFlag == filesCount)");
                            app.setExtraState(true);
                            startActivity(intent);
                        }
                        break;
                    case 4:
                        for (int i = 0; i < 4; i++) {
                            fileName = files[i].getName();
                            if (fileName.contains("4-")) {
                                turnFlag++;
                            } else {
                                Toast.makeText(this, "跳转四分屏失败，请按照教程方法的格式放入对应的文件", Toast.LENGTH_LONG).show();
                                turnFlag = 0;
                                break;
                            }
                        }
                        if (turnFlag == filesCount) {//全部都是true才能跳转
                            Intent intent = new Intent(this, FourSplitViewActivity.class);
                            intent.putExtra("splitView", filesCount);//将分屏样式传输过去
                            intent.putExtra("filesParent", mTarget);
                            Log.d(TAG,"this is if (turnFlag == filesCount)");
                            app.setExtraState(true);
                            startActivity(intent);
                        }
                        break;
                    default:
                        Toast.makeText(this, "请勿放入过多文件，请按照教程方法的格式放入对应的文件", Toast.LENGTH_LONG).show();
                        break;
                }
            } else {
                Log.d(TAG, "this is 子文件夹数量为0");
                Toast.makeText(this,"ghznPlayer文件夹内没有文件",Toast.LENGTH_SHORT).show();
                //todo:执行更新授权状态信息：


            }
        }
    }

    private boolean copyFiles(String source,String target){//通过单个文件copyFile()来逐个复制以实现复制目录内所有内容；
        File root = new File(source);//要复制的目录
        if (!root.exists()) {//确定U盘中ghznPlayer文件是存在的
            root.mkdir();
        }

        File file = new File(target);//先删除，再复制，即若存在目标文件，则删除，再执行赋值操作
        ViewImportUtils.deleteFile(file);

        File[] currentFiles = root.listFiles();
//        File targetDir = new File(target);
        Log.d(TAG, source);
        if (currentFiles != null) {
            for (File currentFile : currentFiles) {
                if (currentFile.isDirectory())//如果当前项为子目录 进行递归
                {
                    copyFiles(currentFile.getPath() + "/", target +"/"+ currentFile.getName() + "/");
                    Log.d(TAG, "copyFiles_" + currentFile.getName() + "_为子目录，可进行递归");
                } else //如果当前项为文件则进行文件拷贝
                {
                    boolean copyFileState = FileUtils.copyFile(currentFile.getPath(), target + currentFile.getName());
                    Log.d(TAG,"currentFile.getPath():" + currentFile.getPath() +"************" + "target + currentFile.getName()" + target + currentFile.getName());
                    Log.d(TAG, "copyFiles_" + currentFile.getName() + "_为文件，可进行复制" + copyFileState);
                }
            }
            return true;
        } else {
            Log.d(TAG,"currentFiles为null");
            return false;
        }
    }

    private void copyExtraFile(String path){
        String extraPath = path.replace("file://", "");//去除uri前缀，得到文件路径(绝对路径)
        Log.d(TAG,"extraPath去除url前缀的值为：" + extraPath);

        File extraDirectory = new File(extraPath);//找到U盘的对象
        if (!extraDirectory.exists()) {
            extraDirectory.mkdir();
        }
        if(extraDirectory.isDirectory()){
            File[] files = extraDirectory.listFiles();//查找给定目录中的所有文件和目录(listFiles()得到的结果类似相对路径)
            LogUtils.e(files);
            //初始化状态变量
            mMatch = false;//无ghznPlayer文件夹则默认false
            String source = "";
            mTarget = "";
            if (files != null&& files.length != 0) {
                for(File file : files){
                    if (file.getName().equals("ghznPlayer")) {//从U盘路径中找到我们放入的文件夹ghznPlayer，以找到文件夹的路径
                        Log.d(TAG, "find extra program:" + file.getAbsolutePath());
                        mMatch = true;//标志找到--找到ghznPlayer文件路径
                        source = file.getAbsolutePath();//U盘存放目标文件ghznPlayer的绝对路径
                        int uFileCount;

                        //todo：自定义--复制之前先检查对象是否合乎U盘A-B-C的规定，不合乎则设此路径无效，即找了也没用
                        if (source != "") {//找到文件路径时
                            File uf = new File(source);//U盘file文件--ghznPlayer对象
                            if (!uf.exists()) {
                                uf.mkdirs();
                            }
                            File[] ufs = uf.listFiles();//存放多个A-B-C文件夹对象的数组
                            uFileCount = ufs.length;//取子文件数

                            Log.d(TAG,"this is uFileCount :" + uFileCount);

                            if (uFileCount != 0) {//非空情况下，遍历检测每个子文件夹的命名是否符合分屏
                                String B1 = null;
                                int CSum = 0;
                                int count = 0;
                                for (File son_ufs : ufs) {//A-B-C文件夹对象
                                    String[] uss = son_ufs.getName().split("\\-");//将每个名字拆分
                                    if (B1 == null) {//取第一次为参照物对比第二次第三次的值
                                        B1 = uss[1];
                                    }
                                    //why is 循环检查A-B-c的文件命名是否符合
                                    if (!uss[0].equals(String.valueOf(uFileCount))) {//只要有一个命名不符我的规定,设找到的路径为无效路径
                                        //mMatch = false;
                                        //Intent ci = new Intent(this,app.getCurrentActivity().getClass());
                                        Log.d(TAG,"this is 找到的错误文件命名格式A，即将跳转回原先播放的activity");
                                        returnOriginalActivity();
                                        //Log.d(TAG,"this is 找到的文件路径是否有效？" + mMatch);
                                        //startActivity(ci);//不符文件则跳转到上次有效的当前activity重新读取
                                        break;
                                    }
                                    if (!uss[1].equals(B1)) {
                                        Log.d(TAG,"this is 找到的错误文件命名格式B，即将跳转回原先播放的activity");
                                        returnOriginalActivity();
                                        break;
                                    }
                                    CSum += Integer.parseInt(uss[2]);//终端的分屏模式子文件并不是有序排列，故需检查。
                                    count++;
                                    if(count == uFileCount){
                                        switch (CSum){
                                            case 1:
                                                if(uFileCount == 1){//case 1仅仅代表总和正确，还需对应文件书正确
                                                    Log.d(TAG,"this is 一分屏的C命名正确");
                                                }else{
                                                    Log.d(TAG,"this is 找到的错误文件命名格式C，即将跳转回原先播放的activity");
                                                    returnOriginalActivity();
                                                }
                                                break;
                                            case 3:
                                                if(uFileCount == 2){
                                                    Log.d(TAG,"this is 二分屏的C命名正确");
                                                }else{
                                                    returnOriginalActivity();
                                                    Log.d(TAG,"this is 找到的错误文件命名格式C，即将跳转回原先播放的activity");
                                                }
                                                break;
                                            case 6:
                                                if (uFileCount == 3) {
                                                    Log.d(TAG, "this is 三分屏的C命名正确");
                                                } else {
                                                    returnOriginalActivity();
                                                    Log.d(TAG,"this is 找到的错误文件命名格式C，即将跳转回原先播放的activity");
                                                }
                                                break;
                                            case 10:
                                                if (uFileCount == 4) {
                                                    Log.d(TAG, "this is 四分屏的C命名正确");
                                                } else {
                                                    returnOriginalActivity();
                                                    Log.d(TAG,"this is 找到的错误文件命名格式C，即将跳转回原先播放的activity");
                                                }
                                                break;
                                            default:
                                                Log.d(TAG,"this is 找到的错误文件命名格式C，即将跳转回原先播放的activity");
                                                returnOriginalActivity();
                                                break;
                                        }
                                    }
                                   /* if (!uss[2].equals(String.valueOf(C++))) {//存在命名模式C在中断设备不是以顺序排序的方式进行排序
                                        Log.d(TAG,"this is 找到的错误文件命名格式C，即将跳转回原先播放的activity");
                                        returnOriginalActivity();
                                        break;
                                    }*/
                                    //todo:自定义--对子文件夹资源中全部的资源后缀名6种进行检查，如图片，jpg，png，jpeg;视频：MP4，avi，3gp。
                                    File[] son_ufss = son_ufs.listFiles();
                                    if (son_ufss != null) {
                                         for (File sons_ufss : son_ufss) {//A-B-C文件夹里单个资源的对象

                                            if (!(sons_ufss.getName().endsWith("jpg") ||sons_ufss.getName().endsWith("jpeg")
                                                    ||sons_ufss.getName().endsWith("png")||sons_ufss.getName().endsWith("mp4")
                                                    || sons_ufss.getName().endsWith("avi") || sons_ufss.getName().endsWith("3gp"))) {
                                                returnOriginalActivity();//该方法的原先注释出自于本处
                                                break;
                                            }
                                         }
                                         if (!mMatch) {//本次break退出外循环
                                             break;
                                         }
                                    }
                                }
                            }


                        }

                        Log.d(TAG,"source的值为：" + source);
                        mTarget = FileUtils.getFilePath(this, Constants.STOREPATH) + "/" + file.getName();//方法返回String类型，拼起来就是完整的复制目标地址,创建目标文件夹
                        Log.d(TAG,"mTarget的值为：" + mTarget);
                        break;
                    } else {
                        Log.d(TAG, "Not find extra program:");
                        //Toast.makeText(this,"没有找到ghznPlayer文件夹",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if(mMatch){//标志找到后需复制的动作,复制之前先把原有的删除了
                boolean success = false;
                success = copyFiles(source, mTarget);//通过打开输入/出通道，执行读写复制
                if(success){//复制的动作执行成功
                    Log.d(TAG,"copy to:"+ mTarget + " " + success);
                }else{
                    Log.d(TAG,"复制失败，即将跳转主界面...");
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    app.setSetSourcePlayer(false);//当文件格式不正确，或复制文件失败时，跳转回MainActivity都重新恢复为初始状态。
                    app.setRelative_time(0);
                    Intent i = new Intent(this,MainActivity.class);
                    startActivity(i);
                }
            }
        }
    }

    /**
     * @description:
     * 当出现资源文件的格式不正确或资源格式文件正确时，复制失败时(几乎不常见)，恢复最初的状态，即上一次播放失败，设create_time为0；
     * 有currentActivity则跳转回去，无则默认原本的MainActivity
     */
    private void returnOriginalActivity() {
        mMatch = false;//不是以上述格式为结尾的。设此路径为无效路径
        app.setCreate_time(0);
        Intent ci;//第一次放入资源时，此时软件是没有分屏记录的，故先跳转到mainActivity
        if(app.getCurrentActivity() != null) {//非第一次放入错误后缀格式文件
            ci = new Intent(this, app.getCurrentActivity().getClass());
            //Log.d(TAG, "this is 找到的错误文件命名格式A-B-C-D.后缀格式，即将跳转回原先播放的activity");
            Log.d(TAG, "this is 找到的文件路径是否有效？" + mMatch);
            startActivity(ci);//不符文件则跳转到上次有效的当前activity重新读取
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
