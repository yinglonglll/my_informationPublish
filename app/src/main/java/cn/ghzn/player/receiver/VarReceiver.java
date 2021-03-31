//package cn.ghzn.player.receiver;
//
//import android.content.BroadcastReceiver;
//
//public class VarReceiver {//没有继承BroadcastReceiver是因为不想直接监听，而是指定监听
//
//    private BroadcastReceiver mBroadcastReceiver;
//    private  int var;
//    private static VarReceiver varInstance;
//
//    public VarReceiver() {
//    }
//
//    public static VarReceiver getInstance(){
//        if(null==varInstance)
//        {
//            varInstance=new VarReceiver();
//        }
//        return varInstance;
//    }
//
//    public void setBroadListener(BroadcastReceiver broadcastReceiver){//需要时才设置监听广播
//        this.mBroadcastReceiver=broadcastReceiver;
//    }
//
//    public void VarsReceiver(boolean isFreeFlag1,boolean isFreeFlag2,boolean isFreeFlag3,boolean isFreeFlag4){//定义自己需要监听的变量数
//        if (isFreeFlag1 || isFreeFlag2 || isFreeFlag3 || isFreeFlag4) {//对于四分屏，只要监听到存在非忙碌状态就执行递归
//
//        }
//    }
//}
