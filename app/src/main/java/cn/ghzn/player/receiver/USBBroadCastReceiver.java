package cn.ghzn.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

/**
 * <pre>
 *     author : yinglonglll
 *     e-mail : xxx@xx
 *     time   : 2021/05/24
 *     desc   :1.此USB接收器实现将文件复制到U盘根目录，无对授权文件，资源文件进行处理。
 *     version: 1.0
 * </pre>
 */
public class USBBroadCastReceiver extends BroadcastReceiver {
    private static final String TAG = "USBBroadCastReceiver";
    private UsbListener usbListener;

    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_USB_PERMISSION:
                    //接受到自定义广播
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //允许权限申请
                        if (usbDevice != null) {
                            //回调
                            if (usbListener != null) {
                                usbListener.getReadUsbPermission(usbDevice);
                            }
                        }
                    } else {
                        if (usbListener != null) {
                            usbListener.failedReadUsb(usbDevice);
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到存储设备插入广播
                    UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_add != null) {
                        if (usbListener != null) {
                            usbListener.insertUsb(device_add);
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    //接收到存储设备拔出广播
                    UsbDevice device_remove = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_remove != null) {
                        if (usbListener != null) {
                            usbListener.removeUsb(device_remove);
                        }
                    }
                    break;
            }
    }

    public void setUsbListener(UsbListener usbListener) {
        this.usbListener = usbListener;
    }

    /**
     * USB 操作监听
     */
    public interface UsbListener {
        //USB 插入
        void insertUsb(UsbDevice device_add);

        //USB 移除
        void removeUsb(UsbDevice device_remove);

        //获取读取USB权限
        void getReadUsbPermission(UsbDevice usbDevice);

        //读取USB信息失败
        void failedReadUsb(UsbDevice usbDevice);
    }
}
