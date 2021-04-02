package cn.ghzn.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import cn.ghzn.player.sqlite.source.Source;
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.ImportActivity.getMap1;
import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.util.InfoUtils.getRandomString;
import static cn.ghzn.player.util.ViewImportUtils.getSonImage;

public class ThreeSplitViewActivity extends Activity {
    private static final String TAG = "ThreeSplitViewActivity";

    private static ImageView imageView_1;
    private static ImageView imageView_2;
    private static ImageView imageView_3;
    private static CustomVideoView videoView_1;
    private static CustomVideoView videoView_2;
    private static CustomVideoView videoView_3;
    private static Handler mHandler;
    private GestureDetector mGestureDetector;

    ArrayList arrayList1;//控件区1地址
    ArrayList arrayList2;//控件区2地址
    ArrayList arrayList3;//控件区2地址

    public static ImageView getImageView_1() {
        return imageView_1;
    }

    public static ImageView getImageView_2() {
        return imageView_2;
    }

    public static ImageView getImageView_3() {
        return imageView_3;
    }

    public static CustomVideoView getVideoView_1() {
        return videoView_1;
    }

    public static CustomVideoView getVideoView_2() {
        return videoView_2;
    }

    public static CustomVideoView getVideoView_3() {
        return videoView_3;
    }

    public static Handler getHandler() {
        return mHandler;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
        Log.d(TAG,"this is 跳转成功");
        if (app.isExtraState()) {
            Intent intent = getIntent();
            int fileCounts = intent.getIntExtra("splitView",0);//以文件的数量获取分屏样式，
            String filesParent = intent.getStringExtra("filesParent");
            Log.d(TAG,"this is splitView" + fileCounts);
            Log.d(TAG,"this is filesParent" + filesParent);

            File f = new File(filesParent);
            if (!f.exists()) {
                f.mkdirs();//区分之二：创建多级目录和创建当前目录区别
            }
            File[] files = f.listFiles();//展开父类文件夹

            String[] splits = files[0].getName().split("\\-");//A-B-C；任取一文件夹，仅作为数据库存储信息的参考对象
            app.setSplit_view(splits[0]);//A，存储于数据库
            app.setSplit_mode(splits[1]);//B
            String split_widget = splits[2];//c
            Log.d(TAG,"this is split_view,split_mode,split_widget" + app.getSplit_view() +"***"+ app.getSplit_mode() + "***" + split_widget);

            for(File file : files){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
                getMap1().put(file.getName(), file.getAbsolutePath());//形成键值对，方便取出作为资源导入
            }

            initWidget(app.getSplit_mode());
            Log.d(TAG,"this is initWidget(split_mode)");

            String key = app.getSplit_view() + "-" + app.getSplit_mode();
            arrayList1 = getSonImage(getMap1().get(key + "-1").toString());
            arrayList2 = getSonImage(getMap1().get(key + "-2").toString());
            arrayList3 = getSonImage(getMap1().get(key + "-3").toString());

            app.setSonSource(getMap1().get(key + "-1").toString() + "***" + getMap1().get(key + "-2").toString() + "***" + getMap1().get(key + "-3").toString());
        }else{
            initWidget(app.getSplit_mode());
            String[] strings = app.getSon_source().split("\\*\\*\\*");
            arrayList1 = getSonImage(strings[0]);
            arrayList2 = getSonImage(strings[1]);
            arrayList3 = getSonImage(strings[2]);
        }
        if (app.getSplit_view() != null) {
            playSonImage(arrayList1,arrayList2,arrayList3);
            if (app.isExtraState()) {
                app.setCreate_time(new Date());//new Date()出来的时间是本地时间
                if(app.getSource() == null){//这一步多余
                    app.setSource(new Source());//表不存在则新建赋值
                    daoManager.getSession().getSourceDao().insert(getSource(app.getSource()));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
                }else{//存在则直接修改
                    daoManager.getSession().getSourceDao().update(getSource(app.getSource()));
                }
            }
        }else {
            Log.d(TAG,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作");
            Toast.makeText(this,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作",Toast.LENGTH_LONG).show();
        }
        //todo:3.成功执行，数据为有效数据，才把信息存储到数据库中，完成更新；以便没U盘插入时，直接执行另外一个activity，取出赋值，
    }

    private Source getSource(Source source) {//对数据库进行覆写；不能直接调用一分屏得的该方法，函数体中非静态变量声明
        source.setProgram_id(getRandomString(5));
        source.setSplit_view(app.getSplit_view());
        source.setSplit_mode(app.getSplit_mode());
        source.setSon_source(app.getSonSource());//存储的是子资源，但取出来用时需用来获取对象。
        return source;
    }
    int listNum1 = 0;//用于记录单个文件夹循环时，处于第几个图片或视频；
    int listNum2 = 0;
    int listNum3 = 0;
    boolean isFreeFlag1 = true;
    boolean isFreeFlag2 = true;
    boolean isFreeFlag3 = true;
    ArrayList[] Recursive = new ArrayList[3];//先声明--仅用于存储递归时的参数
    private void playSonImage(ArrayList arrayList1,ArrayList arrayList2,ArrayList arrayList3){
        Recursive[0] = arrayList1;//以赋值控件12为一个单元，整体递归：最笨的方法
        Recursive[1] = arrayList2;
        Recursive[2] = arrayList3;

        if (isFreeFlag1) {
            Log.d(TAG,"this is 此时空闲，进入设置控件1资源");
            if (listNum1 >= arrayList1.size()) {
                listNum1 = 0;//循环要求，仅重置变量为0功能
                playSonImage(Recursive[0],Recursive[1],Recursive[2]);
//            finish();
            } else {
                Log.d(TAG,"开始执行执行播放程序");
                final File f = new File(arrayList1.get(listNum1).toString());
                if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
                    Log.d(TAG,"playSonImage1执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
                    isFreeFlag1 = false;//进入图片赋值程序，先设为忙线状态

                    imageView_1.setImageURI(Uri.fromFile(f));
                    imageView_1.setVisibility(View.VISIBLE);
                    videoView_1.setVisibility(View.GONE);

                    mHandler = new Handler();
                    mHandler.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
                            listNum1++;
                            isFreeFlag1 = true;//图片赋值程序完成，退出忙线状态
                            playSonImage(Recursive[0],Recursive[1],Recursive[2]);
                        }
                    },3000);//3秒后结束当前图片
                } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
                    Log.d(TAG,"playSonImage1执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
                    isFreeFlag1 = false;//进入图片赋值程序，先设为忙线状态

                    videoView_1.setVideoURI(Uri.fromFile(f));
                    videoView_1.setVisibility(View.VISIBLE);
                    imageView_1.setVisibility(View.GONE);
                    videoView_1.start();
                    try {
                        Thread.sleep(1000);//默认设置1S给videoView加载视频的时间，实际上读取视频都有加载导致黑屏，目前暂无找到合适方法解决
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    videoView_1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
                            listNum1++;
                            isFreeFlag1 = true;//视频赋值程序完成，退出忙线状态
                            playSonImage(Recursive[0],Recursive[1],Recursive[2]);
                        }
                    });
                }
            }
        }
        if (isFreeFlag2) {
            Log.d(TAG,"this is 此时空闲，进入设置控件2资源");
            if (listNum2 >= arrayList2.size()) {
                listNum2 = 0;
                playSonImage(Recursive[0],Recursive[1],Recursive[2]);
//            finish();
            } else {
                Log.d(TAG,"开始执行执行播放程序");
                final File f = new File(arrayList2.get(listNum2).toString());
                if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
                    Log.d(TAG,"playSonImage2执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
                    isFreeFlag2 = false;//执行图片赋值程序，进入忙线状态

                    imageView_2.setImageURI(Uri.fromFile(f));
                    imageView_2.setVisibility(View.VISIBLE);
                    videoView_2.setVisibility(View.GONE);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
                            listNum2++;
                            isFreeFlag2 = true;//图片赋值程序完成，退出忙线状态
                            playSonImage(Recursive[0],Recursive[1],Recursive[2]);
                        }
                    },3000);//3秒后结束当前图片

                } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
                    Log.d(TAG,"playSonImage2执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
                    isFreeFlag2 = false;//执行视频赋值程序，进入忙线状态


                    videoView_2.setVideoURI(Uri.fromFile(f));
                    videoView_2.setVisibility(View.VISIBLE);
                    imageView_2.setVisibility(View.GONE);
//                LogUtils.e(videoView_2);
//                    videoView_2.seekTo(1);
                    videoView_2.start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    videoView_2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
                            listNum2++;
                            isFreeFlag2 = true;//视频赋值程序完成，退出忙线状态
                            playSonImage(Recursive[0],Recursive[1],Recursive[2]);
                        }
                    });
                }
            }
        }
        if (isFreeFlag3) {
            Log.d(TAG,"this is 此时空闲，进入设置控件2资源");
            if (listNum3 >= arrayList3.size()) {
                listNum3 = 0;
                playSonImage(Recursive[0],Recursive[1],Recursive[2]);
//            finish();
            } else {
                Log.d(TAG,"开始执行执行播放程序");
                final File f = new File(arrayList3.get(listNum3).toString());
                if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
                    Log.d(TAG,"playSonImage2执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
                    isFreeFlag3 = false;//执行图片赋值程序，进入忙线状态

                    imageView_3.setImageURI(Uri.fromFile(f));
                    imageView_3.setVisibility(View.VISIBLE);
                    videoView_3.setVisibility(View.GONE);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
                            listNum3++;
                            isFreeFlag3 = true;//图片赋值程序完成，退出忙线状态
                            playSonImage(Recursive[0],Recursive[1],Recursive[2]);
                        }
                    },3000);//3秒后结束当前图片

                } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
                    Log.d(TAG,"playSonImage2执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
                    isFreeFlag3 = false;//执行视频赋值程序，进入忙线状态


                    videoView_3.setVideoURI(Uri.fromFile(f));
                    videoView_3.setVisibility(View.VISIBLE);
                    imageView_3.setVisibility(View.GONE);
//                LogUtils.e(videoView_2);
//                    videoView_2.seekTo(1);
                    videoView_3.start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    videoView_3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
                            listNum3++;
                            isFreeFlag3 = true;//视频赋值程序完成，退出忙线状态
                            playSonImage(Recursive[0],Recursive[1],Recursive[2]);
                        }
                    });
                }
            }
        }
    }

//    private void playSonImage1(ArrayList arrayList){
////            final ArrayList reArrayList = arrayList;//给递归函数传参数,因不知获取参数个数的函数
//        final ArrayList[] Recursive = new ArrayList[1];//注意，使用该方法的前提是已经带入 一个 子文件夹的绝对路径
//        Recursive[0] = arrayList;
//
//        if (listNum1 >= arrayList.size()) {
//
//            listNum1 = 0;
//            finish();
//        } else {
//            Log.d(TAG,"开始执行执行播放程序");
//
//            final File f = new File(arrayList.get(listNum1).toString());
//
//            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
//                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
//
//                //控件1
//                imageView_1.setVisibility(View.VISIBLE);
//                videoView_1.setVisibility(View.INVISIBLE);
//
//                imageView_1.setImageURI(Uri.fromFile(f.getAbsoluteFile()));
//
//                mHandler = new Handler();
//                mHandler.postDelayed(new Runnable(){
//                    @Override
//                    public void run() {
//                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
//
//                        imageView_1.setVisibility(View.GONE);
//                        listNum1++;
//                        playSonImage1( Recursive[0]);
//                    }
//                },3000);//3秒后结束当前图片
//            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
//
//                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
//
//                //控件1
//                videoView_1.setVisibility(View.VISIBLE);
//                imageView_1.setVisibility(View.INVISIBLE);
//
//                videoView_1.setVideoPath(f.getAbsolutePath());
//
//                videoView_1.start();
//
//                videoView_1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
//
//                        videoView_1.setVisibility(View.GONE);
//                        listNum1++;
//                        playSonImage1(Recursive[0]);
//                    }
//                });
//            }
//        }
//    }
//
//    private void playSonImage2(ArrayList arrayList){
//        final ArrayList[] Recursive = new ArrayList[1];//注意，使用该方法的前提是已经带入 一个 子文件夹的绝对路径
//        Recursive[0] = arrayList;
//
//        if (listNum2 >= arrayList.size()) {
//
//            listNum2 = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
//            finish();
//        } else {
//            Log.d(TAG,"开始执行执行播放程序");
//
//            final File f = new File(arrayList.get(listNum2).toString());
//
//            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
//                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
//
//                //控件1
//
//                imageView_2.setVisibility(View.VISIBLE);
//                videoView_2.setVisibility(View.INVISIBLE);
//
//
//                imageView_2.setImageURI(Uri.fromFile(f.getAbsoluteFile()));
//
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable(){
//                    @Override
//                    public void run() {
//                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
//
//                        imageView_2.setVisibility(View.GONE);
//                        listNum2++;
//                        playSonImage2( Recursive[0]);
//                    }
//                },3000);//3秒后结束当前图片
//            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
//                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
//
//                //控件1
//                videoView_2.setVisibility(View.VISIBLE);
//                imageView_2.setVisibility(View.INVISIBLE);
//                videoView_2.setVideoPath(f.getAbsolutePath());
//                videoView_2.start();
//                videoView_2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
//
//                        videoView_2.setVisibility(View.GONE);
//                        listNum2++;
//                        playSonImage2( Recursive[0]);
//                    }
//                });
//            }
//        }
//    }
//
//    private void playSonImage3(ArrayList arrayList){
//        final ArrayList[] Recursive = new ArrayList[1];//注意，使用该方法的前提是已经带入 一个 子文件夹的绝对路径
//        Recursive[0] = arrayList;
//
//        if (listNum3 >= arrayList.size()) {
//
//            listNum3 = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
//            finish();
//        } else {
//            Log.d(TAG,"开始执行执行播放程序");
//
//            final File f = new File(arrayList.get(listNum3).toString());
//
//            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
//                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
//
//                //控件1
//
//                imageView_3.setVisibility(View.VISIBLE);
//                videoView_3.setVisibility(View.INVISIBLE);
//
//
//                imageView_3.setImageURI(Uri.fromFile(f.getAbsoluteFile()));
//
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable(){
//                    @Override
//                    public void run() {
//                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
//
//                        imageView_3.setVisibility(View.GONE);
//                        listNum3++;
//                        playSonImage2( Recursive[0]);
//                    }
//                },3000);//3秒后结束当前图片
//            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
//                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
//
//                //控件1
//                videoView_3.setVisibility(View.VISIBLE);
//                imageView_3.setVisibility(View.INVISIBLE);
//                videoView_3.setVideoPath(f.getAbsolutePath());
//                videoView_3.start();
//                videoView_3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
//
//                        videoView_3.setVisibility(View.GONE);
//                        listNum3++;
//                        playSonImage2( Recursive[0]);
//                    }
//                });
//            }
//        }
//    }

    @SuppressLint("ClickableViewAccessibility")
    private void initWidget(String split_mode) {
        switch (split_mode){
            case "1":
                setContentView(R.layout.activity_splitview_three1);
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three1_1);
                videoView_1 = (CustomVideoView) this.findViewById(R.id.videoView_three1_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three1_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three1_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three1_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three1_3);

                setDialog(this);
                imageView_1.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                imageView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"1\":");
                break;
            case "2":
                setContentView(R.layout.activity_splitview_three2);
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three2_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three2_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three2_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three2_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three2_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three2_3);

                setDialog(this);
                imageView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                imageView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"2\":");
                break;
            case "3":
                setContentView(R.layout.activity_splitview_three3);
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three3_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three3_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three3_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three3_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three3_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three3_3);

                setDialog(this);
                imageView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                imageView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"2\":");
                break;
            case "4":
                setContentView(R.layout.activity_splitview_three4);
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three4_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three4_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three4_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three4_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three4_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three4_3);

                setDialog(this);
                imageView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                imageView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"2\":");
                break;
            case "5":
                setContentView(R.layout.activity_splitview_three5);
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three5_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three5_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three5_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three5_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three5_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three5_3);

                setDialog(this);
                imageView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                imageView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"2\":");
                break;
            case "6":
                setContentView(R.layout.activity_splitview_three6);
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three6_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three6_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three6_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three6_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three6_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three6_3);

                setDialog(this);
                imageView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                imageView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"2\":");
                break;
            case "7":
                setContentView(R.layout.activity_splitview_three7);
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three7_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three7_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three7_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three7_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three7_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three7_3);

                setDialog(this);
                imageView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                imageView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"2\":");
                break;
            case "8":
                setContentView(R.layout.activity_splitview_three8);
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three8_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three8_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three8_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three8_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three8_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three8_3);

                setDialog(this);
                imageView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_1.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                imageView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                videoView_2.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"2\":");
                break;
            default:
                Log.d(TAG,"default");
                break;
        }
    }
    public void setDialog(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        View View = this.getLayoutInflater().inflate(R.layout.activity_dialog, null);
        alertDialog.setView(View);
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
}
