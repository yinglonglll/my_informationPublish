package cn.ghzn.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;
import cn.ghzn.player.util.FileUtils;

public class ImportActivity extends Activity {
    private static final String TAG = "ImportActivity";
    private static String mSplitView;
    private static String mTarget = null;
    private String mFileName = "";
    private static int filesCount = 0;
    private DaoManager daoManager = DaoManager.getInstance();//找到单例(唯一数据库对象)

    public static int getFilesCount() {
        return filesCount;
    }

    public static String getTarget() {
        return mTarget;
    }

    private ArrayList arrayList = new ArrayList<String>();
    public  int listNum = 0;

    private ImageView[] mWidget1_image = null;
    private VideoView[] mWidget1_video = null;
    private ImageView[] mWidget2_image = null;
    private VideoView[] mWidget2_video = null;

    private static Runnable mRunnable;
    private static Map map1 = new HashMap();//存每次导入进来时里面的U盘文件，
    private static Map map2 = new HashMap();//存控件相关
    private static Map map = new HashMap();//将map1和map2存入进来，通过map来对map1和map2进行调用；

    public static Map getMap1() {
        return map1;
    }

    public static Map getMap2() {
        return map2;
    }

    public static Map getMap() {
        return map;
    }

    public static Runnable getRunnable() {
        return mRunnable;
    }

    public static String getSplitView() {
        return mSplitView;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {//监听到U盘的插入，才会执行这个操作，否则和这所有功能等于没有
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        Intent intent = getIntent();//获取意图
        String extraPath = intent.getExtras().getString("extra_path");
        Log.d(TAG,"extraPath的值为：" + extraPath);

        copyExtraFile(extraPath);//从U盘复制指定目标文件夹到U盘指定目录target；Intent.getdata()得到的uri为String型的filePath，现在将uri的前缀格式去除，则找到路径(用于new File(path))；

        if (mTarget != null) {
            turnActivity(mTarget);//从ghznPlayer文件夹中获取信息以跳转对应的activity;如果没有获取U盘的信息，默认读取数据库中的信息进行跳转
        } else {
            Source source = daoManager.getSession().getSourceDao().queryBuilder().unique();//先查出来，再调出来。
            mTarget = source.getMtarget();
        }


//        File target = new File(mTarget);//创建复制后的ghznPlayer的对象
//        if (target != null && target.length() != 0) {
//
//            Log.d(TAG,"跳转成功");
//        } else {
//            Log.d(TAG,"target为空或长度为0.跳转失败");
//        }
    }
//    private ArrayList getSonImage(String sonPath) {
//        File imageNames = new File(sonPath);//ghznPlayer名+分屏模式名+对应控件的子文件夹1234名
//        File[] sonImageName1 = imageNames.listFiles();//子文件夹1的对象，但里面可能不全是图片或视频
//        try {
//            for (File imageAddress : sonImageName1) {
//                if (imageAddress.getName().endsWith("jpg") || imageAddress.getName().endsWith("jpeg") || imageAddress.getName().endsWith("png")
//                        || imageAddress.getName().endsWith("mp4") || imageAddress.getName().endsWith("avi")
//                        || imageAddress.getName().endsWith("3gp")) {
//                            Log.d(TAG, "获取到了一个可用路径：" + imageAddress.getAbsolutePath());
//                            arrayList.add(imageAddress.getAbsolutePath());
//                            }else if (imageAddress.isDirectory()) {
//                                getSonImage(imageAddress.getAbsolutePath());//递归获取格式支持的全部图片和视频
//                            }
//                }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return arrayList;
//    }//获取子文件夹里所有有效图片或视频的绝对地址到动态数组arraylist中

//    {
//        /**
//         * 控件1包含image的一分屏的1，二分屏的1，三分屏的1，四分屏的1；即角标对应0123; widget1_image
//         *      含video的一分屏的1，二分屏的1，三分屏的1，四分屏的1，即角标对应0123;  widget1_video
//         * 控件2包含image的二分屏的2，三分屏的2，四分屏的2，即角标对应012;            widget2_image
//         *      含video的二分屏的2，三分屏的2，四分屏的2，即角标对应012;             widget2_video
//         * 控件3包含image的三分屏的3，四分屏的3；即角标对应01；                      widget3_image
//         *      含video的三分屏的3，四分屏的3；即角标对应01；                       widget3_video
//         * 控件4包含image的四分屏的4，即角标对应0；                                 widget4_image
//         *      含video的四分屏4，即角标对应0；                                    widget4_video
//         * 即可用4个二维数组或集合的空间简易处理，但为了简易理解，故用8个一位数组处理；
//         * 即控件命名1234，是以布局控件自上而下，自左而右命名的，即2分屏只有控件1和2；
//         */
//        //二分屏所需的image和video控件数量固定为2，与模式无关；每加一种模式，只需添加对应的控件到控件数组中即可。
//        mWidget1_image = new ImageView[4];
//        mWidget1_image[0] = sImageView_one1_1;
//        mWidget1_image[1] = sImageView_two1_1;
//        mWidget1_video = new VideoView[4];
//        mWidget1_video[0] = sVideoView_one1_1;
//        mWidget1_video[1] = sVideoView_two1_1;
//
//        mWidget2_image = new ImageView[3];
//        mWidget2_image[0] = sImageView_two1_2;
//        mWidget2_video = new VideoView[3];
//        mWidget2_video[0] = sVideoView_two1_2;
//
//        //控件3
//
//        //控件4
//    }
//
//    /**
//     * 通过据文件夹的分屏名，从组件名中取出对应分屏的组件出来。其中组件名是把其他分屏名同组件都放在一起了，方便取舍
//     *
//     */
//    private ImageView getValidImageWidget(String splitView,ImageView[] mWidgetX_image){
//        //从几分屏中判断取哪个控件,better：查一个方法可以返回多个不同类型的返回值的方法
//        ImageView validImageWidget;
//        switch (splitView){
//            case "one"://如取出一分屏时控件1里实际对应的控件；
//                validImageWidget = mWidgetX_image[0];
//                break;
//            case "two":
//                validImageWidget = mWidgetX_image[1];
//                break;
//            case "three":
//                validImageWidget = mWidgetX_image[2];
//                break;
//            case "four":
//                validImageWidget = mWidgetX_image[3];
//                break;
//            default:
//                throw new IllegalStateException("Unexpected value: " + splitView);
//        }
//        return validImageWidget;
//
//    }
//    private VideoView getValidVideoWidget(String splitView,VideoView[] mWidget_video){
//        VideoView validVideoWidget;
//        switch (splitView){
//            case "one":
//                validVideoWidget = mWidget_video[0];
//                break;
//            case "two":
//                validVideoWidget = mWidget_video[1];
//                break;
//            case "three":
//                validVideoWidget = mWidget_video[2];
//                break;
//            case "four":
//                validVideoWidget = mWidget_video[3];
//                break;
//            default:
//                throw new IllegalStateException("Unexpected value: " + splitView);
//        }
//        return validVideoWidget;
//    }

//    private void playSonImage(ArrayList arrayList, final ImageView[] widget1_image, final VideoView[] widget1_video, final ImageView[] widget2_image,
//                              final VideoView[] widget2_video, final ImageView[] widget3_image, final VideoView[] widget3_video, final ImageView[] widget4_image, final VideoView[] widget4_video){//图片定时X=5秒循环播放，视频播放结束 以循环播放
//        final File f = new File(arrayList.get(listNum).toString());
////            final ArrayList reArrayList = arrayList;//给递归函数传参数,因不知获取参数个数的函数，故投机用9替代
//        final Object[] Recursive = new Object[9];//注意，使用该方法的前提是已经带入 一个 子文件夹的绝对路径
//        Recursive[0] = arrayList;
//        Recursive[1] = widget1_image;
//        Recursive[2] = widget1_video;
//        Recursive[3] = widget2_image;
//        Recursive[4] = widget2_video;
//        Recursive[5] = widget3_image;
//        Recursive[6] = widget3_video;
//        Recursive[7] = widget4_image;
//        Recursive[8] = widget4_video;
//        if (listNum >= arrayList.size()) {
//
//            listNum = 0;//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
//            finish();
//        } else {
//            Log.d(TAG,"开始执行执行播放程序");
//
//
//            if ((f.getName().endsWith("jpg") || f.getName().endsWith("jpeg")||f.getName().endsWith("png"))) {
//                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + f.getAbsolutePath());
//                if (widget4_image != null || widget4_video != null) {
//                    //执行四分屏参数不为null，即进行四分屏，由四到以分屏进行判断
//
//                }else if (widget3_image != null || widget3_video != null) {
//                    //控件3
//
//                }else if (widget2_image != null || widget2_video != null) {
//                    //控件2
//                    ImageView getValidImageWidget = getValidImageWidget(mSplitView,mWidget2_image);
//                    getValidImageWidget.setVisibility(View.VISIBLE);
//                    getValidImageWidget.setVisibility(View.INVISIBLE);
//                    getValidImageWidget(mSplitView,mWidget2_image).setImageURI(Uri.fromFile(f.getAbsoluteFile()));
//                } else if (widget1_image != null || widget1_video != null) {
//                    //控件1
//                    ImageView getValidImageWidget = getValidImageWidget(mSplitView,mWidget1_image);
//                    getValidImageWidget.setVisibility(View.VISIBLE);
//                    getValidVideoWidget(mSplitView,mWidget1_video).setVisibility(View.INVISIBLE);
//                    getValidImageWidget.setImageURI(Uri.fromFile(f.getAbsoluteFile()));
//                }
//                mHandler = new Handler();
//                mHandler.postDelayed(mRunnable = new Runnable(){
//                    @Override
//                    public void run() {
//
//                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + f.getAbsolutePath());
//
//                        if (widget4_image != null || widget4_video != null) {
//                            //指定控件4
//
//                        }else if (widget3_image != null || widget3_video != null) {
//                            //指定控件3
//
//                        }else if (widget2_image != null || widget2_video != null) {
//                            //指定控件2
//                            getValidImageWidget(mSplitView,mWidget2_image).setVisibility(View.GONE);
//                        } else if (widget1_image != null || widget1_video != null) {
//                            //指定控件1
//                            getValidImageWidget(mSplitView,mWidget1_image).setVisibility(View.GONE);
//                        }
//                        listNum++;
//                        playSonImage((ArrayList) Recursive[0],(ImageView[]) Recursive[1],(VideoView[]) Recursive[2],(ImageView[]) Recursive[3],
//                                (VideoView[]) Recursive[4],(ImageView[]) Recursive[5],(VideoView[]) Recursive[6],(ImageView[]) Recursive[7],(VideoView[]) Recursive[8]);
//                    }
//                },3000);//3秒后结束当前图片
//            } else if (f.getName().endsWith("mp4") || f.getName().endsWith("avi") || f.getName().endsWith("3gp")) {
//
//                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + f.getAbsolutePath());
//
//                if (widget4_image != null || widget4_video != null) {
//                    //执行四分屏参数不为null，即进行四分屏，由四到以分屏进行判断
//
//                }else if (widget3_image != null || widget3_video != null) {
//                    //三分屏
//
//                }else if (widget2_image != null || widget2_video != null) {
//                    //控件2
//                    VideoView getValidVideoWidget = getValidVideoWidget(mSplitView,mWidget2_video);
//                    getValidVideoWidget.setVisibility(View.VISIBLE);
//                    getValidVideoWidget.setVisibility(View.INVISIBLE);
//
//                    getValidVideoWidget.setVideoPath(f.getAbsolutePath());
//                    getValidVideoWidget.start();
//                    getValidVideoWidget.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                        @Override
//                        public void onCompletion(MediaPlayer mp) {
//                            getValidVideoWidget(mSplitView,mWidget2_video).setVisibility(View.GONE);
//                        }
//                    });
//                } else if (widget1_image != null || widget1_video != null) {
//                    //控件1
//                    VideoView getValidVideoWidget = getValidVideoWidget(mSplitView,mWidget1_video);
//                    getValidVideoWidget.setVisibility(View.VISIBLE);
//                    getValidImageWidget(mSplitView,mWidget1_image).setVisibility(View.INVISIBLE);
//
//                    getValidVideoWidget.setVideoPath(f.getAbsolutePath());
//                    getValidVideoWidget.start();
//                    getValidVideoWidget.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                        @Override
//                        public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
//                            Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
//
//                            getValidVideoWidget(mSplitView,mWidget1_video).setVisibility(View.GONE);
//                            listNum++;
//                            playSonImage((ArrayList) Recursive[0],(ImageView[]) Recursive[1],(VideoView[]) Recursive[2],(ImageView[]) Recursive[3],
//                                    (VideoView[]) Recursive[4],(ImageView[]) Recursive[5],(VideoView[]) Recursive[6],(ImageView[]) Recursive[7],(VideoView[]) Recursive[8]);
//                        }
//                    });
//                }
//
////                sVideoView_two1_1.start();
////                sVideoView_two1_1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
////                    @Override
////                    public void onCompletion(MediaPlayer mp) {
////
////                        Log.d(TAG,"执行播放完视频，视频位于：" + f.getAbsolutePath());
////
////                        if (widget4_image != null || widget4_video != null) {
////                            //执行四分屏参数不为null，即进行四分屏，由四到以分屏进行判断
////
////                        }else if (widget3_image != null || widget3_video != null) {
////                            //三分屏
////
////                        }else if (widget2_image != null || widget2_video != null) {
////                            //二分屏
////                            widget1_video[1].setVisibility(View.GONE);
////                            widget2_video[0].setVisibility(View.GONE);
////                        } else if (widget1_image != null || widget1_video != null) {
////                            //一分屏
////                            widget1_video[0].setVisibility(View.GONE);
////                        }
////
////                        listNum++;
////                        playSonImage((ArrayList) Recursive[0],(ImageView[]) Recursive[1],(VideoView[]) Recursive[2],(ImageView[]) Recursive[3],
////                                (VideoView[]) Recursive[4],(ImageView[]) Recursive[5],(VideoView[]) Recursive[6],(ImageView[]) Recursive[7],(VideoView[]) Recursive[8]);
////
////                    }
////                });
//
//            }
//        }
//    }

//    private void imageViewImport() {
//        File imageName = new File(mTarget + "/" + mFileName);//得到分屏模式文件夹的对象
//        ArrayList arrayList1 = new ArrayList<String>();//用于存储文件夹1234中所有有效信息的动态数组
//        ArrayList arrayList2 = new ArrayList<String>();
//        ArrayList arrayList3 = new ArrayList<String>();
//        ArrayList arrayList4 = new ArrayList<String>();
//        //定时地将图片uri传送到布局文件中；核心思想：从多个未定的布局文件中先定布局，然后在已定的布局中进行图片或者视频的uri赋值，实现导入功能，再令设方法定时调用uri即可。
//        File[] files = imageName.listFiles();
//        for(File file : files) {
//            switch (file.getName()) {//找到文件夹1，设定uri，为了拿出来在一个方法使用时循环使用
//                case "1"://打开文件夹1，得到文件的绝对地址，转化为uri,即拿到文件夹1中所有图片或视频的uri
//                    Log.d(TAG,"找到文件夹1");
//                    String sonPath1 = mTarget + mFileName + "/" + file.getName();
//                    arrayList1 = getSonImage(sonPath1);//将分屏模式文件夹里的文件夹1中所有有效的资源绝对地址赋给动态数组ArrayList
//                    playSonImage(arrayList1,mWidget1_image,mWidget1_video,null,null,null,null,null,null);
//                    //此时实现的是播完文件夹内的视频或图片，未设置定时循环
//                    //参数的作用是指定使用什么控件；
//
//
////                    imageViewUri1 = Uri.fromFile(sonImageName1[i].getAbsoluteFile());//此时的i默认为0，整体意为文件夹1里第一个文件名的Uri，//存在文件夹非图片或视频的可能
//                        break;
//                case "2"://四个文件都找到，但依据分屏名选择是否执行找文件中的资源
//                    Log.d(TAG,"找到文件夹2");
//                    if (mSplitView == "two" || mSplitView =="three" || mSplitView == "four") {
//                        String sonPath2 = mTarget + mFileName + "/" + file.getName();
//                        arrayList2 = getSonImage(sonPath2);
//                        playSonImage(arrayList2,null,null,mWidget2_image,mWidget2_video,null,null,null,null);
//                    }
//
//                    break;
//                        default:
//                            break;
//                            //需要增加一个判断，分屏为1时，仅打开文件夹1，分屏为2时，仅打开文件夹2
//            }
//        }
//    }

    private void turnActivity(String mTarget) {

        File target = new File(mTarget);//创建复制后的ghznPlayer的对象
        if(target.isDirectory()) {
            int turnFlag = 0;
            String fileName = null;
            File[] files = target.listFiles();
            filesCount = files.length;//循环次数
//            String[] filesAbsAdr = new String[filesCount];
            if (files != null &&  filesCount != 0 ) {
                switch (filesCount){
                    case 1:
                        fileName = files[0].getName();
                        if (fileName.contains("1-1")) {
                            Intent intent = new Intent(this,OneSplitViewActivity.class);
                            intent.putExtra("splitView",filesCount);//分屏样式传递
                            intent.putExtra("filesParent",mTarget);//直接将ghzn文件夹地址传递过去，以获取父类file类型
                            startActivity(intent);
                        } else {
                            Toast.makeText(this,"跳转一分屏失败，请按照教程方法的格式放入对应的文件",Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 2:
                        for (int i = 0; i < 2; i++) {
                            fileName = files[i].getName();
                            if (fileName.contains("2-")) {//检查两个文件是否都符合命名格式
                                turnFlag++;
//                                filesAbsAdr[i] = files[i].getAbsolutePath();//将ghzn文件夹中的命名文件的绝对地址存储在数组中

                            } else {
                                Toast.makeText(this,"跳转二分屏失败，请按照教程方法的格式放入对应的文件",Toast.LENGTH_LONG).show();
                                turnFlag = 0;
                                break;//直接跳出循环
                            }
                        }
                        if (turnFlag == filesCount) {//全部都是true才能跳转
                            Intent intent = new Intent(this,TwoSplitViewActivity.class);
                            intent.putExtra("splitView",filesCount);//将分屏样式传输过去
                            intent.putExtra("filesParent",mTarget);
                            startActivity(intent);
                        }
                        break;
                    case 3:
                        for (int i = 0; i < 3; i++) {
                            fileName = files[i].getName();
                            if (fileName.contains("3-")) {
                                turnFlag++;
                            } else {
                                Toast.makeText(this,"跳转三分屏失败，请按照教程方法的格式放入对应的文件",Toast.LENGTH_LONG).show();
                                turnFlag = 0;
                                break;
                            }
                        }
                        if (turnFlag == filesCount) {//全部都是true才能跳转
                            Intent intent = new Intent(this,ThreeSplitViewActivity.class);
                            intent.putExtra("splitView",filesCount);//将分屏样式传输过去
                            startActivity(intent);
                        }
                        break;
                    case 4:
                        for (int i = 0; i < 4; i++) {
                            fileName = files[i].getName();
                            if (fileName.contains("4-")) {
                                turnFlag++;
                            } else {
                                Toast.makeText(this,"跳转四分屏失败，请按照教程方法的格式放入对应的文件",Toast.LENGTH_LONG).show();
                                turnFlag = 0;
                                break;
                            }
                        }
                        if (turnFlag == filesCount) {//全部都是true才能跳转
                            Intent intent = new Intent(this,FourSplitViewActivity.class);
                            intent.putExtra("splitView",filesCount);//将分屏样式传输过去
                            startActivity(intent);
                        }
                        break;
                    default:
                        Toast.makeText(this,"请勿放入过多文件，请按照教程方法的格式放入对应的文件",Toast.LENGTH_LONG).show();
                        break;
                }
            }
            Log.d(TAG, "已执行完setFileName方法");
        }
    }

    private boolean copyFiles(String source,String target){//通过单个文件copyFile()来逐个复制以实现复制目录内所有内容；
        File root = new File(source);//要复制的目录
        File[] currentFiles = root.listFiles();
        File targetDir = new File(target);

        if (currentFiles != null) {
            for (File currentFile : currentFiles) {
                if (currentFile.isDirectory())//如果当前项为子目录 进行递归
                {
                    copyFiles(currentFile.getPath() + "/", target + currentFile.getName() + "/");
                    Log.d(TAG, "copyFiles_" + currentFile.getName() + "_为子目录，可进行递归");
                } else //如果当前项为文件则进行文件拷贝
                {
                    boolean copyFileState = FileUtils.copyFile(currentFile.getPath(), target + currentFile.getName());
                    Log.d(TAG, "copyFiles_" + currentFile.getName() + "_为文件，可进行复制" + copyFileState);
                }
            }
            return true;
        } else {
            Log.d(TAG,"currentFiles为null");
            return false;
        }
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
                boolean success = copyFiles(source, mTarget);//通过打开输入/出通道，执行读写复制
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
