package cn.ghzn.player;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;

import cn.ghzn.player.util.FileUtils;

public class ImportActivity extends Activity {
    private static final String TAG = "ImportActivity";
    private String mTarget;
    private String mFileName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        Intent intent = getIntent();//获取意图
        String extraPath = intent.getExtras().getString("extra_path");
        copyExtraFile(extraPath);//从U盘复制指定目标文件夹到U盘指定目录target；
        //Intent.getdata()得到的uri为String型的filePath，现在将uri的前缀格式去除，则找到路径(用于new File(path))；

        File target = new File(mTarget);//创建复制后的ghznPlayer的对象
        //据对象找出ghznPlayer文件夹里面的分屏模式文件夹名xx_xx
        mFileName = setFileName(target);
        String file_name = mFileName;
        Log.d(TAG,"file_name的文件名是" +file_name );
        splitViewMode(mFileName);//从得到的文件名进行拆分字符串，得到分屏和模式实现分屏

        File imageName = new File(mTarget + "/" + mFileName);//得到分屏模式文件夹的绝对路径
        String son_target = mTarget + "/" + mFileName;
        Log.d(TAG,"son_target的绝对地址是" + son_target);
        imageViewImport(imageName);//据对象找出子文件夹1234以图片导入
    }

    private void imageViewImport(File imageName) {
        Uri imageViewUri1 = null;
        Uri imageViewUri2 = null;
        Uri imageViewUri3 = null;
        Uri imageViewUri4 = null;
        int i = 0;
        //定时地将图片uri传送到布局文件中；核心思想：从多个未定的布局文件中先定布局，然后在已定的布局中进行图片或者视频的uri赋值，实现导入功能，再令设方法定时调用uri即可。
        File[] files = imageName.listFiles();
        for(File file : files){
            switch (file.getName()){//找到文件夹1，设定uri，为了拿出来在一个方法使用时循环使用
                case "1"://打开文件夹1，得到文件的绝对地址，转化为uri
                    File imageNames = new File(mTarget + mFileName + "/" + file.getName());//ghznPlayer名+分屏模式名+对应控件的子文件夹1234名
                    File[] sonImageName1 = imageNames.listFiles();
                    imageViewUri1 = Uri.fromFile(sonImageName1[i].getAbsoluteFile());//此时的i默认为0，整体意为文件夹1里第一个文件名的Uri，只需再外部调用时循环即可调用其他图片；
                    break;
//                case "2":
//                    imageViewUri2 = Uri.fromFile();
//                    break;
                default:
                    break;

            }
//            if(file.getName().equals("1")){
//                //找到文件名为1，那就对控件1进行设置
//
//                imageViewUri1 = Uri.fromFile()
//            }
        }

    }

    private String setFileName(File fileNames) {
        //mName名为将ghznPlayer文件夹，将其展开，获取以分屏模式命名的文件夹的新名字
        if(fileNames.isDirectory()) {
            File[] files = fileNames.listFiles();
            String fileName = files[0].toString();//这里暂时默认ghznPlayer内只有分屏模式一个文件夹
            Log.d(TAG, "已找到ghznPlayer里的分屏模式名字..." + fileName);
            return fileName;
        }
        return null;
    }

    private void splitViewMode(String fileName) {
        String[] splits = fileName.split("_");
        String SplitView = splits[0];
        String mode = splits[1];

//        if (SplitView.equals("one")) {
//            switch (mode) {
//                case "1":
//                    setContentView(R.layout.activity_SplitViewOne1);
//                    break;
//                case "2":
//                    setContentView(R.layout.activity_SplitViewOne2);
//                    break;
//                default:
//                    break;
//            }
//        }
        if (SplitView.equals("two")) {
            switch (mode) {
                case "1":
                    setContentView(R.layout.activity_splitview_two1);
                    break;
                case "2":
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
        File extraDirectory = new File(extraPath);//
        if(extraDirectory.isDirectory()){
            File[] files = extraDirectory.listFiles();//查找给定目录中的所有文件和目录(listFiles()得到的结果类似相对路径)
            boolean match = false;
            String source = null;
            mTarget = null;
            for(File file : files){
                if(file.getName().equals("ghznPlayer")){//从U盘路径中找到我们放入的文件夹，以找到文件夹的路径
                    Log.d(TAG,"find extra program:"+file.getAbsolutePath());
                    match = true;//标志找到
                    source = file.getAbsolutePath();//含有盘名的全目录，U盘存放目标文件
                    mTarget = FileUtils.getFilePath(this, Constants.STOREPATH) + "/" + file.getName();//方法返回String类型，拼起来就是完整的复制目标地址
                    break;
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
