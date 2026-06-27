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

  private volatile IngresoDao _ingresoDao;

  private volatile EgresoDao _egresoDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `trabajo` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cliente` TEXT NOT NULL, `telefono` TEXT, `patente` TEXT, `marca` TEXT NOT NULL, `modelo` TEXT NOT NULL, `servicio` TEXT NOT NULL, `fechaIngreso` INTEGER NOT NULL, `estadoReparacion` TEXT NOT NULL, `estadoCobro` TEXT NOT NULL, `problema` TEXT, `diagnostico` TEXT, `precioCentavos` INTEGER NOT NULL, `fechaEntrega` INTEGER, `cobroId` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_estadoReparacion` ON `trabajo` (`estadoReparacion`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_patente` ON `trabajo` (`patente`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_cliente` ON `trabajo` (`cliente`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `ingreso` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `montoCentavos` INTEGER NOT NULL, `concepto` TEXT NOT NULL, `metodo` TEXT NOT NULL, `repEfectivo` INTEGER, `repTransferencia` INTEGER, `repTarjeta` INTEGER, `repMercadoPago` INTEGER, `fecha` INTEGER NOT NULL, `fechaRegistro` INTEGER NOT NULL, `origen` TEXT NOT NULL, `trabajoId` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ingreso_fecha` ON `ingreso` (`fecha`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `egreso` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `montoCentavos` INTEGER NOT NULL, `categoria` TEXT NOT NULL, `concepto` TEXT NOT NULL, `fecha` INTEGER NOT NULL, `fechaRegistro` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_egreso_fecha` ON `egreso` (`fecha`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '852aaf15c771a7336df4e9b345fd200d')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `trabajo`");
        db.execSQL("DROP TABLE IF EXISTS `ingreso`");
        db.execSQL("DROP TABLE IF EXISTS `egreso`");
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
        final HashMap<String, TableInfo.Column> _columnsTrabajo = new HashMap<String, TableInfo.Column>(15);
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
        _columnsTrabajo.put("fechaEntrega", new TableInfo.Column("fechaEntrega", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
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
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "852aaf15c771a7336df4e9b345fd200d", "f53e0b1d097c3da819747c6e7e0b5db9");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "trabajo","ingreso","egreso");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `trabajo`");
      _db.execSQL("DELETE FROM `ingreso`");
      _db.execSQL("DELETE FROM `egreso`");
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
    _typeConvertersMap.put(IngresoDao.class, IngresoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EgresoDao.class, EgresoDao_Impl.getRequiredConverters());
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
}
