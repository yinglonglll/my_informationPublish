package cn.ghzn.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.util.FileUtils;
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.Constants.SINGLE_PLAYER_NAME;
import static cn.ghzn.player.MyApplication.mSource;
import static cn.ghzn.player.MyApplication.single;
import static cn.ghzn.player.MyApplication.util;
import static java.lang.Thread.sleep;

public class ImportActivity extends Activity {
    private static final String TAG = "ImportActivity";

    private static String mTargetFolders ="";
    private static String mSinglePlayerFolders ="";
    private static int filesCount = 0;
    private MyApplication app;
    private DaoManager daoManager = DaoManager.getInstance();//找到单例(唯一数据库对象)
    private static Runnable mRunnable;
    private static Map<String,String> map1 = new HashMap<>();//存每次导入进来时里面的U盘文件，
    private boolean mMatch;
    private ArrayList<File> arrayList =  new ArrayList<>();;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {//监听到U盘的插入，才会执行这个操作，否则和这所有功能等于没有
        super.onCreate(savedInstanceState);
        app = (MyApplication)getApplication();//全局变量池：
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_progress);

        //todo：经历一次分析授权刷新主界面信息。
        Intent RenovateIntent = new Intent("cn.ghzn.player.broadcast.RENOVATE_MAIN");
        sendBroadcast(RenovateIntent);

        Toast.makeText(this,"加载数据中，请稍等",Toast.LENGTH_LONG).show();
        Intent intent = getIntent();//获取意图
        String extraPath = intent.getExtras().getString("extra_path");
        util.varyLog(TAG,extraPath,"extraPath的值为");

        /*授权状态下，逐个取消图片延迟线程，再finish()掉第一次分屏播放。即不会出现停止后的黑屏，不会出现第二次线程被取消*/
        if(app.isAuthority_state()){//处于授权状态下才允许资源文件的更新
            copyExtraFile(extraPath);//从U盘复制指定目标文件夹到U盘指定目录target；Intent.getdata()得到的uri为String型的filePath，现在将uri的前缀格式去除，则找到路径(用于new File(path))；
            LogUtils.e(mMatch);//打印复制结果
            //todo：对单屏模式和多屏模式进行分类
            if(app.getMode_() == 0){
                if(single.getSingle_view()!=null && single.getSingle_Son_source()!=null){
                    util.infoLog(TAG,"进入到单屏模式",null);
                    cancelPreviousPlayer();
                    Intent singleIntent = new Intent(this, SingleSplitViewActivity.class);
                    startActivity(singleIntent);
                }
            }else{
                util.varyLog(TAG,mMatch,"是否找到有效ghznPlayer文件夹mMatch");
                if (mMatch) {
                    cancelPreviousPlayer();
                    util.infoLog(TAG,"进入到多屏模式",null);
                    turnActivity(mTargetFolders);//对命名格式进行校验，对分屏模式进行跳转
                } else {
                    util.infoLog(TAG,"您的ghznPlayer文件夹内格式不对或不存在ghznPlayer文件夹",null);//禁止从U盘导入的跳转，如果文件夹为空，那就意味着不存在不对的情况。
                    Toast.makeText(this,"您的ghznPlayer文件夹内格式不对或不存在ghznPlayer文件夹",Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            if(mSource == null){
                app.setCreate_time(0);
                util.infoLog(TAG,"此时处于非授权状态下，无法更新资源文件",null);
            }
        }
        finish();
    }

    private void cancelPreviousPlayer() {
        util.infoLog(TAG,"关闭线程播放",null);
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
        /*关闭分屏播放*/
        if (app.getCurrentActivity() != null) {//即导入分屏资源成功
            LogUtils.e(TAG,app.getCurrentActivity());
            app.getCurrentActivity().finish();//关闭正在播放的资源，准备播放即将导入的资源
            Log.d(TAG,"this is kill curActivity");
        }
    }

    private void turnActivity(String mTarget) {
        util.infoLog(TAG,"进入turnActivity()--",null);
        File target = new File(mTarget);//创建复制后的ghznPlayer的对象
        if (!target.exists()) {
            target.mkdir();
        }
        if(target.isDirectory()) {
            int turnFlag = 0;
            String fileName = "";
            File[] files = target.listFiles();
            if (files != null && files.length != 0) {
                filesCount = files.length;//循环次数
                util.varyLog(TAG,filesCount,"filesCount");
            }else{
                util.infoLog(TAG,"turnActivity--files:为null",null);
            }
            if (filesCount != 0 ) {
                switch (filesCount) {
                    case 1:
                        Log.d(TAG,"this is case1");
                        for (int i = 0; i < 1; i++) {
                            fileName = files[i].getName();
                            if (fileName.contains("1-")) {//通过检测A-前缀，判断是否符合分屏格式名，符合即累加符合分屏格式名文件数
                                turnFlag++;
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
                            util.infoLog(TAG,"ghznPlayer内子文件数和分屏数相同，进入一分屏模式",null);
                            //app.setExtraState(true);
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
                            util.infoLog(TAG,"ghznPlayer内子文件数和分屏数相同，进入二分屏模式",null);
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
                            util.infoLog(TAG,"ghznPlayer内子文件数和分屏数相同，进入三分屏模式",null);
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
                            util.infoLog(TAG,"ghznPlayer内子文件数和分屏数相同，进入四分屏模式",null);
                            startActivity(intent);
                        }
                        break;
                    default:
                        util.varyLog(TAG,filesCount,"filesCount");
                        Toast.makeText(this, "请勿放入过多文件，请按照教程方法的格式放入对应的文件", Toast.LENGTH_LONG).show();
                        break;
                }
            } else {
                util.infoLog(TAG,"子文件夹数量为0",null);
                Toast.makeText(this,"ghznPlayer文件夹内没有文件",Toast.LENGTH_SHORT).show();
                //todo:执行更新授权状态信息：

            }
        }
    }

    /**
     * @description:
     * 当出现资源文件的格式不正确或资源格式文件正确时，复制失败时(几乎不常见)，恢复最初的状态，即上一次播放失败，设create_time为0；
     * 有currentActivity则跳转回去，无则默认原本的MainActivity
     */
    private void returnOriginalActivity() {
        util.infoLog(TAG,"返回原先的状态(Activity)",null);
        mMatch = false;//不是以上述格式为结尾的。设此路径为无效路径
        app.setCreate_time(0);
        Intent ci;//第一次放入资源时，此时软件是没有分屏记录的，故先跳转到mainActivity
        if(app.getCurrentActivity() != null) {//非第一次放入错误后缀格式文件
            ci = new Intent(this, app.getCurrentActivity().getClass());
            //Log.d(TAG, "this is 找到的错误文件命名格式A-B-C-D.后缀格式，即将跳转回原先播放的activity");
            util.varyLog(TAG,mMatch,"找到的文件路径是否有效mMatch");
            startActivity(ci);//不符文件则跳转到上次有效的当前activity重新读取
        }
    }

    /**
     *以下为非主要方法
     */
    public static Map<String,String> getMap1() {
        return map1;
    }

    private boolean copyFiles(String source,String target){//通过单个文件copyFile()来逐个复制以实现复制目录内所有内容；
        util.infoLog(TAG,"进入复制文件夹方法",null);
        File root = new File(source);//要复制的目录--U盘中文件的目录--ghznPlayer文件夹目录
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
                    Log.d(TAG, "this is copyFiles_" + currentFile.getName() + "_为子目录，可进行递归");
                } else //如果当前项为文件则进行文件拷贝
                {
                    boolean copyFileState = FileUtils.copyFile(currentFile.getPath(), target + currentFile.getName());
                    Log.d(TAG,"currentFile.getPath():" + currentFile.getPath() +"************"
                                 + "target + currentFile.getName()" + target + currentFile.getName());
                    Log.d(TAG, "this is copyFiles_" + currentFile.getName() + "_为文件，可进行复制" + copyFileState);
                }
            }
            return true;
        } else {
            util.infoLog(TAG,"currentFiles为null",null);
            return false;
        }
    }

    private void copyExtraFile(String path){
        util.infoLog(TAG,"进入先检查资源文件，后复制资源文件的方法",null);
        String extraPath = path.replace("file://", "");//去除uri前缀，得到U盘文件路径(绝对路径)
        util.varyLog(TAG,extraPath,"extraPath去除url前缀的值为");

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
            mTargetFolders = "";
            if (files != null&& files.length != 0) {
                for(File file : files){//遍历U盘根目录文件
                    if (file.getName().equals("ghznPlayer")) {//1.从U盘路径中找到我们放入的文件夹ghznPlayer并进行检验格式和复制到本地
                        util.infoLog(TAG,file.getAbsolutePath(),"find extra program");
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
                            util.varyLog(TAG,uFileCount,"uFileCount");

                            if (uFileCount != 0) {//非空情况下，遍历检测每个子文件夹的命名是否符合分屏
                                String B1 = null;
                                int CSum = 0;
                                int count = 0;
                                util.infoLog(TAG,"循环检查A-B-c的文件命名",null);
                                for (File son_ufs : ufs) {//A-B-C文件夹对象
                                    String[] uss = son_ufs.getName().split("\\-");//将每个名字拆分
                                    if (B1 == null) {//取第一次为参照物对比第二次第三次的值
                                        B1 = uss[1];
                                    }
                                    if (!uss[0].equals(String.valueOf(uFileCount))) {//只要有一个命名不符我的规定,设找到的路径为无效路径
                                        //mMatch = false;
                                        //Intent ci = new Intent(this,app.getCurrentActivity().getClass());
                                        util.infoLog(TAG,"找到的错误文件命名格式A，即将跳转回原先播放的activity",null);
                                        returnOriginalActivity();
                                        //Log.d(TAG,"this is 找到的文件路径是否有效？" + mMatch);
                                        //startActivity(ci);//不符文件则跳转到上次有效的当前activity重新读取
                                        break;
                                    }
                                    if (!uss[1].equals(B1)) {
                                        util.infoLog(TAG,"找到的错误文件命名格式B，即将跳转回原先播放的activity",null);
                                        returnOriginalActivity();
                                        break;
                                    }
                                    CSum += Integer.parseInt(uss[2]);//终端的分屏模式子文件并不是有序排列，故需检查。
                                    count++;
                                    if(count == uFileCount){
                                        switch (CSum){
                                            case 1:
                                                if(uFileCount == 1){//case 1仅仅代表总和正确，还需对应文件书正确
                                                    util.infoLog(TAG,"一分屏的C命名正确",null);
                                                }else{
                                                    util.infoLog(TAG,"找到的错误文件命名格式C，即将跳转回原先播放的activity",null);
                                                    returnOriginalActivity();
                                                }
                                                break;
                                            case 3:
                                                if(uFileCount == 2){
                                                    util.infoLog(TAG,"二分屏的C命名正确",null);
                                                }else{
                                                    returnOriginalActivity();
                                                    util.infoLog(TAG,"找到的错误文件命名格式C，即将跳转回原先播放的activity",null);
                                                }
                                                break;
                                            case 6:
                                                if (uFileCount == 3) {
                                                    util.infoLog(TAG,"三分屏的C命名正确",null);
                                                } else {
                                                    returnOriginalActivity();
                                                    util.infoLog(TAG,"找到的错误文件命名格式C，即将跳转回原先播放的activity",null);
                                                }
                                                break;
                                            case 10:
                                                if (uFileCount == 4) {
                                                    util.infoLog(TAG,"四分屏的C命名正确",null);
                                                } else {
                                                    returnOriginalActivity();
                                                    util.infoLog(TAG,"找到的错误文件命名格式C，即将跳转回原先播放的activity",null);
                                                }
                                                break;
                                            default:
                                                util.infoLog(TAG,"找到的错误文件命名格式C，即将跳转回原先播放的activity",null);
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
                                    LogUtils.e(son_ufss.length);
                                    if(son_ufss.length == 0 ){
                                        returnOriginalActivity();
                                        break;
                                    }
                                    if (son_ufss != null) {
                                         for (File sons_ufss : son_ufss) {//A-B-C文件夹里单个资源的对象
                                             LogUtils.e(sons_ufss);
                                            if (!(sons_ufss.getName().endsWith("jpg") ||sons_ufss.getName().endsWith("jpeg")
                                                    ||sons_ufss.getName().endsWith("png")||sons_ufss.getName().endsWith("mp4")
                                                    || sons_ufss.getName().endsWith("avi") || sons_ufss.getName().endsWith("3gp"))) {
                                                returnOriginalActivity();//该方法的原先注释出自于本处
                                                break;
                                            }
                                         }
                                         util.infoLog(TAG,"退出外循环",null);
                                         /*if (!mMatch) {//本次break退出外循环
                                             break;
                                         }*/
                                    }else{
                                        returnOriginalActivity();
                                        LogUtils.e(mMatch);
                                        break;
                                    }
                                }
                            }
                        }
                        util.varyLog(TAG,source,"source的值为");
                        mTargetFolders = FileUtils.getFilePath(this, Constants.STOREPATH) + "/" + file.getName();//方法返回String类型，拼起来就是完整的复制目标地址,创建目标文件夹
                        util.varyLog(TAG,mTargetFolders,"mTargetFolders的值为");
                        /*将根目录符合文件进行复制到本地*/
                    }else if((file.getName().endsWith("jpg") || file.getName().endsWith("jpeg") || file.getName().endsWith("png")
                            || file.getName().endsWith("mp4")|| file.getName().endsWith("avi")|| file.getName().endsWith("3gp"))
                            || file.equals(files[files.length-1])){//判断是否符合单屏文件;判断数据是最后一位，当属于最后一位时，需再判断是否是符合文件，进行放入再复制

                        if(file.equals(files[files.length-1])){
                            util.infoLog(TAG,"处于遍历最后一步",null);
                            if(file.getName().endsWith("jpg") || file.getName().endsWith("jpeg") || file.getName().endsWith("png")
                                    || file.getName().endsWith("mp4")|| file.getName().endsWith("avi")|| file.getName().endsWith("3gp")){
                                util.infoLog(TAG,"处于遍历最后一步时，在U盘根目录中找到单屏分屏的资源文件",null);
                                arrayList.add(file);
                            }
                            if(arrayList.size()!= 0){//说明存在单屏资源，则创建单屏文件夹
                                mSinglePlayerFolders = FileUtils.getFilePath(this,Constants.STOREPATH) + "/" + SINGLE_PLAYER_NAME;
                                single.setSingle_Son_source(mSinglePlayerFolders);
                                single.setSingle_view("0");
                                daoManager.getSession().getSingleSourceDao().update(single);
                                util.infoLog(TAG,"更新single表",null);
                                File singlePlayer = new File(mSinglePlayerFolders);
                                if(!singlePlayer.exists()){
                                    singlePlayer.mkdirs();
                                    Log.d(TAG,"this is singlePlayer文件夹的创建");
                                }
                                String iteratorName = null;
                                util.infoLog(TAG,"对U盘根目录所有符合的资源文件进行赋值到本地",null);//next()方法在同一循环中不能出现俩次
                                for (File value : arrayList) {
                                    iteratorName = value.getName();
                                    FileUtils.copyFile(extraPath +"/"+ iteratorName, mSinglePlayerFolders + "/" + iteratorName);
                                }

                                util.infoLog(TAG,"遍历所有符合的单屏文件，复制到singlePlayer文件夹内",null);
                            }else{
                                util.infoLog(TAG,"本地无单屏资源文件singlePlayer，故根目录无单屏资源",null);
                            }
                        }else{
                            util.infoLog(TAG,"在U盘根目录中找到单屏分屏的资源文件",null);
                            arrayList.add(file);
                        }
                    } else {
                        util.infoLog(TAG,"Not find extra program",null);

                        //Toast.makeText(this,"没有找到ghznPlayer文件夹",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if(mMatch){//对格式正确的ghznPlayer文件夹进行复制
                boolean success = false;
                success = copyFiles(source, mTargetFolders);//通过打开输入/出通道，执行读写复制
                if(success){//复制的动作执行成功
                    Log.d(TAG,"this is copy to:"+ mTargetFolders + " " + success);
                    //todo:获取ghznPlayer的文件信息，保证模式切换时，能够使用split_view,son_Source;
                    /*File f = new File(mTargetFolders);
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    File[] fs = f.listFiles();
                    if(fs == null)return;
                    String[] splits = fs[0].getName().split("\\-");//A-B-C
                    app.setSplit_view(splits[0]);//A，存储于数据库
                    app.setSplit_mode(splits[1]);//B
                    Log.d(TAG,"this is split_view,split_mode,split_widget" + app.getSplit_view() +"***"+ app.getSplit_mode());

                    for(File file : fs){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
                        getMap1().put(file.getName(), file.getAbsolutePath());
                    }
                    String key = app.getSplit_view() + "-" + app.getSplit_mode();
                    app.setSonSource(getMap1().get(key + "-1").toString() + "***" + getMap1().get(key + "-2").toString()
                            + "***" + getMap1().get(key + "-3").toString() + "***" + getMap1().get(key + "-4").toString());*/
                }else{
                    Log.d(TAG,"this is 复制失败，即将跳转主界面...");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
