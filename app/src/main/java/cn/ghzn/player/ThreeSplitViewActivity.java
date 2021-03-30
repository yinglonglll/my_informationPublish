package cn.ghzn.player;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.ImportActivity.getMap1;

public class ThreeSplitViewActivity extends Activity {

    private static final String TAG = "ThreeSplitViewActivity";
    private static ImageView imageView_1;
    private static ImageView imageView_2;
    private static ImageView imageView_3;
    private static CustomVideoView videoView_1;
    private static CustomVideoView videoView_2;
    private static CustomVideoView videoView_3;
    private static Handler mHandler;

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

        Intent intent = getIntent();
        int splitView = intent.getIntExtra("splitView",0);//以文件的数量获取分屏样式，
        String filesParent = intent.getStringExtra("filesParent");
        File f = new File(filesParent);
        File[] files = f.listFiles();//展开父类文件夹

        String[] splits = files[0].getName().split("\\-");//A-B-C；任取一文件夹，仅作为数据库存储信息的参考对象
        String split_view = splits[0];//A，存储于数据库
        String split_mode = splits[1];//B
        String split_widget = splits[2];//c

        for(File file : files){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
            getMap1().put(file.getName(), file.getAbsolutePath());//形成键值对，方便取出作为资源导入
        }
        initWidget(split_mode);

        String key = split_view + "\\-" + split_mode;
        arrayList1 = ViewImportUtils.getSonImage(getMap1().get(key + "\\-1").toString());
        arrayList2 = ViewImportUtils.getSonImage(getMap1().get(key + "\\-2").toString());
        arrayList3 = ViewImportUtils.getSonImage(getMap1().get(key + "\\-3").toString());

        if (getMap1().size() == splitView) {
            playSonImage1(arrayList1);//对控件1进行赋值
            playSonImage2(arrayList2);//对控件2进行赋值
            playSonImage3(arrayList3);//对控件3进行赋值

            switch (split_mode) {
                case "1":
                    setContentView(R.layout.activity_splitview_three1);
                    break;
                case "2":
                    setContentView(R.layout.activity_splitview_three2);
                    break;
                case "3":
                    setContentView(R.layout.activity_splitview_three3);
                    break;
                case "4":
                    setContentView(R.layout.activity_splitview_three4);
                    break;
                case "5":
                    setContentView(R.layout.activity_splitview_three5);
                    break;
                case "6":
                    setContentView(R.layout.activity_splitview_three6);
                    break;
                case "7":
                    setContentView(R.layout.activity_splitview_three7);
                    break;
                case "8":
                    setContentView(R.layout.activity_splitview_three8);
                    break;
                default:
                    Log.d(TAG,"Location is switch(split_mode)_default");
                    break;
            }
        }else {
            Log.d(TAG,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作");
            Toast.makeText(this,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作",Toast.LENGTH_LONG).show();
        }
        //todo:3.成功执行，数据为有效数据，才把信息存储到数据库中，完成更新；以便没U盘插入时，直接执行另外一个activity，取出赋值，
    }


    int listNum1 = 0;//用于记录单个文件夹循环时，处于第几个图片或视频；
    int listNum2 = 0;
    int listNum3 = 0;
    private void playSonImage1(ArrayList arrayList){
//            final ArrayList reArrayList = arrayList;//给递归函数传参数,因不知获取参数个数的函数
        final ArrayList[] Recursive = new ArrayList[1];//注意，使用该方法的前提是已经带入 一个 子文件夹的绝对路径
        Recursive[0] = arrayList;

        if (listNum1 >= arrayList.size()) {

            listNum1 = 0;
            finish();
        } else {
            Log.d(TAG,"开始执行执行播放程序");

            final File f = new File(arrayList.get(listNum1).toString());

            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());

                //控件1
                imageView_1.setVisibility(View.VISIBLE);
                videoView_1.setVisibility(View.INVISIBLE);

                imageView_1.setImageURI(Uri.fromFile(f.getAbsoluteFile()));

                mHandler = new Handler();
                mHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());

                        imageView_1.setVisibility(View.GONE);
                        listNum1++;
                        playSonImage1( Recursive[0]);
                    }
                },3000);//3秒后结束当前图片
            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {

                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());

                //控件1
                videoView_1.setVisibility(View.VISIBLE);
                imageView_1.setVisibility(View.INVISIBLE);

                videoView_1.setVideoPath(f.getAbsolutePath());

                videoView_1.start();

                videoView_1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());

                        videoView_1.setVisibility(View.GONE);
                        listNum1++;
                        playSonImage1(Recursive[0]);
                    }
                });
            }
        }
    }

    private void playSonImage2(ArrayList arrayList){
        final ArrayList[] Recursive = new ArrayList[1];//注意，使用该方法的前提是已经带入 一个 子文件夹的绝对路径
        Recursive[0] = arrayList;

        if (listNum2 >= arrayList.size()) {

            listNum2 = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
            finish();
        } else {
            Log.d(TAG,"开始执行执行播放程序");

            final File f = new File(arrayList.get(listNum2).toString());

            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());

                //控件1

                imageView_2.setVisibility(View.VISIBLE);
                videoView_2.setVisibility(View.INVISIBLE);


                imageView_2.setImageURI(Uri.fromFile(f.getAbsoluteFile()));

                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());

                        imageView_2.setVisibility(View.GONE);
                        listNum2++;
                        playSonImage2( Recursive[0]);
                    }
                },3000);//3秒后结束当前图片
            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());

                //控件1
                videoView_2.setVisibility(View.VISIBLE);
                imageView_2.setVisibility(View.INVISIBLE);
                videoView_2.setVideoPath(f.getAbsolutePath());
                videoView_2.start();
                videoView_2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());

                        videoView_2.setVisibility(View.GONE);
                        listNum2++;
                        playSonImage2( Recursive[0]);
                    }
                });
            }
        }
    }

    private void playSonImage3(ArrayList arrayList){
        final ArrayList[] Recursive = new ArrayList[1];//注意，使用该方法的前提是已经带入 一个 子文件夹的绝对路径
        Recursive[0] = arrayList;

        if (listNum3 >= arrayList.size()) {

            listNum3 = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
            finish();
        } else {
            Log.d(TAG,"开始执行执行播放程序");

            final File f = new File(arrayList.get(listNum3).toString());

            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());

                //控件1

                imageView_3.setVisibility(View.VISIBLE);
                videoView_3.setVisibility(View.INVISIBLE);


                imageView_3.setImageURI(Uri.fromFile(f.getAbsoluteFile()));

                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());

                        imageView_3.setVisibility(View.GONE);
                        listNum3++;
                        playSonImage2( Recursive[0]);
                    }
                },3000);//3秒后结束当前图片
            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());

                //控件1
                videoView_3.setVisibility(View.VISIBLE);
                imageView_3.setVisibility(View.INVISIBLE);
                videoView_3.setVideoPath(f.getAbsolutePath());
                videoView_3.start();
                videoView_3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());

                        videoView_3.setVisibility(View.GONE);
                        listNum3++;
                        playSonImage2( Recursive[0]);
                    }
                });
            }
        }
    }

    private void initWidget(String split_mode) {
        switch (split_mode){
            case "1":
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three1_1);
                videoView_1 = (CustomVideoView) this.findViewById(R.id.videoView_three1_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three1_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three1_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three1_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three1_3);
                break;
            case "2":
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three2_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three2_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three2_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three2_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three2_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three2_3);
                break;
            case "3":
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three3_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three3_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three3_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three3_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three3_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three3_3);
                break;
            case "4":
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three4_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three4_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three4_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three4_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three4_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three4_3);
                break;
            case "5":
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three5_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three5_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three5_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three5_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three5_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three5_3);
                break;
            case "6":
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three6_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three6_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three6_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three6_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three6_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three6_3);
                break;
            case "7":
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three7_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three7_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three7_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three7_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three7_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three7_3);
                break;
            case "8":
                imageView_1 = (ImageView)this.findViewById(R.id.imageView_three8_1);
                videoView_1 = (CustomVideoView)this.findViewById(R.id.videoView_three8_1);
                imageView_2 = (ImageView)this.findViewById(R.id.imageView_three8_2);
                videoView_2 = (CustomVideoView)this.findViewById(R.id.videoView_three8_2);
                imageView_3 = (ImageView)this.findViewById(R.id.imageView_three8_3);
                videoView_3 = (CustomVideoView)this.findViewById(R.id.videoView_three8_3);
                break;
            default:
                Log.d(TAG,"default");
                break;
        }
    }
}
