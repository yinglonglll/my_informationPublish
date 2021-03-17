package cn.ghzn.player.sqlite;

import cn.ghzn.player.Constants;
import cn.ghzn.player.MyApplication;

public class DaoManager {

    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private static DaoManager mInstance; //单例

    //创建私有构造器
    private DaoManager(String dbName){//构造器生成对象
        if (mInstance == null) {
            DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(MyApplication.getmContext(), "info",null);//此处为自己需要处理的表
            mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
            mDaoSession = mDaoMaster.newSession();
        }
    }
    //以创建单例
    public static DaoManager getInstance() {//通过私有构造器 得到单例
        String dbName = Constants.DBNAME;//info
        if (mInstance == null) {
            synchronized (DaoManager.class) {//保证异步处理安全操作

                if (mInstance == null) {
                    mInstance = new DaoManager(dbName);
                }
            }
        }
        return mInstance;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }
    public DaoSession getSession() {
        return mDaoSession;
    }
    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }

}
