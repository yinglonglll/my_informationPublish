package cn.ghzn.player;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import java.util.Date;

import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;

public class MyApplication extends Application {

    private String program_id = "";//存储数据库数据声明;
    private String split_view = "";//记住，该application类是优先加载的！！！
    private String split_mode = "";
    private String son_source = "";
    private Date create_time ;

    private String device_Name = "";
    private String device_Id = "";
    private boolean authority_state;
    private String authority_time = "";
    private String authorization = "";
    private String software_version = "";
    private String firmware_version = "";
    private int width ;
    private int height ;

    private static Context mContext;//变量声明
    private Device mDevice;
    private Source mSource;
    private boolean extraState = false;
    private boolean ConnectionState = false;
    private String sonSource;
    private long startTime;
    private long endTime;
    private long timeDiff;
    private Activity currentActivity;
    private long delayMillis = 5000;
    private boolean playSonImageFlag =true;

    private int playFlag = 0;//默认模式：播放：playFlag = 0；暂停：playFlag = 1；停止：playFlag = 2；
    private boolean finishFlag = false;
    private int forMat1 = 0;//默认模式：0：初试状态无播放属性；1：播放图片，2：播放视频 ；其中1234对应控件1234
    private int forMat2 = 0;
    private int forMat3 = 0;
    private int forMat4 = 0;
    private int listNum1 = 0;
    private int listNum2 = 0;
    private int listNum3 = 0;
    private int listNum4 = 0;
    //    private final DaoManager daoManager = DaoManager.getInstance();//不可这么使用,app类加载时，数据库还没加载



    private ImageView imageView_1;//控件声明
    private ImageView imageView_2;//控件声明
    private ImageView imageView_3;//控件声明
    private ImageView imageView_4;//控件声明
    private CustomVideoView videoView_1;//自定义video类，原本的无法自适应全屏；
    private CustomVideoView videoView_2;//自定义video类，原本的无法自适应全屏；
    private CustomVideoView videoView_3;//自定义video类，原本的无法自适应全屏；
    private CustomVideoView videoView_4;//自定义video类，原本的无法自适应全屏；
    private View view;//存储菜单布局的VIew
    private android.os.Handler handler = new Handler();
    private Runnable runnable1;
    private Runnable runnable2;
    private Runnable runnable3;
    private Runnable runnable4;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //greenDao全局配置,只希望有一个数据库操作对象
        DaoManager.getInstance();
    }

    public boolean isPlaySonImageFlag() {
        return playSonImageFlag;
    }

    public void setPlaySonImageFlag(boolean playSonImageFlag) {
        this.playSonImageFlag = playSonImageFlag;
    }

    public Runnable getRunnable1() {
        return runnable1;
    }

    public void setRunnable1(Runnable runnable1) {
        this.runnable1 = runnable1;
    }

    public Runnable getRunnable2() {
        return runnable2;
    }

    public void setRunnable2(Runnable runnable2) {
        this.runnable2 = runnable2;
    }

    public Runnable getRunnable3() {
        return runnable3;
    }

    public void setRunnable3(Runnable runnable3) {
        this.runnable3 = runnable3;
    }

    public Runnable getRunnable4() {
        return runnable4;
    }

    public void setRunnable4(Runnable runnable4) {
        this.runnable4 = runnable4;
    }

    public int getListNum2() {
        return listNum2;
    }

    public void setListNum2(int listNum2) {
        this.listNum2 = listNum2;
    }

    public int getListNum3() {
        return listNum3;
    }

    public void setListNum3(int listNum3) {
        this.listNum3 = listNum3;
    }

    public int getListNum4() {
        return listNum4;
    }

    public void setListNum4(int listNum4) {
        this.listNum4 = listNum4;
    }

    public int getListNum1() {
        return listNum1;
    }

    public void setListNum1(int listNum1) {
        this.listNum1 = listNum1;
    }

    public int getForMat1() {
        return forMat1;
    }

    public void setForMat1(int forMat1) {
        this.forMat1 = forMat1;
    }

    public int getForMat2() {
        return forMat2;
    }

    public void setForMat2(int forMat2) {
        this.forMat2 = forMat2;
    }

    public int getForMat3() {
        return forMat3;
    }

    public void setForMat3(int forMat3) {
        this.forMat3 = forMat3;
    }

    public int getForMat4() {
        return forMat4;
    }

    public void setForMat4(int forMat4) {
        this.forMat4 = forMat4;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    public boolean isFinishFlag() {
        return finishFlag;
    }

    public void setFinishFlag(boolean finishFlag) {
        this.finishFlag = finishFlag;
    }

    public int getPlayFlag() {
        return playFlag;
    }

    public void setPlayFlag(int playFlag) {
        this.playFlag = playFlag;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getTimeDiff() {
        return timeDiff;
    }

    public void setTimeDiff(long timeDiff) {
        this.timeDiff = timeDiff;
    }

    public ImageView getImageView_2() {
        return imageView_2;
    }

    public void setImageView_2(ImageView imageView_2) {
        this.imageView_2 = imageView_2;
    }

    public ImageView getImageView_3() {
        return imageView_3;
    }

    public void setImageView_3(ImageView imageView_3) {
        this.imageView_3 = imageView_3;
    }

    public ImageView getImageView_4() {
        return imageView_4;
    }

    public void setImageView_4(ImageView imageView_4) {
        this.imageView_4 = imageView_4;
    }

    public CustomVideoView getVideoView_2() {
        return videoView_2;
    }

    public void setVideoView_2(CustomVideoView videoView_2) {
        this.videoView_2 = videoView_2;
    }

    public CustomVideoView getVideoView_3() {
        return videoView_3;
    }

    public void setVideoView_3(CustomVideoView videoView_3) {
        this.videoView_3 = videoView_3;
    }

    public CustomVideoView getVideoView_4() {
        return videoView_4;
    }

    public void setVideoView_4(CustomVideoView videoView_4) {
        this.videoView_4 = videoView_4;
    }

    public ImageView getImageView_1() {
        return imageView_1;
    }

    public void setImageView_1(ImageView imageView_1) {
        this.imageView_1 = imageView_1;
    }

    public CustomVideoView getVideoView_1() {
        return videoView_1;
    }

    public void setVideoView_1(CustomVideoView videoView_1) {
        this.videoView_1 = videoView_1;
    }

    public String getSonSource() {
        return sonSource;
    }

    public void setSonSource(String sonSource) {
        this.sonSource = sonSource;
    }

    public boolean isConnectionState() {
        return ConnectionState;
    }

    public void setConnectionState(boolean connectionState) {
        ConnectionState = connectionState;
    }

    public String getDevice_Name() {
        return device_Name;
    }

    public void setDevice_Name(String device_Name) {
        this.device_Name = device_Name;
    }

    public String getDevice_Id() {
        return device_Id;
    }

    public void setDevice_Id(String device_Id) {
        this.device_Id = device_Id;
    }

    public boolean isAuthority_state() {
        return authority_state;
    }

    public void setAuthority_state(boolean authority_state) {
        this.authority_state = authority_state;
    }

    public String getAuthority_time() {
        return authority_time;
    }

    public void setAuthority_time(String authority_time) {
        this.authority_time = authority_time;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getSoftware_version() {
        return software_version;
    }

    public void setSoftware_version(String software_version) {
        this.software_version = software_version;
    }

    public String getFirmware_version() {
        return firmware_version;
    }

    public void setFirmware_version(String firmware_version) {
        this.firmware_version = firmware_version;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public Device getDevice() {
        return mDevice;
    }

    public void setDevice(Device device) {
        mDevice = device;
    }

    public Source getSource() {
        return mSource;
    }

    public void setSource(Source source) {
        mSource = source;
    }

    public boolean isExtraState() {
        return extraState;
    }

    public void setExtraState(boolean extraState) {
        this.extraState = extraState;
    }

    public String getProgram_id() {
        return program_id;
    }

    public void setProgram_id(String program_id) {
        this.program_id = program_id;
    }

    public String getSplit_view() {
        return split_view;
    }

    public void setSplit_view(String split_view) {
        this.split_view = split_view;
    }

    public String getSplit_mode() {
        return split_mode;
    }

    public void setSplit_mode(String split_mode) {
        this.split_mode = split_mode;
    }

    public String getSon_source() {
        return son_source;
    }

    public void setSon_source(String son_source) {
        this.son_source = son_source;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public static Context getmContext() {
        return mContext;
    }

    public static void setmContext(Context mContext) {
        MyApplication.mContext = mContext;
    }

}
