package cn.ghzn.player;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;

import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.util.AuthorityUtils;
import cn.ghzn.player.util.InfoUtils;

import static cn.ghzn.player.util.InfoUtils.getRandomString;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mDeviceName;
    private TextView mDeviceId;
    private TextView mConnectionState;
    private TextView mAuthorityState;
    GestureDetector mGestureDetector;

    private DaoManager daoManager = DaoManager.getInstance();//找到单例(唯一数据库对象)
    private MyApplication application;
//    private TextView mInstallTime;
//    private TextView mProbationTime;


    private boolean ConnectionState = false;
    private boolean AuthorityState = false;
    private TextClock mLocalTime;
    private AlertDialog mAlertDialogs;
    private View mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        daoManager.getSession().getDeviceDao();
        //开机检查数据库中设备信息:数据库有则取出设置为当前值
        Device device = DaoManager.getInstance().getSession().getDeviceDao().queryBuilder().unique();
        if(device==null){
            device = new Device();
            daoManager.getSession().getDeviceDao().insert(getDevice(device));
        }else{
            daoManager.getSession().getDeviceDao().update(getDevice(device));
        }
        Log.d(TAG,"--------设备信息---------");
        LogUtils.e(device);//利用第三方插件打印出对象的属性和方法值；
//        application.setDevice(device);//设置全局缓存作用：

        initView();//找到layout控件
        initWidget(device);//设置layout控件；从上述数据库中取信息出来显示


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        mView = this.getLayoutInflater().inflate(R.layout.activity_dialog, null);
        alertDialog.setView(mView);
        mAlertDialogs = alertDialog.create();
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
                Log.d(TAG,"OnDoubleTap");
                mAlertDialogs.show();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });


    }


    @SuppressLint("ClickableViewAccessibility")
    private void initWidget(Device device) {//不要忘记穿参数近来，自己忘记传参折腾很久，没传参时，非全局不可调用；
//        mDeviceName.setText("设备名字:ghzn_" + System.currentTimeMillis());
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
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });


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
        Toast.makeText(this,"返重新读取分屏模式文件的信息，加载读取",Toast.LENGTH_SHORT);

    }

    public void suspendBtn(View view) {
        Log.d(TAG,"this is suspendBtn");

    }

    public void stopBtn(View view) {
        Log.d(TAG,"this is stopBtn");
        Toast.makeText(this,"返回默认页面activity",Toast.LENGTH_SHORT);
    }

    public void exitBtn(View view) {
        Log.d(TAG,"this is exitBtn");
        System.exit(0);
    }

    public void MachineIdOutBtn(View view) {
        Log.d(TAG,"this is MachineIdOutBtn");
    }
}