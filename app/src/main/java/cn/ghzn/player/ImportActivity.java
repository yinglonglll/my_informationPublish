package cn.ghzn.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    public static Runnable getRunnable() {
        return mRunnable;
    }

    public static int getFilesCount() {
        return filesCount;
    }

    public static Map getMap1() {
        return map1;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {//监听到U盘的插入，才会执行这个操作，否则和这所有功能等于没有
        super.onCreate(savedInstanceState);
        app = (MyApplication)getApplication();//全局变量池：
        setContentView(R.layout.activity_progress);

        Toast.makeText(this,"加载数据中，请稍等",Toast.LENGTH_LONG).show();
        Intent intent = getIntent();//获取意图
        String extraPath = intent.getExtras().getString("extra_path");
        Log.d(TAG,"extraPath的值为：" + extraPath);
        copyExtraFile(extraPath);//从U盘复制指定目标文件夹到U盘指定目录target；Intent.getdata()得到的uri为String型的filePath，现在将uri的前缀格式去除，则找到路径(用于new File(path))；

        if (mMatch) {//需找到有路径，路径为有效
            if (app.getCurrentActivity() != null) {//即导入分屏资源成功
                LogUtils.e(TAG,app.getCurrentActivity());
                app.getCurrentActivity().finish();//关闭正在播放的资源，准备播放即将导入的资源
                Log.d(TAG,"this is kill curActivity");
            }
            turnActivity(mTarget);//对命名格式，文件夹数量进行检错才跳转
        } else {
            Log.d(TAG,"this is 您的ghznPlayer文件夹内格式不对");//禁止从U盘导入的跳转
            Toast.makeText(this,"您的ghznPlayer文件夹内格式不对",Toast.LENGTH_SHORT).show();
        }
        finish();
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
            mMatch = false;
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
                            File uf = new File(source);
                            if (!uf.exists()) {
                                uf.mkdirs();
                            }
                            File[] ufs = uf.listFiles();
                            uFileCount = ufs.length;//取子文件数
                            Log.d(TAG,"this is uFileCount :" + uFileCount);

                            if (uFileCount != 0) {//非空情况下，遍历检测每个子文件夹的命名是否符合分屏
                                String B1 = null;
                                int C = 1;
                                for (File son_ufs : ufs) {
                                    String[] uss = son_ufs.getName().split("\\-");//将每个名字拆分
                                    if (B1 == null) {
                                        B1 = uss[1];//取第一次为参照物对比第二次第三次的值
                                    }
                                    //why is 循环检查A-B-c的文件命名是否符合
                                    if (!uss[0].equals(String.valueOf(uFileCount))) {//只要有一个命名不符我的规定,设找到的路径为无效路径
                                        mMatch = false;
                                        Intent ci = new Intent(this,app.getCurrentActivity().getClass());
                                        Log.d(TAG,"this is 找到的错误文件命名格式A，即将跳转回原先播放的activity");
                                        Log.d(TAG,"this is 找到的文件路径是否有效？" + mMatch);
                                        startActivity(ci);//不符文件则跳转到上次有效的当前activity重新读取
                                        break;
                                    }
                                    if (!uss[1].equals(B1)) {
                                        mMatch = false;
                                        Intent ci = new Intent(this,app.getCurrentActivity().getClass());
                                        Log.d(TAG,"this is 找到的错误文件命名格式B，即将跳转回原先播放的activity");
                                        Log.d(TAG,"this is 找到的文件路径是否有效？" + mMatch);
                                        startActivity(ci);//不符文件则跳转到上次有效的当前activity重新读取
                                        break;
                                    }
                                    if (!uss[2].equals(String.valueOf(C++))) {
                                        mMatch = false;
                                        Intent ci = new Intent(this,app.getCurrentActivity().getClass());
                                        Log.d(TAG,"this is 找到的错误文件命名格式C，即将跳转回原先播放的activity");
                                        Log.d(TAG,"this is 找到的文件路径是否有效？" + mMatch);
                                        startActivity(ci);//不符文件则跳转到上次有效的当前activity重新读取
                                        break;
                                    }
                                    //todo:自定义--对子文件夹资源中全部的资源后缀名6种进行检查，如图片，jpg，png，jpeg;视频：MP4，avi，3gp。
                                    File[] son_ufss = son_ufs.listFiles();
                                    if (son_ufss != null) {
                                         for (File sons_ufss : son_ufss) {

                                            if (!(sons_ufss.getName().endsWith("jpg") ||sons_ufss.getName().endsWith("jpeg")
                                                    ||sons_ufss.getName().endsWith("png")||sons_ufss.getName().endsWith("mp4")
                                                    || sons_ufss.getName().endsWith("avi") || sons_ufss.getName().endsWith("3gp"))) {
                                                mMatch = false;
                                                Intent ci;//第一次放入资源时，此时软件是没有分屏记录的，故先跳转到mainActivity
                                                if(app.getCurrentActivity() != null) {//非第一次放入错误后缀格式文件
                                                    ci = new Intent(this, app.getCurrentActivity().getClass());
                                                    Log.d(TAG, "this is 找到的错误文件命名格式A-B-C-D.后缀格式，即将跳转回原先播放的activity");
                                                } else {//第一次放入后缀错误文件在U盘
                                                    ci = new Intent(this, MainActivity.class);
                                                    Log.d(TAG, "this is 第一次放入后缀错误文件在U盘，找到的错误文件命名格式A-B-C-D.后缀格式");
                                                }
                                                Log.d(TAG, "this is 找到的文件路径是否有效？" + mMatch);
                                                startActivity(ci);//不符文件则跳转到上次有效的当前activity重新读取
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
                    Intent i = new Intent(this,MainActivity.class);
                    startActivity(i);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
