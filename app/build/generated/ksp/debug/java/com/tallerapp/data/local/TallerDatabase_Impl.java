package com.tallerapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TallerDatabase_Impl extends TallerDatabase {
  private volatile TrabajoDao _trabajoDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `trabajo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cliente` TEXT NOT NULL, `telefono` TEXT, `patente` TEXT, `marca` TEXT NOT NULL, `modelo` TEXT NOT NULL, `servicio` TEXT NOT NULL, `fechaIngreso` INTEGER NOT NULL, `estadoReparacion` TEXT NOT NULL, `estadoCobro` TEXT NOT NULL, `problema` TEXT, `diagnostico` TEXT, `precioCentavos` INTEGER NOT NULL, `cobroId` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_estadoReparacion` ON `trabajo` (`estadoReparacion`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_patente` ON `trabajo` (`patente`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_cliente` ON `trabajo` (`cliente`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fba41f43a0a5f90fb132f7ca513641d3')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `trabajo`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTrabajo = new HashMap<String, TableInfo.Column>(14);
        _columnsTrabajo.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("cliente", new TableInfo.Column("cliente", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("telefono", new TableInfo.Column("telefono", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("patente", new TableInfo.Column("patente", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("marca", new TableInfo.Column("marca", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("modelo", new TableInfo.Column("modelo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("servicio", new TableInfo.Column("servicio", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("fechaIngreso", new TableInfo.Column("fechaIngreso", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("estadoReparacion", new TableInfo.Column("estadoReparacion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("estadoCobro", new TableInfo.Column("estadoCobro", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("problema", new TableInfo.Column("problema", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("diagnostico", new TableInfo.Column("diagnostico", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("precioCentavos", new TableInfo.Column("precioCentavos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrabajo.put("cobroId", new TableInfo.Column("cobroId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrabajo = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTrabajo = new HashSet<TableInfo.Index>(3);
        _indicesTrabajo.add(new TableInfo.Index("index_trabajo_estadoReparacion", false, Arrays.asList("estadoReparacion"), Arrays.asList("ASC")));
        _indicesTrabajo.add(new TableInfo.Index("index_trabajo_patente", false, Arrays.asList("patente"), Arrays.asList("ASC")));
        _indicesTrabajo.add(new TableInfo.Index("index_trabajo_cliente", false, Arrays.asList("cliente"), Arrays.asList("ASC")));
        final TableInfo _infoTrabajo = new TableInfo("trabajo", _columnsTrabajo, _foreignKeysTrabajo, _indicesTrabajo);
        final TableInfo _existingTrabajo = TableInfo.read(db, "trabajo");
        if (!_infoTrabajo.equals(_existingTrabajo)) {
          return new RoomOpenHelper.ValidationResult(false, "trabajo(com.tallerapp.data.local.TrabajoEntity).\n"
                  + " Expected:\n" + _infoTrabajo + "\n"
                  + " Found:\n" + _existingTrabajo);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "fba41f43a0a5f90fb132f7ca513641d3", "26552e7c02688fec3eeb1c5b370aa86d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "trabajo");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `trabajo`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TrabajoDao.class, TrabajoDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TrabajoDao trabajoDao() {
    if (_trabajoDao != null) {
      return _trabajoDao;
    } else {
      synchronized(this) {
        if(_trabajoDao == null) {
          _trabajoDao = new TrabajoDao_Impl(this);
        }
        return _trabajoDao;
      }
    }
  }
}
