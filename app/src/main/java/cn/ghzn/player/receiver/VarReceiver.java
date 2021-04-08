package cn.ghzn.player.receiver;

import android.content.BroadcastReceiver;

public class VarReceiver {//没有继承BroadcastReceiver是因为不想直接监听，而是指定监听；监听变量广播

    private BroadcastReceiver mBroadcastReceiver;
    private  int var;
    private static VarReceiver varInstance;

    public VarReceiver() {
    }

    public static VarReceiver getInstance(){
        if(null==varInstance)
        {
            varInstance=new VarReceiver();
        }
        return varInstance;
    }

    public BroadcastReceiver setBroadListener(BroadcastReceiver broadcastReceiver){//需要时才设置监听广播
        this.mBroadcastReceiver=broadcastReceiver;
        return mBroadcastReceiver;
    }


//    public void setVar(int var) {
//        this.var = var;
//        if(null!=mBroadcastReceiver){
//            if(var==1){
//                mBroadcastReceiver.;
//            }
//            else {
//                mBroadcastReceiver.onFail("收到失败回复");
//            }
//        }
//    }
}
