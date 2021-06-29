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
import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.MyApplication.mSource;
import static cn.ghzn.player.MyApplication.single;
import static cn.ghzn.player.MyApplication.util;
import static cn.ghzn.player.util.InfoUtils.getRandomString;
import static cn.ghzn.player.util.ViewImportUtils.getSonImage;

public class FourSplitViewActivity extends Activity {
    private static final String TAG = "FourSplitViewActivity";
    private Runnable mRunnable;
    private GestureDetector mGestureDetector;
    private BroadcastReceiver mBroadcastReceiver;
    private AlertDialog AlertDialogs = null;

    private ArrayList arrayList1;//控件区1地址
    private ArrayList arrayList2;//控件区2地址
    private ArrayList arrayList3;//控件区3地址
    private ArrayList arrayList4;//控件区4地址

    private boolean isFreeFlag1 = true;
    private boolean isFreeFlag2 = true;
    private boolean isFreeFlag3 = true;
    private boolean isFreeFlag4 = true;
    private ArrayList[] Recursive = new ArrayList[4];//先声明--仅用于存储递归时的参数

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setFormat(PixelFormat.TRANSPARENT);
        app.setMediaPlayState(true);
        app.setPlaySonImageFlag(true);
        app.setCurrentActivity(this);
        Log.d(TAG,"this is 跳转成功");
        if (app.isImportState()) {
            util.infoLog(TAG,"有U盘接入，执行资源播放",null);
            initWidget(mSource.getSplit_mode());
            String[] strings = mSource.getSon_source().split("\\*\\*\\*");
            arrayList1 = getSonImage(strings[0]);
            arrayList2 = getSonImage(strings[1]);
            arrayList3 = getSonImage(strings[2]);
            arrayList4 = getSonImage(strings[3]);
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
        }else{
            util.infoLog(TAG,"无U盘接入，执行资源播放",null);
            initWidget(mSource.getSplit_mode());
            String[] strings = mSource.getSon_source().split("\\*\\*\\*");
            arrayList1 = getSonImage(strings[0]);
            arrayList2 = getSonImage(strings[1]);
            arrayList3 = getSonImage(strings[2]);
            arrayList4 = getSonImage(strings[3]);
        }
        if (mSource.getSplit_view() != null) {
            mBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {//播放按钮的重播功能的广播
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG,"this is 广播执行playSonImage()");
                    playSonImage(arrayList1,arrayList2,arrayList3,arrayList4);
                }
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction("0");
            registerReceiver(mBroadcastReceiver,filter);
            //todo:资源播放程序在这儿
            playSonImage(arrayList1,arrayList2,arrayList3,arrayList4);
            if (app.isImportState()) {
                daoManager.getSession().getSourceDao().update(getSource(mSource));
                daoManager.getSession().getSingleSourceDao().update(single);
            }
        } else {
            Log.d(TAG,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作");
            Toast.makeText(this,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作",Toast.LENGTH_LONG).show();
        }
    }

    private void playSonImage(ArrayList arrayList1,ArrayList arrayList2,ArrayList arrayList3,ArrayList arrayList4){
        Recursive[0] = arrayList1;//以赋值控件12为一个单元，整体递归：最笨的方法
        Recursive[1] = arrayList2;
        Recursive[2] = arrayList3;
        Recursive[3] = arrayList4;

        /*//打印标志位,检查播放空闲信息
        Log.d(TAG,"this is app.isPlaySonImageFlag() "+ app.isPlaySonImageFlag());
        Log.d(TAG,"this is 是否播放状态0？ "+ app.getPlayFlag());
        Log.d(TAG,"this is AllisFreeFlag "+"1:"+isFreeFlag1+"2:"+isFreeFlag2+"3:"+isFreeFlag3+"4:"+isFreeFlag4);*/

        if (app.isPlaySonImageFlag()) {
            Log.d(TAG,"this is 进入资源播放方法");
            if (isFreeFlag1) {
                Log.d(TAG,"this is 此时空闲，进入设置控件1资源");
                if (app.getListNum1() >= arrayList1.size()) {
                    app.setListNum1(0);//循环要求，仅重置变量为0功能
//                    playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
                }
                Log.d(TAG,"开始执行执行播放程序");
                app.setFile(new File(arrayList1.get(app.getListNum1()).toString()));//如果f没有消亡，那就会导致新导入的资源不会找到对应的对象。
                if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg")||app.getFile().getName().endsWith("png"))) {
                    Log.d(TAG,"playSonImage1执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute1(1);//记录此时控件播放为图片
                    isFreeFlag1 = false;//进入图片赋值程序，先设为忙线状态

                    app.getImageView_1().setImageURI(Uri.fromFile(app.getFile()));
                    app.getImageView_1().setVisibility(View.VISIBLE);
                    app.getVideoView_1().setVisibility(View.GONE);
                    app.setListNum1(app.getListNum1() + 1);

                    app.getHandler().postDelayed(mRunnable = new Runnable(){
                        @Override
                        public void run() {
                            Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + app.getFile().getAbsolutePath());
                            isFreeFlag1 = true;//图片赋值程序完成，退出忙线状态
                            if (app.getPlayFlag() == 0){
                                playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
                            }
                        }
                    },app.getDelayMillis());//3秒后结束当前图片
                    app.setRunnable1(mRunnable);

                } else if (app.getFile().getName().endsWith("mp4") || app.getFile().getName().endsWith("avi") || app.getFile().getName().endsWith("3gp")) {
                    Log.d(TAG,"playSonImage1执行视频播放，添加了视频：《《《《《" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute1(2);//记录此时控件播放为视频
                    isFreeFlag1 = false;//进入图片赋值程序，先设为忙线状态
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
                                isFreeFlag1 = true;//视频赋值程序完成，退出忙线状态
                                playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
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
            if (isFreeFlag2) {
                Log.d(TAG,"this is 此时空闲，进入设置控件2资源");
                if (app.getListNum2() >= arrayList2.size()) {
                    app.setListNum2(0);
//                    playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
//            finish();
                }
                Log.d(TAG,"开始执行执行播放程序");
                app.setFile(new File(arrayList2.get(app.getListNum2()).toString()));
                if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg")||app.getFile().getName().endsWith("png"))) {
                    Log.d(TAG,"playSonImage2执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute2(1);//记录此时控件播放为图片
                    isFreeFlag2 = false;//执行图片赋值程序，进入忙线状态

                    app.getImageView_2().setImageURI(Uri.fromFile(app.getFile()));
                    app.getImageView_2().setVisibility(View.VISIBLE);
                    app.getVideoView_2().setVisibility(View.GONE);
                    app.setListNum2(app.getListNum2() + 1);

                    app.getHandler().postDelayed(mRunnable = new Runnable(){
                        @Override
                        public void run() {
                            Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + app.getFile().getAbsolutePath());
                            isFreeFlag2 = true;//图片赋值程序完成，退出忙线状态
                            if (app.getPlayFlag() == 0){
                                playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
                            }
                        }
                    },app.getDelayMillis());//3秒后结束当前图片
                    app.setRunnable2(mRunnable);

                } else if (app.getFile().getName().endsWith("mp4") || app.getFile().getName().endsWith("avi") || app.getFile().getName().endsWith("3gp")) {
                    Log.d(TAG,"playSonImage2执行视频播放，添加了视频：《《《《《" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute2(2);//记录此时控件播放为视频
                    isFreeFlag2 = false;//执行视频赋值程序，进入忙线状态
                    if (app.isMediaPlayState()) {
                        app.getVideoView_2().setVideoURI(Uri.fromFile(app.getFile()));
                        app.getVideoView_2().setVisibility(View.VISIBLE);
                        app.getImageView_2().setVisibility(View.GONE);
                        app.getVideoView_2().start();

                        app.getVideoView_2().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                Log.d(TAG,"执行播放完视频，视频位于：" + app.getFile().getAbsolutePath());
                                app.setListNum2(app.getListNum2() + 1);
                                isFreeFlag2 = true;//视频赋值程序完成，退出忙线状态
                                playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
                            }
                        });
                        app.getVideoView_2().setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                app.getVideoView_2().stopPlayback();
                                return true;
                            }
                        });
                    }
                }
            }
            if (isFreeFlag3) {
                Log.d(TAG,"this is 此时空闲，进入设置控件3资源");
                if (app.getListNum3() >= arrayList3.size()) {
                    app.setListNum3(0);
//                    playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
//            finish();
                }
                Log.d(TAG,"开始执行执行播放程序");
                app.setFile(new File(arrayList3.get(app.getListNum3()).toString()));
                if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg")||app.getFile().getName().endsWith("png"))) {
                    Log.d(TAG,"playSonImage3执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute3(1);//记录此时控件播放为图片
                    isFreeFlag3 = false;//执行图片赋值程序，进入忙线状态

                    app.getImageView_3().setImageURI(Uri.fromFile(app.getFile()));
                    app.getImageView_3().setVisibility(View.VISIBLE);
                    app.getVideoView_3().setVisibility(View.GONE);
                    app.setListNum3(app.getListNum3() + 1);


                    app.getHandler().postDelayed(mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "执行延迟播放图片3秒，图片位于：" + app.getFile().getAbsolutePath());
                            isFreeFlag3 = true;//图片赋值程序完成，退出忙线状态
                            if (app.getPlayFlag() == 0){
                                playSonImage(Recursive[0], Recursive[1], Recursive[2], Recursive[3]);
                            }
                        }
                    }, 3000);//3秒后结束当前图片
                    app.setRunnable3(mRunnable);

                } else if (app.getFile().getName().endsWith("mp4") || app.getFile().getName().endsWith("avi") || app.getFile().getName().endsWith("3gp")) {
                    Log.d(TAG, "playSonImage2执行视频播放，添加了视频：《《《《《" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute3(2);//记录此时控件播放为视频
                    isFreeFlag3 = false;//执行视频赋值程序，进入忙线状态
                    if (app.isMediaPlayState()) {
                        app.getVideoView_3().setVideoURI(Uri.fromFile(app.getFile()));
                        app.getVideoView_3().setVisibility(View.VISIBLE);
                        app.getImageView_3().setVisibility(View.GONE);
                        app.getVideoView_3().start();

                        app.getVideoView_3().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
                                app.setListNum3(app.getListNum3() + 1);
                                isFreeFlag3 = true;//视频赋值程序完成，退出忙线状态
                                playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
                            }
                        });
                        app.getVideoView_3().setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                app.getVideoView_3().stopPlayback();
                                return true;
                            }
                        });
                    }
                }
            }
            if (isFreeFlag4) {
                Log.d(TAG,"this is 此时空闲，进入设置控件4资源");
                if (app.getListNum4() >= arrayList4.size()) {
                    app.setListNum4(0);
//                    playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
//            finish();
                }
                Log.d(TAG,"开始执行执行播放程序");
                app.setFile(new File(arrayList4.get(app.getListNum4()).toString()));
                if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg")||app.getFile().getName().endsWith("png"))) {
                    Log.d(TAG,"playSonImage4执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute4(1);
                    isFreeFlag4 = false;//执行图片赋值程序，进入忙线状态

                    app.getImageView_4().setImageURI(Uri.fromFile(app.getFile()));
                    app.getImageView_4().setVisibility(View.VISIBLE);
                    app.getVideoView_4().setVisibility(View.GONE);
                    app.setListNum4(app.getListNum4() + 1);

                    app.getHandler().postDelayed(mRunnable = new Runnable(){
                        @Override
                        public void run() {
                            Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + app.getFile().getAbsolutePath());
                            isFreeFlag4 = true;//图片赋值程序完成，退出忙线状态
                            if (app.getPlayFlag() == 0){
                                playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
                            }
                        }
                    },app.getDelayMillis());//3秒后结束当前图片
                    app.setRunnable4(mRunnable);

                } else if (app.getFile().getName().endsWith("mp4") || app.getFile().getName().endsWith("avi") || app.getFile().getName().endsWith("3gp")) {
                    Log.d(TAG,"playSonImage4执行视频播放，添加了视频：《《《《《" + app.getFile().getAbsolutePath());
                    app.setWidgetAttribute4(2);
                    isFreeFlag4 = false;//执行视频赋值程序，进入忙线状态
                    if (app.isMediaPlayState()) {
                        app.getVideoView_4().setVideoURI(Uri.fromFile(app.getFile()));
                        app.getVideoView_4().setVisibility(View.VISIBLE);
                        app.getImageView_4().setVisibility(View.GONE);
                        app.getVideoView_4().start();

                        app.getVideoView_4().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
                                app.setListNum4(app.getListNum4() + 1);
                                isFreeFlag4 = true;//视频赋值程序完成，退出忙线状态
                                playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
                            }
                        });
                        app.getVideoView_4().setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                app.getVideoView_4().stopPlayback();
                                return true;
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     *以下为非主要方法
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//重写back为无效按键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Source getSource(Source source) {//对数据库进行覆写；不能直接调用一分屏得的该方法，函数体中非静态变量声明
        //source.setLicense_dir(app.getLicenceDir());
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
        switch (split_mode){
            case "1":
                setContentView(R.layout.activity_splitview_four1);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_four1_1));
                app.setVideoView_1((CustomVideoView)this.findViewById(R.id.videoView_four1_1));
                app.setImageView_2((ImageView)this.findViewById(R.id.imageView_four1_2));
                app.setVideoView_2((CustomVideoView)this.findViewById(R.id.videoView_four1_2));
                app.setImageView_3((ImageView)this.findViewById(R.id.imageView_four1_3));
                app.setVideoView_3((CustomVideoView)this.findViewById(R.id.videoView_four1_3));
                app.setImageView_4((ImageView)this.findViewById(R.id.imageView_four1_4));
                app.setVideoView_4((CustomVideoView)this.findViewById(R.id.videoView_four1_4));

                setDialog(this);
                app.getImageView_1().setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
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
                app.getImageView_2().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                app.getVideoView_2().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                app.getImageView_3().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                app.getVideoView_3().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                app.getImageView_4().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                app.getVideoView_4().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"1\":");
                break;
            default:
                Log.d(TAG,"default");
                break;
        }
    }

    public void setDialog(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        if(app.getView() != null){//若重新导入资源，原先子类app.view调用的父类可能未移除，此后则无法长按出弹窗，需先移除才可赋值view，否则报需先移除父类的错误提示，保证父类parentView为空才能正常长按
            ViewGroup parentView = (ViewGroup) app.getView().getParent();
            if (parentView != null) {
                LogUtils.e(parentView);
                parentView.removeView(app.getView());
                Log.d(TAG,"this is parentView.removeView(app.getView())");
            }
        }
        alertDialog.setView(app.getView());
        /*ViewGroup parentView = (ViewGroup) app.getView().getParent();
        LogUtils.e(parentView);*/

        Log.d(TAG,"this is parentView.removeView(app.getView())111");
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
                Log.d(TAG, "this is OnLongPressTap_4");
                /*if (app.getView() != null) {
                    ViewGroup parentView = (ViewGroup) app.getView().getParent();
                    if (parentView != null) {
                        parentView.removeView(app.getView());
                        Log.d(TAG,"this is to doing ：parentView.removeView(view)");
                    }
                }*/
                try {
                    AlertDialogs.show();
                    Log.d(TAG,"this is AlertDialogs.show()");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
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
        app.getVideoView_2().pause();
        app.getVideoView_3().pause();
        app.getVideoView_4().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG,"this is onResume()");
        //实现视频恢复，图片播放
        app.setPlayFlag(0);
        playSonImage(arrayList1,arrayList2,arrayList3,arrayList4);

        app.getVideoView_1().resume();
        app.getVideoView_2().resume();
        app.getVideoView_3().resume();
        app.getVideoView_4().resume();
    }

    @Override
    protected void onDestroy() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if(AlertDialogs != null){
            AlertDialogs.dismiss();
        }
        super.onDestroy();
    }
}
