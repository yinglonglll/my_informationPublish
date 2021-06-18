package cn.ghzn.player.order;

import android.content.Context;

/**
 * <pre>
 *     author : yinglonglll
 *     e-mail : 949706806@qq.com
 *     time   : 2021/06/18
 *     desc   :
 *     func--->1.关机，重启，休眠，定时开关机
 *     version: 1.0
 * </pre>
 */
public interface OrderImpl {
    /**
     * 关机
     * @param context
     */
    public boolean shutdown(Context context);

    /**
     * 重启
     * @param context
     */
    public boolean reboot(Context context);

    /**
     * 唤醒
     * @param context
     */
    public boolean wakeup(Context context);
    /**
     * 定时开关机-开
     * @param context
     */
    public boolean startup_shutdow_on(Context context, String startTime, String endTime);

    /**
     * 定时开关机-关
     * @param context
     */
    public boolean startup_shutdow_off(Context context);

}
