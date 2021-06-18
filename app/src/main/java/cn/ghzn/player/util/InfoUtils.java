package cn.ghzn.player.util;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import cn.ghzn.player.Constants;
import cn.ghzn.player.MyApplication;

import static cn.ghzn.player.MainActivity.*;

public class InfoUtils {

    private static final String TAG = "InfoUtils";

    public static long dateString2Mills(String dateString){
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar.getTimeInMillis();
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

    public static String getDeviceName(){
        app.setDevice_Name(Constants.DEVICE_PREFIX + getRandomString(10));
        return app.getDevice_Name();
    }

    public static String getDeviceId() {
        app.setDevice_Id(MacUtils.getMac(MyApplication.getmContext()) + System.currentTimeMillis());//ID明文，其中getMac码需要wifi，网络状态和INTERNET的权限
        return app.getDevice_Id();
    }

    public static String getAuthorityTime() {
        return app.getAuthority_time();
    }

    public static String getMachineCode() {//机器码就是加密的mac值,在device表初始化的时候，就给app.setAuthorization()设定值了，也就是我们只需要get()即可。
        //当第一次生成时，直接生成，后续生成时，直接以第一次为准。
        app.setMachine_code(AuthorityUtils.digest(MacUtils.getMac(MyApplication.getmContext())));//重新生成一次mac加密设备ID作为授权码；
//        if(app.getFirst_machineCodeOut() == null){//第一次导出机器码时，将其存储在数据中，后续导出的机器码都是以此为准
//            app.setAuthorization(AuthorityUtils.digest(MacUtils.getMac(mContext)));//重新生成一次mac加密设备ID作为授权码；
//            app.getDevice().setFirst_machineCodeOut(app.getAuthorization());//命名错误，这里的Authorization应改为MachineCode，每次获取的机器码存储在这儿
//            daoManager.getSession().getDeviceDao().update(app.getDevice());
//        }

        Log.d(TAG,"设置机器码码成功");
        return app.getMachine_code();
    }

    public static String getSoftware_version() {
//        app.setSoftware_version("***");
        return app.getSoftware_version();

    }

    public static String FirmwareVersion() {
//        app.setFirmware_version("***");
        return app.getFirmware_version();

    }

    public static int getWidth() {
//        app.setWidth(0);
        return app.getWidth();

    }

    public static int getHeight() {
//        app.setHeight(0);
        return app.getHeight();
    }
    public static boolean getSingleSplitMode(){
        return app.isSingle_split_mode();
    }
}
