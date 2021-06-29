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
import cn.ghzn.player.sqlite.source.Source;

import static cn.ghzn.player.Constants.SINGLE_PLAYER_NAME;
import static cn.ghzn.player.MainActivity.*;
import static cn.ghzn.player.MyApplication.mSource;
import static cn.ghzn.player.MyApplication.single;
import static cn.ghzn.player.MyApplication.util;
import static cn.ghzn.player.util.InfoUtils.getRandomString;
import static cn.ghzn.player.util.ViewImportUtils.getSonImage;

public class OneSplitViewActivity extends Activity {
    private static final String TAG = "OneSplitViewActivity";
    private BroadcastReceiver mBroadcastReceiver;
    private ArrayList arrayList;
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

        if (app.isImportState()) {
            util.infoLog(TAG,"有U盘接入，执行资源播放",null);
            initWidget(mSource.getSplit_mode());
            String[] strings = mSource.getSon_source().split("\\*\\*\\*");
            arrayList = getSonImage(strings[0]);
            /*若U盘中存在单屏资源则进行存储并分析*/
            File f1 = new File(app.getLicenceDir() + SINGLE_PLAYER_NAME);
            if(f1.exists()){
                util.infoLog(TAG,"本地singlePlayer文件夹存在，对其进行分析存储-->",null);
                File[] files1 = f1.listFiles();
                if(files1.length==0){
                    return;//多屏模式下未导入导入多屏资源
                }
                util.infoLog(TAG,"存在单屏资源，进行存储",null);
                single.setSingle_view("0");
                single.setSource(f1.getAbsolutePath());
            }else{
                util.infoLog(TAG,"本地singlePlayer文件夹不存在，即根目录无单屏资源",null);
            }
        }else {
            util.infoLog(TAG,"无U盘接入，执行资源播放",null);
            initWidget(mSource.getSplit_mode());
            String[] strings = mSource.getSon_source().split("\\*\\*\\*");
            arrayList = getSonImage(strings[0]);
        }

        if (mSource.getSplit_view() != null) {//存在多屏资源，执行播放控制逻辑
            mBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG,"this is 广播执行playSonImage()");
                    playSonImage();
                }
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction("0");//自定义值;//执行播放方法原理：由于视频处于暂停，即处于忙线状态，重播不会对现有视频进行干扰，而此时图片是退出忙线状态的，故此时执行此方法仅对图片有效
            registerReceiver(mBroadcastReceiver,filter);
            //todo:资源播放程序在这儿
            playSonImage();
            if (app.isImportState()) {
                daoManager.getSession().getSourceDao().update(getSource(mSource));
                daoManager.getSession().getSingleSourceDao().update(single);
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
