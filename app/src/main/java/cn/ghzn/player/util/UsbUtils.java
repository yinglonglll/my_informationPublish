package cn.ghzn.player.util;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.apkfuns.logutils.LogUtils;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author : GuQiuSheng
 * @e-mail : guqiusheng@ghzn.cn
 * @date : 2021-04-16 09:22
 * @desc :
 */
public class UsbUtils {

    private void checkUsb(Context context){
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        StringBuilder sb = new StringBuilder();
        while(deviceIterator.hasNext()){
            UsbDevice usbDevice = deviceIterator.next();
            sb.append("DeviceName="+usbDevice.getDeviceName()+"\n");
            sb.append("DeviceId="+usbDevice.getDeviceId()+"\n");
            sb.append("VendorId="+usbDevice.getVendorId()+"\n");
            sb.append("ProductId="+usbDevice.getProductId()+"\n");
            sb.append("DeviceClass="+usbDevice.getDeviceClass()+"\n");
            int deviceClass=usbDevice.getDeviceClass();
            if(deviceClass==0) {
                UsbInterface anInterface = usbDevice.getInterface(0);
                int interfaceClass = anInterface.getInterfaceClass();

                sb.append("device Class 为0-------------\n");
                sb.append("Interface.describeContents()="+anInterface.describeContents()+"\n");
                sb.append("Interface.getEndpointCount()="+anInterface.getEndpointCount()+"\n");
                sb.append("Interface.getId()="+anInterface.getId()+"\n");
                //http://blog.csdn.net/u013686019/article/details/50409421  
                //http://www.usb.org/developers/defined_class/#BaseClassFFh  
                //通过下面的InterfaceClass 来判断到底是哪一种的，例如7就是打印机，8就是usb的U盘  
                sb.append("Interface.getInterfaceClass()="+anInterface.getInterfaceClass()+"\n");
                if(anInterface.getInterfaceClass()==7){
                    sb.append("此设备是打印机\n");
                }else if(anInterface.getInterfaceClass()==8){
                    sb.append("此设备是U盘\n");
                }
                sb.append("anInterface.getInterfaceProtocol()="+anInterface.getInterfaceProtocol()+"\n");
                sb.append("anInterface.getInterfaceSubclass()="+anInterface.getInterfaceSubclass()+"\n");
                sb.append("device Class 为0------end-------\n");
            }

            sb.append("DeviceProtocol="+usbDevice.getDeviceProtocol()+"\n");
            sb.append("DeviceSubclass="+usbDevice.getDeviceSubclass()+"\n");
            sb.append("+++++++++++++++++++++++++++\n");
            sb.append("                           \n");
        }
        LogUtils.e(sb.toString());
    }

}
