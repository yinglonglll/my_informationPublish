package cn.ghzn.player.util;

import android.content.Context;

import java.util.Random;

import cn.ghzn.player.Constants;

public class InfoUtils {
    private static Context mContext;//上下文，需要填上下文就直接新建一个填入参数使用即可，如下文getMac
    public static String sDeviceId;

    /**
     * 获取软件名称
     * @return
     */
    public static String getDeviceName(){
        return Constants.DEVICE_PREFIX + getRandomString(10);
    }

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

    public static String getDeviceId() {
        sDeviceId = MacUtils.getMac(mContext) + System.currentTimeMillis();//ID明文，其中getMac码需要wifi，网络状态和INTERNET的权限

        return sDeviceId;

    }

    public static boolean AuthorityState() {
        return true;

    }

    public static String getAuthorityTime() {
        return null;

    }

    public static String getAuthorization() {
        return null;

    }

    public static String getSoftware_version() {
        return null;

    }

    public static String FirmwareVersion() {
        return null;

    }

    public static int getDWidth() {
        return 0;

    }

    public static int getHeight() {
        return 0;

    }
}
