package cn.ghzn.player.sqlite.source;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class Source {
    @Id(autoincrement = true)
    private Long id;
    /**
     * 节目ID
     */
    private String program_id;
    /**
     * 分频数
     */
    private String split_view;
    /**
     * 分频模式
     */
    private String split_mode;
    /**
     * 存储所有子文件夹的绝对地址,以拼字串形式 “ *** ”
     */
    private String son_source;
    /**
     * 每次节目导入时间；用于
     */
    private long create_time = 0;
    /**
     * 节目导入时间戳
     */
    private long start_time;
    /**
     * 节目导入时间戳
     */
    private long end_time;
    /**
     * 节目导入时间戳的差值
     */
    private long time_difference;
    /**
     * 首次导入节目时间(本地时间)
     */
    private long first_time;
    /**
     * 用于存储是本地时间还是服务器时间的抽象变量
     */
    private long relative_time;
    /**
     * 授权文件的绝对地址
     */
    private String license_dir;
    @Generated(hash = 537373540)
    public Source(Long id, String program_id, String split_view, String split_mode,
            String son_source, long create_time, long start_time, long end_time,
            long time_difference, long first_time, long relative_time,
            String license_dir) {
        this.id = id;
        this.program_id = program_id;
        this.split_view = split_view;
        this.split_mode = split_mode;
        this.son_source = son_source;
        this.create_time = create_time;
        this.start_time = start_time;
        this.end_time = end_time;
        this.time_difference = time_difference;
        this.first_time = first_time;
        this.relative_time = relative_time;
        this.license_dir = license_dir;
    }
    @Generated(hash = 615387317)
    public Source() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getProgram_id() {
        return this.program_id;
    }
    public void setProgram_id(String program_id) {
        this.program_id = program_id;
    }
    public String getSplit_view() {
        return this.split_view;
    }
    public void setSplit_view(String split_view) {
        this.split_view = split_view;
    }
    public String getSplit_mode() {
        return this.split_mode;
    }
    public void setSplit_mode(String split_mode) {
        this.split_mode = split_mode;
    }
    public String getSon_source() {
        return this.son_source;
    }
    public void setSon_source(String son_source) {
        this.son_source = son_source;
    }
    public long getCreate_time() {
        return this.create_time;
    }
    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }
    public String getLicense_dir() {
        return this.license_dir;
    }
    public void setLicense_dir(String license_dir) {
        this.license_dir = license_dir;
    }
    public long getStart_time() {
        return this.start_time;
    }
    public void setStart_time(long start_time) {
        this.start_time = start_time;
    }
    public long getEnd_time() {
        return this.end_time;
    }
    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }
    public long getTime_difference() {
        return this.time_difference;
    }
    public void setTime_difference(long time_difference) {
        this.time_difference = time_difference;
    }
    public long getFirst_time() {
        return this.first_time;
    }
    public void setFirst_time(long first_time) {
        this.first_time = first_time;
    }
    public long getRelative_time() {
        return this.relative_time;
    }
    public void setRelative_time(long relative_time) {
        this.relative_time = relative_time;
    }





}