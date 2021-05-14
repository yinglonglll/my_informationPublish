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

import static cn.ghzn.player.Constants.LICENCE_NAME;
import static cn.ghzn.player.MainActivity.app;
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
        if (path != null || app.isImportState()) {//接入的不是U盘则不会进行读取
            Log.d(TAG, "U盘接入");

            Toast.makeText(context, "U盘接入，路径为：", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, path, Toast.LENGTH_SHORT).show();
            Log.d(TAG, path);
            if (path.equals("file:///storage/emulated/0")) {//减少无用功运行，即不符退出即可。
                return;
            }
            app.setExtraPath(path.replace("file://", "") + "/Android/data/cn.ghzn.player/files/");//去除uri前缀，得到文件路径(绝对路径);记录U盘中我们新建ghznPlayer的绝对地址
            Log.d(TAG, "this is extraPath" + app.getExtraPath());

            //todo:对搜寻授权码进行更新检查，保持最新的授权码
            File updateLicence = new File(app.getExtraPath() + LICENCE_NAME);
            if (updateLicence.exists()) {
                Log.d(TAG, "this is 授权码存在 :" + updateLicence.getAbsolutePath());

                File deleteLicence = new File(app.getLicenceDir() + LICENCE_NAME);
                if (deleteLicence.exists()) {
                    deleteLicence.delete();
                    Log.d(TAG, "this is 原机器码存在，进行删除 ：" + deleteLicence.delete());
                }
                //存在app.getLicenceDir()为null的情况;在重复U盘考入文件播放时,值有时会被清掉
                if (app.getLicenceDir() == null) {
                    app.setLicenceDir(getFilePath(context, Constants.STOREPATH) + "/");
                    Log.d(TAG, "this is app.getLicenceDir() :" + app.getLicenceDir());
                }
                //若是非法授权文件，以“,”区分，则直接放弃读取，不执行覆盖。
                //todo：U盘导入后，先搜寻授权文件-验证mac-分析内容，符合则真状态，不符则假状态;授权文件以","为区分
                if (FileUtils.readTxt(updateLicence.getAbsolutePath()).contains(",")) {//检验U盘授权文件的合法性
                    Log.d(TAG, "this is 合法授权文件");
                    Toast.makeText(context, "this is 合法授权文件", Toast.LENGTH_SHORT).show();
                    FileUtils.copyFile(updateLicence.getAbsolutePath(), app.getLicenceDir() + LICENCE_NAME);


                    //导出的机器码为：AuthorityUtils.digest(MacUtils.getMac(mContext))
                    mMacStrings = FileUtils.readTxt(app.getLicenceDir() + LICENCE_NAME).split(",");
                    if (mMacStrings[0].equals(digest(MacUtils.getMac(context)))) {//合法文件中mac验证身份正确--正确且符合的授权文件
                        Log.d(TAG, "this is 合法文件中mac验证身份正确");

                        Log.d(TAG, "this is 非法身份验证或逗号过多的非法授权文件");
                        Log.d(TAG, "this is mMacStrings[0] :" + mMacStrings[0]);
                        Log.d(TAG, "this is digest(MacUtils.getMac(context))" + digest(MacUtils.getMac(context)));
                        Log.d(TAG,"this is MacUtils.getMac(mContext)" + MacUtils.getMac(app.getmContext()));

                        Toast.makeText(context, "this is 合法文件中mac验证身份正确", Toast.LENGTH_SHORT).show();

                        app.setMap(AuthorityUtils.getAuthInfo(mMacStrings[1]));
                        app.setStart_time((long) app.getMap().get("startTime"));//存储授权时间信息；暂时不设定Date显示格式
                        app.setEnd_time((long) app.getMap().get("endTime"));

                        //todo:设置显示授权时间和授权失效时间
                        LogUtils.e(app.getAuthority_time());
                        if (app.getAuthority_time().equals("无")) {//仅执行一次的第一次初始化--数据库的授权时间默认为无
                            setAuthorityTimes();
                        }else{
                            switch (app.getAuthorityName()){
                                case "已授权":
                                    //判断是否重复的授权时间段信息，是则退出，否则更新
                                    if (app.getSource().getEnd_time() == app.getEnd_time()) {
                                        Log.d(TAG,"this is 重复的授权过期时间");
                                        break;
                                    }else{//不同授权过期时间则进行更新
                                        Log.d(TAG,"this is 更新授权时间");
                                        setAuthorityTimes();
                                    }
                                    break;
                                case "授权过期":
                                    Log.d(TAG,"this is 授权过期，更新授权时间");
                                    setAuthorityTimes();
                                    break;
                                default:
                                    break;
                            }
                        }

                        //todo:获取并存储授权信息的内容，再进行对内容的取出，用于判断授权状态以限制其他操作---嵌入跳转功能
                        if (app.getCreate_time() == 0) {//数据库的当前时间默认为0，用于记录每次成功播放资源时的时间
                            Toast.makeText(context, "this is 第一次资源导入", Toast.LENGTH_SHORT).show();
                            app.setAuthority_state(true);//此次已获取授权状态，完成授权文件的更新

                            usbTurnActivity(context, path);
                        } else {////正常情况下，本次导入的节目时间一定比上一次时间大；授权时间一定比当前时间大；避免修改安卓本地时间简易破解授权
                            //app.getRelative_time() > app.getCreate_time() ||多余的相对时间判断
                            LogUtils.e(app.getRelative_time() > app.getCreateTime());//1.过了授权期就有问题
                            Log.d(TAG, "this is app.getCreateTime()-app.getFirst_time() < app.getTime_difference() :" + ((app.getCreateTime() - app.getFirst_time()) < app.getTime_difference()));
                            Log.d(TAG, "this is app.getCreateTime() > app.getSource().getCreate_time() :" + (app.getCreateTime() > app.getSource().getCreate_time()) + app.getCreateTime() + ">>>" + app.getSource().getCreate_time());
                            app.setCreateTime(System.currentTimeMillis());//重新设置时间差是因为多次U盘导入时，可能不再执行mainActivity的setcreateTime()
                            if (((app.getCreateTime() - app.getFirst_time()) < app.getTime_difference())
                                    && app.getCreateTime() > app.getCreate_time() //保证播放时间是向前的，即授权过期时重新授权，此时数据库仍存有上次成功导入信息的时间，故重新授权时需重置上次成功导入时间；如同加速度方向向前
                                    && app.getRelative_time() > app.getCreateTime()) {//1.第一次导入资源时间与当前时间差<授权时间段；2.当前时间一定大于上一次的当前时间；3.设置相对过期时间，当前时间过了就不允许播放
                                Toast.makeText(context, "this is 后续资源导入", Toast.LENGTH_SHORT).show();
                                app.setAuthority_state(true);

                                usbTurnActivity(context, path);
                            } else {
                                Log.d(TAG, "this is 后续导入资源时，不在有效授权时间内");
                                Toast.makeText(context, "this is 后续导入资源时，不在有效授权时间内", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.d(TAG, "this is 非法身份验证或逗号过多的非法授权文件");
                        Log.d(TAG, "this is mMacStrings[0] :" + mMacStrings[0]);
                        Log.d(TAG, "this is digest(MacUtils.getMac(context))" + digest(MacUtils.getMac(context)));
                        Log.d(TAG,"this is MacUtils.getMac(mContext)" + MacUtils.getMac(app.getmContext()));
                        Toast.makeText(context, "this is 非法身份验证或逗号过多的非法授权文件", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "this is 非法授权文件");
                    Toast.makeText(context, "this is 非法授权文件，无进行复制授权文件", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "this is 无授权文件，请检查授权状态");
                if (app.getDevice().getAuthority_state()) {//数据库中的授权状态为真才能执行资源读取
                    Log.d(TAG, "this is 无授权文件，存在授权状态");

                    usbTurnActivity(context, path);
                }
            }
        } else {
            Toast.makeText(context, "path为空，未接入U盘", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "U盘未接入");
        }
    }

    private static void setAuthorityTimes() {
        app.setFirst_time(System.currentTimeMillis());//记录第一次导入时本地的时间
        app.setTime_difference(app.getEnd_time() - app.getStart_time());//记录两时间戳的差值
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//                                df.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));//加上这一行代码之后，将当前时间转化为世界时间，但不适用此处
        //todo：这里是授权文件的时间，如果电脑的时间不是准确的，则本地加时间差。
        if (app.getStart_time() < app.getCreateTime()) {//获取本地时间为准还是以服务器时间为准
            Log.d(TAG, "this is 本地时间是服务器时间");
            app.setAuthority_time(df.format(new Date(app.getStart_time())));
            app.setAuthority_expired(df.format(new Date(app.getEnd_time())));
            app.setRelative_time(app.getEnd_time());//将服务器时间设为设为授权到期时间
        } else {
            Log.d(TAG, "this is 本地时间不是服务器时间");
            app.setAuthority_time(df.format(new Date(app.getCreateTime())));
            app.setAuthority_expired(df.format(new Date(app.getCreateTime() + app.getTime_difference())));
            app.setRelative_time(app.getCreateTime() + app.getTime_difference());//将本地时间设为授权到期时间
        }
    }

    public static void usbTurnActivity(Context context, String path) {
        app.setCreate_time(app.getCreateTime());//获取当前时间赋值给与数据库相关数据的"当前时间"--作为上次时导入时间
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
