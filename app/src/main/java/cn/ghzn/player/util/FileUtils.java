package cn.ghzn.player.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static android.os.Environment.MEDIA_MOUNTED;

public class FileUtils {
    private static final String TAG = "FileUtils";

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
    public static boolean copyFile(String source, String target){
        try {
            File targetFile = new File(target);
            if(!targetFile.getParentFile().exists()){
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
}