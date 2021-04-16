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

    public static void checkUsb(Context context){
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();//声明一个访问集合的迭代器方法；Collection依赖于Iterator，是因为Collection的实现类都要实现iterator()函数，返回一个Iterator对象。故可用迭代器
        StringBuilder sb = new StringBuilder();
        while(deviceIterator.hasNext()){
            UsbDevice usbDevice = deviceIterator.next();
            sb.append("DeviceName="+usbDevice.getDeviceName()+"\n");
            sb.append("DeviceId="+usbDevice.getDeviceId()+"\n");
            sb.append("VendorId="+usbDevice.getVendorId()+"\n");//是指销售商ID，为区分不同的产品代理商而设定的。
            sb.append("ProductId="+usbDevice.getProductId()+"\n");//是指产品的ID，包括生产厂家，产地，生产日期等;
            // 还有GUID：GUID 一般在驱动程序的 .inf 文件里或到注册表里面找："HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Enum\\USB\\Vid_厂家标识&Pid_产品标识\\驱动程序"
            sb.append("DeviceClass="+usbDevice.getDeviceClass()+"\n");//获取类来唯一标识。
            int deviceClass=usbDevice.getDeviceClass();
            if(deviceClass==0) {
                UsbInterface anInterface = usbDevice.getInterface(0);//参考开发文档确定Usb接口0类型；本代码排除其他功能的Usb类的判定
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
                sb.append("anInterface.getInterfaceProtocol()="+anInterface.getInterfaceProtocol()+"\n");//获取接口协议
                sb.append("anInterface.getInterfaceSubclass()="+anInterface.getInterfaceSubclass()+"\n");//获取子类
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
