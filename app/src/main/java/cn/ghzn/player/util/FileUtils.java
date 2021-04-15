package cn.ghzn.player.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static android.os.Environment.MEDIA_MOUNTED;
import static cn.ghzn.player.MainActivity.app;

public class FileUtils {
    private static final String TAG = "FileUtils";
    private static File mFile;
    private static boolean isSave;
    private static File mSaveFile;
    private static Context mContext;

    public static String getFilePath(Context context, String dir) {
        String directoryPath = "";
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {//判断外部存储是否可用
//            Log.i(TAG, "外部存储可用");
            directoryPath = context.getExternalFilesDir(dir).getAbsolutePath();//通过getExternalFilesDir()方法可以获取到 SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
            //即指定目录的路径，参数dir为自自定义文件夹.
        } else {//没外部存储就使用内部存储
            Log.i(TAG, "外部存储不可用");
            directoryPath = context.getFilesDir() + File.separator + dir;//内部存储的路径是: /data/data/< package name >/files/… ；File.separator 的作用相当于 '\'；
        }
        File file = new File(directoryPath);
        if (!file.exists()) {//判断文件目录是否存在
            file.mkdirs();
        }
        return directoryPath;
    }

    public static boolean copyFile(String source, String target){//对文件进行赋值，不能直接以目录为参数进行复制目录内所有参数；
        try {
            File targetFile = new File(target);
//            LogUtils.e(targetFile);
//            LogUtils.e(targetFile.getParentFile());
//            LogUtils.e(targetFile.getParentFile().exists());

            if(!targetFile.getParentFile().exists()){//对单个文件复制，先判断父文件夹是否存在，要存在才一起复制过去
                targetFile.getParentFile().mkdirs();//每次new文件时，都判断是否成功建立，不成再建立一次
            }
            FileChannel input = new FileInputStream(new File(source)).getChannel();//得到复制处的输入流通道
            FileChannel output = new FileOutputStream(new File(target)).getChannel();//得到粘贴处的输出流通道
            ByteBuffer buffer = ByteBuffer.allocate(4096);//allocate()方法用于分配缓冲区。但是如果是聚集写入,与分散读取,就需要注意这个大小设置,
            int len = -1;
            while((len = input.read(buffer))!=-1){ //调用read(buffer,long)或write(buffer,long)方法，在文件的绝对位置进行字节读或写；读取缓冲有内容时，则执行如下操作；
                buffer.flip();//设置了position和limit的正确值，//为新通道写入或获取操作做好准备：...；固定
                output.write(buffer);//读到内容，写出内容，清楚缓存，往复直至读完
                buffer.clear();
            }
            output.close();
            input.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void getMachineId(){
        if (app.isImportState()) {
            //todo:实现将授权文件生成到U盘目录下，取U盘绝对地址进行赋值

            mSaveFile = new File(app.getExtraPath(),"Licence.txt");//U盘ghznPlayer文件夹内授权文件绝对地址的对象
            if (mSaveFile.exists()) {
                Log.d(TAG, "U盘的机器码或授权码已存在，无法导出到U盘指定文件处");//如果U盘存在授权文件，我则不将机器码往U盘复制，否则复制到U盘
                Toast.makeText(mContext,"机器码已存在",Toast.LENGTH_SHORT).show();
            } else {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(mSaveFile);
                    outStream.write(app.getAuthorization().getBytes("gbk"));//UFT-8在android不能用，只能用gbk!!!不设置的话可能会变成乱码！！！
                    outStream.close();
                    outStream.flush();
                    isSave = true;
                    Log.d(TAG,"this is 文件已经保存啦！赶快去查看吧!");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mSaveFile = new File(app.getLicenceDir(),"Licence.txt");//手机内授权文件绝对地址的对象
            if (mSaveFile.exists()) {
                Log.d(TAG, "终端的机器码或授权码已存在，无法导出到本地指定文件处");
            } else {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(mSaveFile);
                    outStream.write(app.getAuthorization().getBytes("gbk"));//UFT-8在android不能用，只能用gbk!!!不设置的话可能会变成乱码！！！
                    outStream.close();
                    outStream.flush();
                    isSave = true;
                    Log.d(TAG,"this is 文件已经保存啦！赶快去查看吧!");
//            copyFile(app.getLicenseDir(),app.getExtraPath());
//            Log.d(TAG,"this is copyFile ：" + copyFile(app.getLicenseDir(),app.getExtraPath()));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    public static void deleteMachineId(){
//        ViewImportUtils.deleteFile(mSaveFile);//先删再建
//    }

    public static String readTxt(String path){//txt文件的绝对地址
        String txtStr = "";
        try {
            File txtFile = new File(path);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(txtFile), "GBK");//UTF-8
            BufferedReader br = new BufferedReader(isr);

            String mimeTypeLine = null ;
            while ((mimeTypeLine = br.readLine()) != null) {
                txtStr = txtStr + mimeTypeLine;//将缓存池的逐行衔接到字符串上
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  txtStr;
    }
}