package cn.ghzn.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.ghzn.player.MainActivity;

/**
 * <pre>
 *     author : yinglonglll
 *     e-mail : xxx@xx
 *     time   : 2021/04/22
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class BootUpReceiver extends BroadcastReceiver {
    private static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(action_boot))
        {
            Log.d("onReceive:", "Boot system");
            Intent startIntent = new Intent(context, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startIntent);
        }
    }
}