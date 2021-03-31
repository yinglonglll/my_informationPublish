package cn.ghzn.player;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.apkfuns.logutils.LogUtils;

import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.util.AuthorityUtils;
import cn.ghzn.player.util.InfoUtils;
import cn.ghzn.player.util.ViewImportUtils;

import static cn.ghzn.player.util.InfoUtils.getRandomString;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    GestureDetector mGestureDetector;
    private TextView mDeviceName;
    private TextView mDeviceId;
    private TextView mConnectionState;
    private TextView mAuthorityState;

    private DaoManager daoManager = DaoManager.getInstance();//找到单例(唯一数据库对象)

    private MyApplication application;
//    private TextView mInstallTime;
//    private TextView mProbationTime;
    private boolean ConnectionState = false;

    private boolean AuthorityState = false;
    private TextClock mLocalTime;
    private AlertDialog mAlertDialogs;
    private View mView;
    private int playFlag = 0;//播放：playFlag = 0；暂停：playFlag = 1；停止：playFlag = 2；

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWritePermission();//动态获取写入权限，如果静态获取失败的话
        setContentView(R.layout.activity_main);

//        Log.d(TAG,"this is test");
//
//        final CustomVideoView customVideoView = this.findViewById(R.id.videoView_one1_1);
//        customVideoView.setVideoURI(Uri.parse("android.resource://cn.ghzn.player/" + R.raw.test));
//
//        customVideoView.setVisibility(View.VISIBLE);
//        customVideoView.start();
//        customVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
//                Log.i("tag","视频播放失败");
//                return false;
//            }
//        });
//        customVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Log.i("tag","可以播放了");
//                customVideoView.start();
//            }
//        });

        daoManager.getSession().getDeviceDao();
        //开机检查数据库中设备信息:数据库有则取出设置为当前值
        Device device = DaoManager.getInstance().getSession().getDeviceDao().queryBuilder().unique();
        if(device==null){
            device = new Device();
            daoManager.getSession().getDeviceDao().insert(getDevice(device));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
        }else{
            daoManager.getSession().getDeviceDao().update(getDevice(device));
        }
        Log.d(TAG,"--------设备信息---------");
        LogUtils.e(device);//利用第三方插件打印出对象的属性和方法值；

        initView();//找到layout控件
        initWidget(device);//设置layout控件；从上述数据库中取信息出来显示


        setDialog();


    }
    public void setDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        View View = this.getLayoutInflater().inflate(R.layout.activity_dialog, null);
        alertDialog.setView(View);
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
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }



    @SuppressLint("ClickableViewAccessibility")
    private void initWidget(Device device) {//不要忘记穿参数近来，自己忘记传参折腾很久，没传参时，非全局不可调用；

        mDeviceName.setText("设备名字:" + device.getDevice_name());//从数据库中取已存的名字，而不是从方法中取；
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
        mDeviceId.setText("设备 ID：" + AuthorityUtils.digest(device.getDevice_id()));//ID密文
        mDeviceId.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        mConnectionState.setText("连接状态：" + ConnectionState);
        mConnectionState.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        mAuthorityState.setText("授权状态：" + AuthorityState);
        mAuthorityState.setOnTouchListener(new View.OnTouchListener() {
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


    }
    private void requestWritePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
    private Device getDevice(Device device){
        if(device.getDevice_name()==null)device.setDevice_name(InfoUtils.getDeviceName());
        if(device.getDevice_id()==null)device.setDevice_id(InfoUtils.getDeviceId());
        if(device.getAuthority_state()==true)device.setAuthority_state(InfoUtils.AuthorityState());//默认为false，数据库获取为true才是已授权
        if(device.getAuthority_time()==null)device.setAuthority_time(InfoUtils.getAuthorityTime());
        if(device.getAuthorization()==null)device.setAuthorization(InfoUtils.getAuthorization());
//        if(device.getAuthority_expried().toString()==null)device.setAuthority_expried(InfoUtils.getAuthorityExpried());//data类数据，不知这样操作是否对
        device.setSoftware_version(InfoUtils.getSoftware_version());
        device.setFirmware_version(InfoUtils.FirmwareVersion());
        device.setWidth(InfoUtils.getDWidth());//默认赋值为0
        device.setHeight(InfoUtils.getHeight());

        return device;
    }

    public static String getDeviceName(){
        return Constants.DEVICE_PREFIX + getRandomString(10);
    }

    private void initView() {
        mDeviceName = (TextView) this.findViewById(R.id.DeviceName);
        mDeviceId = (TextView) this.findViewById(R.id.DeviceID);
        mConnectionState = (TextView) this.findViewById(R.id.ConnectionState);
        mAuthorityState = (TextView) this.findViewById(R.id.AuthorityState);
        mLocalTime = (TextClock) this.findViewById(R.id.localTime);


//        mInstallTime = (TextView) this.findViewById(R.id.InstallTime);
//        mProbationTime = (TextView) this.findViewById(R.id.ProbationTime);
    }

    public void playBtn(View view) {
        //重新读取分屏模式文件的信息，加载读取
        Log.d(TAG,"this is playBtn");
        Toast.makeText(this,"重新读取分屏模式文件的信息，加载读取",Toast.LENGTH_SHORT).show();

        switch (ImportActivity.getFilesCount()){//可以从数据库中的mtarget绝对地址中算出来
            case 1://一分屏时，三种状态下触发对对应控件进行操作
                if (playFlag == 0) {//播放状态
                    Log.d(TAG,"播放按键为无效状态");
                }
                if (playFlag == 1) {//暂停状态
                    OneSplitViewActivity.getVideoView_1().resume();
                    OneSplitViewActivity.getImageView_1().getHandler().notify();
                }
                if (playFlag == 2) {//停止状态
                    //todo:实现重新运行程序，即重新执行turnActivity();
                }
            case 2:
                if (playFlag == 0) {//播放状态
                    Log.d(TAG,"播放按键为无效状态");
                }
                if (playFlag == 1) {//暂停状态
                    TwoSplitViewActivity.getVideoView_1().resume();
                    TwoSplitViewActivity.getVideoView_2().resume();
                    TwoSplitViewActivity.getImageView_1().getHandler().notify();
                    TwoSplitViewActivity.getImageView_2().getHandler().notify();
                }
                if (playFlag == 2) {//停止状态
                    //todo:实现重新运行程序，即重新执行turnActivity();
                }
            case 3:
                if (playFlag == 0) {//播放状态
                    Log.d(TAG,"播放按键为无效状态");
                }
                if (playFlag == 1) {//暂停状态
                    ThreeSplitViewActivity.getVideoView_1().resume();
                    ThreeSplitViewActivity.getVideoView_2().resume();
                    ThreeSplitViewActivity.getVideoView_3().resume();
                    ThreeSplitViewActivity.getImageView_1().getHandler().notify();
                    ThreeSplitViewActivity.getImageView_2().getHandler().notify();
                    ThreeSplitViewActivity.getImageView_3().getHandler().notify();
                }
                if (playFlag == 2) {//停止状态
                    //todo:实现重新运行程序，即重新执行turnActivity();
                }
            case 4:
                if (playFlag == 0) {//播放状态
                    Log.d(TAG,"播放按键为无效状态");
                }
                if (playFlag == 1) {//暂停状态
                    FourSplitViewActivity.getVideoView_1().resume();
                    FourSplitViewActivity.getVideoView_2().resume();
                    FourSplitViewActivity.getVideoView_3().resume();
                    FourSplitViewActivity.getVideoView_4().resume();
                    FourSplitViewActivity.getImageView_1().getHandler().notify();
                    FourSplitViewActivity.getImageView_2().getHandler().notify();
                    FourSplitViewActivity.getImageView_3().getHandler().notify();
                    FourSplitViewActivity.getImageView_4().getHandler().notify();
                }
                if (playFlag == 2) {//停止状态
                    //实现重写运行程序
                }

        }
    }

    public void suspendBtn(View view) {//在播放时才为有效按钮，其他都无效
        Log.d(TAG,"this is suspendBtn");
        Toast.makeText(this,"实现界面的控件暂停状态",Toast.LENGTH_SHORT).show();//获取当前文件夹的命名格式中的分屏字符串，以此获得对应的控件，控件又分图片和视频子控件；即先判分屏名，再判类型

        switch (ImportActivity.getFilesCount()){
            case 1://一分屏
                if (playFlag == 0) {//播放状态，默认为0：U盘导入时，正常播放，即原状态为0；直接两控件设置暂停状态
                    OneSplitViewActivity.getVideoView_1().pause();
                    try {
                        OneSplitViewActivity.getHandler().wait();//两个方法搭配使用，wait()使线程进入阻塞状态，调用notify()时，线程进入可执行状态。
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (playFlag == 1) {//暂停状态
                    Log.d(TAG,"暂停按键为无效状态");
                } else if (playFlag ==2) {//停止状态
                    Log.d(TAG,"暂停按键为无效状态");//或跳回主界面
                }
                break;
            case 2:
                if (playFlag == 0){
                    TwoSplitViewActivity.getVideoView_1().pause();
                    TwoSplitViewActivity.getVideoView_2().pause();
                    try {
                        TwoSplitViewActivity.getImageView_1().getHandler().wait();
                        TwoSplitViewActivity.getImageView_2().getHandler().wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (playFlag == 1) {//暂停状态
                    Log.d(TAG,"暂停按键为无效状态");
                } else if (playFlag ==2) {//停止状态
                    Log.d(TAG,"暂停按键为无效状态");
                }
                break;
            case 3:
                if (playFlag == 0){
                    ThreeSplitViewActivity.getVideoView_1().pause();
                    ThreeSplitViewActivity.getVideoView_2().pause();
                    ThreeSplitViewActivity.getVideoView_3().pause();
                    try {
                        ThreeSplitViewActivity.getImageView_1().getHandler().wait();
                        ThreeSplitViewActivity.getImageView_2().getHandler().wait();
                        ThreeSplitViewActivity.getImageView_3().getHandler().wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (playFlag == 1) {//暂停状态
                    Log.d(TAG,"暂停按键为无效状态");
                } else if (playFlag ==2) {//停止状态
                    Log.d(TAG,"暂停按键为无效状态");
                }
                break;
            case 4:
                if (playFlag == 0){
                    FourSplitViewActivity.getVideoView_1().pause();
                    FourSplitViewActivity.getVideoView_2().pause();
                    FourSplitViewActivity.getVideoView_3().pause();
                    FourSplitViewActivity.getVideoView_4().pause();
                    try {
                        FourSplitViewActivity.getImageView_1().getHandler().wait();
                        FourSplitViewActivity.getImageView_2().getHandler().wait();
                        FourSplitViewActivity.getImageView_3().getHandler().wait();
                        FourSplitViewActivity.getImageView_4().getHandler().wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (playFlag == 1) {//暂停状态
                    Log.d(TAG,"暂停按键为无效状态");
                } else if (playFlag ==2) {//停止状态
                    Log.d(TAG,"暂停按键为无效状态");
                }
                break;
            default:
                Log.d(TAG,"Location is switch (ImportActivity.filesCount)_default");
                break;
        }
    }

    public void stopBtn(View view) {
        switch (ImportActivity.getFilesCount()){
            case 1://一分屏
                if (playFlag == 0) {
                    OneSplitViewActivity.getImageView_1().getHandler().removeCallbacks(OneSplitViewActivity.getRunnable());//不知实际效果，待测；
                    //todo：结束所有线程，清除所有视频状态等，再跳转回主界面；
                } else if (playFlag == 1) {
                    //todo：结束所有线程，清除所有视频状态等，再跳转回主界面；
                } else if (playFlag ==2) {
                    Log.d(TAG,"停止按键为无效状态");
                }
                break;

        }
        Log.d(TAG,"this is stopBtn");
        Toast.makeText(this,"返回默认页面activity",Toast.LENGTH_SHORT).show();
    }

    public void exitBtn(View view) {
        Log.d(TAG,"this is exitBtn");
        System.exit(0);
    }

    public void MachineIdOutBtn(View view) {
        Log.d(TAG,"this is MachineIdOutBtn");
        setContentView(R.layout.activity_progress);
    }

//    public void initWidgets(int splitView, String mode){//以A-B来选择性的初始化控件
//        if (splitView == 1) {
//            switch (mode){
//                case "1":
//                    break;
//                ImageView imageView_two1_1= (ImageView) this.findViewById(R.id.imageView_two1_1);
//                default:
//                    //log
//                    break;
//            }
//        } else if (splitView == 2) {
//            switch (mode){
//                case "1":
//                    //content
//                    break;
//                case "2":
//                    //content
//                    break;
//                case "3":
//                    //content
//                    break;
//                case "4":
//                    //content
//                    break;
//                case "5":
//                    //content
//                    break;
//                case "6":
//                    //content
//                    break;
//                default:
//                    //log
//                    break;
//
//            }
//        } else if (splitView == 3) {
//            switch (mode){
//                case "1":
//                    //content
//                    break;
//                case "2":
//                    //content
//                    break;
//                case "3":
//                    //content
//                    break;
//                case "4":
//                    //content
//                    break;
//                case "5":
//                    //content
//                    break;
//                case "6":
//                    //content
//                    break;
//                case "7":
//                    //content
//                    break;
//                case "8":
//                    //content
//                    break;
//                default:
//                    //log
//                    break;
//            }
//        } else if (splitView ==4) {
//            switch (mode){
//                case "1":
//                    //content
//                    break;
//                default:
//                    //log
//                    break;
//            }
//        }
//
//    }
}