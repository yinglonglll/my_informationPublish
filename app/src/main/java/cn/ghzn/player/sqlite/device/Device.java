package cn.ghzn.player.sqlite.device;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class Device {
    @Id(autoincrement = true)
    private Long id;
    /**
     * 设备名字
     */
    private String device_name;
    /**
     * 设备 ID
     */
    private String device_id;
    /**
     * 授权状态
     */
    private boolean authority_state;
    /**
     * 授权时间
     */
    private String authority_time = "无";
    /**
     * 授权到期时间
     */
    private String authority_expired = "无";
    /**
     * 授权码
     */
    private String machine_code;
    /**
     * 系统软件版本号
     */
    private String software_version;
    /**
     * 系统固件版本号
     */
    private String firmware_version;
    /**
     * 终端屏幕宽度
     */
    private int width;
    /**
     * 终端屏幕高度
     */
    private int height;
    /**
     * 单屏视图模式:0为单屏，1为多屏
     */
    private int mode;
    /**
     * 定时任务开始时分
     */
    private String power_start_time;
    /**
     * 定时任务结束时分
     */
    private String power_end_time;

    @Generated(hash = 642847208)
    public Device(Long id, String device_name, String device_id, boolean authority_state,
            String authority_time, String authority_expired, String machine_code,
            String software_version, String firmware_version, int width, int height, int mode,
            String power_start_time, String power_end_time) {
        this.id = id;
        this.device_name = device_name;
        this.device_id = device_id;
        this.authority_state = authority_state;
        this.authority_time = authority_time;
        this.authority_expired = authority_expired;
        this.machine_code = machine_code;
        this.software_version = software_version;
        this.firmware_version = firmware_version;
        this.width = width;
        this.height = height;
        this.mode = mode;
        this.power_start_time = power_start_time;
        this.power_end_time = power_end_time;
    }
    @Generated(hash = 1469582394)
    public Device() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDevice_name() {
        return this.device_name;
    }
    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }
    public String getDevice_id() {
        return this.device_id;
    }
    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }
    public boolean getAuthority_state() {
        return this.authority_state;
    }
    public void setAuthority_state(boolean authority_state) {
        this.authority_state = authority_state;
    }
    public String getAuthority_time() {
        return this.authority_time;
    }
    public void setAuthority_time(String authority_time) {
        this.authority_time = authority_time;
    }
    public String getMachine_code() {
        return this.machine_code;
    }
    public void setMachine_code(String machine_code) {
        this.machine_code = machine_code;
    }

    public String getSoftware_version() {
        return this.software_version;
    }
    public void setSoftware_version(String software_version) {
        this.software_version = software_version;
    }
    public String getFirmware_version() {
        return this.firmware_version;
    }
    public void setFirmware_version(String firmware_version) {
        this.firmware_version = firmware_version;
    }
    public int getWidth() {
        return this.width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return this.height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public String getAuthority_expired() {
        return this.authority_expired;
    }
    public void setAuthority_expired(String authority_expired) {
        this.authority_expired = authority_expired;
    }
    
    public String getPower_start_time() {
        return this.power_start_time;
    }
    public void setPower_start_time(String power_start_time) {
        this.power_start_time = power_start_time;
    }
    public String getPower_end_time() {
        return this.power_end_time;
    }
    public void setPower_end_time(String power_end_time) {
        this.power_end_time = power_end_time;
    }
    public int getMode() {
        return this.mode;
    }
    public void setMode(int mode) {
        this.mode = mode;
    }

}