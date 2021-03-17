package cn.ghzn.player.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

import cn.ghzn.player.sqlite.source.Source;

public class SourceDao extends AbstractDao<Source, Long> {

    public static final String TABLENAME = "SOURCE";

    /**
     * Properties of entity Source.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property File_name = new Property(1, String.class, "file_name", false, "FILE_NAME");
        public final static Property Mtarget = new Property(2, String.class, "mtarget", false, "MTARGET");
    }


    public SourceDao(DaoConfig config) {
        super(config);
    }

    public SourceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SOURCE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"FILE_NAME\" TEXT," + // 1: file_name
                "\"MTARGET\" TEXT);"); // 2: mtarget
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SOURCE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Source entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        String file_name = entity.getFile_name();
        if (file_name != null) {
            stmt.bindString(2, file_name);
        }

        String mtarget = entity.getMtarget();
        if (mtarget != null) {
            stmt.bindString(3, mtarget);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Source entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        String file_name = entity.getFile_name();
        if (file_name != null) {
            stmt.bindString(2, file_name);
        }

        String mtarget = entity.getMtarget();
        if (mtarget != null) {
            stmt.bindString(3, mtarget);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    public Source readEntity(Cursor cursor, int offset) {
        Source entity = new Source( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // file_name
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // mtarget
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, Source entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setFile_name(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setMtarget(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
    }

    @Override
    protected final Long updateKeyAfterInsert(Source entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    public Long getKey(Source entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Source entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }

}