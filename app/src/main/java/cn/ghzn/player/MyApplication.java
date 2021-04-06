package cn.ghzn.player;

import android.app.Application;
import android.content.Context;

import java.util.Date;

import cn.ghzn.player.sqlite.DaoManager;
import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;

public class MyApplication extends Application {

    private String program_id = "";//存储数据库数据声明;记住，该application类是优先加载的！！！
    private String split_view = "";
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

    //    private final DaoManager daoManager = DaoManager.getInstance();//不可这么使用,app类加载时，数据库还没加载
    private static Context mContext;//变量声明
    private Device mDevice;
    private Source mSource;
    private boolean extraState = false;
    private boolean ConnectionState = false;
    private String sonSource;

    public String getSonSource() {
        return sonSource;
    }

    public void setSonSource(String sonSource) {
        this.sonSource = sonSource;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //greenDao全局配置,只希望有一个数据库操作对象
        DaoManager.getInstance();
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
