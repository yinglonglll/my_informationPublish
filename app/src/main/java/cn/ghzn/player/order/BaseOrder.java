package cn.ghzn.player.order;

import android.content.Context;

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
public abstract class BaseOrder implements OrderImpl{

    @Override
    public abstract boolean shutdown(Context context);

    @Override
    public abstract boolean reboot(Context context);

    @Override
    public abstract boolean wakeup(Context context);

    @Override
    public abstract boolean startup_shutdow_on(Context context, String startTime, String endTime);

    @Override
    public abstract boolean startup_shutdow_off(Context context);
}
