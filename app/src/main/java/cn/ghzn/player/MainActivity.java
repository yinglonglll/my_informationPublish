package cn.ghzn.player;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.apkfuns.logutils.LogUtils;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.ghzn.player.entity.UsbHelper;
import cn.ghzn.player.order.YangYuOrder;
import cn.ghzn.player.receiver.USBBroadCastReceiver;
import cn.ghzn.player.receiver.VarReceiver;
import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.singleSource.SingleSource;
import cn.ghzn.player.sqlite.source.Source;
import cn.ghzn.player.util.AuthorityUtils;
import cn.ghzn.player.util.InfoUtils;
import cn.ghzn.player.util.UsbUtils;
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.Constants.LICENCE_NAME;
import static cn.ghzn.player.Constants.MACHINE_CODE_NAME;
import static cn.ghzn.player.MyApplication.single;
import static cn.ghzn.player.MyApplication.util;
import static cn.ghzn.player.util.FileUtils.getFilePath;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    public static MyApplication app;
    public final static DaoManager daoManager = DaoManager.getInstance();//找到单例(唯一数据库对象，只管取来用)
    private static final String TAG = "MainActivity";
    private static boolean isSave;
    private View viewLp;

    GestureDetector mGestureDetector;
    private TextView mSingleSplitMode;
    private TextView mDeviceName;
    private TextView mDeviceId;
    private TextView mAuthorityState;
    private TextClock mLocalTime;
    private TextView mAuthorityTime;
    private TextView mAuthorityExpired;
    private TextView mLeftMargin;
    private TimePicker tp_Start;
    private TimePicker tp_End;
    private CheckBox cb_monday;
    private CheckBox cb_tuesday;
    private CheckBox cb_wednesday;
    private CheckBox cb_thursday;
    private CheckBox cb_friday;
    private CheckBox cb_saturday;
    private CheckBox cb_sunday;
    private Switch mSwitch;
    private Button mSingleBtn;

    private BroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver mRenovateBroadcastReceiver;
    private Intent mIntent_FinishFlag = new Intent();
    private File mLicenceSaveFile;
    private File mMachineCodeSaveFile;
    private UsbHelper usbHelper;
    private AlertDialog AlertDialogs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWritePermission();//实现动态获取写入权限。避免静态获取权限失败
        app = (MyApplication)getApplication();//实现获取Application对象，全局缓存调用
        UsbUtils.checkUsb(this);//实现通过USB协议检查出USB是什么类型设备
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//实现取消导航栏
        setContentView(R.layout.activity_main);
        AutoOutPutMachineCode();//方法内部取消了自动导出机器码，仅保留动态获取usbHelper的权限
        if (app.getCurrentActivity() != null) {//实现返回主界面时，关闭先前播放的内容界面
            LogUtils.e(app.getCurrentActivity());
            app.getCurrentActivity().finish();
            util.infoLog(TAG,"关闭了正在播放的分屏资源",null);
        }

        app.setSource(DaoManager.getInstance().getSession().getSourceDao().queryBuilder().unique());
        if (app.getSource() != null && app.getSource().getRelative_time() != 0) {
            app.setRelative_time(app.getSource().getRelative_time());//initDevice()中需要用到此数据，故先提前初始化；main代码若优化应先初始化，展示main界面，再跳转。
        }

        initView();//找到layout控件，初始化主界面的信息
        initBroadReceiver();//广播监听：保证资源播放activity被finish掉
        initDevice();
        initSource();//资源初始化放在如上
        initSingleSource();
        setDialog();

        if(app.getDevice() != null && app.getDevice().getPower_end_time() != null){//不可去掉device != null，无数据库时其数据无法获取
            YangYuOrder order = new YangYuOrder();
            order.startup_shutdow_off(this);
            order.startup_shutdow_on(this, app.getDevice().getPower_start_time(), app.getDevice().getPower_end_time());
            LogUtils.e("this is ----------初始化定时任务----------"+"\n"
                    + "定时开始时间为：" + app.getDevice().getPower_start_time()
                    + "定时结束时间为：" + app.getDevice().getPower_end_time());
        }else{
            util.infoLog(TAG,"定时任务无数据，无法进行定时",null);
        }

        if (app.isImportState() && app.isSetSourcePlayer()) {//仅允许在初次资源导入时，U盘插入顺序与软件打开顺序无关；在已导入资源的情况下，必须在播放的情况下，再插入U盘进行资源变更。
            app.setStrings(UsbUtils.getVolumePaths(this));//通过获取U盘挂载状态检查所有存储的绝对地址，
            for(String str : app.getStrings()){
                if (!str.equals("/storage/emulated/0")) {
                    util.infoLog(TAG,"执行处于先U盘插入，后软件打开时，软件自动导入U盘内容",null);
                    //app.setExtraPath(str + "/Android/data/cn.ghzn.player/files/");//设置U盘的路径
                    app.setExtraPath(str + "/");//设置U盘的路径,以检查授权文件和资源文件
                    UsbUtils.checkUsbFileForm(this,str);
                }
            }
        }
    }

    private void initSingleSource() {
        /*if(single == null){
            single = new SingleSource();//表不存在则新建赋值
            daoManager.getSession().getSingleSourceDao().insert(getSingleSource(single));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
        }else{//存在则直接修改
            daoManager.getSession().getSingleSourceDao().update(getSingleSource(single));
        }*/
    }

    public void initView() {
        util.infoLog(TAG,"初始化当前MainActivity视图控件",null);
        mSingleSplitMode = (TextView) this.findViewById(R.id.tx_SingleSplitMode);
        mDeviceName = (TextView) this.findViewById(R.id.tx_DeviceName);
        mDeviceId = (TextView) this.findViewById(R.id.tx_DeviceID);
        mAuthorityState = (TextView) this.findViewById(R.id.tx_AuthorityState);
        mAuthorityTime = (TextView) this.findViewById(R.id.tx_AuthorityTime);
        mAuthorityExpired = (TextView) this.findViewById(R.id.tx_AuthorityExpired);
        mLocalTime = (TextClock) this.findViewById(R.id.tc_localTime);
        mLeftMargin = (TextView) this.findViewById(R.id.tx_leftMargin);
        /*非当前视图在对应的控件方法中声明*/
    }

    private void initBroadReceiver() {
        util.infoLog(TAG,"初始化广播设置",null);
        mBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                util.infoLog(TAG,"广播执行finish掉当前的Activity",null);
                /*取消图片线程，无效化视频监听执行的内容*/
                if (app.getRunnable1() != null) {
                    app.getHandler().removeCallbacks(app.getRunnable1());
                }
                if (app.getRunnable2() != null) {
                    app.getHandler().removeCallbacks(app.getRunnable2());
                }
                if (app.getRunnable3() != null) {
                    app.getHandler().removeCallbacks(app.getRunnable3());
                }
                if (app.getRunnable4() != null) {
                    app.getHandler().removeCallbacks(app.getRunnable4());
                }
                app.setMediaPlayState(false);

                if (app.getCurrentActivity() != null) {
                    app.getCurrentActivity().finish();//如果将已分屏的逻辑没finish掉，则强制finish掉，重新执行分屏，避免线程过多
                }
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction("true");
        registerReceiver(mBroadcastReceiver,filter);//注册广播
        mRenovateBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {//一个对象未取消注册广播置null时，不可复用、
            @Override
            public void onReceive(Context context, Intent intent) {
                util.infoLog(TAG,"广播刷新主界面授权信息",null);
                if (app.getRelative_time() == 0) {//授权与未授权区分之一的方法在于 授权到期时间 有无；
                    app.setAuthorityName("未授权");
                } else {
                    Log.d(TAG,"this is enter 刷新授权状态");
                    if (app.isAuthority_state()) {//授权状态为真，则显示已授权，否则则授权过期。
                        app.setAuthorityName("已授权");
                    } else {
                        app.setAuthorityName("授权过期");
                        app.setCreate_time(0);//授权为假时，为授权过期，则设置上一次的成功导入资源时间为0，模拟初始状态；比喻为只能向前的点的位置重置为初试状态的位置再重新向前。
                    }
                }
                daoManager.getSession().getDeviceDao().update(app.getDevice());
                mAuthorityState.setText("授权状态：" + app.getAuthorityName());
                mAuthorityTime.setText("授权时间：" + app.getAuthority_time());
                mAuthorityExpired.setText("授权到期：" + app.getAuthority_expired());
            }
        });
        IntentFilter RenovateFilter = new IntentFilter("cn.ghzn.player.broadcast.RENOVATE_MAIN");
        registerReceiver(mRenovateBroadcastReceiver,RenovateFilter);//注册广播
    }

    public void initDevice() {
        util.infoLog(TAG,"初始化设备信息",null);
        app.setDevice(DaoManager.getInstance().getSession().getDeviceDao().queryBuilder().unique());
        if(app.getDevice() == null){
            util.infoLog(TAG,"初始化设备信息--新建",null);
            app.setDevice(new Device());//表不存在则新建赋值
            daoManager.getSession().getDeviceDao().insert(getDevice(app.getDevice()));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
        }else{//存在则直接修改
            util.infoLog(TAG,"初始化设备信息-更新",null);
            LogUtils.e(app.getDevice().getAuthority_state());
            daoManager.getSession().getDeviceDao().update(getDevice(app.getDevice()));
            LogUtils.e(app.getDevice().getAuthority_state());
        }

        initImportDevice(app.getDevice());//初始化数据且设置layout控件；从上述数据库中取信息出来显示
        Log.d(TAG,"--------设备信息---------");
        LogUtils.e(app.getDevice());//利用第三方插件打印出对象的属性和方法值；
    }

    private Device getDevice(Device device){
        if(device.getDevice_name()==null)device.setDevice_name(InfoUtils.getDeviceName());
        if(device.getDevice_id()==null)device.setDevice_id(InfoUtils.getDeviceId());
        if(device.getAuthority_time()==null)device.setAuthority_time(InfoUtils.getAuthorityTime());
        device.setMachine_code(InfoUtils.getMachineCode());
//        if(device.getAuthority_expried().toString()==null)device.setAuthority_expried(InfoUtils.getAuthorityExpried());//data类数据，不知这样操作是否对
        device.setSoftware_version(InfoUtils.getSoftware_version());
        device.setFirmware_version(InfoUtils.FirmwareVersion());
        device.setWidth(InfoUtils.getWidth());
        device.setHeight(InfoUtils.getHeight());
        return device;
    }

    private void initSource() {
        util.infoLog(TAG,"初始化资源信息-->",null);
        app.setLicenceDir(getFilePath(MainActivity.this, Constants.STOREPATH) + "/");//获取生成授权文件的文件夹地址
        app.setCreateTime(System.currentTimeMillis());
        File file = new File(app.getLicenceDir());
        if (!file.exists()) {
            file.mkdirs();
        }
        util.varyLog(TAG,app.getLicenceDir(),"自动生成的本地目录为LicenceDir");
//        app.getSource().setLicense_dir(app.getLicenceDir());//获得source表的setLicense_dir方法，把导出license文件时的地址存储在数据库对应数据中
//        LogUtils.e(app.getSource());//未U盘导入资源时，表为空，不可调用赋值。

        //todo：授权期内过期(通过时间比较)，禁止资源初始化和跳转并提醒
        if (app.getSource() != null &&  (app.getSource().getSplit_view() != null || single!=null&&single.getSingle_Son_source()!=null)) {//1.判断授权文件；2.判断资源文件
            initImportSource(app.getSource());//初始化数据库数据到全局变量池--含device与source表
            Log.d(TAG,"--------资源信息---------");
            LogUtils.e(app.getSource());

            util.infoLog(TAG,"检查授权时间逻辑的3个条件-->",null);
            util.varyLog(TAG,app.getCreateTime() > app.getSource().getCreate_time(),"app.getCreateTime() > app.getSource().getCreate_time()");
            util.varyLog(TAG,(app.getCreateTime()-app.getFirst_time()) < app.getTime_difference(),"(app.getCreateTime()-app.getFirst_time()) < app.getTime_difference())");
            util.varyLog(TAG,app.getRelative_time() > app.getCreateTime(),"app.getRelative_time() > app.getCreateTime()");
            util.varyLog(TAG,app.getRelative_time(),"app.getRelative_time()");
            util.varyLog(TAG,app.getCreateTime(),"app.getCreateTime()");
            if (app.getCreateTime() > app.getCreate_time()
                    && (app.getCreateTime()-app.getFirst_time()) < app.getTime_difference()
                    && app.getRelative_time() > app.getCreateTime()) {//1.当前时间一定大于上一次的当前时间,即保证播放时间是向前的；2.第一次导入资源时间与当前时间差<授权时间段；3.设置相对过期时间，当前时间过了就不允许播放
                app.getDevice().setAuthority_state(true);
                Intent RenovateIntent = new Intent("cn.ghzn.player.broadcast.RENOVATE_MAIN");
                sendBroadcast(RenovateIntent);
                app.setSetSourcePlayer(false);//此处若进来则进行播放，此时false，避免搜索挂载U盘的目录来重复播放
                if(app.isSingle_split_mode()){
                    util.infoLog(TAG,"进入到单屏模式",null);
                    turnActivity(single.getSingle_view());//0
                }else{
                    util.infoLog(TAG,"进入到多屏模式",null);
                    util.varyLog(TAG,app.getSplit_view(),"app.getSplit_view");
                    turnActivity(app.getSplit_view());//1.保证导入时间只能向前；2.保证正常授权的时间段内(指定时间范围长度)；3.强制授权期内过期
                }
            } else {
                util.infoLog(TAG,"授权过期，进入无授权状态",null);
                app.getDevice().setAuthority_state(false);
                Intent RenovateIntent = new Intent("cn.ghzn.player.broadcast.RENOVATE_MAIN");
                sendBroadcast(RenovateIntent);
            }
            //daoManager.getSession().getDeviceDao().update(app.getDevice());
            //daoManager.getSession().getSourceDao().update(app.getSource());//正常情况下，自动播放资源为正确，但非正常操作会导致异常，使得存储错误信息或修正后信息无法存储
        }
    }

    public void setDialog() {
        util.infoLog(TAG,"在MainActivity设置了弹窗Dialog",null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        app.setView(this.getLayoutInflater().inflate(R.layout.activity_dialog, null));
        alertDialog.setView(app.getView());
        AlertDialogs = alertDialog.create();//如上是我自己找到新建的弹窗，下面是把新建的弹窗赋给新建的手势命令中的长按。
        mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                util.infoLog(TAG,"触发长按按钮",null);
                AlertDialogs.show();
                //解决右键退出AlertDialogs的bug：The specified child already has a parent. You must call removeView() on the child's parent first. The specified child already has a parent. You must call removeView() on the child's parent first.
                AlertDialogs.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        util.infoLog(TAG,"执行onDismiss()--",null);
                        if (viewLp != null) {
                            ViewGroup parentView = (ViewGroup) viewLp.getParent();
                            if (parentView != null) {
                                parentView.removeView(viewLp);
                                util.infoLog(TAG,"执行onDismiss()--removeView()",null);
                            }
                        }
                    }
                });
            }
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    private void AutoOutPutMachineCode() {
        usbHelper = new UsbHelper(this, new USBBroadCastReceiver.UsbListener() {
            @Override
            public void insertUsb(UsbDevice device_add) {
                Log.e(TAG, "usb insert:"+device_add.getDeviceName());
                //检测刚接入USB，申请权限--
                usbHelper.requestPermission(device_add);
            }

            @Override
            public void removeUsb(UsbDevice device_remove) {
                Log.e(TAG, "usb remove:"+device_remove.getDeviceName());
            }

            @Override
            public void getReadUsbPermission(UsbDevice usbDevice) {
                Log.e(TAG, "usb get read permission:"+usbDevice.getDeviceName());
                //申请权限成功，执行文件写入--
                /*UsbMassStorageDevice[] devices = usbHelper.getDeviceList();
                for(UsbMassStorageDevice device : devices){
                    List<UsbFile> usbFiles = usbHelper.readDevice(device);
                    if(usbFiles==null)break;
                    Log.e(TAG, "find device:"+ device.getUsbDevice().getDeviceName());
                    Log.e(TAG, usbHelper.getCurrentFolder().getAbsolutePath());

                    boolean result = usbHelper.saveSDFileToUsb(getLocalFile(), usbHelper.getCurrentFolder(), new UsbHelper.DownloadProgressListener() {
                        @Override
                        public void downloadProgress(int progress) {
                            Log.e(TAG, "download to usb_MachineCode.txt:"+progress);
                        }
                    });
                    Log.e(TAG, "download and result :" + result);
                    device.close();

                }*/

            }

            @Override
            public void failedReadUsb(UsbDevice usbDevice) {
                Log.e(TAG, "usb fail read permission:"+usbDevice.getDeviceName());
            }
        });
    }

    private File getLocalFile(){
        File localFile = new File("/storage/emulated/0/Android/data/cn.ghzn.player/files/ghzn/", "MachineCode.txt");
        try {
            FileWriter writer = new FileWriter(localFile);
            writer.write(app.getMachine_code());//机器码导出
            writer.flush();
            writer.close();
            return localFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void turnActivity(String split_view) {//仅给数据库的数据使用的方法,无检错跳转
        switch (split_view) {
            case "1":
                Log.d(TAG,"this is case1");
                    Intent intent1 = new Intent(this, OneSplitViewActivity.class);
                    startActivity(intent1);
                break;
            case "2":
                    Intent intent2 = new Intent(this, TwoSplitViewActivity.class);
                    startActivity(intent2);
                break;
            case "3":
                    Intent intent3 = new Intent(this,ThreeSplitViewActivity.class);
                    startActivity(intent3);

                break;
            case "4":
                    Intent intent4 = new Intent(this, FourSplitViewActivity.class);
                    startActivity(intent4);
                break;
            case "0":
                Intent singleIntent = new Intent(this, SingleSplitViewActivity.class);
                Log.d(TAG,"this is 从主界面进入到单屏界面");
                String splitView = "0";
                String filesParent = "/storage/emulated/0/Android/data/cn.ghzn.player/files/ghzn/singlePlayer";
                singleIntent.putExtra("splitView",splitView);
                singleIntent.putExtra("filesParent",filesParent);
                startActivity(singleIntent);
                break;
            default:
                util.varyLog(TAG,split_view,"split_view");
                Toast.makeText(this, "请勿放入过多文件，请按照教程方法的格式放入对应的文件", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void machineIdOutBtn(final View view) {
        Log.d(TAG,"this is MachineIdOutBtn");

        if (app.isImportState()) {
            /*实现将机器码文件生成到U盘根目录下*/
            Log.d(TAG,"this is ImportState :" + app.isImportState());
            Log.d(TAG,"this is 将机器码导出到U盘中");
            mLicenceSaveFile = new File(app.getExtraPath(),LICENCE_NAME);
            mMachineCodeSaveFile = new File(app.getExtraPath(),MACHINE_CODE_NAME);

            UsbMassStorageDevice[] devices = usbHelper.getDeviceList();
            for(UsbMassStorageDevice device : devices){
                app.setReadDeviceState(true);//readDevice方法会先发送UNMOUNTED，再发送MOUNTED来获取设备信息。会对广播进行干扰，现进行标屏蔽。
                List<UsbFile> usbFiles = usbHelper.readDevice(device);
                if(usbFiles==null)break;
                Log.e(TAG, "find device:"+ device.getUsbDevice().getDeviceName());
                Log.e(TAG, usbHelper.getCurrentFolder().getAbsolutePath());

                boolean result = usbHelper.saveSDFileToUsb(getLocalFile(), usbHelper.getCurrentFolder(), new UsbHelper.DownloadProgressListener() {
                    @Override
                    public void downloadProgress(int progress) {
                        Log.e(TAG, "download to usb_MachineCode.txt:"+progress);
                    }
                });
                Log.e(TAG, "download and result :" + result);
                if(result){
                    Toast.makeText(this,"导出机器码成功",Toast.LENGTH_SHORT).show();
                }
                device.close();
            }
        } else {
            Log.d(TAG,"this is 将机器码导出到本地中");
            mMachineCodeSaveFile = new File(app.getLicenceDir(),MACHINE_CODE_NAME);
            ViewImportUtils.deleteFile(mMachineCodeSaveFile);
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(mMachineCodeSaveFile);
                outStream.write(app.getMachine_code().getBytes("gbk"));//UFT-8在android不能用，只能用gbk!!!不设置的话可能会变成乱码！！！
                outStream.close();
                outStream.flush();
                isSave = true;
                Log.d(TAG, "this is 文件已经保存啦！赶快去查看吧!");
                //Toast.makeText(this, "导出机器码成功", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
                Log.d(TAG, "this is 已重新生成机器码文件到本地目录上");
                Toast.makeText(this,"未插入U盘，无法导出机器码到U盘上",Toast.LENGTH_LONG).show();
        }
    }

    public void SetPowerOnOffBtn(View view) {
        final boolean timeSwitchFlag = true;
        final SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        final View timeView = this.getLayoutInflater().inflate(R.layout.activity_timepicker, null);;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        /*查找非当前界面显示view的控件，需先找到布局文件，再从布局文件对象中调用方法找控件*/
        tp_Start = timeView.findViewById(R.id.tp_Start);
        tp_End = timeView.findViewById(R.id.tp_End);
        cb_monday = timeView.findViewById(R.id.cb_monday);
        cb_tuesday = timeView.findViewById(R.id.cb_tuesday);
        cb_wednesday = timeView.findViewById(R.id.cb_wednesday);
        cb_thursday= timeView.findViewById(R.id.cb_thursday);
        cb_friday = timeView.findViewById(R.id.cb_friday);
        cb_saturday = timeView.findViewById(R.id.cb_saturday);
        cb_sunday = timeView.findViewById(R.id.cb_sunday);
        mSwitch = timeView.findViewById(R.id.swc_switcher);

        tp_Start.setIs24HourView(true);
        tp_End.setIs24HourView(true);

        /*从SharedPreferences获取数据加载配置项--星期和定时任务开*/
        if (preferences != null) {
            boolean powerFlag = preferences.getBoolean("timeSwitchFlag", timeSwitchFlag);
            mSwitch.setChecked(powerFlag);
            /*取字符串，转化为json，转化为map，取map的值set给单选框来恢复状态*/
            if(preferences.getString("weekString",null) != null) {//在进行过设置后才进行取值
                String getWeekString = preferences.getString("weekString", null);
                Log.d(TAG,"this is getWeekString" + getWeekString);
                HashMap<String, Boolean> weekMap = JSON.parseObject(getWeekString, new TypeReference<HashMap<String, Boolean>>() {
                });
                Log.d(TAG,"this is weekMap.get(\"MON\") : " + weekMap.get("MON"));
                cb_monday.setChecked(weekMap.get("MON"));
                cb_tuesday.setChecked(weekMap.get("TUE"));
                cb_wednesday.setChecked(weekMap.get("WED"));
                cb_thursday.setChecked(weekMap.get("THU"));
                cb_friday.setChecked(weekMap.get("FRI"));
                cb_saturday.setChecked(weekMap.get("SAT"));
                cb_sunday.setChecked(weekMap.get("SUN"));
            }
        }
        /*将已设定的定时开关机时间加载到当前timePicker上*/
        if(app.getDevice()!=null && app.getDevice().getPower_start_time()!=null){
            String[] PowerStartTime = app.getDevice().getPower_start_time().split(":");
            String[] PowerEndTime = app.getDevice().getPower_end_time().split(":");
            tp_Start.setHour(Integer.parseInt(PowerStartTime[0]));//字符06转化为整型带入tp中会自动转化为6
            tp_Start.setMinute(Integer.parseInt(PowerStartTime[1]));
            tp_End.setHour(Integer.parseInt(PowerEndTime[0]));
            tp_End.setMinute(Integer.parseInt(PowerEndTime[1]));
            Log.d(TAG,"this is 设置了已定时的时间为tp的默认显示时间");
        }

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Toast.makeText(MainActivity.this,"this is 选中状态",Toast.LENGTH_SHORT).show();
                    //将数据保存至SharedPreferences:
                    SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("timeSwitchFlag", true);
                    editor.apply();
                }else{
                    Toast.makeText(MainActivity.this,"this is 非选中状态",Toast.LENGTH_SHORT).show();
                    //将数据保存至SharedPreferences:
                    SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("timeSwitchFlag", false);
                    editor.apply();

                    /*YangYuOrder order = new YangYuOrder();//按钮被关，代表关闭定时任务，需重新设置定时任务
                    order.startup_shutdow_off(MainActivity.this);*/
                }
            }
        });

        alertDialog.setView(timeView);
        alertDialog
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*时分时间转化为xx:xx格式*/
                String startHour = tp_Start.getHour() < 10 ? "0" + tp_Start.getHour() : "" + tp_Start.getHour();;
                String startMinute = tp_Start.getMinute() < 10 ? "0" + tp_Start.getMinute() : ""+ tp_Start.getMinute();;
                String endHour = tp_End.getHour() < 10 ? "0" + tp_End.getHour() : "" + tp_End.getHour();;
                String endMinute = tp_End.getMinute() < 10 ? "0" + tp_End.getMinute() : ""+ tp_End.getMinute();;
                String startTime = startHour + ":" + startMinute + ":" + "00";
                String endTime = endHour + ":" + endMinute + ":" + "00";

                //todo:获取单选框中的星期-->通过fastjson转为json存储在shareP中-->每次进来时恢复上次选中的状态；底逻辑：通过shareP的数据判断是否执行定时任务。
                Map<String,Boolean> weekMap = new HashMap<>();
                weekMap.put("MON",cb_monday.isChecked());
                weekMap.put("TUE",cb_tuesday.isChecked());
                weekMap.put("WED",cb_wednesday.isChecked());
                weekMap.put("THU",cb_thursday.isChecked());
                weekMap.put("FRI",cb_friday.isChecked());
                weekMap.put("SAT",cb_saturday.isChecked());
                weekMap.put("SUN",cb_sunday.isChecked());

                SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                String weekString = JSON.toJSONString(weekMap);//存储map信息
                editor.putString("weekString",weekString);
                editor.apply();

                /*二.条件判断中对map的判断方法*//*
                for(String key:WeekMap.keySet()){
                    switch (key){
                        case "Mon":
                            if(WeekMap.get(key)){
                                Log.d(TAG,"this is 执行操作mon");
                            }
                            break;
                        case "TUE":
                            if(WeekMap.get(key)){
                                Log.d(TAG,"this is 执行操作TUE");
                            }
                            break;
                        case "WED":
                            if(WeekMap.get(key)){
                                Log.d(TAG,"this is 执行操作WED");
                            }
                            break;
                        case "THU":
                            if(WeekMap.get(key)){
                                Log.d(TAG,"this is 执行操作THU");
                            }
                            break;
                        case "FRI":
                            if(WeekMap.get(key)){
                                Log.d(TAG,"this is 执行操作FRI");
                            }
                            break;
                        case "SAT":
                            if(WeekMap.get(key)){
                                Log.d(TAG,"this is 执行操作SAT");
                            }
                            break;
                        case "SUN":
                            if(WeekMap.get(key)){
                                Log.d(TAG,"this is 执行操作SUN");
                            }
                            break;
                    }
                }
                String weekString = JSON.toJSONString(WeekMap);//存数据库的字符串
                HashMap<String,Boolean> nameMap = JSON.parseObject(weekString,new TypeReference<HashMap<String,Boolean>>(){});//从数据库取出来进行转换为Map*/

                //todo：将获取的开始时间和结束时间，传给定时开关机方法去设定执行
                if (preferences != null) {
                    boolean powerFlag = preferences.getBoolean("timeSwitchFlag", timeSwitchFlag);
                    YangYuOrder order = new YangYuOrder();
                    if(powerFlag){
                        Log.d(TAG,"this is 设置定时开关机on");
                        order.startup_shutdow_off(MainActivity.this);
                        order.startup_shutdow_on(MainActivity.this,startTime,endTime);
                    }
                }
                dialog.cancel();
            }
        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog AlertDialogs = alertDialog.create();
        AlertDialogs.show();
    }

    public void singleSplitModeBtn(View view) {
        View singleView = null;
        singleView = this.getLayoutInflater().inflate(R.layout.activity_dialog,null);
        mSingleBtn = singleView.findViewById(R.id.btn_singleSplitMode);
        //设置是否单屏的状态
        LogUtils.e(app.isSingle_split_mode());
        if (app.isSingle_split_mode()) {
            mSingleSplitMode.setText("分屏模式：多屏模式");
            Toast.makeText(this,"多屏模式",Toast.LENGTH_SHORT).show();
            app.getDevice().setSingle_Split_Mode(false);
            app.setSingle_split_mode(false);
            stopBtn(view);
            if(app.getSon_source()!=null){
                playBtn(view);
            }
        } else {
            mSingleSplitMode.setText("分屏状态：单屏模式");
            Toast.makeText(this,"单屏模式",Toast.LENGTH_SHORT).show();
            app.getDevice().setSingle_Split_Mode(true);
            app.setSingle_split_mode(true);
            stopBtn(view);
            LogUtils.e(app);
            LogUtils.e(single);
            if(single.getSingle_Son_source()!=null){
                playBtn(view);
            }
        }
        daoManager.getSession().getDeviceDao().update(app.getDevice());
    }

    public void PowerOff(View view) {
        YangYuOrder order = new YangYuOrder();
        order.shutdown(MainActivity.this);

    }

    public void ReStart(View view) {
        YangYuOrder order = new YangYuOrder();
        order.reboot(MainActivity.this);
    }

    /**
     *以下为子方法
     */
    private void initImportSource(Source source) {
        util.infoLog(TAG,"从数据库中导入资源信息到全局变量的资源信息中",null);
        //app.setLicenceDir(source.getLicense_dir());//通用自动生成信息
        app.setCreate_time(source.getCreate_time());
        app.setStart_time(source.getStart_time());//U盘授权文件信息
        app.setEnd_time(source.getEnd_time());
        app.setFirst_time(source.getFirst_time());
        app.setTime_difference(source.getTime_difference());
        app.setProgram_id(source.getProgram_id());//U盘文件获取信息
        app.setSplit_view(source.getSplit_view());
        //app.setSingle_view(source.getSingle_view());
        //app.setSingle_son_source(source.getSingle_Son_source());
        app.setSplit_mode(source.getSplit_mode());
        app.setSon_source(source.getSon_source());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initImportDevice(Device device) {
        util.infoLog(TAG,"从数据库中导入设备信息到全局变量的设备信息中",null);
        app.setDevice_Name(device.getDevice_name());
        app.setDevice_Id(device.getDevice_id());
        LogUtils.e(app.isAuthority_state());
        app.setAuthority_state(device.getAuthority_state());
        LogUtils.e(app.isAuthority_state());
        app.setMachine_code(device.getMachine_code());
        app.setAuthority_time(device.getAuthority_time());
        app.setAuthority_expired(device.getAuthority_expired());
        app.setSingle_split_mode(device.getSingle_Split_Mode());

        //fixme：若使用者修改了系统时间超过授权到期时间，则从数据库取出的上次授权状态时不对的，需source表的信息进行判断，但device里无法判断；
        util.varyLog(TAG,app.getRelative_time(),"app.getRelative_time()");
        app.setCreateTime(System.currentTimeMillis());
        LogUtils.e(InfoUtils.dateString2Mills(app.getAuthority_expired()));
        if(app.getCreateTime() > InfoUtils.dateString2Mills(app.getAuthority_expired()) && app.getRelative_time() != 0){//非首次的简易判断
            app.setAuthorityName("授权过期");
        }
        if(app.getDevice().getSingle_Split_Mode()){
            mSingleSplitMode.setText("分屏模式：单屏模式" );
        }else{
            mSingleSplitMode.setText("分屏模式: 多屏模式");
        }
        mSingleSplitMode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);//调用重写的手势方法
                return true;
            }
        });

        mDeviceName.setText("设备名字:" + app.getDevice_Name());//从数据库中取已存的名字，而不是从方法中取；
        mDeviceName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        //ID明文;getMac需要三个权限，wifi，网络状态，还有INTERENT
//        sDeviceId = MacUtils.getMac(MainActivity.this) + System.currentTimeMillis();//ID明文，其中getMac码需要wifi，网络状态和INTERNET的权限
//        mDeviceId.setText("设备 ID：" + AuthorityUtils.digest(DeviceDao.Properties.Device_id.toString()));//ID密文
        mDeviceId.setText("设备 ID：" + AuthorityUtils.digest(app.getDevice_Id()));//ID密文
        mDeviceId.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        mAuthorityState.setText("授权状态：" + app.getAuthorityName());
        mAuthorityState.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        mAuthorityTime.setText("授权时间：" + app.getAuthority_time());
        mAuthorityTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        mAuthorityExpired.setText("授权到期：" + app.getAuthority_expired());
        mAuthorityExpired.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        mLocalTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);//设置好mGes后，此行为调用mGes的触屏事件
                return true;
            }
        });
        mLeftMargin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    private void requestWritePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public void playBtn(View view) {
        Log.d(TAG,"this is playBtn");
        Log.d(TAG,"this is spilt_view: " + app.getSplit_view());
        //Toast.makeText(this,"执行播放，加载读取",Toast.LENGTH_SHORT).show();
        if(app.isSingle_split_mode() && (single.getSingle_Son_source()!=null&&single.getSingle_view()!=null)){
            switch (single.getSingle_view()){
                case "0"://一分屏时，三种状态下触发对对应控件进行操作
                    if (app.getPlayFlag() == 0) {//播放状态:前缀状态播放为播放状态时，是重启功能，不需重置状态

                        //自定义广播对象，重写监听到后执行的程序，广播在任意的内容页上
                        app.setFinishState(true);
                        mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                        sendBroadcast(mIntent_FinishFlag);//发送广播
                        initDevice();
                        initSource();
                        setDialog();


                    }else if (app.getPlayFlag() == 1) {//暂停状态
                        Log.d(TAG,"this is app.getPlayFlag() == 1");
                        switch (app.getWidgetAttribute1()){
                            case 1:
                                app.setPlayFlag(0);
                                Log.d(TAG,"why is app.getDelayMillis()-app.getTimeDiff() :" + (app.getDelayMillis()-app.getTimeDiff())/1000);

                                Intent intent = new Intent();
                                intent.setAction(String.valueOf(app.getPlayFlag()));
                                sendBroadcast(intent);//发送广播
                                break;
                            case 2:
                                app.getVideoView_1().resume();
                                break;
                            default:
                                break;
                        }
                        app.setPlayFlag(0);
                    }else if (app.getPlayFlag() == 2) {//停止状态
                        app.setPlaySonImageFlag(true);

                        initSource();
                        setDialog();
                        app.setPlayFlag(0);
                    }
                    break;
                default:
                    break;
            }
        }else{
            if(app.getSon_source()!=null){
                switch (app.getSplit_view()){//文件数就是分屏数
                    case "1"://一分屏时，三种状态下触发对对应控件进行操作
                        if (app.getPlayFlag() == 0) {//播放状态:前缀状态播放为播放状态时，是重启功能，不需重置状态

                            //自定义广播对象，重写监听到后执行的程序，广播在任意的内容页上
                            app.setFinishState(true);
                            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                            sendBroadcast(mIntent_FinishFlag);//发送广播
                            initDevice();
                            initSource();
                            setDialog();
                        }else if (app.getPlayFlag() == 1) {//暂停状态
                            Log.d(TAG,"this is app.getPlayFlag() == 1");
                            switch (app.getWidgetAttribute1()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Log.d(TAG,"why is app.getDelayMillis()-app.getTimeDiff() :" + (app.getDelayMillis()-app.getTimeDiff())/1000);

                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_1().resume();
                                    break;
                                default:
                                    break;
                            }
                            app.setPlayFlag(0);
                        }else if (app.getPlayFlag() == 2) {//停止状态
                            app.setPlaySonImageFlag(true);

                            initSource();
                            setDialog();
                            app.setPlayFlag(0);
                        }
                        break;
                    case "2":
                        if (app.getPlayFlag() == 0) {//播放状态
                            app.setFinishState(true);
                            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                            sendBroadcast(mIntent_FinishFlag);//发送广播
                            initDevice();
                            initSource();

                            setDialog();
                        }else if (app.getPlayFlag() == 1) {//暂停状态
                            Log.d(TAG,"this is app.getPlayFlag() == 1");
                            switch (app.getWidgetAttribute1()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_1().resume();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute2()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_2().resume();
                                    break;
                                default:
                                    break;
                            }
                            app.setPlayFlag(0);
                        }else if (app.getPlayFlag() == 2) {//停止状态
                            app.setPlaySonImageFlag(true);

                            initSource();
                            setDialog();
                            app.setPlayFlag(0);
                        }
                        break;
                    case "3":
                        if (app.getPlayFlag() == 0) {//播放状态
                            app.setFinishState(true);
                            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                            sendBroadcast(mIntent_FinishFlag);//发送广播
                            initDevice();
                            initSource();

                            setDialog();
                        }else if (app.getPlayFlag() == 1) {//暂停状态
                            switch (app.getWidgetAttribute1()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_1().resume();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute2()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_2().resume();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute3()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_3().resume();
                                    break;
                                default:
                                    break;
                            }
                            app.setPlayFlag(0);
                        }else if (app.getPlayFlag() == 2) {//停止状态
                            app.setPlaySonImageFlag(true);

                            initSource();
                            setDialog();
                            app.setPlayFlag(0);
                        }
                        break;
                    case "4":
                        if (app.getPlayFlag() == 0) {//播放状态
                            app.setFinishState(true);
                            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                            sendBroadcast(mIntent_FinishFlag);//发送广播

                            initDevice();
                            initSource();
                            setDialog();

                        }else if (app.getPlayFlag() == 1) {//暂停状态
                            switch (app.getWidgetAttribute1()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_1().resume();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute2()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_2().resume();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute3()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_3().resume();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute4()){
                                case 1:
                                    app.setPlayFlag(0);
                                    Intent intent = new Intent();
                                    intent.setAction(String.valueOf(app.getPlayFlag()));
                                    sendBroadcast(intent);//发送广播
                                    break;
                                case 2:
                                    app.getVideoView_4().resume();
                                    break;
                                default:
                                    break;
                            }
                            app.setPlayFlag(0);
                        }else if (app.getPlayFlag() == 2) {//停止状态
                            app.setPlaySonImageFlag(true);

                            initSource();
                            setDialog();
                            app.setPlayFlag(0);
                        }
                        break;
                    default:
                        Toast.makeText(this,"不合规操作，请卸载再安装重新操作",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    public void suspendBtn(View view) {//在播放时才为有效按钮，其他都无效
        Log.d(TAG,"this is suspendBtn");
        Toast.makeText(this,"实现界面的控件暂停状态",Toast.LENGTH_SHORT).show();//获取当前文件夹的命名格式中的分屏字符串，以此获得对应的控件，控件又分图片和视频子控件；即先判分屏名，再判类型
        if(app.isSingle_split_mode() && (single.getSingle_Son_source()!=null&&single.getSingle_view()!=null)){
            switch (single.getSingle_view()){//单屏模式
                case "0":
                    if (app.getPlayFlag() == 0) {//播放状态，默认为0：U盘导入时，正常播放，即原状态为0；直接两控件设置暂停状态
                        switch (app.getWidgetAttribute1()){
                            case 1:
                                app.setEndTime(System.currentTimeMillis());
                                app.setTimeDiff((app.getEndTime()-app.getStartTime()));//获取已播放多少时间；
//                            app.getHandler().removeCallbacks(app.getRunnable1());//线程不会立即取消，而是执行完本次后才取消
                                break;
                            case 2:
                                app.getVideoView_1().pause();
                                break;
                        }
                        app.setPlayFlag(1);//实现图片暂停的地方在这儿
                    } else if (app.getPlayFlag() == 1) {//暂停状态
                        Log.d(TAG,"暂停按键为无效状态");
                        //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                    } else if (app.getPlayFlag() ==2) {//停止状态
                        Log.d(TAG,"暂停按键为无效状态");
                        //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }else{
            if(app.getSon_source()!=null){
                switch (app.getSplit_view()){//多屏模式
                    case "1"://一分屏
                        if (app.getPlayFlag() == 0) {//播放状态，默认为0：U盘导入时，正常播放，即原状态为0；直接两控件设置暂停状态
                            switch (app.getWidgetAttribute1()){
                                case 1:
                                    app.setEndTime(System.currentTimeMillis());
                                    app.setTimeDiff((app.getEndTime()-app.getStartTime()));//获取已播放多少时间；
//                            app.getHandler().removeCallbacks(app.getRunnable1());//线程不会立即取消，而是执行完本次后才取消
                                    break;
                                case 2:
                                    app.getVideoView_1().pause();
                                    break;
                            }
                            app.setPlayFlag(1);//实现图片暂停的地方在这儿
                        } else if (app.getPlayFlag() == 1) {//暂停状态
                            Log.d(TAG,"暂停按键为无效状态");
                            //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                        } else if (app.getPlayFlag() ==2) {//停止状态
                            Log.d(TAG,"暂停按键为无效状态");
                            //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "2":
                        if (app.getPlayFlag() == 0) {//播放状态，默认为0：U盘导入时，正常播放，即原状态为0；直接两控件设置暂停状态
                            switch (app.getWidgetAttribute1()){
                                case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable1());//线程不会立即取消，而是执行完本次后才取消
                                    break;
                                case 2:
                                    app.getVideoView_1().pause();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute2()){
                                case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable2());//线程不会立即取消，而是执行完本次后才取消
                                    break;
                                case 2:
                                    app.getVideoView_2().pause();
                                    break;
                                default:
                                    break;
                            }
                            app.setPlayFlag(1);
                        } else if (app.getPlayFlag() == 1) {//暂停状态
                            Log.d(TAG,"暂停按键为无效状态");
                            //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                        } else if (app.getPlayFlag() ==2) {//停止状态
                            Log.d(TAG,"暂停按键为无效状态");
                            //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "3":
                        if (app.getPlayFlag() == 0){
                            switch (app.getWidgetAttribute1()){
                                case 1:
                                    break;
                                case 2:
                                    app.getVideoView_1().pause();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute2()) {
                                case 1:
                                    break;
                                case 2:
                                    app.getVideoView_2().pause();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute3()) {
                                case 1:
                                    break;
                                case 2:
                                    app.getVideoView_3().pause();
                                    break;
                                default:
                                    break;
                            }
                            app.setPlayFlag(1);
                        } else if (app.getPlayFlag() == 1) {//暂停状态
                            Log.d(TAG,"暂停按键为无效状态");
                            //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                        } else if (app.getPlayFlag() ==2) {//停止状态
                            Log.d(TAG,"暂停按键为无效状态");
                            //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "4":
                        if (app.getPlayFlag() == 0){
                            switch (app.getWidgetAttribute1()){
                                case 1:
                                    break;
                                case 2:
                                    app.getVideoView_1().pause();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute2()) {
                                case 1:
                                    break;
                                case 2:
                                    app.getVideoView_2().pause();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute3()) {
                                case 1:
                                    break;
                                case 2:
                                    app.getVideoView_3().pause();
                                    break;
                                default:
                                    break;
                            }
                            switch (app.getWidgetAttribute4()) {
                                case 1:
                                    break;
                                case 2:
                                    app.getVideoView_4().pause();
                                    break;
                                default:
                                    break;
                            }
                            app.setPlayFlag(1);
                        } else if (app.getPlayFlag() == 1) {//暂停状态
                            Log.d(TAG,"暂停按键为无效状态");
                            //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                        } else if (app.getPlayFlag() == 2) {//停止状态
                            Log.d(TAG,"暂停按键为无效状态");
                            //Toast.makeText(this,"暂停按键为无效状态",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        Toast.makeText(this,"不合规操作，请卸载再安装重新操作",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    public void stopBtn(View view) {
        if(app.getPlayFlag() != 2){
            app.setFinishState(true);
            app.setPlaySonImageFlag(false);
            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
            sendBroadcast(mIntent_FinishFlag);//发送广播

            setContentView(R.layout.activity_main);
            initView();//找到layout控件
            initDevice();
            setDialog();
            app.setPlayFlag(2);
        }else{
            util.infoLog(TAG,"停止按键为无效状态",null);
        }

        /*try {
            if(app.isSingle_split_mode()){
                switch (app.getSingle_view()){
                    case "0":
                        if (app.getPlayFlag() == 0) {
                            app.setFinishState(true);
                            app.setPlaySonImageFlag(false);
                            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                            sendBroadcast(mIntent_FinishFlag);//发送广播

                            setContentView(R.layout.activity_main);
                            initView();//找到layout控件
                            initDevice();
                            setDialog();

                            app.setPlayFlag(2);
                        } else if (app.getPlayFlag() == 1) {
                            app.setFinishState(true);
                            app.setPlaySonImageFlag(false);//禁止自动监听U盘挂载状态
                            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                            sendBroadcast(mIntent_FinishFlag);//发送广播，关闭当前广播

                            setContentView(R.layout.activity_main);
                            initView();//找到layout控件
                            initDevice();
                            setDialog();

                            app.setPlayFlag(2);
                        } else if (app.getPlayFlag() ==2) {
                            Log.d(TAG,"停止按键为无效状态");
                            //Toast.makeText(this,"停止按键为无效状态",Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }else{
                switch (app.getSplit_view()){
                    case "1"://一分屏
                    case "2":
                    case "3":
                    case "4":
                        if (app.getPlayFlag() == 0) {
                            app.setFinishState(true);
                            app.setPlaySonImageFlag(false);
                            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                            sendBroadcast(mIntent_FinishFlag);//发送广播

                            setContentView(R.layout.activity_main);
                            initView();//找到layout控件
                            initDevice();
                            setDialog();

                            app.setPlayFlag(2);
                        } else if (app.getPlayFlag() == 1) {
                            app.setFinishState(true);
                            mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                            sendBroadcast(mIntent_FinishFlag);//发送广播，关闭当前广播
                            app.setPlaySonImageFlag(false);//禁止自动监听U盘挂载状态

                            setContentView(R.layout.activity_main);
                            initView();//找到layout控件
                            initDevice();
                            setDialog();

                            app.setPlayFlag(2);
                        } else if (app.getPlayFlag() ==2) {
                            Log.d(TAG,"停止按键为无效状态");
                            //Toast.makeText(this,"停止按键为无效状态",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        app.setFinishState(true);
                        app.setPlaySonImageFlag(false);
                        mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                        sendBroadcast(mIntent_FinishFlag);//发送广播
                        setContentView(R.layout.activity_main);
                        initView();//找到layout控件
                        initDevice();
                        setDialog();
                        app.setPlayFlag(2);
                        Toast.makeText(this,"不合规操作，请卸载再安装重新操作",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        } catch (Exception e) {
            util.varyLog(TAG,e,"Exception e");

        }*/
    }

    public void exitBtn(View view) {
        Log.d(TAG,"this is exitBtn");
        LogUtils.e(app.getCurrentActivity());
        app.setSetSourcePlayer(false);
        if(app.getCurrentActivity() != null){
            app.getCurrentActivity().finish();//先finish掉(可采用循环finish，但activity少没必要)，然后再退出，不然退不出
        }
        if(usbHelper != null){
            usbHelper.finishUsbHelper();
        }
        finish();
        System.exit(0);
    }

    /**
     * 生命周期
     */
    @Override
    protected void onResume() {
        super.onResume();
        app.setPlayFlag(0);
    }

    @Override
    protected void onDestroy() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        if(mRenovateBroadcastReceiver != null){
            unregisterReceiver(mRenovateBroadcastReceiver);
        }
        if(AlertDialogs != null){
            AlertDialogs.dismiss();
        }
        super.onDestroy();
        //usbHelper.finishUsbHelper();
    }
}