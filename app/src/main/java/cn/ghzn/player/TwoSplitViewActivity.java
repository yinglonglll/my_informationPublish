package cn.ghzn.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.ghzn.player.receiver.VarReceiver;
import cn.ghzn.player.sqlite.source.Source;
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.ImportActivity.getMap1;
import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.util.InfoUtils.getRandomString;
import static cn.ghzn.player.util.ViewImportUtils.getSonImage;

public class TwoSplitViewActivity extends Activity {
    private static final String TAG = "TwoSplitViewActivity";
    private Runnable mRunnable;
    private GestureDetector mGestureDetector;
    private BroadcastReceiver mBroadcastReceiver;

    private ArrayList arrayList1;//控件区1地址
    private ArrayList arrayList2;//控件区2地址



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app.setCurrentActivity(this);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
        app.setMediaPlayState(true);
        Log.d(TAG,"this is 跳转成功");
        if (app.isExtraState()) {//若是U盘插入时，文件为不合规定，此时U盘也仍未插入状态true
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
            File[] files = f.listFiles();//展开父类文件夹ghznPlayer

            String[] splits = files[0].getName().split("\\-");//A-B-C；任取一文件夹，仅作为数据库存储信息的参考对象
            app.setSplit_view(splits[0]);
            app.setSplit_mode(splits[1]);
            String split_widget = splits[2];
            Log.d(TAG,"this is split_view,split_mode,split_widget" + app.getSplit_view() +"***"+ app.getSplit_mode() + "***" + split_widget);

            initWidget(app.getSplit_mode());
            Log.d(TAG,"this is initWidget(split_mode)");

            for(File file : files){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
                getMap1().put(file.getName(), file.getAbsolutePath());//形成键值对，方便取出作为资源导入
            }

            String key = app.getSplit_view() + "-" + app.getSplit_mode();//直接按分配类型取文件
            arrayList1 = getSonImage(getMap1().get(key + "-1").toString());
            arrayList2 = getSonImage(getMap1().get(key + "-2").toString());//获取到有效的资源信息

            app.setSonSource(getMap1().get(key + "-1").toString() + "***" + getMap1().get(key + "-2").toString());//data：存储子文件夹的地址
            //取出时记得拆分
        }else {
            initWidget(app.getSplit_mode());
            String[] strings = app.getSon_source().split("\\*\\*\\*");
            arrayList1 = getSonImage(strings[0]);
            arrayList2 = getSonImage(strings[1]);
        }
        if (app.getSplit_view() != null) {
            mBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG,"this is varReceiver");
                    playSonImage(arrayList1,arrayList2);
                }
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction("0");
            registerReceiver(mBroadcastReceiver,filter);//注册广播

            playSonImage(arrayList1,arrayList2);
            if (app.isExtraState()) {
//                app.setCreate_time(new Date());//new Date()出来的时间是本地时间
                if(app.getSource() == null){//这一步多余
                    app.setSource(new Source());//表不存在则新建赋值
                    daoManager.getSession().getSourceDao().insert(getSource(app.getSource()));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
                }else{//存在则直接修改
                    daoManager.getSession().getSourceDao().update(getSource(app.getSource()));
                }
                app.getDevice().setAuthority_state(app.isAuthority_state());//device表在main中一定创建，故不为null
                app.getDevice().setAuthority_time(app.getAuthority_time());
                app.getDevice().setAuthority_expired(app.getAuthority_expired());
                daoManager.getSession().getDeviceDao().update(app.getDevice());//更新表
                Log.d(TAG,"this is done 数据存储");

            }
        }else {
            Log.d(TAG,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作");
            Toast.makeText(this,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作",Toast.LENGTH_LONG).show();
        }
        //todo:3.成功执行，数据为有效数据，才把信息存储到数据库中，完成更新；以便没U盘插入时，直接执行另外一个activity，取出赋值{KEY:A,B,ghznPlayer内所有文件的绝对地址以寻资源的地址键值对，}
    }

    private Source getSource(Source source) {//对数据库进行覆写；不能直接调用一分屏得的该方法，函数体中非静态变量声明
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
        switch (split_mode){
            case "1":
                setContentView(R.layout.activity_splitview_two1);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_two1_1));
                app.setVideoView_1((CustomVideoView) this.findViewById(R.id.videoView_two1_1));
                app.setImageView_2((ImageView)this.findViewById(R.id.imageView_two1_2));
                app.setVideoView_2((CustomVideoView) this.findViewById(R.id.videoView_two1_2));

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
                Log.d(TAG,"this is case \"1\":");
                break;
            case "2":
                setContentView(R.layout.activity_splitview_two2);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_two2_1));
                app.setVideoView_1((CustomVideoView)this.findViewById(R.id.videoView_two2_1));
                app.setImageView_2((ImageView)this.findViewById(R.id.imageView_two2_2));
                app.setVideoView_2((CustomVideoView)this.findViewById(R.id.videoView_two2_2));

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
                Log.d(TAG,"this is case \"2\":");
                break;
            case "3":
                setContentView(R.layout.activity_splitview_two3);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_two3_1));
                app.setVideoView_1((CustomVideoView)this.findViewById(R.id.videoView_two3_1));
                app.setImageView_2((ImageView)this.findViewById(R.id.imageView_two3_2));
                app.setVideoView_2((CustomVideoView)this.findViewById(R.id.videoView_two3_2));

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
                break;
            case "4":
                setContentView(R.layout.activity_splitview_two4);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_two4_1));
                app.setVideoView_1((CustomVideoView)this.findViewById(R.id.videoView_two4_1));
                app.setImageView_2((ImageView)this.findViewById(R.id.imageView_two4_2));
                app.setVideoView_2((CustomVideoView)this.findViewById(R.id.videoView_two4_2));

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
                break;
            case "5":
                setContentView(R.layout.activity_splitview_two5);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_two5_1));
                app.setVideoView_1((CustomVideoView)this.findViewById(R.id.videoView_two5_1));
                app.setImageView_2((ImageView)this.findViewById(R.id.imageView_two5_2));
                app.setVideoView_2((CustomVideoView)this.findViewById(R.id.videoView_two5_2));

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
                break;
            case "6":
                setContentView(R.layout.activity_splitview_two6);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_two6_1));
                app.setVideoView_1((CustomVideoView)this.findViewById(R.id.videoView_two6_1));
                app.setImageView_2((ImageView)this.findViewById(R.id.imageView_two6_2));
                app.setVideoView_2((CustomVideoView)this.findViewById(R.id.videoView_two6_2));

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
                break;
            default:
                Log.d(TAG,"this is switch(split_mode)_default");
                break;
        }
    }

//    ArrayList[] Recursive1 = new ArrayList[1];//先声明--仅用于存储递归时的参数
//    ArrayList[] Recursive2 = new ArrayList[1];//先声明--仅用于存储递归时的参数

//    int listNum1 = 0;//用于记录单个文件夹循环时，处于第几个图片或视频；
//    int listNum2 = 0;
    boolean isFreeFlag1 = true;
    boolean isFreeFlag2 = true;
    ArrayList[] Recursive = new ArrayList[2];//先声明--仅用于存储递归时的参数

    private void playSonImage(ArrayList arrayList1,ArrayList arrayList2){
        Recursive[0] = arrayList1;//以赋值控件12为一个单元，整体递归：最笨的方法
        Recursive[1] = arrayList2;
        if (isFreeFlag1) {
            Log.d(TAG,"this is 此时空闲，进入设置控件1资源");
            if (app.getListNum1() >= arrayList1.size()) {
                app.setListNum1(0);//循环要求，仅重置变量为0功能
//                playSonImage(Recursive[0],Recursive[1]);
//            finish();
            }
                Log.d(TAG,"开始执行执行播放程序");
                app.setFile(new File(arrayList1.get(app.getListNum1()).toString()));
                if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg")||app.getFile().getName().endsWith("png"))) {
                    Log.d(TAG,"playSonImage1执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                    app.setForMat1(1);//记录此时控件播放为图片
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
                            if (app.getPlayFlag() == 0) {
                                playSonImage(Recursive[0], Recursive[1]);
                            }
                        }
                    }, app.getDelayMillis());//5秒后结束当前图片
                    app.setRunnable1(mRunnable);//无法set线程，只好绑定mRunnable到全局Runnable，用于暂停时取消线程

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
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
                                app.setListNum1(app.getListNum1() + 1);
                                isFreeFlag1 = true;//视频赋值程序完成，退出忙线状态
                                playSonImage(Recursive[0],Recursive[1]);
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
//                playSonImage(Recursive[0],Recursive[1]);
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


                    app.setStartTime(System.currentTimeMillis());
                    app.getHandler().postDelayed(mRunnable = new Runnable(){
                        @Override
                        public void run() {
                            Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + app.getFile().getAbsolutePath());
                            isFreeFlag2 = true;//图片赋值程序完成，退出忙线状态
                            if (app.getPlayFlag() == 0) {
                                playSonImage(Recursive[0], Recursive[1]);
                            }
                        }
                    },3000);//3秒后结束当前图片
                    app.setRunnable2(mRunnable);//无法set线程，只好绑定mRunnable到全局Runnable，用于暂停时取消线程

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
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
                                app.setListNum2(app.getListNum2() + 1);
                                isFreeFlag2 = true;//视频赋值程序完成，退出忙线状态
                                playSonImage(Recursive[0],Recursive[1]);
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
    }

//    private void playSonImage1(ArrayList arrayList){
//        Recursive1[0] = arrayList;//后使用
//        Log.d(TAG,"this is thread1");
//
//        if (listNum1 >= arrayList.size()) {
//            listNum1 = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
//            playSonImage1(Recursive1[0]);
////            finish();
//        } else {
//            Log.d(TAG,"开始执行执行播放程序");
//
//            final File f = new File(arrayList.get(listNum1).toString());
//            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
//                Log.d(TAG,"playSonImage1执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
//
//                videoView_1.setVisibility(View.GONE);
//                imageView_1.setVisibility(View.VISIBLE);
////                imageView_2.setVisibility(View.VISIBLE);
////                videoView_2.setVisibility(View.INVISIBLE);
//                imageView_1.setImageURI(Uri.fromFile(f));
////                imageView_2.setImageURI(Uri.fromFile(f));
////                Log.d(TAG,"this is Uri.fromFile(f)" + Uri.fromFile(f));
//
//                mHandler = new Handler();
//                mHandler.postDelayed(new Runnable(){
//                    @Override
//                    public void run() {
//                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
//                        listNum1++;
//                        playSonImage1(Recursive1[0]);
//                    }
//                },3000);//3秒后结束当前图片
//
//            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
//                Log.d(TAG,"playSonImage1执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
//
//                imageView_1.setVisibility(View.GONE);
//                videoView_1.setVisibility(View.VISIBLE);
////                videoView_2.setVisibility(View.VISIBLE);
////                imageView_2.setVisibility(View.INVISIBLE);
//                videoView_1.setVideoURI(Uri.fromFile(f));
////                videoView_2.setVideoPath(f.getAbsolutePath());
////                Log.d(TAG,"this is videoView_1.setVideoURI(Uri.fromFile(f)) ：" + Uri.fromFile(f));
//                videoView_1.start();
////                videoView_2.start();
////                Log.d(TAG,"this is videoView_1.start()");
//                videoView_1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
////                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
//                        listNum1++;
//                        playSonImage1(Recursive1[0]);
//                    }
//                });
////                videoView_2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
////                    @Override
////                    public void onCompletion(MediaPlayer mp) {
////                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
////
////                        videoView_2.setVisibility(View.GONE);
////                        listNum++;
////                        playSonImage();
////                    }
////                });
//            }
//        }
//    }
//
//    private void playSonImage2(ArrayList arrayList){
//        Recursive2[0] = arrayList;
//
//        if (listNum2 >= arrayList.size()) {
//            listNum2 = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
//            playSonImage1(Recursive2[0]);
////            finish();
//        } else {
//            Log.d(TAG,"开始执行执行播放程序");
//            final File f = new File(arrayList.get(listNum2).toString());
//            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
//                Log.d(TAG,"playSonImage2执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
//
//                videoView_2.setVisibility(View.GONE);
//                imageView_2.setVisibility(View.VISIBLE);
//                imageView_2.setImageURI(Uri.fromFile(f));
//
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable(){
//                    @Override
//                    public void run() {
////                        Looper.prepare();
//                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
//                        listNum2++;
//                        playSonImage2( Recursive2[0]);
////                        Looper.loop();
//                    }
//                },3000);//3秒后结束当前图片
//            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
//                Log.d(TAG,"playSonImage2执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
//
//                imageView_2.setVisibility(View.GONE);
//                videoView_2.setVisibility(View.VISIBLE);
//                videoView_2.setVideoURI(Uri.fromFile(f));
////                Log.d(TAG,"this is videoView_2.setVideoURI(Uri.fromFile(f)); ：" + Uri.fromFile(f));
////                LogUtils.e(videoView_2);
//                videoView_2.start();
////                Log.d(TAG,"this is videoView_2.start()");
//                videoView_2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
////                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
//                        listNum2++;
//                        playSonImage2(Recursive2[0]);
//                    }
//                });
//            }
//        }
//    }

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

//    private class playRunnable1 implements Runnable{
//        @Override
//        public void run() {
//            Looper.prepare();
//            playSonImage1(arrayList1);
////            Looper.loop();
//        }
//    }
//    private class playRunnable2 implements Runnable{
//        @Override
//        public void run() {
//            Looper.prepare();
//            playSonImage2(arrayList2);
//        }
//    }


    @Override
    protected void onResume() {
        super.onResume();
        app.setPlayFlag(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.setMediaPlayState(false);
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        if (app.getRunnable1() != null) {
            app.getHandler().removeCallbacks(app.getRunnable1());
        }
        if (app.getRunnable2() != null) {
            app.getHandler().removeCallbacks(app.getRunnable2());
        }
    }

}
