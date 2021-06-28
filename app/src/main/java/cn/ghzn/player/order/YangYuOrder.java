package cn.ghzn.player.order;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.ghzn.player.util.SystemUtil;

import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.MyApplication.mDevice;

/**
 * <pre>
 *     author : yinglonglll
 *     e-mail : 949706806@qq.com
 *     time   : 2021/06/18
 *     desc   :
 *     func--->
 *     version: 1.0
 * </pre>
 */
public class YangYuOrder extends BaseOrder{
    private static final String TAG = "YangyuOrder";
    public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public boolean shutdown(Context context) {
        try{
            Intent intent = new Intent();
            intent = new Intent();
            intent.setAction("android.intent.action.shutdown");
            LogUtils.e("设备关机");
            context.sendBroadcast(intent);
        }catch (Exception e){
            LogUtils.e(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean reboot(Context context) {
        try{
            Intent intent = new Intent();
            intent = new Intent();
            intent.setAction("android.intent.action.reboot");
            LogUtils.e("设备重启");
            context.sendBroadcast(intent);
        }catch (Exception e){
            LogUtils.e(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean wakeup(Context context) {
        try{
            Intent intent = new Intent();
            intent = new Intent();
            intent.setAction("android.intent.action.exitsleep");
            context.sendBroadcast(intent);
        }catch (Exception e){
            LogUtils.e(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean startup_shutdow_on(Context context, String startTime, String endTime) {
        Log.d(TAG,"this is 執行了startup_shutdow_on方法");
        try {
            Intent intent = new Intent("android.intent.action.setpoweronoff");
            String date = simpleDateFormat.format(new Date());
            int[] timeonArray = SystemUtil.getTime(date + " " +startTime); // 2014/10/1 8:30
            int[] timeoffArray = SystemUtil.getTime(date + " " +endTime);
            //修正前的时间
            LogUtils.e(timeonArray);
            LogUtils.e(timeoffArray);

            /*判断当前dayOfWeek是否是存储的星期时间*/
            LogUtils.e(SystemUtil.checkTimeFormat(context,timeonArray).length);//不符默认返回1
            if(SystemUtil.checkTimeFormat(context,timeonArray).length != 1){
                timeonArray = SystemUtil.checkTimeFormat(context,timeonArray);
            }else{
                Log.d(TAG,"this is 开机时间不在dayOfWeek的时间内");
                return false;
            }
            if(SystemUtil.checkTimeFormat(context,timeoffArray).length != 1){
                timeoffArray = SystemUtil.checkTimeFormat(context,timeoffArray);
            }else{
                Log.d(TAG,"this is 关机时间不在dayOfWeek的时间内");
                return false;
            }

            //修正后的时间
            LogUtils.e(timeonArray);
            LogUtils.e(timeoffArray);

            int onTimeMinute = timeonArray[3]*60 + timeonArray[4];
            int offTimeMinute = timeoffArray[3]*60 + timeoffArray[4];
            LogUtils.e(Math.abs(onTimeMinute-offTimeMinute) > 2);
            if(Math.abs(onTimeMinute-offTimeMinute) > 2){//设置定时任务的开关机分钟差至少大于2
                Log.d(TAG,"this is 开关机分钟差大于2分钟");
                //成功设置才存储在数据库中
                mDevice.setPower_start_time(startTime);
                mDevice.setPower_end_time(endTime);
                daoManager.getSession().getDeviceDao().update(mDevice);
                Log.d(TAG,"this is startTime: " + startTime + "***" + "数据库中的startTime" + mDevice.getPower_start_time());
                Log.d(TAG,"this is EndTime: " + endTime + "***" + "数据库中的startTime" + mDevice.getPower_end_time());

                SystemUtil.setOnTimeAlarm(context,timeonArray);//带入修正后的开机时间；
                Log.d(TAG,"this is 将timeon和timeoff的开关机时间通过广播发送出去");
                LogUtils.e(timeonArray);
                LogUtils.e(timeoffArray);
                intent.putExtra("timeon",timeonArray);
                intent.putExtra("timeoff",timeoffArray);
                intent.putExtra( "enable" ,true); // false
                context.sendBroadcast(intent);
            }else{
                Log.d(TAG,"this is 定时开机关机时差至少大于2分钟，保证开关机正常进行");
                Toast.makeText(context,"定时开机关机时差至少大于2分钟",Toast.LENGTH_SHORT).show();
                return  false;
            }
        } catch (ParseException e) {
            LogUtils.e(e.getMessage());
            Log.d(TAG,"this is 设置定时任务失败");
            return  false;
        }
        return true;
    }

    @Override
    public boolean startup_shutdow_off(Context context) {
        try{
            Intent intent = new Intent("android.intent.action.setpoweronoff");
            intent.putExtra( "enable" ,false); // false
            context.sendBroadcast(intent);
        }catch (Exception e){
            LogUtils.e(e.getMessage());
            return false;
        }
        Log.d(TAG,"this is 执行完关闭定时开关机");
        return true;
    }
}
