package cn.ghzn.player.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.apkfuns.logutils.LogUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SystemUtil {

    private static final String TAG = "SystemUtil";

    /**
     * 获取StackTraceElement对象
     * @return
     */
    public static StackTraceElement getStackTrace(){
        return Thread.currentThread().getStackTrace()[4];
    }
    public static int[] checkTimeFormat(Context context,int[] onTime){//以onTime为例
        //年月日时分秒 参数位置固定；0为年，1为月，2为日，3为时，4为分；
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        //将24制时分化为最小单位分钟
        int onTimeMinute = onTime[3]*60 + onTime[4];
        //int offTimeMinute = offTime[3]*60 + offTime[4];
        int curTimeMinute = hour*60 + minute;

        if(onTimeMinute <= curTimeMinute){
            //1.更新时间设置为明天同样的时分，即更新天数即可
            Calendar calendar = Calendar.getInstance();
            long curMill = System.currentTimeMillis();
            calendar.setTimeInMillis(curMill + 86400000);//当前的毫秒加上一天的毫秒数，这样直接取得下一天的日期，不用考虑日月年换算。
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;//1月是0，故加1
            int data = calendar.get(Calendar.DATE);

            //todo:检查星期的方法放在这儿：带入修正后的星期时间，根据返回的布尔值判断是否执行定时任务
            if(checkWeekFormat(context,calendar)){//1.checkTime带个context的参数；2.接个if，套住后面的程序，只有符合的时间才允许定时。3.将此检查方法运用到广播onTime，mainActivity的初始化检查上
                LogUtils.e(year);
                LogUtils.e(month);
                LogUtils.e(data);

                onTime[0] = year;
                onTime[1] = month;
                onTime[2] = data;
                //此时年月日已修正,返回已修正值。
                return onTime;
            }else{
                return new int[]{0};
            }
        }else{
            //2.直接设置定时时间为今天的时间，年月日无问题
            if(checkWeekFormat(context,c)){
                return onTime;
            }else{
                return new int[]{0};
            }
        }
    }

    public static void setOnTimeAlarm(Context context, int[] onTime) {//带入定时的开机时间

        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date  = null;
        try {
            date = simpleDateFormat1.parse(onTime[0] +"-"+onTime[1] + "-" + onTime[2] + " " + onTime[3] + ":" + onTime[4]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long onTimeMill = date != null ? date.getTime() : 0;
        LogUtils.e(onTimeMill);//返回设定开机时间的毫秒数。
        long systemTime = System.currentTimeMillis();//返回当前毫秒数
        long time = onTimeMill-systemTime;
        //LogUtils.e(systemTime);
        LogUtils.e(time);//返回开机时间差

        //通过AlarmManager定时启动广播，进行更新开关机定时任务
        AlarmManager alarmManager= (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        //再设置开机时间点的定时广播
        Intent intentOnTime = new Intent();
        intentOnTime.setAction("android.intent.action.ALARM_ON_TIME");
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intentOnTime, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, onTimeMill+120000, 24*3600*1000, pIntent);//考虑到开关机时间通常是在定时后1分钟执行，为保证开关机在1分钟内顺理执行，故设闹钟为关机时间点2分钟(60000*2)后执行，避免冲突
        Log.d(TAG,"this is 成功设置了延迟闹钟广播,闹钟效果在开机时间+2分钟后实现");
    }
    public static int[]  getTime(String time) throws ParseException {
        int[] result = new int[5];
        String[] times = time.split(" ");
        String[] day = times[0].split("-");
        String[] t = times[1].split("\\:");
        LogUtils.e(day);
        LogUtils.e(t);
        for(int i=0;i<day.length;i++){
            result[i] = Integer.parseInt(day[i]);
        }
        for(int i=0;i<t.length-1;i++){
            result[day.length+i] = Integer.parseInt(t[i]);
        }
        LogUtils.e(result);
        return result;
    }
    public static boolean checkWeekFormat(Context context,Calendar c){
        //todo：通过shareP的数据判断是否执行定时任务；通过对map键值对信息的获取，进行判断星期周期的当天内是否应该进行定时设置；
        final SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        /*从sharePre取出集合*/
        if(preferences.contains("weekString")) {
            String getWeekString = preferences.getString("weekString", null);
            HashMap<String, Boolean> weekMap = JSON.parseObject(getWeekString, new TypeReference<HashMap<String, Boolean>>() {});
            /*将sharePre与修正后的时间星期进行比较*/
            int curDayOfWeek = c.get(Calendar.DAY_OF_WEEK);//1为周末，2位周一
            switch(curDayOfWeek){
                case 1:
                    if(weekMap.get("SUN")){
                        //返回周日状态为可执行
                        return true;
                    }
                    break;
                case 2:
                    if(weekMap.get("Mon")){
                        //返回周一状态为可执行
                        return true;
                    }
                    break;
                case 3:
                    if(weekMap.get("TUE")){
                        //返回周二状态为可执行
                        return true;
                    }
                    break;
                case 4:
                    if(weekMap.get("WED")){
                        //返回周三状态为可执行
                        return true;
                    }
                    break;
                case 5:
                    if(weekMap.get("THU")){
                        //返回周四状态为可执行
                        return true;
                    }
                    break;
                case 6:
                    if(weekMap.get("FRI")){
                        //返回周五状态为可执行
                        return true;
                    }
                    break;
                case 7:
                    if(weekMap.get("SAT")){
                        //返回周六状态为可执行
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
}
