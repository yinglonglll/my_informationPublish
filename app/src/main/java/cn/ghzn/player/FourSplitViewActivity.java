package cn.ghzn.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import cn.ghzn.player.receiver.VarReceiver;
import cn.ghzn.player.sqlite.source.Source;
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.ImportActivity.getMap1;
import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.util.InfoUtils.getRandomString;
import static cn.ghzn.player.util.ViewImportUtils.getSonImage;

public class FourSplitViewActivity extends Activity {
    private static final String TAG = "FourSplitViewActivity";
    private Runnable mRunnable;
    private GestureDetector mGestureDetector;
    private BroadcastReceiver mBroadcastReceiver;

    ArrayList arrayList1;//控件区1地址
    ArrayList arrayList2;//控件区2地址
    ArrayList arrayList3;//控件区3地址
    ArrayList arrayList4;//控件区4地址
    private View view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setFormat(PixelFormat.TRANSPARENT);
        app.setMediaPlayState(true);
        app.setPlaySonImageFlag(true);
        app.setCurrentActivity(this);
        Log.d(TAG,"this is 跳转成功");
        if (app.isExtraState()) {
            Intent intent = getIntent();
            if (app.getFileCounts() == 0 && app.getFilesParent() == null) {
                app.setFileCounts(intent.getIntExtra("splitView",0));
                app.setFilesParent(intent.getStringExtra("filesParent"));
            }
            Log.d(TAG,"this is splitView" + app.getFileCounts());
            Log.d(TAG,"this is filesParent" + app.getFilesParent());

            File f = new File(app.getFilesParent());
            if (!f.exists()) {
                f.mkdirs();//区分之二：创建多级目录和创建当前目录区别
            }
            File[] files = f.listFiles();

            String[] splits = files[0].getName().split("\\-");//A-B-C
            app.setSplit_view(splits[0]);//A，存储于数据库
            app.setSplit_mode(splits[1]);//B
            String split_widget = splits[2];//c
            Log.d(TAG,"this is split_view,split_mode,split_widget" + app.getSplit_view() +"***"+ app.getSplit_mode() + "***" + split_widget);

            initWidget(app.getSplit_mode());
            Log.d(TAG,"this is initWidget(split_mode)");

            for(File file : files){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
                getMap1().put(file.getName(), file.getAbsolutePath());//形成键值对，方便取出作为资源导入
            }

            String key = app.getSplit_view() + "-" + app.getSplit_mode();
            arrayList1 = getSonImage(getMap1().get(key + "-1").toString());
            arrayList2 = getSonImage(getMap1().get(key + "-2").toString());
            arrayList3 = getSonImage(getMap1().get(key + "-3").toString());
            arrayList4 = getSonImage(getMap1().get(key + "-4").toString());

            app.setSonSource(getMap1().get(key + "-1").toString() + "***" + getMap1().get(key + "-2").toString()
                    + "***" + getMap1().get(key + "-3").toString() + "***" + getMap1().get(key + "-4").toString());
        }else{
            initWidget(app.getSplit_mode());
            String[] strings = app.getSon_source().split("\\*\\*\\*");
            arrayList1 = getSonImage(strings[0]);
            arrayList2 = getSonImage(strings[1]);
            arrayList3 = getSonImage(strings[2]);
            arrayList4 = getSonImage(strings[3]);
        }

        if (app.getSplit_view() != null) {
            mBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG,"this is varReceiver");
                    playSonImage(arrayList1,arrayList2,arrayList3,arrayList4);
                }
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction("0");
            registerReceiver(mBroadcastReceiver,filter);//注册广播

            playSonImage(arrayList1,arrayList2,arrayList3,arrayList4);

            if (app.isExtraState()) {
//                app.setCreate_time(new Date());//new Date()出来的时间是本地时间
                if(app.getSource() == null){//这一步多余
                    app.setSource(new Source());//表不存在则新建赋值
                    daoManager.getSession().getSourceDao().insert(getSource(app.getSource()));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
                }else{//存在则直接修改
                    daoManager.getSession().getSourceDao().update(getSource(app.getSource()));
                }
//                app.getDevice().setAuthority_state(app.isAuthority_state());//device表在main中一定创建，故不为null
//                app.getDevice().setAuthority_time(app.getAuthority_time());
//                app.getDevice().setAuthority_expired(app.getAuthority_expired());
//                daoManager.getSession().getDeviceDao().update(app.getDevice());//更新表
//                Log.d(TAG,"this is done 数据存储");

            }
        } else {
            Log.d(TAG,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作");
            Toast.makeText(this,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//重写back为无效按键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Source getSource(Source source) {//对数据库进行覆写；不能直接调用一分屏得的该方法，函数体中非静态变量声明
        //source.setLicense_dir(app.getLicenceDir());
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

    boolean isFreeFlag1 = true;
    boolean isFreeFlag2 = true;
    boolean isFreeFlag3 = true;
    boolean isFreeFlag4 = true;
    ArrayList[] Recursive = new ArrayList[4];//先声明--仅用于存储递归时的参数

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
        alertDialog.setView(app.getView());
        final AlertDialog AlertDialogs = alertDialog.create();//如上是我自己找到新建的弹窗，下面是把新建的弹窗赋给新建的手势命令中的长按。
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

    private void playSonImage(ArrayList arrayList1,ArrayList arrayList2,ArrayList arrayList3,ArrayList arrayList4){
        if (app.isPlaySonImageFlag()) {
            Log.d(TAG,"this is 进入资源播放方法");
            Recursive[0] = arrayList1;//以赋值控件12为一个单元，整体递归：最笨的方法
            Recursive[1] = arrayList2;
            Recursive[2] = arrayList3;
            Recursive[3] = arrayList4;
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
                        app.setForMat1(1);//记录此时控件播放为图片
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
                        app.setForMat1(2);//记录此时控件播放为视频
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
                        app.setForMat2(1);//记录此时控件播放为图片
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
                        app.setForMat2(2);//记录此时控件播放为视频
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
                Log.d(TAG,"this is 此时空闲，进入设置控件2资源");
                if (app.getListNum3() >= arrayList3.size()) {
                    app.setListNum3(0);
//                    playSonImage(Recursive[0],Recursive[1],Recursive[2],Recursive[3]);
//            finish();
                }
                    Log.d(TAG,"开始执行执行播放程序");
                    app.setFile(new File(arrayList3.get(app.getListNum3()).toString()));
                    if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg")||app.getFile().getName().endsWith("png"))) {
                        Log.d(TAG,"playSonImage2执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                        app.setForMat3(1);//记录此时控件播放为图片
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
                        app.setForMat3(2);//记录此时控件播放为视频
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
                        app.setForMat4(1);
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
                        app.setForMat4(2);
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

    @Override
    protected void onResume() {
        super.onResume();
//        app.setPlayFlag(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.setMediaPlayState(false);
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

//        if (app.getRunnable1() != null) {
//            app.getHandler().removeCallbacks(app.getRunnable1());
//        }
//        if (app.getRunnable2() != null) {
//            app.getHandler().removeCallbacks(app.getRunnable2());
//        }
//        if (app.getRunnable3() != null) {
//            app.getHandler().removeCallbacks(app.getRunnable3());
//        }
//        if (app.getRunnable4() != null) {
//            app.getHandler().removeCallbacks(app.getRunnable4());
//        }
    }
}
