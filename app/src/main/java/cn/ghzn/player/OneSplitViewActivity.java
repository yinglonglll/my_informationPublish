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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cn.ghzn.player.util.ViewImportUtils;

import static androidx.core.os.HandlerCompat.postDelayed;
import static cn.ghzn.player.ImportActivity.getMap1;

public class OneSplitViewActivity extends Activity {
    private static final String TAG = "OneSplitViewActivity";
    private static ImageView imageView_1;
    private static VideoView videoView_1;
    private static Handler mHandler;
    private static Runnable mRunnable;

    public static Runnable getRunnable() {
        return mRunnable;
    }

    public static Handler getHandler() {
        return mHandler;
    }

    public static ImageView getImageView_1() {
        return imageView_1;
    }

    public static VideoView getVideoView_1() {
        return videoView_1;
    }
    ArrayList arrayList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int splitView = intent.getIntExtra("splitView",0);//以文件的数量获取分屏样式，
        String filesParent = intent.getStringExtra("filesParent");
        File f = new File(filesParent);
        File[] files = f.listFiles();

        String[] splits = files[0].getName().split("\\-");//A-B-C
        String split_view = splits[0];//A，存储于数据库
        String split_mode = splits[1];//B
        String split_widget = splits[2];//c

        initWidget(split_mode);

        for(File file : files){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
            getMap1().put(file.getName(), file.getAbsolutePath());//形成键值对，方便取出作为资源导入
        }

        String key = split_view + "\\-" + split_mode;
        arrayList = ViewImportUtils.getSonImage(getMap1().get(key + "\\-1").toString());
        //arrayList = ViewImportUtils.getSonImage(files[0].getAbsolutePath());
        //getMap1().put(key + "\\-1", arrayList);//一分屏只有1-1-1文件夹，少一个则报错(判断key的数量)；用于getMap1.get("1-1-1")

        if (getMap1().size() == splitView) {//判断分屏模式与需要的子文件夹数量是否相对，以进行资源赋值操作
            playSonImage();//参数arrayList是默认的，可含或不含参，结果都一样
            setContentView(R.layout.activity_splitview_one1);
            ViewImportUtils.saveTarget();//如果正常赋值资源成功，则将此复制后的文件ghznPlayer的绝对目录存储到greenDao数据库；没有U盘时，直接读取数据库中的mtarget

        } else {
            Log.d(TAG,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作");
            Toast.makeText(this,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作",Toast.LENGTH_LONG).show();
        }

    }

    private void initWidget(String split_mode) {

        switch (split_mode){//该步骤可以简写，这样写仅为了和其他activity代码相似
            case "1":
                imageView_1 = (ImageView) this.findViewById(R.id.imageView_one1_1);
                videoView_1 = (VideoView) this.findViewById(R.id.videoView_one1_1);
                break;
            default:
                Log.d(TAG,"Location is switch(split_mode)_default");
                break;
        }
    }
    int listNum = 0;
    private void playSonImage(){

        if (listNum >= arrayList.size()) {
            listNum = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
            finish();
        } else {
            Log.d(TAG,"开始执行执行播放程序");

            final File f = new File(arrayList.get(listNum).toString());

            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());

                    //控件1
                    imageView_1.setVisibility(View.VISIBLE);
                    videoView_1.setVisibility(View.INVISIBLE);
                    imageView_1.setImageURI(Uri.fromFile(f.getAbsoluteFile()));

                mHandler = new Handler();
                mHandler.postDelayed(mRunnable = new Runnable(){
                    @Override
                    public void run() {

                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());


                            imageView_1.setVisibility(View.GONE);

                        listNum++;
                        playSonImage();
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
                            listNum++;
                            playSonImage();
                        }
                    });


            }
        }
    }
}
