package cn.ghzn.player;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;

import static cn.ghzn.player.util.FileUtils.getFilePath;

public class MyApplication extends Application {

    //数据库数据初始化声明;
    private String program_id = "";
    private String split_view = "";
    private String split_mode = "";
    private String son_source = "";//存数据库数据的变量
    private long create_time;//记录上一次节目导入的时间
    private String device_Name = "";
    private String device_Id = "";
    private boolean authority_state = false;//授权状态_
    private String authority_time;//授权文件的授权开始时间戳转换为格式化的日期时间_
    private String authority_expired;//授权到期时间_
    private String authorization = "";//授权码_
    private String software_version = "";
    private String firmware_version = "";
    private int width ;
    private int height ;
    private long start_time;//授权文件的授权开始时间戳
    private long end_time;//授权文件的授权结束时间戳
    private long time_difference;
    private long first_time;
    private long relative_time;
    private String first_machineCodeOut;

    //全局变量声明--赋一值全局用
    private Device mDevice;
    private Source mSource;//表示数据库source表
    private static Context mContext;
    private Map<String,Object> mMap;
    private String sonSource;//存储所有子文件夹绝对地址
    private String licenceDir;//本地调用license文件的地址
    private String extraPath;
    private String filesParent;
    private long startTime;
    private long endTime;
    private long timeDiff;
    private long delayMillis = 5000;
    private long createTime;//记录当前的本地时间，而create_time是成功播放资源才记录的本地时间，不成功则不记录
    private int fileCounts;
    private int listNum1 = 0;
    private int listNum2 = 0;
    private int listNum3 = 0;
    private int listNum4 = 0;
    private boolean setSourcePlayer = true;//对标MainActivity的98行的问题，暂时的缓和办法

    //临时全局变量--暂无

    //全局对象声明
    private Activity currentActivity;
    private android.os.Handler handler = new Handler();

    //临时全局对象--一赋一用后就闲置，可重复赋值--取代临时对象
    private File file;
    private Intent intent;
    private String[] strings;


    //标志状态声明
    private boolean extraState = false;//表示由ImportActivity跳转的标志，与类似U盘接入状态importState类似--不知是否多余(待测)
    private boolean playSonImageFlag =true;
    private boolean finishState = false;//activity的结束广播状态
    private boolean importState = false;//U盘导入状态
    private boolean mediaPlayState = true;//作用：在activity销毁时，使监听内容无效化。
    private int playFlag = 0;//当前的播放状态：播放：playFlag = 0；暂停：playFlag = 1；停止：playFlag = 2；实际效果为对图片播放的控制
    private int forMat1 = 0;//控件1的播放属性：0：初试状态无播放属性；1：播放图片，2：播放视频 ；其中1234对应控件1234
    private int forMat2 = 0;
    private int forMat3 = 0;
    private int forMat4 = 0;

    private boolean MondayState = true;//从控件中获取定时星期的确定。默认全为有效。
    private boolean TuesdayState = true;
    private boolean WednesdayState = true;
    private boolean ThursdayState = true;
    private boolean FridayState = true;
    private boolean SaturdayState = true;
    private boolean SundayState = true;

    //private final DaoManager daoManager = DaoManager.getInstance();//不可这么使用,app类加载时，数据库还没加载


    //控件相关声明--分类优先级大于全局变量
    private String AuthorityName = "未连接";
    private ImageView imageView_1;
    private ImageView imageView_2;
    private ImageView imageView_3;
    private ImageView imageView_4;
    private CustomVideoView videoView_1;//自定义video类，原本的无法自适应全屏,且无法正常性暂停播放
    private CustomVideoView videoView_2;
    private CustomVideoView videoView_3;
    private CustomVideoView videoView_4;
    private View view;//存储菜单布局的VIew
    private Runnable runnable1;
    private Runnable runnable2;
    private Runnable runnable3;
    private Runnable runnable4;

    /**
     * 单例初始化；5月27日编写此处
     */
    public static Calendar cld;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mContext.getExternalFilesDirs(null);

        //greenDao全局配置,只希望有一个数据库操作对象
        DaoManager.getInstance();
        cld = Calendar.getInstance();

    }

    public boolean isMondayState() {
        return MondayState;
    }

    public void setMondayState(boolean mondayState) {
        MondayState = mondayState;
    }

    public boolean isTuesdayState() {
        return TuesdayState;
    }

    public void setTuesdayState(boolean tuesdayState) {
        TuesdayState = tuesdayState;
    }

    public boolean isWednesdayState() {
        return WednesdayState;
    }

    public void setWednesdayState(boolean wednesdayState) {
        WednesdayState = wednesdayState;
    }

    public boolean isThursdayState() {
        return ThursdayState;
    }

    public void setThursdayState(boolean thursdayState) {
        ThursdayState = thursdayState;
    }

    public boolean isFridayState() {
        return FridayState;
    }

    public void setFridayState(boolean fridayState) {
        FridayState = fridayState;
    }

    public boolean isSaturdayState() {
        return SaturdayState;
    }

    public void setSaturdayState(boolean saturdayState) {
        SaturdayState = saturdayState;
    }

    public boolean isSundayState() {
        return SundayState;
    }

    public void setSundayState(boolean sundayState) {
        SundayState = sundayState;
    }

    public String getFirst_machineCodeOut() {
        return first_machineCodeOut;
    }

    public void setFirst_machineCodeOut(String first_machineCodeOut) {
        this.first_machineCodeOut = first_machineCodeOut;
    }

    public boolean isSetSourcePlayer() {
        return setSourcePlayer;
    }

    public void setSetSourcePlayer(boolean setSourcePlayer) {
        this.setSourcePlayer = setSourcePlayer;
    }

    public String[] getStrings() {
        return strings;
    }

    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public boolean isMediaPlayState() {
        return mediaPlayState;
    }

    public void setMediaPlayState(boolean mediaPlayState) {
        this.mediaPlayState = mediaPlayState;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getAuthorityName() {
        return AuthorityName;
    }

    public void setAuthorityName(String authorityName) {
        AuthorityName = authorityName;
    }

    public int getFileCounts() {
        return fileCounts;
    }

    public void setFileCounts(int fileCounts) {
        this.fileCounts = fileCounts;
    }

    public String getFilesParent() {
        return filesParent;
    }

    public void setFilesParent(String filesParent) {
        this.filesParent = filesParent;
    }

    public long getRelative_time() {
        return relative_time;
    }

    public void setRelative_time(long relative_time) {
        this.relative_time = relative_time;
    }

    public long getTime_difference() {
        return time_difference;
    }

    public void setTime_difference(long time_difference) {
        this.time_difference = time_difference;
    }

    public long getFirst_time() {
        return first_time;
    }

    public void setFirst_time(long first_time) {
        this.first_time = first_time;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getStart_time() {
        return start_time;
    }

    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }

    public Map<String, Object> getMap() {
        return mMap;
    }

    public void setMap(Map<String, Object> map) {
        mMap = map;
    }

    public String getExtraPath() {
        return extraPath;
    }

    public void setExtraPath(String extraPath) {
        this.extraPath = extraPath;
    }

    public String getLicenceDir() {
        return licenceDir;
    }

    public void setLicenceDir(String licenceDir) {
        this.licenceDir = licenceDir;
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

    public boolean isFinishState() {
        return finishState;
    }

    public void setFinishState(boolean finishState) {
        this.finishState = finishState;
    }

    public boolean isImportState() {
        return importState;
    }

    public void setImportState(boolean importState) {
        this.importState = importState;
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

    public String getAuthority_expired() {
        return authority_expired;
    }

    public void setAuthority_expired(String authority_expired) {
        this.authority_expired = authority_expired;
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

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public static Context getmContext() {
        return mContext;
    }

    public static void setmContext(Context mContext) {
        MyApplication.mContext = mContext;
    }

}
