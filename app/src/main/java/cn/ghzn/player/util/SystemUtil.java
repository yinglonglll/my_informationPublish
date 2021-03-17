package cn.ghzn.player.util;

public class SystemUtil {

    /**
     * 获取StackTraceElement对象
     * @return
     */
    public static StackTraceElement getStackTrace(){
        return Thread.currentThread().getStackTrace()[4];
    }
}
