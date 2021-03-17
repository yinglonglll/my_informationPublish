package cn.ghzn.player.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

import cn.ghzn.player.sqlite.device.Device;

public class DeviceDao extends AbstractDao<Device, Long> {

    public static final String TABLENAME = "DEVICE";

    /**
     * Properties of entity Device.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Device_name = new Property(1, String.class, "device_name", false, "DEVICE_NAME");
        public final static Property Device_id = new Property(2, String.class, "device_id", false, "DEVICE_ID");
        public final static Property Authority_state = new Property(3, boolean.class, "authority_state", false, "AUTHORITY_STATE");
        public final static Property Authority_time = new Property(4, String.class, "authority_time", false, "AUTHORITY_TIME");
        public final static Property Authorization = new Property(5, String.class, "authorization", false, "AUTHORIZATION");
        public final static Property Authority_expried = new Property(6, String.class, "authority_expried", false, "AUTHORITY_EXPRIED");
        public final static Property Software_version = new Property(7, String.class, "software_version", false, "SOFTWARE_VERSION");
        public final static Property Firmware_version = new Property(8, String.class, "firmware_version", false, "FIRMWARE_VERSION");
        public final static Property Width = new Property(9, int.class, "width", false, "WIDTH");
        public final static Property Height = new Property(10, int.class, "height", false, "HEIGHT");
    }


    public DeviceDao(DaoConfig config) {
        super(config);
    }

    public DeviceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DEVICE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"DEVICE_NAME\" TEXT," + // 1: device_name
                "\"DEVICE_ID\" TEXT," + // 2: device_id
                "\"AUTHORITY_STATE\" INTEGER NOT NULL ," + // 3: authority_state
                "\"AUTHORITY_TIME\" TEXT," + // 4: authority_time
                "\"AUTHORIZATION\" TEXT," + // 5: authorization
                "\"AUTHORITY_EXPRIED\" TEXT," + // 6: authority_expried
                "\"SOFTWARE_VERSION\" TEXT," + // 7: software_version
                "\"FIRMWARE_VERSION\" TEXT," + // 8: firmware_version
                "\"WIDTH\" INTEGER NOT NULL ," + // 9: width
                "\"HEIGHT\" INTEGER NOT NULL );"); // 10: height
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DEVICE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Device entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        String device_name = entity.getDevice_name();
        if (device_name != null) {
            stmt.bindString(2, device_name);
        }

        String device_id = entity.getDevice_id();
        if (device_id != null) {
            stmt.bindString(3, device_id);
        }
        stmt.bindLong(4, entity.getAuthority_state() ? 1L: 0L);

        String authority_time = entity.getAuthority_time();
        if (authority_time != null) {
            stmt.bindString(5, authority_time);
        }

        String authorization = entity.getAuthorization();
        if (authorization != null) {
            stmt.bindString(6, authorization);
        }

        String authority_expried = entity.getAuthority_expried();
        if (authority_expried != null) {
            stmt.bindString(7, authority_expried);
        }

        String software_version = entity.getSoftware_version();
        if (software_version != null) {
            stmt.bindString(8, software_version);
        }

        String firmware_version = entity.getFirmware_version();
        if (firmware_version != null) {
            stmt.bindString(9, firmware_version);
        }
        stmt.bindLong(10, entity.getWidth());
        stmt.bindLong(11, entity.getHeight());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Device entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        String device_name = entity.getDevice_name();
        if (device_name != null) {
            stmt.bindString(2, device_name);
        }

        String device_id = entity.getDevice_id();
        if (device_id != null) {
            stmt.bindString(3, device_id);
        }
        stmt.bindLong(4, entity.getAuthority_state() ? 1L: 0L);

        String authority_time = entity.getAuthority_time();
        if (authority_time != null) {
            stmt.bindString(5, authority_time);
        }

        String authorization = entity.getAuthorization();
        if (authorization != null) {
            stmt.bindString(6, authorization);
        }

        String authority_expried = entity.getAuthority_expried();
        if (authority_expried != null) {
            stmt.bindString(7, authority_expried);
        }

        String software_version = entity.getSoftware_version();
        if (software_version != null) {
            stmt.bindString(8, software_version);
        }

        String firmware_version = entity.getFirmware_version();
        if (firmware_version != null) {
            stmt.bindString(9, firmware_version);
        }
        stmt.bindLong(10, entity.getWidth());
        stmt.bindLong(11, entity.getHeight());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    public Device readEntity(Cursor cursor, int offset) {
        Device entity = new Device( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // device_name
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // device_id
                cursor.getShort(offset + 3) != 0, // authority_state
                cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // authority_time
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // authorization
                cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // authority_expried
                cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // software_version
                cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // firmware_version
                cursor.getInt(offset + 9), // width
                cursor.getInt(offset + 10) // height
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, Device entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDevice_name(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setDevice_id(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setAuthority_state(cursor.getShort(offset + 3) != 0);
        entity.setAuthority_time(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setAuthorization(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAuthority_expried(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSoftware_version(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setFirmware_version(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setWidth(cursor.getInt(offset + 9));
        entity.setHeight(cursor.getInt(offset + 10));
    }

    @Override
    protected final Long updateKeyAfterInsert(Device entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    public Long getKey(Device entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Device entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }

}