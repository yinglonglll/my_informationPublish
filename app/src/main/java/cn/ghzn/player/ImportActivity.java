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

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.util.ArrayList;

import cn.ghzn.player.util.FileUtils;

import static cn.ghzn.player.MainActivity.sImageView_one1_1;
import static cn.ghzn.player.MainActivity.sImageView_two1_1;
import static cn.ghzn.player.MainActivity.sImageView_two1_2;
import static cn.ghzn.player.MainActivity.sVideoView_one1_1;
import static cn.ghzn.player.MainActivity.sVideoView_two1_1;
import static cn.ghzn.player.MainActivity.sVideoView_two1_2;

public class ImportActivity extends Activity {
    private static final String TAG = "ImportActivity";
    private String mTarget = null;
    private String mFileName = "";
    private ArrayList arrayList = new ArrayList<String>();

    public  int listNum = 0;
    private ImageView[] mWidget1_image = null;
    private VideoView[] mWidget1_video = null;
    private ImageView[] mWidget2_image = null;
    private VideoView[] mWidget2_video = null;
    private String mSplitView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {//监听到U盘的插入，才会执行这个操作，否则和这所有功能等于没有
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

//        Toast.makeText(this,"成功跳转到importActivity：",Toast.LENGTH_SHORT).show();
//        try {
//            Thread.sleep( 3000 );
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        Intent intent = getIntent();//获取意图
        String extraPath = intent.getExtras().getString("extra_path");
        Log.d(TAG,"extraPath的值为：" + extraPath);
        copyExtraFile(extraPath);//从U盘复制指定目标文件夹到U盘指定目录target；
        //Intent.getdata()得到的uri为String型的filePath，现在将uri的前缀格式去除，则找到路径(用于new File(path))；


        File target = new File(mTarget);//创建复制后的ghznPlayer的对象

        if (target != null && target.length() != 0) {
            mFileName = setFileName(target);//据对象找出ghznPlayer文件夹里面的分屏模式文件夹名xx_xx
        }
        String file_name = mFileName;//无意义的一步？
        Log.d(TAG,"file_name的文件名是" +file_name );
        String son_target = mTarget + "/" + mFileName;//得到分屏模式文件夹的绝对路径
        Log.d(TAG,"son_target的绝对地址是" + son_target);

        //splitViewMode(mFileName);//从得到的文件名进行拆分字符串，得到分屏和模式名,仅实现了跳转，未实现资源赋值；


//        imageViewImport();//这里将分屏模式中的据对象找出子文件夹1234以图片导入


    }
    private ArrayList getSonImage(String sonPath) {
        File imageNames = new File(sonPath);//ghznPlayer名+分屏模式名+对应控件的子文件夹1234名
        File[] sonImageName1 = imageNames.listFiles();//子文件夹1的对象，但里面可能不全是图片或视频
        try {
            for (File imageAddress : sonImageName1) {
                if (imageAddress.getName().endsWith("jpg") || imageAddress.getName().endsWith("jpeg") || imageAddress.getName().endsWith("png")
                        || imageAddress.getName().endsWith("mp4") || imageAddress.getName().endsWith("avi")
                        || imageAddress.getName().endsWith("3gp")) {
                            Log.d(TAG, "获取到了一个可用路径：" + imageAddress.getAbsolutePath());
                            arrayList.add(imageAddress.getAbsolutePath());
                            }else if (imageAddress.isDirectory()) {
                                getSonImage(imageAddress.getAbsolutePath());//递归获取格式支持的全部图片和视频
                            }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }//获取子文件夹里所有有效图片或视频的绝对地址到动态数组arraylist中

    {
        /**
         * 控件1包含image的一分屏的1，二分屏的1，三分屏的1，四分屏的1；即角标对应0123; widget1_image
         *      含video的一分屏的1，二分屏的1，三分屏的1，四分屏的1，即角标对应0123;  widget1_video
         * 控件2包含image的二分屏的2，三分屏的2，四分屏的2，即角标对应012;            widget2_image
         *      含video的二分屏的2，三分屏的2，四分屏的2，即角标对应012;             widget2_video
         * 控件3包含image的三分屏的3，四分屏的3；即角标对应01；                      widget3_image
         *      含video的三分屏的3，四分屏的3；即角标对应01；                       widget3_video
         * 控件4包含image的四分屏的4，即角标对应0；                                 widget4_image
         *      含video的四分屏4，即角标对应0；                                    widget4_video
         * 即可用4个二维数组或集合的空间简易处理，但为了简易理解，故用8个一位数组处理；
         * 即控件命名1234，是以布局控件自上而下，自左而右命名的，即2分屏只有控件1和2；
         */
        //二分屏所需的image和video控件数量固定为2，与模式无关；每加一种模式，只需添加对应的控件到控件数组中即可。
        mWidget1_image = new ImageView[4];
        mWidget1_image[0] = sImageView_one1_1;
        mWidget1_image[1] = sImageView_two1_1;
        mWidget1_video = new VideoView[4];
        mWidget1_video[0] = sVideoView_one1_1;
        mWidget1_video[1] = sVideoView_two1_1;

        mWidget2_image = new ImageView[3];
        mWidget2_image[0] = sImageView_two1_2;
        mWidget2_video = new VideoView[3];
        mWidget2_video[0] = sVideoView_two1_2;

        //控件3

        //控件4
    }

    private ImageView getValidImageWidget(String splitView,ImageView[] mWidgetX_image){
        //从几分屏中判断取哪个控件,better：查一个方法可以返回多个不同类型的返回值的方法
        ImageView validImageWidget;
        switch (splitView){
            case "one"://如取出一分屏时控件1里实际对应的控件；
                validImageWidget = mWidgetX_image[0];
                break;
            case "two":
                validImageWidget = mWidgetX_image[1];
                break;
            case "three":
                validImageWidget = mWidgetX_image[2];
                break;
            case "four":
                validImageWidget = mWidgetX_image[3];
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + splitView);
        }
        return validImageWidget;

    }
    private VideoView getValidVideoWidget(String splitView,VideoView[] mWidget_video){
        VideoView validVideoWidget;
        switch (splitView){
            case "one":
                validVideoWidget = mWidget_video[0];
                break;
            case "two":
                validVideoWidget = mWidget_video[1];
                break;
            case "three":
                validVideoWidget = mWidget_video[2];
                break;
            case "four":
                validVideoWidget = mWidget_video[3];
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + splitView);
        }
        return validVideoWidget;
    }
    /**
     * 实现图片或赋值的方法，用于通过文件名区分分屏模式时，执行赋值图片或视频，再跳转界面
     */
    private void playSonImage(ArrayList arrayList, final ImageView[] widget1_image, final VideoView[] widget1_video, final ImageView[] widget2_image,
                              final VideoView[] widget2_video, final ImageView[] widget3_image, final VideoView[] widget3_video, final ImageView[] widget4_image, final VideoView[] widget4_video){//图片定时X=5秒循环播放，视频播放结束 以循环播放
        final File f = new File(arrayList.get(listNum).toString());
//            final ArrayList reArrayList = arrayList;//给递归函数传参数,因不知获取参数个数的函数，故投机用9替代
        final Object[] Recursive = new Object[9];//注意，使用该方法的前提是已经带入 一个 子文件夹的绝对路径
        Recursive[0] = arrayList;
        Recursive[1] = widget1_image;
        Recursive[2] = widget1_video;
        Recursive[3] = widget2_image;
        Recursive[4] = widget2_video;
        Recursive[5] = widget3_image;
        Recursive[6] = widget3_video;
        Recursive[7] = widget4_image;
        Recursive[8] = widget4_video;
        if (listNum >= arrayList.size()) {

            listNum = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
            finish();
        } else {
            Log.d(TAG,"开始执行执行播放程序");

            
            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
                if (widget4_image != null || widget4_video != null) {
                    //执行四分屏参数不为null，即进行四分屏，由四到以分屏进行判断

                }else if (widget3_image != null || widget3_video != null) {
                    //控件3

                }else if (widget2_image != null || widget2_video != null) {
                    //控件2
                    ImageView getValidImageWidget = getValidImageWidget(mSplitView,mWidget2_image);
                    getValidImageWidget.setVisibility(View.VISIBLE);
                    getValidImageWidget.setVisibility(View.INVISIBLE);
                    getValidImageWidget(mSplitView,mWidget2_image).setImageURI(Uri.fromFile(f.getAbsoluteFile()));
                } else if (widget1_image != null || widget1_video != null) {
                    //控件1
                    ImageView getValidImageWidget = getValidImageWidget(mSplitView,mWidget1_image);
                    getValidImageWidget.setVisibility(View.VISIBLE);
                    getValidVideoWidget(mSplitView,mWidget1_video).setVisibility(View.INVISIBLE);
                    getValidImageWidget.setImageURI(Uri.fromFile(f.getAbsoluteFile()));
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run() {

                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());

                        if (widget4_image != null || widget4_video != null) {
                            //指定控件4

                        }else if (widget3_image != null || widget3_video != null) {
                            //指定控件3

                        }else if (widget2_image != null || widget2_video != null) {
                            //指定控件2
                            getValidImageWidget(mSplitView,mWidget2_image).setVisibility(View.GONE);
                        } else if (widget1_image != null || widget1_video != null) {
                            //指定控件1
                            getValidImageWidget(mSplitView,mWidget1_image).setVisibility(View.GONE);
                        }
                        listNum++;
                        playSonImage((ArrayList) Recursive[0],(ImageView[]) Recursive[1],(VideoView[]) Recursive[2],(ImageView[]) Recursive[3],
                                (VideoView[]) Recursive[4],(ImageView[]) Recursive[5],(VideoView[]) Recursive[6],(ImageView[]) Recursive[7],(VideoView[]) Recursive[8]);
                    }
                },3000);//3秒后结束当前图片
            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {

                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());

                if (widget4_image != null || widget4_video != null) {
                    //执行四分屏参数不为null，即进行四分屏，由四到以分屏进行判断

                }else if (widget3_image != null || widget3_video != null) {
                    //三分屏

                }else if (widget2_image != null || widget2_video != null) {
                    //控件2
                    VideoView getValidVideoWidget = getValidVideoWidget(mSplitView,mWidget2_video);
                    getValidVideoWidget.setVisibility(View.VISIBLE);
                    getValidVideoWidget.setVisibility(View.INVISIBLE);

                    getValidVideoWidget.setVideoPath(f.getAbsolutePath());
                    getValidVideoWidget.start();
                    getValidVideoWidget.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            getValidVideoWidget(mSplitView,mWidget2_video).setVisibility(View.GONE);
                        }
                    });
                } else if (widget1_image != null || widget1_video != null) {
                    //控件1
                    VideoView getValidVideoWidget = getValidVideoWidget(mSplitView,mWidget1_video);
                    getValidVideoWidget.setVisibility(View.VISIBLE);
                    getValidImageWidget(mSplitView,mWidget1_image).setVisibility(View.INVISIBLE);

                    getValidVideoWidget.setVideoPath(f.getAbsolutePath());
                    getValidVideoWidget.start();
                    getValidVideoWidget.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
                            Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());

                            getValidVideoWidget(mSplitView,mWidget1_video).setVisibility(View.GONE);
                            listNum++;
                            playSonImage((ArrayList) Recursive[0],(ImageView[]) Recursive[1],(VideoView[]) Recursive[2],(ImageView[]) Recursive[3],
                                    (VideoView[]) Recursive[4],(ImageView[]) Recursive[5],(VideoView[]) Recursive[6],(ImageView[]) Recursive[7],(VideoView[]) Recursive[8]);
                        }
                    });
                }

//                sVideoView_two1_1.start();
//                sVideoView_two1_1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//
//                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
//
//                        if (widget4_image != null || widget4_video != null) {
//                            //执行四分屏参数不为null，即进行四分屏，由四到以分屏进行判断
//
//                        }else if (widget3_image != null || widget3_video != null) {
//                            //三分屏
//
//                        }else if (widget2_image != null || widget2_video != null) {
//                            //二分屏
//                            widget1_video[1].setVisibility(View.GONE);
//                            widget2_video[0].setVisibility(View.GONE);
//                        } else if (widget1_image != null || widget1_video != null) {
//                            //一分屏
//                            widget1_video[0].setVisibility(View.GONE);
//                        }
//
//                        listNum++;
//                        playSonImage((ArrayList) Recursive[0],(ImageView[]) Recursive[1],(VideoView[]) Recursive[2],(ImageView[]) Recursive[3],
//                                (VideoView[]) Recursive[4],(ImageView[]) Recursive[5],(VideoView[]) Recursive[6],(ImageView[]) Recursive[7],(VideoView[]) Recursive[8]);
//
//                    }
//                });

            }
        }
    }

    private void imageViewImport() {
        File imageName = new File(mTarget + "/" + mFileName);//得到分屏模式文件夹的对象
        ArrayList arrayList1 = new ArrayList<String>();//用于存储文件夹1234中所有有效信息的动态数组
        ArrayList arrayList2 = new ArrayList<String>();
        ArrayList arrayList3 = new ArrayList<String>();
        ArrayList arrayList4 = new ArrayList<String>();
        //定时地将图片uri传送到布局文件中；核心思想：从多个未定的布局文件中先定布局，然后在已定的布局中进行图片或者视频的uri赋值，实现导入功能，再令设方法定时调用uri即可。
        File[] files = imageName.listFiles();
        for(File file : files) {
            switch (file.getName()) {//找到文件夹1，设定uri，为了拿出来在一个方法使用时循环使用
                case "1"://打开文件夹1，得到文件的绝对地址，转化为uri,即拿到文件夹1中所有图片或视频的uri
                    Log.d(TAG,"找到文件夹1");
                    String sonPath1 = mTarget + mFileName + "/" + file.getName();
                    arrayList1 = getSonImage(sonPath1);//将分屏模式文件夹里的文件夹1中所有有效的资源绝对地址赋给动态数组ArrayList
                    playSonImage(arrayList1,mWidget1_image,mWidget1_video,null,null,null,null,null,null);
                    //此时实现的是播完文件夹内的视频或图片，未设置定时循环
                    //参数的作用是指定使用什么控件；


//                    imageViewUri1 = Uri.fromFile(sonImageName1[i].getAbsoluteFile());//此时的i默认为0，整体意为文件夹1里第一个文件名的Uri，//存在文件夹非图片或视频的可能
                        break;
                case "2"://四个文件都找到，但依据分屏名选择是否执行找文件中的资源
                    Log.d(TAG,"找到文件夹2");
                    if (mSplitView == "two" || mSplitView =="three" || mSplitView == "four") {
                        String sonPath2 = mTarget + mFileName + "/" + file.getName();
                        arrayList2 = getSonImage(sonPath2);
                        playSonImage(arrayList2,null,null,mWidget2_image,mWidget2_video,null,null,null,null);
                    }

                    break;
                        default:
                            break;
                            //需要增加一个判断，分屏为1时，仅打开文件夹1，分屏为2时，仅打开文件夹2

            }
//
        }

    }

    private String setFileName(File fileNames) {
        //mName名为将ghznPlayer文件夹，将其展开，获取以分屏模式命名的文件夹的新名字
        if(fileNames.isDirectory()) {
            File[] files = fileNames.listFiles();
            String fileName = null;//这里暂时默认ghznPlayer内只有分屏模式一个文件夹
            if (files != null && files.length != 0 ) {
                fileName = files[0].toString();
            }
            Log.d(TAG, "已找到ghznPlayer里的分屏模式名字..." + fileName);
            return fileName;
        }
        return null;
    }

    private void splitViewMode(String fileName) {
        String[] splits = fileName.split("\\.");
        mSplitView = splits[0];
        String mode = splits[1];
        imageViewImport();

        if (mSplitView.equals("one")) {
            switch (mode) {
                case "1":
                    setContentView(R.layout.activity_splitview_one1);
                    break;
//                case "2":
//                    setContentView(R.layout.activity_SplitViewOne2);
//                    break;
                default:
                    break;
            }
        }
        if (mSplitView.equals("two")) {
            switch (mode) {
                case "1":
                    setContentView(R.layout.activity_splitview_two1);
                    break;
                case "2"://目前代码还没实现添加，暂不可用这个布局；
                    setContentView(R.layout.activity_splitview_two2);
                    break;
                default:
                    break;
            }
        }
//        if (SplitView.equals("three")) {
//            switch (mode) {
//                case "1":
//                    setContentView(R.layout.activity_SplitViewThree1);
//                    break;
//                case "2":
//                    setContentView(R.layout.activity_SplitViewThree2);
//                    break;
//                default:
//                    break;
//            }
//        }
//        if (SplitView.equals("four")) {
//            switch (mode) {
//                case "1":
//                    setContentView(R.layout.activity_SplitViewFour1);
//                    break;
//                case "2":
//                    setContentView(R.layout.activity_SplitViewFour2);
//                    break;
//                default:
//                    break;
//            }
//        }
    }



    private void copyExtraFile(String path){
        String extraPath = path.replace("file://", "");//去除uri前缀，得到文件路径(绝对路径)
        Log.d(TAG,"extraPath去除url前缀的值为：" + extraPath);
        File extraDirectory = new File(extraPath);//
        if(extraDirectory.isDirectory()){
            File[] files = extraDirectory.listFiles();//查找给定目录中的所有文件和目录(listFiles()得到的结果类似相对路径)
            LogUtils.e(files);
            boolean match = false;//初始化状态变量
            String source = null;
            mTarget = null;
            if (files != null&& files.length != 0) {
                for(File file : files){
                    if (file.getName().equals("ghznPlayer")) {//从U盘路径中找到我们放入的文件夹，以找到文件夹的路径
                        Log.d(TAG, "find extra program:" + file.getAbsolutePath());
                        match = true;//标志找到
                        source = file.getAbsolutePath();//含有盘名的全目录，U盘存放目标文件
                        Log.d(TAG,"source的值为：" + source);
                        mTarget = FileUtils.getFilePath(this, Constants.STOREPATH) + "/" + file.getName();//方法返回String类型，拼起来就是完整的复制目标地址,创建目标文件夹
                        Log.d(TAG,"mTarget的值为：" + mTarget);
                        break;
                    } else {
                        Log.d(TAG, "Not find extra program:");

                    }
                }
            }
            if(match){//标志找到后需复制的动作
                boolean success = FileUtils.copyFile(source, mTarget);//通过打开输入/出通道，执行读写复制
                if(success){//复制的动作执行成功
                    Log.d(TAG,"copy to:"+ mTarget + " " + success);
                }else{
                    Log.d(TAG,"复制失败，即将跳转主界面...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent i = new Intent(this,MainActivity.class);
                    startActivity(i);
                }
            }

        }
    }
}
