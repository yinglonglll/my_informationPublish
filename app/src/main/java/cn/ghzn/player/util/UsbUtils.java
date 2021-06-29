package cn.ghzn.player.util;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import cn.ghzn.player.Constants;
import cn.ghzn.player.ImportActivity;
import cn.ghzn.player.MainActivity;
import cn.ghzn.player.MyApplication;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;

import static cn.ghzn.player.Constants.LICENCE_NAME;
import static cn.ghzn.player.MainActivity.app;
import static cn.ghzn.player.MainActivity.daoManager;
import static cn.ghzn.player.MyApplication.mDevice;
import static cn.ghzn.player.MyApplication.mSource;
import static cn.ghzn.player.MyApplication.util;
import static cn.ghzn.player.util.AuthorityUtils.digest;
import static cn.ghzn.player.util.FileUtils.getFilePath;

/**
 * @author : GuQiuSheng
 * @e-mail : guqiusheng@ghzn.cn
 * @date : 2021-04-16 09:22
 * @desc :
 */
public class UsbUtils {
    private static final String TAG = "UsbUtils";
    private static String[] mMacStrings;

    public static String[] getVolumePaths(Context context) {
        String paths[] = null;
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", paramClasses);
            getVolumePathsMethod.setAccessible(true);
            Object[] params = {};
            paths = (String[]) getVolumePathsMethod.invoke(storageManager, params);
            LogUtils.e(paths);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return paths;
    }

    public static void checkUsb(Context context) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();//声明一个访问集合的迭代器方法；Collection依赖于Iterator，是因为Collection的实现类都要实现iterator()函数，返回一个Iterator对象。故可用迭代器
        StringBuilder sb = new StringBuilder();
        while (deviceIterator.hasNext()) {
            UsbDevice usbDevice = deviceIterator.next();
            sb.append("DeviceName=" + usbDevice.getDeviceName() + "\n");
            sb.append("DeviceId=" + usbDevice.getDeviceId() + "\n");
            sb.append("VendorId=" + usbDevice.getVendorId() + "\n");//是指销售商ID，为区分不同的产品代理商而设定的。
            sb.append("ProductId=" + usbDevice.getProductId() + "\n");//是指产品的ID，包括生产厂家，产地，生产日期等;
            // 还有GUID：GUID 一般在驱动程序的 .inf 文件里或到注册表里面找："HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Enum\\USB\\Vid_厂家标识&Pid_产品标识\\驱动程序"
            sb.append("DeviceClass=" + usbDevice.getDeviceClass() + "\n");//获取类来唯一标识。
            int deviceClass = usbDevice.getDeviceClass();
            if (deviceClass == 0) {
                UsbInterface anInterface = usbDevice.getInterface(0);//参考开发文档确定Usb接口0类型；本代码排除其他功能的Usb类的判定
                int interfaceClass = anInterface.getInterfaceClass();

                sb.append("device Class 为0-------------\n");
                sb.append("Interface.describeContents()=" + anInterface.describeContents() + "\n");
                sb.append("Interface.getEndpointCount()=" + anInterface.getEndpointCount() + "\n");
                sb.append("Interface.getId()=" + anInterface.getId() + "\n");
                //http://blog.csdn.net/u013686019/article/details/50409421  
                //http://www.usb.org/developers/defined_class/#BaseClassFFh  
                //通过下面的InterfaceClass 来判断到底是哪一种的，例如7就是打印机，8就是usb的U盘  
                sb.append("Interface.getInterfaceClass()=" + anInterface.getInterfaceClass() + "\n");
                if (anInterface.getInterfaceClass() == 7) {
                    sb.append("此设备是打印机\n");
                } else if (anInterface.getInterfaceClass() == 8) {
                    sb.append("此设备是U盘\n");
                    app.setImportState(true);
                    util.infoLog(TAG,"此设备是USB类型是U盘",null);
                }
                sb.append("anInterface.getInterfaceProtocol()=" + anInterface.getInterfaceProtocol() + "\n");//获取接口协议
                sb.append("anInterface.getInterfaceSubclass()=" + anInterface.getInterfaceSubclass() + "\n");//获取子类
                sb.append("device Class 为0------end-------\n");
            }

            sb.append("DeviceProtocol=" + usbDevice.getDeviceProtocol() + "\n");
            sb.append("DeviceSubclass=" + usbDevice.getDeviceSubclass() + "\n");
            sb.append("+++++++++++++++++++++++++++\n");
            sb.append("                           \n");
        }
        LogUtils.e(sb.toString());
    }

    public static void checkUsbFileForm(Context context, String path) {
        if (path != null || app.isImportState()) {//USB设备路径不为空；接入的USB设备类型不是U盘则不会进行读取
            Toast.makeText(context, "U盘接入，路径为：", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, path, Toast.LENGTH_SHORT).show();
            Log.d(TAG, path);
            if (path.equals("file:///storage/emulated/0")) {//非有效USB设备路径；减少无用功运行，即不符退出即可。
                return;
            }
            app.setExtraPath(path.replace("file://", "") + "/");//去除uri前缀
            util.varyLog(TAG,app.getExtraPath(),"app.getExtraPath()");
            //app.setExtraPath(path.replace("file://", "") + "/Android/data/cn.ghzn.player/files/");//去除uri前缀,这是自动生成的路径。

            //todo:确认U盘存在授权文件，并删除本地的授权文件
            File updateLicence = new File(app.getExtraPath() + LICENCE_NAME);
            if (updateLicence.exists()) {
                util.varyLog(TAG,updateLicence.getAbsolutePath(),"this is 授权码存在于");
                File deleteLicence = new File(app.getLicenceDir() + LICENCE_NAME);
                if (deleteLicence.exists()) {
                    deleteLicence.delete();
                    util.varyLog(TAG,"deleteLicence.delete()","原机器码存在，进行删除");
                }
                //存在app.getLicenceDir()为null的情况;在重复U盘考入文件播放时,值有时会被清掉
                if (app.getLicenceDir() == null) {
                    app.setLicenceDir(getFilePath(context, Constants.STOREPATH) + "/");
                    util.varyLog(TAG,app.getLicenceDir(),"app.getLicenceDir()");
                }

                //若是非法授权文件，以“,”区分，则直接放弃读取，不执行覆盖。
                //todo：对U盘的授权文件进行是否拆分读取判断，若可拆分则1.验证授权码身份；2.验证授权码时间格式；3.若时间格式正确则检查时间信息并存储
                // ，更新到展示信息；4.再跳转到检查资源文件方法
                if (FileUtils.readTxt(updateLicence.getAbsolutePath()).contains(",")) {//检验U盘授权文件的合法性
                    Log.d(TAG,"updateLicence.getAbsolutePath()" + updateLicence.getAbsolutePath());
                    FileUtils.copyFile(updateLicence.getAbsolutePath(), app.getLicenceDir() + LICENCE_NAME);//会自动覆盖的复制方法
                    //第一次授权时，将授权码复制到本地，后续检查授权信息都是依据数据库的授权信息，而不是本地的授权码。仅为备份

                    //导出的机器码为：AuthorityUtils.digest(MacUtils.getMac(mContext))
                    mMacStrings = FileUtils.readTxt(app.getLicenceDir() + LICENCE_NAME).split(",");
                        if (mMacStrings[0].equals(digest(MacUtils.getMac(context)))) {//1.mac值相对应；2.加密的授权时间的加密内容统一为88个字数限制，加密规则(也可以采用两者mac值长度是否相等判断)
                            //digest(MacUtils.getMac(context))本机的MAC地址值
                            util.infoLog(TAG,"合法文件中mac验证身份正确",null);
                            util.varyLog(TAG,mMacStrings[0],"mMacStrings[0]");
                            util.varyLog(TAG,digest(MacUtils.getMac(context)),"digest(MacUtils.getMac(context))");

                            if(null == AuthorityUtils.getAuthInfo(mMacStrings[1])){
                                util.infoLog(TAG,"授权码时间部分错误",null);
                                Toast.makeText(context,"授权码信息是处于非法授权时间",Toast.LENGTH_SHORT).show();
                                usbTurnActivity(context, path);
                                return;//先确保时间组成正确
                            }
                            util.infoLog(TAG,"授权身份与授权码都正确",null);
                            app.setMap(AuthorityUtils.getAuthInfo(mMacStrings[1]));
                            app.setStart_time((long) app.getMap().get("startTime"));//存储授权时间信息；暂时不设定Date显示格式
                            app.setEnd_time((long) app.getMap().get("endTime"));
                            app.setCreateTime(System.currentTimeMillis());//重新设置时间差是因为多次U盘导入时，可能不再执行mainActivity的setcreateTime()
                            //todo:获取，存储并更新授权信息变量
                            LogUtils.e(app.getAuthority_time());
                            if (app.getAuthority_time().equals("无")) {//仅执行一次的第一次初始化--数据库的授权时间默认为无
                                util.infoLog(TAG,"第一次设置授权时间",null);
                                setAuthorityTimes();
                            }else{
                                switch (app.getAuthorityName()){
                                    case "已授权":
                                        //判断是否重复的授权时间段或小于原先授权时间段信息，是则退出，否则更新
                                        if (mSource.getEnd_time() >= app.getEnd_time()) {
                                            util.infoLog(TAG,"重复的授权过期时间",null);
                                            break;
                                        }else{//不同授权过期时间则进行更新
                                            util.infoLog(TAG,"更新授权时间",null);
                                            setAuthorityTimes();
                                        }
                                        break;
                                    case "授权过期":
                                        util.infoLog(TAG,"授权过期，更新授权时间",null);
                                        setAuthorityTimes();
                                        break;
                                    default:
                                        break;
                                }
                            }
                            LogUtils.e(app.getCreate_time());
                            //todo:判断首次与非首次授权情况，对是否检查资源文件进行判断：首次若授权失败则不能跳转检查资源文件
                            if (app.getCreate_time() == 0) {//数据库的当前时间默认为0，用于记录每次成功播放资源时的时间,第一次授权时原信息为空，所以直接覆盖即可
                                Toast.makeText(context, "this is 第一次资源导入", Toast.LENGTH_SHORT).show();
                                app.setAuthority_state(true);//此次已获取授权状态，完成授权文件的更新；并且更新相关的信息

                                //仅授权文件导入时，存储相关信息，但未更新表
                                mDevice.setAuthority_state(app.isAuthority_state());
                                mDevice.setAuthority_time(app.getAuthority_time());
                                mDevice.setAuthority_expired(app.getAuthority_expired());

                                util.varyLog(TAG,app.getCreateTime(),"getCreateTime");
                                util.varyLog(TAG,app.getEnd_time(),"getEnd_time");
                                util.varyLog(TAG,app.getStart_time(),"getStart_time");
                                util.varyLog(TAG,app.getRelative_time(),"getRelative_time()");
                                //第一次播放时，因为没有上一次播放记录，因此无法实现一次播放必须比上一次播放时间晚的判断要求，故在此允许播放时间范围为授权时间内
                                if(app.getCreateTime() > app.getRelative_time()){//情景：正确的授权码，但是本地时间是异常的，导致授权失败
                                    Toast.makeText(context,"第一次授权操作错误，请根据手册进行操作",Toast.LENGTH_SHORT).show();
                                    //若失败则恢复原先的初始状态
                                    app.setAuthority_state(false);
                                    app.setRelative_time(0);
                                    app.setAuthority_time("无");
                                    app.setAuthority_expired("无");
                                }else{
                                    //若成功则存储该授权信息，则此时为第一次授权成功，并跳转检查资源文件
                                    if(mSource.getSon_source() == null){
                                        util.infoLog(TAG,"成功则存储该授权信息，则此时为第一次授权成功",null);

                                        //app.setFirst_time(System.currentTimeMillis());//记录第一次导入时本地的时间
                                        mSource.setRelative_time(app.getRelative_time());
                                        mSource.setFirst_time(System.currentTimeMillis());
                                        mSource.setTime_difference(app.getTime_difference());
                                        daoManager.getSession().getSourceDao().update(mSource);
                                    }else{
                                        mSource.setRelative_time(app.getRelative_time());
                                        daoManager.getSession().getSourceDao().update(mSource);
                                    }
                                    usbTurnActivity(context, path);
                                }
                                //更新正确的存储信息
                                util.infoLog(TAG,"第一次导入时的存储device表",null);
                                if(mDevice == null){
                                    mDevice = new Device();
                                    daoManager.getSession().getDeviceDao().insert(mDevice);
                                }else{
                                    daoManager.getSession().getDeviceDao().update(mDevice);
                                }

                            } else {//非首次授权，无需条件限制，直接存储授权时间
                                //正常情况下，本次导入的节目时间一定比上一次时间大；授权时间一定比当前时间大；避免修改安卓本地时间简易破解授权
                                /*LogUtils.e(app.getRelative_time() > app.getCreateTime());//1.过了授权期就有问题
                                Log.d(TAG, "this is app.getCreateTime()-app.getFirst_time() < app.getTime_difference() :" + ((app.getCreateTime() - app.getFirst_time()) < app.getTime_difference()));
                                Log.d(TAG, "this is app.getCreateTime() > mSource.getCreate_time() :" + (app.getCreateTime() > mSource.getCreate_time()) + app.getCreateTime() + ">>>" + mSource.getCreate_time());*/
                                //存储正确的授权时间信息并更新数据库
                                util.infoLog(TAG,"非首次授权信息更新",null);
                                if(app.getRelative_time() > mSource.getRelative_time() ){//授权到期时差>0才刷新授权信息
                                    util.infoLog(TAG,"有效授权时间，进行更新",null);
                                    mDevice.setAuthority_state(app.isAuthority_state());
                                    mDevice.setAuthority_time(app.getAuthority_time());
                                    mDevice.setAuthority_expired(app.getAuthority_expired());
                                    if(mDevice == null){
                                        mDevice = new Device();
                                        daoManager.getSession().getDeviceDao().insert(mDevice);
                                    }else{
                                        daoManager.getSession().getDeviceDao().update(mDevice);
                                    }
                                    if (((app.getCreateTime() - app.getFirst_time()) < app.getTime_difference())
                                            && app.getCreateTime() > app.getCreate_time()
                                            && app.getRelative_time() > app.getCreateTime()) {//1.第一次导入资源时间与当前时间差<授权时间段；2.当前时间一定大于上一次的当前时间；3.设置相对过期时间，当前时间过了就不允许播放
                                        app.setAuthority_state(true);
                                    }
                                }
                                usbTurnActivity(context, path);

                                /*if (((app.getCreateTime() - app.getFirst_time()) < app.getTime_difference())
                                        && app.getCreateTime() > app.getCreate_time()
                                        && app.getRelative_time() > app.getCreateTime()) {//1.第一次导入资源时间与当前时间差<授权时间段；2.当前时间一定大于上一次的当前时间；3.设置相对过期时间，当前时间过了就不允许播放
                                    Toast.makeText(context, "this is 后续资源导入", Toast.LENGTH_SHORT).show();
                                    app.setAuthority_state(true);

                                    usbTurnActivity(context, path);
                                } else {
                                    Log.d(TAG, "this is 后续导入资源时，不在有效授权时间内");
                                    Toast.makeText(context, "导入资源时，不在有效授权时间内", Toast.LENGTH_SHORT).show();
                                }*/
                            }
                        } else {
                            util.infoLog(TAG,"非法授权身份",null);
                            util.varyLog(TAG,mMacStrings[0],"mMacStrings[0]");
                            util.varyLog(TAG,mMacStrings[1].length(),"mMacStrings[1].length()");
                            util.varyLog(TAG,MacUtils.getMac(app.getmContext()),"MacUtils.getMac(app.getmContext())");
                            util.varyLog(TAG,digest(MacUtils.getMac(context)),"digest(MacUtils.getMac(context))");
                            Toast.makeText(context, "非法授权身份", Toast.LENGTH_SHORT).show();
                            if (mDevice.getAuthority_state()) {//数据库中的授权状态为真才能执行资源读取
                                util.infoLog(TAG,"非法授权身份“，”，若存在授权状态，则进行对资源文件的更新",null);
                                usbTurnActivity(context, path);
                            }
                        }
                } else {
                    util.infoLog(TAG,"非法授权文件",null);
                    Toast.makeText(context, "非法授权文件", Toast.LENGTH_SHORT).show();
                    if (mDevice.getAuthority_state()) {//数据库中的授权状态为真才能执行资源读取
                        util.infoLog(TAG,"非法授权文件“，”，若存在授权状态，则进行对资源文件的更新",null);
                        usbTurnActivity(context, path);
                    }
                }
            } else {
                util.infoLog(TAG,"无授权文件，请检查授权文件",null);
                if (mDevice.getAuthority_state()) {//数据库中的授权状态为真才能执行资源读取
                    util.infoLog(TAG,"无授权文件，若存在授权状态，则进行对资源文件的更新",null);
                    usbTurnActivity(context, path);
                }
            }
        } else {
            Toast.makeText(context, "U盘路径为空，未接入U盘", Toast.LENGTH_SHORT).show();
            util.infoLog(TAG,"U盘未接入",null);
        }
    }

    private static void setAuthorityTimes() {
        //app.setFirst_time(System.currentTimeMillis());//记录第一次导入时本地的时间
        LogUtils.e(mSource);
        if(mSource!=null){//有表
            if(mSource.getFirst_time() == 0){//有表的第一次授权导入(即有资源播放的第一次)
                app.setFirst_time(System.currentTimeMillis());//记录第一次导入时本地的时间
            }
        }

        app.setTime_difference(app.getEnd_time() - app.getStart_time());//记录两时间戳的差值
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//                                df.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));//加上这一行代码之后，将当前时间转化为世界时间，但不适用此处
        //todo：这里是授权文件的时间，如果电脑的时间不是准确的，则本地加时间差。
        if (app.getStart_time() <= app.getCreateTime()) {//获取本地时间为准还是以服务器时间为准
            util.infoLog(TAG,"本地时间是服务器时间",null);
            app.setAuthority_time(df.format(new Date(app.getStart_time())));
            app.setAuthority_expired(df.format(new Date(app.getEnd_time())));
            app.setRelative_time(app.getEnd_time());//将服务器时间设为设为授权到期时间
            util.varyLog(TAG,app.getRelative_time(),"授权时间设置完成，值为app.getRelative_time()");
        } else {
            util.infoLog(TAG,"本地时间不是服务器时间",null);
            app.setAuthority_time(df.format(new Date(app.getCreateTime())));
            app.setAuthority_expired(df.format(new Date(app.getCreateTime() + app.getTime_difference())));
            app.setRelative_time(app.getCreateTime() + app.getTime_difference());//将本地时间设为授权到期时间
            util.varyLog(TAG,app.getRelative_time(),"授权时间设置完成,值为app.getRelative_time()");
        }
        LogUtils.e(app.getAuthority_time());
        LogUtils.e(app.getAuthority_expired());
    }

    public static void usbTurnActivity(Context context, String path) {
        util.infoLog(TAG,"USB跳转进入到ImportActivity",null);
        app.setCreate_time(app.getCreateTime());//获取当前时间赋值给与数据库相关数据的"当前时间"--作为上次时导入时间,非授权相关信息，属于播放相关信息。
        Intent i = new Intent(context, ImportActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString("extra_path", path);
        i.putExtras(bundle);
        if (path == null) {
            Toast.makeText(context, "path为null,无法跳转",Toast.LENGTH_SHORT).show();
        } else {
            context.startActivity(i);
        }
    }
}
