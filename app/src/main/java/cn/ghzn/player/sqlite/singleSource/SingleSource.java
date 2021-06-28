package cn.ghzn.player.sqlite.singleSource;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * <pre>
 *     author : yinglonglll
 *     e-mail : 949706806@qq.com
 *     time   : 2021/06/25
 *     desc   :
 *     func--->
 *     version: 1.0
 * </pre>
 */
@Entity
public class SingleSource {
    @Id(autoincrement = true)
    private Long id;
    /**
     * 单屏分屏状态(有无单屏资源)
     */
    private String single_view;
    /**
     * 单屏资源文件目录
     */
    private String single_Son_source;
    @Generated(hash = 383734104)
    public SingleSource(Long id, String single_view, String single_Son_source) {
        this.id = id;
        this.single_view = single_view;
        this.single_Son_source = single_Son_source;
    }
    @Generated(hash = 1709481184)
    public SingleSource() {
    }
    public String getSingle_view() {
        return this.single_view;
    }
    public void setSingle_view(String single_view) {
        this.single_view = single_view;
    }
    public String getSingle_Son_source() {
        return this.single_Son_source;
    }
    public void setSingle_Son_source(String single_Son_source) {
        this.single_Son_source = single_Son_source;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
