package cn.ghzn.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.apkfuns.logutils.LogUtils;

import cn.ghzn.player.MainActivity;
import cn.ghzn.player.order.YangYuOrder;
import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;

import static cn.ghzn.player.MainActivity.app;

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
public class AlarmOnTimeReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmOnTimeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e("action === " + intent.getAction());
        //保证开机时，若处于开机状态，则重置定时任务，使得定时任务循环下去
        if(intent.getAction().equals("android.intent.action.ALARM_ON_TIME")){
            LogUtils.e("this is 执行重设定定时任务操作");
            YangYuOrder order = new YangYuOrder();
            Device device = DaoManager.getInstance().getSession().getDeviceDao().queryBuilder().unique();
            LogUtils.e(device.getPower_start_time());
            LogUtils.e(device.getPower_end_time());
            //todo：执行定时任务的设定：取出上次修正后的定时时间，与当前时间进行对比，观察是否需要进行定时任务时间的修正，再发送到API
            if(device.getPower_start_time()!=null && device.getPower_end_time()!=null){
                final SharedPreferences preferences = app.getSharedPreferences("user", Context.MODE_PRIVATE);
                if (preferences != null){
                    boolean powerFlag = preferences.getBoolean("timeSwitchFlag", true);
                    if(powerFlag){
                        Log.d(TAG,"this is 执行延迟广播中的定时任务");
                        order.startup_shutdow_off(context);
                        order.startup_shutdow_on(context,device.getPower_start_time(),device.getPower_end_time());
                    }
                }
            }
        }
    }
}
