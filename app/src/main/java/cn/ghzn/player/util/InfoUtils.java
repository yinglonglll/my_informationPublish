package cn.ghzn.player.util;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import java.util.Random;

import cn.ghzn.player.Constants;
import cn.ghzn.player.MainActivity;
import cn.ghzn.player.MyApplication;

import static cn.ghzn.player.MainActivity.*;

public class InfoUtils {

    private static final String TAG = "InfoUtils";
    private static Context mContext;//上下文，需要填上下文就直接新建一个填入参数使用即可，如下文getMac

    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String getDeviceName(){
        app.setDevice_Name(Constants.DEVICE_PREFIX + getRandomString(10));
        return app.getDevice_Name();
    }

    public static String getDeviceId() {
        app.setDevice_Id(MacUtils.getMac(mContext) + System.currentTimeMillis());//ID明文，其中getMac码需要wifi，网络状态和INTERNET的权限

        return app.getDevice_Id();

    }

    public static boolean getAuthorityState() {
        app.setAuthority_state(false);
        return app.isAuthority_state();

    }

    public static String getAuthorityTime() {
        app.setAuthority_time("XX.XX.XX");
        return app.getAuthority_time();

    }

    public static String getAuthorization() {
//        app.setAuthorization(AuthorityUtils.digest(app.getDevice_Id()));//重新生成一次mac加密设备ID作为授权码；
        app.setAuthorization(AuthorityUtils.digest(MacUtils.getMac(mContext)));//重新生成一次mac加密设备ID作为授权码；
        Log.d(TAG,"设置授权码成功");
        return app.getAuthorization();

    }

    public static String getSoftware_version() {
        app.setSoftware_version("***");
        return app.getSoftware_version();

    }

    public static String FirmwareVersion() {
        app.setFirmware_version("***");
        return app.getFirmware_version();

    }

    public static int getWidth() {
        app.setWidth(0);
        return app.getWidth();

    }

    public static int getHeight() {
        app.setHeight(0);
        return app.getHeight();

    }
}
