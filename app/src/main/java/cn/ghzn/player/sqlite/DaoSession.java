package cn.ghzn.player.sqlite;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.Map;

import cn.ghzn.player.sqlite.device.Device;
import cn.ghzn.player.sqlite.source.Source;

public class DaoSession extends AbstractDaoSession {

    private final DaoConfig deviceDaoConfig;
    private final DaoConfig sourceDaoConfig;

    private final DeviceDao deviceDao;
    private final SourceDao sourceDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        deviceDaoConfig = daoConfigMap.get(DeviceDao.class).clone();
        deviceDaoConfig.initIdentityScope(type);

        sourceDaoConfig = daoConfigMap.get(SourceDao.class).clone();
        sourceDaoConfig.initIdentityScope(type);

        deviceDao = new DeviceDao(deviceDaoConfig, this);
        sourceDao = new SourceDao(sourceDaoConfig, this);

        registerDao(Device.class, deviceDao);
        registerDao(Source.class, sourceDao);
    }

    public void clear() {
        deviceDaoConfig.clearIdentityScope();
        sourceDaoConfig.clearIdentityScope();
    }

    public DeviceDao getDeviceDao() {
        return deviceDao;
    }

    public SourceDao getSourceDao() {
        return sourceDao;
    }

}