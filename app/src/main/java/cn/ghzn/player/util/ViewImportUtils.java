package cn.ghzn.player.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.util.ArrayList;

import cn.ghzn.player.ImportActivity;
import cn.ghzn.player.MainActivity;
import cn.ghzn.player.R;
import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;

public class ViewImportUtils extends Activity {
    private static final String TAG = "ViewImportUtils";
    private static DaoManager daoManager = DaoManager.getInstance();//找到单例(唯一数据库对象)
    public static String lastSplitMode = "";

    /**
     *有效图片或视频资源导入的方法
     */

    public static ArrayList getSonImage(String sonPath) {
        ArrayList arrayList = new ArrayList<String>();
        File imageNames = null;//ghznPlayer名+分屏模式名+对应控件的子文件夹1234名
        imageNames = new File(sonPath);
        File[] sonImageName1 = imageNames.listFiles();//子文件夹1的对象，但里面可能不全是图片或视频
        try {
            if (sonImageName1 != null) {
                for (File imageAddress : sonImageName1) {
                    if (imageAddress.getName().endsWith("jpg") || imageAddress.getName().endsWith("jpeg") || imageAddress.getName().endsWith("png")
                            || imageAddress.getName().endsWith("mp4") || imageAddress.getName().endsWith("avi")
                            || imageAddress.getName().endsWith("3gp")) {
                        Log.d(TAG, "获取到了一个可用路径：" + imageAddress.getAbsolutePath());
                        arrayList.add(imageAddress.getAbsolutePath());
                    } else if (imageAddress.isDirectory()) {
                        getSonImage(imageAddress.getAbsolutePath());//递归获取格式支持的全部图片和视频
                    }
                }
            } else {
                Log.d(TAG,"this is sonImageName1 = null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }//获取子文件夹里所有有效图片或视频的绝对地址到动态数组arraylist中


    public static void resetSplitMode(String lastSplitMode){

    }


    //flie：要删除的文件夹的所在位置
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }

}
