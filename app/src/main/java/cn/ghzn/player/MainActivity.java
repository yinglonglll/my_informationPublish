package cn.ghzn.player;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.icu.util.LocaleData;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.UsbFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cn.ghzn.player.receiver.USBBroadCastReceiver;
import cn.ghzn.player.receiver.VarReceiver;
import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;
import cn.ghzn.player.util.AuthorityUtils;
import cn.ghzn.player.util.FileUtils;
import cn.ghzn.player.util.InfoUtils;
import cn.ghzn.player.util.UsbUtils;
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.Constants.LICENCE_NAME;
import static cn.ghzn.player.Constants.MACHINE_CODE_NAME;
import static cn.ghzn.player.util.FileUtils.getFilePath;
import static cn.ghzn.player.util.InfoUtils.getRandomString;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    public static MyApplication app;
    public final static DaoManager daoManager = DaoManager.getInstance();//找到单例(唯一数据库对象，只管取来用)
    private static final String TAG = "MainActivity";
    private static boolean isSave;
    private View viewLp;

    GestureDetector mGestureDetector;
    private TextView mDeviceName;
    private TextView mDeviceId;
    private TextView mConnectionState;
    private TextView mAuthorityState;
    private TextClock mLocalTime;
    private BroadcastReceiver mBroadcastReceiver;
    private BroadcastReceiver mRenovateBroadcastReceiver;
    private OneSplitViewActivity mOneSplitViewActivity;
    private Intent mIntent_FinishFlag = new Intent();
    private TextView mAuthorityTime;
    private TextView mAuthorityExpired;
    private File mLicenceSaveFile;
    private File mMachineCodeSaveFile;
    private TextView mLeftMargin;
    private UsbHelper usbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWritePermission();//权限：动态获取写入权限，如果静态获取失败的话
        app = (MyApplication)getApplication();//全局变量：
        UsbUtils.checkUsb(this);//通过USB协议检查出USB是什么类型设备
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//取消导航栏
        setContentView(R.layout.activity_main);

        AutoOutPutMachineCode();//取消自动导出机器码，仅保留动态获取usbHelper的权限

        if (app.getCurrentActivity() != null) {
            LogUtils.e(app.getCurrentActivity());
            app.getCurrentActivity().finish();//关闭正在播放的资源，准备播放即将导入的资源
            Log.d(TAG,"this is 关闭了正在播放的分屏资源");
        }
        app.setSource(DaoManager.getInstance().getSession().getSourceDao().queryBuilder().unique());
        if (app.getSource() != null) {
            app.setRelative_time(app.getSource().getRelative_time());//initDevice()中需要用到此数据，故先提前初始化；main代码若优化应先初始化，展示main界面，再跳转。
        }
        initView();//找到layout控件，初始化主界面的信息
        initBroadReceiver();//广播监听：保证资源播放activity被finish掉
        initDevice();
        LogUtils.e(app.getSplit_view());
        initSource();//资源初始化放在如上
        Log.d(TAG,"this is app.getLicenceDir()>>>>1" + app.getLicenceDir());
        setDialog();
        Log.d(TAG,"this is app.getLicenceDir()>>>>2" + app.getLicenceDir());



        //LogUtils.e(app.getCurrentActivity());
        if (app.isImportState() && app.isSetSourcePlayer()) {//仅允许在初次资源导入时，U盘插入顺序与软件打开顺序无关；在已导入资源的情况下，必须在播放的情况下，再插入U盘进行资源变更。
            app.setStrings(UsbUtils.getVolumePaths(this));//通过获取U盘挂载状态检查所有存储的绝对地址，
            for(String str : app.getStrings()){
                if (!str.equals("/storage/emulated/0")) {
                    //app.setExtraPath(str + "/Android/data/cn.ghzn.player/files/");//设置U盘的路径
                    app.setExtraPath(str + "/");//设置U盘的路径,以检查授权文件和资源文件
                    UsbUtils.checkUsbFileForm(this,str);
                }
            }
        }
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
            writer.write(app.getAuthorization());//机器码导出
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

    private void initSource() {

        app.setLicenceDir(getFilePath(MainActivity.this, Constants.STOREPATH) + "/");//获取生成授权文件的文件夹地址
        app.setCreateTime(System.currentTimeMillis());

        File file = new File(app.getLicenceDir());
        if (!file.exists()) {
            file.mkdirs();
        }
        Log.d(TAG,"this is app.getLicenceDir()>>>>" + app.getLicenceDir());
//        app.getSource().setLicense_dir(app.getLicenceDir());//获得source表的setLicense_dir方法，把导出license文件时的地址存储在数据库对应数据中
//        LogUtils.e(app.getSource());//未U盘导入资源时，表为空，不可调用赋值。

        //todo：授权期内过期(通过时间比较)，禁止资源初始化和跳转并提醒
        Log.d(TAG,"this is  app.setSource");
        LogUtils.e(app.getSplit_view());
        if (app.getSource() != null &&  app.getSource().getSplit_view() != null) {//1.判断授权文件；2.判断资源文件
            initImportSource(app.getSource());//初始化数据库数据到全局变量池--含device与source表
            Log.d(TAG,"--------资源信息---------");
            LogUtils.e(app.getSource());
            //正常情况下，本次导入的节目时间一定比上一次时间大；授权时间一定比当前时间大；//这里为了保证有效期过期时，不能播放

            LogUtils.e(app.getCreateTime() > app.getSource().getCreate_time());
            LogUtils.e((app.getCreateTime()-app.getFirst_time()) < app.getTime_difference());
            LogUtils.e(app.getRelative_time() > app.getCreateTime());//1.防止本地或服务器时间大于授权到期相对时间；
            Log.d(TAG,"app.getRelative_time() :" + app.getRelative_time());
            Log.d(TAG,"app.getCreateTime()>>> :" + app.getCreateTime());

            if (app.getCreateTime() > app.getSource().getCreate_time()
                    && (app.getCreateTime()-app.getFirst_time()) < app.getTime_difference()
                    && app.getRelative_time() > app.getCreateTime()) {
                app.getDevice().setAuthority_state(true);
                app.setSetSourcePlayer(false);//此处若进来则进行播放，此时false，避免搜索挂载U盘的目录来重复播放
                turnActivity(app.getSplit_view());//1.保证导入时间只能向前；2.保证正常授权的时间段内(指定时间范围长度)；3.强制授权期内过期
            } else {
                Log.d(TAG,"this is 授权过期，进入无授权状态《《《《《《《《《《《《《《《《《《《");
                app.getDevice().setAuthority_state(false);
            }
            daoManager.getSession().getDeviceDao().update(app.getDevice());
//            daoManager.getSession().getSourceDao().update(app.getSource());//正常情况下，自动播放资源为正确，但非正常操作会导致异常，使得存储错误信息或修正后信息无法存储
        }
    }

    public void initDevice() {
        app.setDevice(DaoManager.getInstance().getSession().getDeviceDao().queryBuilder().unique());
        if(app.getDevice() == null){
            app.setDevice(new Device());//表不存在则新建赋值
            daoManager.getSession().getDeviceDao().insert(getDevice(app.getDevice()));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
        }else{//存在则直接修改
            LogUtils.e(app.getDevice().getAuthority_state());
            daoManager.getSession().getDeviceDao().update(getDevice(app.getDevice()));
            LogUtils.e(app.getDevice().getAuthority_state());
        }
        initImportDevice(app.getDevice());//初始化数据且设置layout控件；从上述数据库中取信息出来显示
        Log.d(TAG,"--------设备信息---------");
        LogUtils.e(app.getDevice());//利用第三方插件打印出对象的属性和方法值；
    }

    private void initBroadReceiver() {
        mBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"this is varReceiver_finish掉当前的Activity的广播");
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
                Log.d(TAG,"this is varReceiver_刷新主界面授权信息");
                mAuthorityState.setText("授权状态：" + app.getAuthorityName());
                mAuthorityTime.setText("授权时间：" + app.getAuthority_time());
                mAuthorityExpired.setText("授权到期：" + app.getAuthority_expired());
                if (mRenovateBroadcastReceiver != null) {//执行一次
                    unregisterReceiver(mRenovateBroadcastReceiver);
                }
            }
        });
        IntentFilter RenovateFilter = new IntentFilter("cn.ghzn.player.broadcast.RENOVATE_MAIN");
        registerReceiver(mRenovateBroadcastReceiver,RenovateFilter);//注册广播
    }

    private void initImportSource(Source source) {
        //将读取的数据赋值给全局变量
        //app.setLicenceDir(source.getLicense_dir());//通用自动生成信息
        app.setCreate_time(source.getCreate_time());

        app.setStart_time(source.getStart_time());//U盘授权文件信息
        app.setEnd_time(source.getEnd_time());
        app.setFirst_time(source.getFirst_time());
        app.setTime_difference(source.getTime_difference());

        app.setProgram_id(source.getProgram_id());//U盘文件获取信息
        app.setSplit_view(source.getSplit_view());
        app.setSplit_mode(source.getSplit_mode());
        app.setSon_source(source.getSon_source());

        //app.setLicenceDir(source.getLicense_dir());//程序执行时，U盘未插入，此时内容为空;将txt文本的绝对地址从数据库中取出再赋值给全局变量
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initImportDevice(Device device) {//不要忘记穿参数近来，自己忘记传参折腾很久，没传参时，非全局不可调用；

        app.setDevice_Name(device.getDevice_name());
        app.setDevice_Id(device.getDevice_id());
        LogUtils.e(app.isAuthority_state());
        app.setAuthority_state(device.getAuthority_state());
        LogUtils.e(app.isAuthority_state());
        Log.d(TAG,"this is initImportDeviced的getDevice().getAuthority_state()" + app.getDevice().getAuthority_state());
        Log.d(TAG,"this is initImportDeviced的app.isAuthority_state()" + app.isAuthority_state());
        app.setAuthorization(device.getAuthorization());
        app.setAuthority_time(device.getAuthority_time());
        app.setAuthority_expired(device.getAuthority_expired());

        Log.d(TAG,"this is app.getRelative_time()" + app.getRelative_time());

        if (app.getRelative_time() == 0) {//授权与未授权区分之一的方法在于 授权到期时间 有无；
            app.setAuthorityName("未授权");
        } else {
            Log.d(TAG,"this is enter else");
            if (app.isAuthority_state()) {//授权状态为真，则显示已授权，否则则授权过期。
                app.setAuthorityName("已授权");
            } else {
                app.setAuthorityName("授权过期");
                app.setCreate_time(0);//授权为假时，为授权过期，则设置上一次的成功导入资源时间为0，模拟初始状态；比喻为只能向前的点的位置重置为初试状态的位置再重新向前。
            }
        }

        LogUtils.e(mDeviceName);
        Log.d(TAG,"device.getDevice_name()" + device.getDevice_name());
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

    public void setDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        app.setView(this.getLayoutInflater().inflate(R.layout.activity_dialog, null));
        alertDialog.setView(app.getView());
        final AlertDialog AlertDialogs = alertDialog.create();//如上是我自己找到新建的弹窗，下面是把新建的弹窗赋给新建的手势命令中的长按。
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
                Log.d(TAG,"OnLongPressTap");
                AlertDialogs.show();

                //解决右键退出AlertDialogs的bug：The specified child already has a parent. You must call removeView() on the child's parent first. The specified child already has a parent. You must call removeView() on the child's parent first.
                AlertDialogs.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Log.d(TAG,"this is to do on Dismiss()");
                        if (viewLp != null) {
                            ViewGroup parentView = (ViewGroup) viewLp.getParent();
                            if (parentView != null) {
                                parentView.removeView(viewLp);
                                Log.d(TAG,"this is to doing ：parentView.removeView(view)");
                            }
                        }
                        Log.d(TAG,"this is to done Dismiss()");
                    }
                });


            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
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
            default:
                Toast.makeText(this, "请勿放入过多文件，请按照教程方法的格式放入对应的文件", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void requestWritePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private Device getDevice(Device device){
        if(device.getDevice_name()==null)device.setDevice_name(InfoUtils.getDeviceName());
        if(device.getDevice_id()==null)device.setDevice_id(InfoUtils.getDeviceId());
        if(device.getAuthority_time()==null)device.setAuthority_time(InfoUtils.getAuthorityTime());
        device.setAuthorization(InfoUtils.getAuthorization());//此处Authorization实际存储的是机器码(mac值的md5加密值)，命名错误
//        if(device.getAuthority_expried().toString()==null)device.setAuthority_expried(InfoUtils.getAuthorityExpried());//data类数据，不知这样操作是否对
        device.setSoftware_version(InfoUtils.getSoftware_version());
        device.setFirmware_version(InfoUtils.FirmwareVersion());
        device.setWidth(InfoUtils.getWidth());//默认赋值为0
        device.setHeight(InfoUtils.getHeight());
        return device;
    }

    public static String getDeviceName(){
        return Constants.DEVICE_PREFIX + getRandomString(10);
    }

    public void initView() {
        mDeviceName = (TextView) this.findViewById(R.id.DeviceName);
        mDeviceId = (TextView) this.findViewById(R.id.DeviceID);
        mAuthorityState = (TextView) this.findViewById(R.id.AuthorityState);
        mAuthorityTime = (TextView) this.findViewById(R.id.AuthorityTime);
        mAuthorityExpired = (TextView) this.findViewById(R.id.AuthorityExpired);
        mLocalTime = (TextClock) this.findViewById(R.id.localTime);
        mLeftMargin = (TextView) this.findViewById(R.id.leftMargin);
    }

    public void playBtn(View view) {
        //重新读取分屏模式文件的信息，加载读取
        Log.d(TAG,"this is playBtn");
        Log.d(TAG,"this is spilt_view: " + app.getSplit_view());
        Toast.makeText(this,"执行播放，加载读取",Toast.LENGTH_SHORT).show();
        switch (app.getSplit_view()){//文件数就是分屏数
            case "1"://一分屏时，三种状态下触发对对应控件进行操作
                if (app.getPlayFlag() == 0) {//播放状态:前缀状态播放为播放状态时，是重启功能，不需重置状态

                    //自定义广播对象，重写监听到后执行的程序
                    app.setFinishState(true);
                    mIntent_FinishFlag.setAction(String.valueOf(app.isFinishState()));
                    sendBroadcast(mIntent_FinishFlag);//发送广播
                    initDevice();
                    initSource();

                    setDialog();
                }else if (app.getPlayFlag() == 1) {//暂停状态
                    Log.d(TAG,"this is app.getPlayFlag() == 1");
                    switch (app.getForMat1()){
                        case 1:
                            app.setPlayFlag(0);
                            Log.d(TAG,"why is app.getDelayMillis()-app.getTimeDiff() :" + (app.getDelayMillis()-app.getTimeDiff())/1000);

//                            long newDelayMillis = (app.getDelayMillis()-app.getTimeDiff());
//                            Log.d(TAG,"why is app.getDelayMillis()-app.getTimeDiff() :" + app.getDelayMillis()/1000 + "-" + app.getTimeDiff()/1000 + "=" + (app.getDelayMillis()-app.getTimeDiff())/1000);
//                            Runnable runnable;
//                            app.getHandler().postDelayed(runnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (app.getPlayFlag() == 0) {//只有在播放状态下才可以进行执行递归，也就是按下暂停或停止时，不会执行递归
////                                        mOneSplitViewActivity.playSonImage();
//                                        Log.d(TAG,"why is 进入了意图传递");
//                                        Intent intent = new Intent();
//                                        intent.setAction(String.valueOf(app.getPlayFlag()));
//                                        sendBroadcast(intent);//发送广播
//                                    }
//                                }
//                            },newDelayMillis);
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
                    switch (app.getForMat1()){
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
                    switch (app.getForMat2()){
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
                    switch (app.getForMat1()){
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
                    switch (app.getForMat2()){
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
                    switch (app.getForMat3()){
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
                    switch (app.getForMat1()){
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
                    switch (app.getForMat2()){
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
                    switch (app.getForMat3()){
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
                    switch (app.getForMat4()){
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

    public void suspendBtn(View view) {//在播放时才为有效按钮，其他都无效
        Log.d(TAG,"this is suspendBtn");
        Toast.makeText(this,"实现界面的控件暂停状态",Toast.LENGTH_SHORT).show();//获取当前文件夹的命名格式中的分屏字符串，以此获得对应的控件，控件又分图片和视频子控件；即先判分屏名，再判类型

        switch (app.getSplit_view()){
            case "1"://一分屏
                if (app.getPlayFlag() == 0) {//播放状态，默认为0：U盘导入时，正常播放，即原状态为0；直接两控件设置暂停状态
                    switch (app.getForMat1()){
                        case 1:
                            app.setEndTime(System.currentTimeMillis());
                            app.setTimeDiff((app.getEndTime()-app.getStartTime()));//获取已播放多少时间；
//                            app.getHandler().removeCallbacks(app.getRunnable1());//线程不会立即取消，而是执行完本次后才取消
                            break;
                        case 2:
                            app.getVideoView_1().pause();
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
            case "2":
                if (app.getPlayFlag() == 0) {//播放状态，默认为0：U盘导入时，正常播放，即原状态为0；直接两控件设置暂停状态
                    switch (app.getForMat1()){
                        case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable1());//线程不会立即取消，而是执行完本次后才取消
                            break;
                        case 2:
                            app.getVideoView_1().pause();
                            break;
                        default:
                            break;
                    }
                    switch (app.getForMat2()){
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
                    switch (app.getForMat1()){
                        case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable1());//线程不会立即取消，而是执行完本次后才取消
                            break;
                        case 2:
                            app.getVideoView_1().pause();
                            break;
                        default:
                            break;
                    }
                    switch (app.getForMat2()) {
                        case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable2());//线程不会立即取消，而是执行完本次后才取消
                            break;
                        case 2:
                            app.getVideoView_2().pause();
                            break;
                        default:
                            break;
                    }
                    switch (app.getForMat3()) {
                        case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable3());//线程不会立即取消，而是执行完本次后才取消
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
                    switch (app.getForMat1()){
                        case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable1());//只有在暂停的时候才可以被取消，配合delay
                            break;
                        case 2:
                            app.getVideoView_1().pause();
                            break;
                        default:
                            break;
                    }
                    switch (app.getForMat2()) {
                        case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable2());//线程不会立即取消，而是执行完本次后才取消
                            break;
                        case 2:
                            app.getVideoView_2().pause();
                            break;
                        default:
                            break;
                    }
                    switch (app.getForMat3()) {
                        case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable3());//线程不会立即取消，而是执行完本次后才取消
                            break;
                        case 2:
                            app.getVideoView_3().pause();
                            break;
                        default:
                            break;
                    }
                    switch (app.getForMat4()) {
                        case 1:
//                            app.getHandler().removeCallbacks(app.getRunnable4());//线程不会立即取消，而是执行完本次后才取消
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

    public void stopBtn(View view) {
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
                    sendBroadcast(mIntent_FinishFlag);//发送广播
                    app.setPlaySonImageFlag(false);

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
                Toast.makeText(this,"不合规操作，请卸载再安装重新操作",Toast.LENGTH_SHORT).show();
                break;
        }
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

    public void machineIdOutBtn(final View view) {
        Log.d(TAG,"this is MachineIdOutBtn");

        if (app.isImportState()) {
            //todo:实现将机器码文件生成到U盘根目录下
            Log.d(TAG,"this is 将机器码导出到U盘中");
            mLicenceSaveFile = new File(app.getExtraPath(),LICENCE_NAME);
            mMachineCodeSaveFile = new File(app.getExtraPath(),MACHINE_CODE_NAME);

            UsbMassStorageDevice[] devices = usbHelper.getDeviceList();
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
                if(result){
                    Toast.makeText(this,"导出机器码成功",Toast.LENGTH_SHORT).show();
                }
                device.close();
            }

            /*if (mMachineCodeSaveFile.exists()) {
                ViewImportUtils.deleteFile(mMachineCodeSaveFile);
                machineIdOut(mMachineCodeSaveFile);
                Toast.makeText(this,"导出机器码成功",Toast.LENGTH_SHORT).show();
            } else {
                machineIdOut(mMachineCodeSaveFile);//U盘无机器码则导出机器码
                Toast.makeText(this,"导出机器码成功",Toast.LENGTH_SHORT).show();
            }*/
        } else {
            Log.d(TAG,"this is 将机器码导出到本地中");
            //mLicenceSaveFile = new File(app.getLicenceDir(),LICENCE_NAME);//手机内授权文件绝对地址的对象
            mMachineCodeSaveFile = new File(app.getLicenceDir(),MACHINE_CODE_NAME);

            /*if (mLicenceSaveFile.exists()) {//为了手动删除过期的授权文件而重新导出机器码文件重新授权
                //弹窗提示是否删除本地已存在的licence文件，删除后自动导出。
                new AlertDialog.Builder(this)
                        .setTitle("提醒")
                        .setMessage("本地目录中已存在授权码文件Licence.txt，请问是否删除授权文件？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ViewImportUtils.deleteFile(mLicenceSaveFile);
                                machineIdOut(mMachineCodeSaveFile);
                                Log.d(TAG, "this is 本地目录中已存在授权码文件Licence.txt，进行删除授权文件并重新导出机器码MachineCode.txt文件");
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create().show();
            } else {*/
                ViewImportUtils.deleteFile(mMachineCodeSaveFile);
                machineIdOut(mMachineCodeSaveFile);
                Log.d(TAG, "this is 已重新生成机器码文件到本地目录上");
                Toast.makeText(this,"未插入U盘，无法导出机器码到U盘上",Toast.LENGTH_LONG).show();
            //}
        }
    }

    private void machineIdOut(File mSaveFile) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(mSaveFile);
            outStream.write(app.getAuthorization().getBytes("gbk"));//UFT-8在android不能用，只能用gbk!!!不设置的话可能会变成乱码！！！
            outStream.close();
            outStream.flush();
            isSave = true;
            Log.d(TAG, "this is 文件已经保存啦！赶快去查看吧!");
            Toast.makeText(this, "导出机器码成功", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.setPlayFlag(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        //usbHelper.finishUsbHelper();
    }
}