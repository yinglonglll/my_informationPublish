package cn.ghzn.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.util.ArrayList;

import cn.ghzn.player.layout.CustomVideoView;
import cn.ghzn.player.receiver.VarReceiver;
import cn.ghzn.player.sqlite.singleSource.SingleSource;
import cn.ghzn.player.sqlite.source.Source;

import static cn.ghzn.player.Constants.GHZNPLAYER_NAME;
import static cn.ghzn.player.ImportActivity.getMap1;
import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.MyApplication.mSource;
import static cn.ghzn.player.MyApplication.single;
import static cn.ghzn.player.MyApplication.util;
import static cn.ghzn.player.util.InfoUtils.getRandomString;

/**
 * <pre>
 *     author : yinglonglll
 *     e-mail : 949706806@qq.com
 *     time   : 2021/06/23
 *     desc   :
 *     func--->
 *     version: 1.0
 * </pre>
 */
public class SingleSplitViewActivity extends Activity {
    private static final String TAG = "SingleSplitViewActivity";
    private BroadcastReceiver mBroadcastReceiver;
    private final ArrayList<String> arrayList = new ArrayList<>();
    GestureDetector mGestureDetector;
    private Runnable mRunnable;
    private AlertDialog AlertDialogs = null;
    private boolean isFreeFlag1 = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        app.setCurrentActivity(this);
        app.setMediaPlayState(true);
        app.setPlaySonImageFlag(true);
        Log.d(TAG, "this is 跳转成功");
        /*手动初始化数据*/
        if (app.isImportState()) {
            //进行U盘读取--单屏播放
            /*Intent intent = getIntent();
            String filesParent = intent.getStringExtra("filesParent");
            String singleView = intent.getStringExtra("singleView");
            LogUtils.e(filesParent);
            LogUtils.e(singleView);*/

            /*if(app.isUpdateOnceSource()){
                Log.d(TAG,"this is 进行U盘读取");

                app.setUpdateOnceSource(false);
            }else{
                //app.getSingle_son_source()
                app.setFilesParent(single.getSingle_Son_source());
                //util.varyLog(TAG,app.getSingle_view(),"getSingle_view");
                util.varyLog(TAG,single.getSingle_view(),"getSingle_view");
            }*/
            if(single.getSingle_Son_source()!=null){

                app.setUpdateOnceSource(false);
                File f = new File(single.getSingle_Son_source());
                if (!f.exists()) {
                    f.mkdirs();
                }
                File[] files = f.listFiles();
                if (files == null){
                    return;
                }
                for (File file : files) {
                    arrayList.add(file.getAbsolutePath());
                }
                initWidget(single.getSingle_view());
                //单屏播放同时判断是否ghznPlayer文件夹，有的话进行分析存储相关资源
                File f1 = new File(app.getLicenceDir() + GHZNPLAYER_NAME);
                if(f1.exists()){
                    util.infoLog(TAG,"ghznPlayer文件夹存在进行存储信息-->",null);
                    File[] files1 = f1.listFiles();
                    if(files1.length==0){
                        return;//单屏模式下未导入导入多屏资源
                    }
                    util.infoLog(TAG,"仅导入多屏资源存储",null);
                    //单屏模式下已经导入多屏资源
                    String[] splits = files1[0].getName().split("\\-");//A-B-C；任取一文件夹，仅作为数据库存储信息的参考对象
                    app.setSplit_view(splits[0]);//用于作为存储sonSonrce的判断依据
                    app.setSplit_mode(splits[1]);//用于初始化控件
                    Log.d(TAG,"this is split_view,split_mode,split_widget" + app.getSplit_view() +"***"+ app.getSplit_mode());
                    for(File file : files1){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
                        getMap1().put(file.getName(), file.getAbsolutePath());//形成键值对，方便取出作为资源导入
                        util.varyLog(TAG,file.getName(),"file.getName()");
                    }
                    LogUtils.e(getMap1());
                    String key = app.getSplit_view() + "-" + app.getSplit_mode();
                    saveSonSource(key);
                }
            }else{
                return;//单屏模式下，进来但是无资源则退回去
            }
        } else {
            //进行数据库读取
            Log.d(TAG,"this is 进行数据库读取");
            initWidget(single.getSingle_view());
            File f = new File(single.getSingle_Son_source());
            if (!f.exists()) {
                f.mkdirs();
            }
            File[] files = f.listFiles();
            if (files == null){
                return;
            }
            for (File file : files) {
                arrayList.add(file.getAbsolutePath());
            }
        }
        if (single.getSingle_view() != null) {//该判断可有可无，命名规则跳转前已检查
            mBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG,"this is varReceiver");
                    Log.d(TAG,"why is mBroadcastReceiver:app.getListNum1()广播：" + app.getListNum1());
                    playSonImage();
                }
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction("0");
            registerReceiver(mBroadcastReceiver,filter);//注册广播

            playSonImage();//参数arrayList是默认的，可含或不含参，结果都一样
            LogUtils.e(mSource);
            if (app.isImportState()) {
                Log.d(TAG,"this is U盘接入、单屏模式状态下，进行资源的存储");
                if(mSource == null){//这一步多余
                    mSource = new Source();
                    daoManager.getSession().getSourceDao().insert(getSource(mSource));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
                }else{//存在则直接修改
                    daoManager.getSession().getSourceDao().update(getSource(mSource));
                }
                if(single == null){
                    single = new SingleSource();
                    daoManager.getSession().getSingleSourceDao().insert(single);
                }else{
                    daoManager.getSession().getSingleSourceDao().update(single);
                }
            }
        }
    }

    /**
     * 根据分屏信息来确定存储几分屏的所有子文件夹绝对地址
     * @param key
     */
    private void saveSonSource(String key) {
        switch (app.getSplit_view()){
            case "1":
                app.setSonSource(getMap1().get(key + "-1"));//data：所有存储子文件夹的地址
                break;
            case "2":
                app.setSonSource(getMap1().get(key + "-1")+ "***" + getMap1().get(key + "-2"));
                break;
            case "3":
                app.setSonSource(getMap1().get(key + "-1") + "***" + getMap1().get(key + "-2")
                        + "***" + getMap1().get(key + "-3"));
                break;
            case "4":
                app.setSonSource(getMap1().get(key + "-1") + "***" + getMap1().get(key + "-2")
                        + "***" + getMap1().get(key + "-3") + "***" + getMap1().get(key + "-4"));
                break;
        }
    }

    private Source getSource(Source source) {
        //source.setSingle_view(app.getSingle_view());
        //source.setSingle_Son_source(app.getFilesParent());
        if(app.getSon_source() != null){
            source.setSplit_view(app.getSplit_view());
            source.setSplit_mode(app.getSplit_mode());
            source.setSon_source(app.getSonSource());
        }
        source.setProgram_id(getRandomString(5));
        source.setStart_time(app.getStart_time());
        source.setEnd_time(app.getEnd_time());
        source.setCreate_time(app.getCreate_time());
        source.setFirst_time(app.getFirst_time());
        //source.setTime_difference(app.getTime_difference());
        //source.setRelative_time(app.getRelative_time());
        return source;
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initWidget(String split_mode) {
        Log.d(TAG,"this is initWidget()");
        switch (split_mode) {//该步骤可以简写，这样写仅为了和其他activity代码相似
            case "0":
                setContentView(R.layout.activity_splitview_one1);
                app.setImageView_1((ImageView) this.findViewById(R.id.imageView_one1_1));//this指当前当前layout里找控件，若是去其他layout找，需要用到layout过滤器如上
                app.setVideoView_1((CustomVideoView) this.findViewById(R.id.videoView_one1_1));

                setDialog(this);
                app.getImageView_1().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                app.getVideoView_1().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG, "this is case \"1\":");
                break;
            default:
                Log.d(TAG, "this is switch(split_mode)_default");
                break;
        }
    }

    public void playSonImage() {
        if (app.isPlaySonImageFlag()) {
            if (isFreeFlag1){
                if (app.getListNum1() >= arrayList.size()) {
                    app.setListNum1(0);//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
                }
                Log.d(TAG, "开始执行执行播放程序");

                app.setFile(new File(arrayList.get(app.getListNum1())));
                if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg") || app.getFile().getName().endsWith("png"))) {
                    Log.d(TAG, "执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute1(1);//记录此时控件播放为图片
                    isFreeFlag1 = false;//进入图片赋值程序，先设为忙线状态
                    app.getImageView_1().setImageURI(Uri.fromFile(app.getFile()));
                    app.getImageView_1().setVisibility(View.VISIBLE);
                    app.getVideoView_1().setVisibility(View.GONE);
                    app.setListNum1(app.getListNum1() + 1);

                    app.setStartTime(System.currentTimeMillis());
                    app.getHandler().postDelayed(mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "执行延迟播放图片3秒，图片位于：" + app.getFile().getAbsolutePath());
                            isFreeFlag1 = true;//图片赋值程序完成，退出忙线状态
                            LogUtils.e(app.getPlayFlag());
                            if (app.getPlayFlag() == 0) {//非播放状态下，使其递归方法失效，但listNum递增1.当播放按下时，新建剩余延迟，再进行递增
                                playSonImage();
                            }
                        }
                    }, app.getDelayMillis());//5秒后结束当前图片
                    app.setRunnable1(mRunnable);//无法set线程，只好绑定mRunnable到全局Runnable
                } else if (app.getFile().getName().endsWith("mp4") || app.getFile().getName().endsWith("avi") || app.getFile().getName().endsWith("3gp")) {
                    Log.d(TAG, "执行视频播放，添加了视频：《《《《《" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute1(2);//记录此时控件播放为视频
                    isFreeFlag1 = false;//进入图片赋值程序，先设为忙线状态
                    Log.d(TAG, "this is isMediaPlayState()" + app.isMediaPlayState());
                    if (app.isMediaPlayState()) {
                        app.getVideoView_1().setVideoURI(Uri.fromFile(app.getFile()));
                        app.getVideoView_1().setVisibility(View.VISIBLE);
                        app.getImageView_1().setVisibility(View.GONE);
                        app.getVideoView_1().start();

                        app.getVideoView_1().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
                                Log.d(TAG, "执行播放完视频，视频位于：" + app.getFile().getAbsolutePath());
                                app.setListNum1(app.getListNum1() + 1);
                                isFreeFlag1 = true;//视频赋值程序完成，退出忙线状态
                                playSonImage();
                            }
                        });
                        app.getVideoView_1().setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                app.getVideoView_1().stopPlayback();
                                return true;
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     *有效图片或视频资源导入的方法
     */

    public static ArrayList getSonImage(String sonPath) {
        ArrayList arrayList = new ArrayList<String>();
        File imageNames = null;//ghznPlayer名+分屏模式名+对应控件的子文件夹1234名
        imageNames = new File(sonPath);
        File[] sonImageName1 = imageNames.listFiles();//子文件夹1的对象，但里面可能不全是图片或视频
        try {
            if (sonImageName1 != null) {
                for (File imageAddress : sonImageName1) {
                    if (imageAddress.getName().endsWith("jpg") || imageAddress.getName().endsWith("jpeg") || imageAddress.getName().endsWith("png")
                            || imageAddress.getName().endsWith("mp4") || imageAddress.getName().endsWith("avi")
                            || imageAddress.getName().endsWith("3gp")) {
                        Log.d(TAG, "获取到了一个可用路径：" + imageAddress.getAbsolutePath());
                        arrayList.add(imageAddress.getAbsolutePath());
                    } else if (imageAddress.isDirectory()) {
                        getSonImage(imageAddress.getAbsolutePath());//递归获取格式支持的全部图片和视频
                    }
                }
            } else {
                Log.d(TAG,"this is sonImageName1 = null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }//获取子文件夹里所有有效图片或视频的绝对地址到动态数组arraylist中

    public void setDialog(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        if(app.getView() != null){
            ViewGroup parentView = (ViewGroup) app.getView().getParent();
            if (parentView != null) {
                LogUtils.e(parentView);
                parentView.removeView(app.getView());
                Log.d(TAG,"this is parentView.removeView(app.getView())");
            }
        }

        alertDialog.setView(app.getView());
        AlertDialogs = alertDialog.create();//如上是我自己找到新建的弹窗，下面是把新建的弹窗赋给新建的手势命令中的长按。
        mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "OnLongPressTap");
                AlertDialogs.show();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //实现视频暂停，图片不跳转。
        if(arrayList.size() != 0){
            app.setPlayFlag(1);
            app.getVideoView_1().pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //实现视频恢复，图片播放
        LogUtils.e(arrayList);
        if(arrayList.size() != 0){
            app.setPlayFlag(0);
            playSonImage();
            app.getVideoView_1().resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //app.setMediaPlayState(false);
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if(AlertDialogs != null){
            AlertDialogs.dismiss();
        }
    }
}
