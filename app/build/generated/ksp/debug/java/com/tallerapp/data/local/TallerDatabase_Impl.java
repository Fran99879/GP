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
  private volatile IngresoDao _ingresoDao;

  private volatile EgresoDao _egresoDao;

  private volatile DeudaDao _deudaDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `ingreso` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `montoCentavos` INTEGER NOT NULL, `concepto` TEXT NOT NULL, `metodo` TEXT NOT NULL, `repEfectivo` INTEGER, `repTransferencia` INTEGER, `repTarjeta` INTEGER, `repMercadoPago` INTEGER, `fecha` INTEGER NOT NULL, `fechaRegistro` INTEGER NOT NULL, `origen` TEXT NOT NULL, `trabajoId` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ingreso_fecha` ON `ingreso` (`fecha`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `egreso` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `montoCentavos` INTEGER NOT NULL, `categoria` TEXT NOT NULL, `concepto` TEXT NOT NULL, `fecha` INTEGER NOT NULL, `fechaRegistro` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_egreso_fecha` ON `egreso` (`fecha`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `deuda` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `montoCentavos` INTEGER NOT NULL, `fecha` INTEGER NOT NULL, `nota` TEXT NOT NULL, `cobrada` INTEGER NOT NULL, `fechaCobro` INTEGER, `fechaRegistro` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_deuda_cobrada` ON `deuda` (`cobrada`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '614eb1ad7dcfe79611827e7eb03bcefd')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `ingreso`");
        db.execSQL("DROP TABLE IF EXISTS `egreso`");
        db.execSQL("DROP TABLE IF EXISTS `deuda`");
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
        final HashMap<String, TableInfo.Column> _columnsIngreso = new HashMap<String, TableInfo.Column>(12);
        _columnsIngreso.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("montoCentavos", new TableInfo.Column("montoCentavos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("concepto", new TableInfo.Column("concepto", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("metodo", new TableInfo.Column("metodo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("repEfectivo", new TableInfo.Column("repEfectivo", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("repTransferencia", new TableInfo.Column("repTransferencia", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("repTarjeta", new TableInfo.Column("repTarjeta", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("repMercadoPago", new TableInfo.Column("repMercadoPago", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("fecha", new TableInfo.Column("fecha", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("fechaRegistro", new TableInfo.Column("fechaRegistro", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("origen", new TableInfo.Column("origen", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIngreso.put("trabajoId", new TableInfo.Column("trabajoId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysIngreso = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesIngreso = new HashSet<TableInfo.Index>(1);
        _indicesIngreso.add(new TableInfo.Index("index_ingreso_fecha", false, Arrays.asList("fecha"), Arrays.asList("ASC")));
        final TableInfo _infoIngreso = new TableInfo("ingreso", _columnsIngreso, _foreignKeysIngreso, _indicesIngreso);
        final TableInfo _existingIngreso = TableInfo.read(db, "ingreso");
        if (!_infoIngreso.equals(_existingIngreso)) {
          return new RoomOpenHelper.ValidationResult(false, "ingreso(com.tallerapp.data.local.IngresoEntity).\n"
                  + " Expected:\n" + _infoIngreso + "\n"
                  + " Found:\n" + _existingIngreso);
        }
        final HashMap<String, TableInfo.Column> _columnsEgreso = new HashMap<String, TableInfo.Column>(6);
        _columnsEgreso.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEgreso.put("montoCentavos", new TableInfo.Column("montoCentavos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEgreso.put("categoria", new TableInfo.Column("categoria", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEgreso.put("concepto", new TableInfo.Column("concepto", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEgreso.put("fecha", new TableInfo.Column("fecha", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEgreso.put("fechaRegistro", new TableInfo.Column("fechaRegistro", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEgreso = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEgreso = new HashSet<TableInfo.Index>(1);
        _indicesEgreso.add(new TableInfo.Index("index_egreso_fecha", false, Arrays.asList("fecha"), Arrays.asList("ASC")));
        final TableInfo _infoEgreso = new TableInfo("egreso", _columnsEgreso, _foreignKeysEgreso, _indicesEgreso);
        final TableInfo _existingEgreso = TableInfo.read(db, "egreso");
        if (!_infoEgreso.equals(_existingEgreso)) {
          return new RoomOpenHelper.ValidationResult(false, "egreso(com.tallerapp.data.local.EgresoEntity).\n"
                  + " Expected:\n" + _infoEgreso + "\n"
                  + " Found:\n" + _existingEgreso);
        }
        final HashMap<String, TableInfo.Column> _columnsDeuda = new HashMap<String, TableInfo.Column>(8);
        _columnsDeuda.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeuda.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeuda.put("montoCentavos", new TableInfo.Column("montoCentavos", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeuda.put("fecha", new TableInfo.Column("fecha", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeuda.put("nota", new TableInfo.Column("nota", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeuda.put("cobrada", new TableInfo.Column("cobrada", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeuda.put("fechaCobro", new TableInfo.Column("fechaCobro", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeuda.put("fechaRegistro", new TableInfo.Column("fechaRegistro", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeuda = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDeuda = new HashSet<TableInfo.Index>(1);
        _indicesDeuda.add(new TableInfo.Index("index_deuda_cobrada", false, Arrays.asList("cobrada"), Arrays.asList("ASC")));
        final TableInfo _infoDeuda = new TableInfo("deuda", _columnsDeuda, _foreignKeysDeuda, _indicesDeuda);
        final TableInfo _existingDeuda = TableInfo.read(db, "deuda");
        if (!_infoDeuda.equals(_existingDeuda)) {
          return new RoomOpenHelper.ValidationResult(false, "deuda(com.tallerapp.data.local.DeudaEntity).\n"
                  + " Expected:\n" + _infoDeuda + "\n"
                  + " Found:\n" + _existingDeuda);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "614eb1ad7dcfe79611827e7eb03bcefd", "852689cc99d0a275da50488f63ec5ad4");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "ingreso","egreso","deuda");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `ingreso`");
      _db.execSQL("DELETE FROM `egreso`");
      _db.execSQL("DELETE FROM `deuda`");
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
    _typeConvertersMap.put(IngresoDao.class, IngresoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EgresoDao.class, EgresoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DeudaDao.class, DeudaDao_Impl.getRequiredConverters());
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
  public IngresoDao ingresoDao() {
    if (_ingresoDao != null) {
      return _ingresoDao;
    } else {
      synchronized(this) {
        if(_ingresoDao == null) {
          _ingresoDao = new IngresoDao_Impl(this);
        }
        return _ingresoDao;
      }
    }
  }

  @Override
  public EgresoDao egresoDao() {
    if (_egresoDao != null) {
      return _egresoDao;
    } else {
      synchronized(this) {
        if(_egresoDao == null) {
          _egresoDao = new EgresoDao_Impl(this);
        }
        return _egresoDao;
      }
    }
  }

  @Override
  public DeudaDao deudaDao() {
    if (_deudaDao != null) {
      return _deudaDao;
    } else {
      synchronized(this) {
        if(_deudaDao == null) {
          _deudaDao = new DeudaDao_Impl(this);
        }
        return _deudaDao;
      }
    }
  }
}
