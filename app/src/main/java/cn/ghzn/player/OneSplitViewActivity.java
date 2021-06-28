package cn.ghzn.player;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.util.ArrayList;

import cn.ghzn.player.layout.CustomVideoView;
import cn.ghzn.player.receiver.VarReceiver;
import cn.ghzn.player.sqlite.singleSource.SingleSource;
import cn.ghzn.player.sqlite.source.Source;

import static cn.ghzn.player.Constants.SINGLE_PLAYER_NAME;
import static cn.ghzn.player.ImportActivity.getMap1;
import static cn.ghzn.player.MainActivity.*;
import static cn.ghzn.player.MyApplication.single;
import static cn.ghzn.player.MyApplication.util;
import static cn.ghzn.player.util.InfoUtils.getRandomString;
import static cn.ghzn.player.util.ViewImportUtils.getSonImage;

public class OneSplitViewActivity extends Activity {
    private static final String TAG = "OneSplitViewActivity";
    private BroadcastReceiver mBroadcastReceiver;
    private ArrayList<String> arrayList;
    GestureDetector mGestureDetector;
    private Runnable mRunnable;
    private AlertDialog AlertDialogs = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        app.setCurrentActivity(this);
        app.setMediaPlayState(true);
        app.setPlaySonImageFlag(true);
        Log.d(TAG,"this is 跳转成功");

        if (app.isImportState()) {//外部导入状态为真

            if(app.isUpdateOnceSource()){
                //以文件的数量获取分屏样式，
                if (app.getFileCounts() == 0 && app.getFilesParent() == null) {
                    Intent intent = getIntent();
                    app.setFileCounts(intent.getIntExtra("splitView",0));
                    app.setFilesParent(intent.getStringExtra("filesParent"));
                }

                File f = new File(app.getFilesParent());
                if (!f.exists()) {
                    f.mkdirs();
                }
                File[] files = f.listFiles();
                if (files == null){
                    util.infoLog(TAG,"一分屏内无资源文件",null);
                    return;
                }
                /*取A-B-C文件信息*/
                util.infoLog(TAG,"更新一次资源，更新一分屏资源信息",null);
                String[] splits = files[0].getName().split("\\-");//A-B-C；任取一文件夹，仅作为数据库存储信息的参考对象
                Log.d(TAG,"this is files[0].getName()" + files[0].getName());
                app.setSplit_view(splits[0]);
                app.setSplit_mode(splits[1]);
                String split_widget = splits[2];//c
                Log.d(TAG,"this is split_view,split_mode,split_widget" + app.getSplit_view() +"***"+ app.getSplit_mode() + "***" + split_widget);
                for(File file : files){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
                    getMap1().put(file.getName(), file.getAbsolutePath());//形成键值对，方便取出作为资源导入
                }
                String key = app.getSplit_view() + "-" + app.getSplit_mode();
                arrayList = getSonImage(getMap1().get(key + "-1").toString());//将子文件夹里的地址全部赋给ArrayList的数组里
                app.setSonSource(getMap1().get(key + "-1").toString());//data：所有存储子文件夹的地址
                LogUtils.d(TAG,"getMap1().size()" + getMap1().size(),"Integer.parseInt(app.getSplit_view())" + Integer.parseInt(app.getSplit_view()));
                //多屏播放的同时判断是否singlePlayer文件夹，有的话进行分析存储相关资源
                app.setUpdateOnceSource(false);
            }else{
                util.infoLog(TAG,"无更新一次资源，已有资源信息",null);
                arrayList = getSonImage(app.getSon_source());
            }
            initWidget(app.getSplit_mode());

            File f1 = new File(app.getLicenceDir() + SINGLE_PLAYER_NAME);//取的时候再找一次值
            if(f1.exists()){
                util.infoLog(TAG,"singlePlayer文件夹存在，对其进行分析存储-->",null);
                File[] files1 = f1.listFiles();
                if(files1.length==0){
                    return;//多屏模式下未导入导入多屏资源
                }
                util.infoLog(TAG,"仅导入单屏资源存储",null);
                single.setSingle_view("0");
                single.setSingle_Son_source(f1.getAbsolutePath());
            }else{
                util.infoLog(TAG,"本地无单屏资源文件singlePlayer，故根目录无单屏资源",null);
            }

        }else {
            /*进行数据库读取*/
            initWidget(app.getSplit_mode());
            LogUtils.e(app.getSon_source());
            util.varyLog(TAG,app.getSon_source(),"app.getSon_source()");
            arrayList = getSonImage(app.getSon_source());
        }

        if (app.getSplit_view() != null) {//该判断可有可无，命名规则跳转前已检查
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
            //只需要存储有效数据
            if (app.isImportState()) {
//                app.setCreate_time(new Date());//new Date()出来的时间是本地时间
//            Source source = DaoManager.getInstance().getSession().getSourceDao().queryBuilder().unique();//一查新建的Device表
                if(app.getSource() == null){//这一步多余
                    app.setSource(new Source());//表不存在则新建赋值
                    daoManager.getSession().getSourceDao().insert(getSource(app.getSource()));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
                }else{//存在则直接修改
                    daoManager.getSession().getSourceDao().update(getSource(app.getSource()));
                }
                if(single == null){
                    single = new SingleSource();//表不存在则新建赋值
                    daoManager.getSession().getSingleSourceDao().insert(single);//单例(操作库对象)-操作表对象-操作表实例.进行操作；
                }else{//存在则直接修改
                    daoManager.getSession().getSingleSourceDao().update(single);
        }
            }
        } else {
            Log.d(TAG,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作");
            Toast.makeText(this,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作",Toast.LENGTH_LONG).show();
        }
    }


    public void playSonImage(){
        if (app.isPlaySonImageFlag()) {
            if (app.getListNum1() >= arrayList.size()) {
                app.setListNum1(0);//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
            }
            Log.d(TAG,"开始执行执行播放程序");

            app.setFile(new File(arrayList.get(app.getListNum1()).toString()));
            if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg")||app.getFile().getName().endsWith("png"))) {
                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                app.setWidgetAttribute1(1);//记录此时控件播放为图片
                app.getImageView_1().setImageURI(Uri.fromFile(app.getFile()));
                app.getImageView_1().setVisibility(View.VISIBLE);
                app.getVideoView_1().setVisibility(View.GONE);
                app.setListNum1(app.getListNum1() + 1);

                app.setStartTime(System.currentTimeMillis());
                app.getHandler().postDelayed(mRunnable = new Runnable(){
                    @Override
                    public void run() {
                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + app.getFile().getAbsolutePath());
                        if (app.getPlayFlag() == 0) {//非播放状态下，使其递归方法失效，但listNum递增1.当播放按下时，新建剩余延迟，再进行递增
                            playSonImage();
                        }
                    }
                },app.getDelayMillis());//5秒后结束当前图片
                app.setRunnable1(mRunnable);//无法set线程，只好绑定mRunnable到全局Runnable
            }
            else if (app.getFile().getName().endsWith("mp4") || app.getFile().getName().endsWith("avi") || app.getFile().getName().endsWith("3gp")) {
                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + app.getFile().getAbsolutePath());
                app.setWidgetAttribute1(2);//记录此时控件播放为视频
                Log.d(TAG,"this is isMediaPlayState()" + app.isMediaPlayState());
                if (app.isMediaPlayState()) {
                    app.getVideoView_1().setVideoURI(Uri.fromFile(app.getFile()));
                    app.getVideoView_1().setVisibility(View.VISIBLE);
                    app.getImageView_1().setVisibility(View.GONE);
                    app.getVideoView_1().start();

                    app.getVideoView_1().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
                            Log.d(TAG,"执行播放完视频，视频位于：" + app.getFile().getAbsolutePath());
                            app.setListNum1(app.getListNum1() + 1);
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

    /**
     *以下为非主要方法
     */

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

    private Source getSource(Source source) {//对数据库进行覆写
        /*if(app.getSingle_son_source()!=null){
            source.setSingle_view(app.getSingle_view());
            source.setSingle_Son_source(app.getSingle_son_source());
        }*/
        source.setProgram_id(getRandomString(5));
        source.setSplit_view(app.getSplit_view());
        source.setSplit_mode(app.getSplit_mode());
        source.setSon_source(app.getSonSource());//存储的是子资源，但取出来用时需用来获取对象。
        source.setStart_time(app.getStart_time());
        source.setEnd_time(app.getEnd_time());
        source.setCreate_time(app.getCreate_time());
        source.setFirst_time(app.getFirst_time());
        source.setTime_difference(app.getTime_difference());
        source.setRelative_time(app.getRelative_time());
        return source;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initWidget(String split_mode) {

        switch (split_mode){//该步骤可以简写，这样写仅为了和其他activity代码相似
            case "1":
//                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//                View view=inflater.inflate(R.layout.activity_splitview_one1, null);//setContent()的实际底层代码逻辑相关
                setContentView(R.layout.activity_splitview_one1);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_one1_1));//this指当前当前layout里找控件，若是去其他layout找，需要用到layout过滤器如上
                app.setVideoView_1((CustomVideoView)this.findViewById(R.id.videoView_one1_1));

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
                Log.d(TAG,"this is case \"1\":");
                break;
            default:
                Log.d(TAG,"this is switch(split_mode)_default");
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 以下为生命周期
     */
    @Override
    protected void onPause() {
        super.onPause();
        //实现视频暂停，图片不跳转。
        app.setPlayFlag(1);

        app.getVideoView_1().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //实现视频恢复，图片播放
        app.setPlayFlag(0);
        playSonImage();

        app.getVideoView_1().resume();
        //Log.d(TAG,"this is onResume()");
    }

    @Override
    protected void onDestroy() {
        //app.setMediaPlayState(false);
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if(AlertDialogs != null){
            AlertDialogs.dismiss();
        }
        super.onDestroy();
//        if (app.getRunnable1() != null) {
//            app.getHandler().removeCallbacks(app.getRunnable1());
//        }
    }

}
