package cn.ghzn.player.sqlite.source;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class Source {
    @Id(autoincrement = true)
    private Long id;
    /**
     * 分屏模式文件的文件名
     */
    private String file_name;
    /**
     * 复制文件的绝对地址
     */
    private String mtarget;
    @Generated(hash = 514193615)
    public Source(Long id, String file_name, String mtarget) {
        this.id = id;
        this.file_name = file_name;
        this.mtarget = mtarget;
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
    public String getFile_name() {
        return this.file_name;
    }
    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
    public String getMtarget() {
        return this.mtarget;
    }
    public void setMtarget(String mtarget) {
        this.mtarget = mtarget;
    }




}