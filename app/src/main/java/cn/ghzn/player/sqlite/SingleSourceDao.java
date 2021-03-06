package cn.ghzn.player.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import cn.ghzn.player.sqlite.singleSource.SingleSource;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SINGLE_SOURCE".
*/
public class SingleSourceDao extends AbstractDao<SingleSource, Long> {

    public static final String TABLENAME = "SINGLE_SOURCE";

    /**
     * Properties of entity SingleSource.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Single_view = new Property(1, String.class, "single_view", false, "SINGLE_VIEW");
        public final static Property Source = new Property(2, String.class, "source", false, "SOURCE");
    }


    public SingleSourceDao(DaoConfig config) {
        super(config);
    }
    
    public SingleSourceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SINGLE_SOURCE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"SINGLE_VIEW\" TEXT," + // 1: single_view
                "\"SOURCE\" TEXT);"); // 2: source
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SINGLE_SOURCE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SingleSource entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String single_view = entity.getSingle_view();
        if (single_view != null) {
            stmt.bindString(2, single_view);
        }
 
        String source = entity.getSource();
        if (source != null) {
            stmt.bindString(3, source);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SingleSource entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String single_view = entity.getSingle_view();
        if (single_view != null) {
            stmt.bindString(2, single_view);
        }
 
        String source = entity.getSource();
        if (source != null) {
            stmt.bindString(3, source);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public SingleSource readEntity(Cursor cursor, int offset) {
        SingleSource entity = new SingleSource( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // single_view
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // source
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SingleSource entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setSingle_view(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSource(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(SingleSource entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(SingleSource entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SingleSource entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
