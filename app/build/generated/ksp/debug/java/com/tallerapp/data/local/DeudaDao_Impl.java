package com.tallerapp.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DeudaDao_Impl implements DeudaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DeudaEntity> __insertionAdapterOfDeudaEntity;

  private final EntityDeletionOrUpdateAdapter<DeudaEntity> __updateAdapterOfDeudaEntity;

  private final SharedSQLiteStatement __preparedStmtOfEliminar;

  public DeudaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDeudaEntity = new EntityInsertionAdapter<DeudaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `deuda` (`id`,`nombre`,`montoCentavos`,`fecha`,`nota`,`cobrada`,`fechaCobro`,`fechaRegistro`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeudaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNombre());
        statement.bindLong(3, entity.getMontoCentavos());
        statement.bindLong(4, entity.getFecha());
        statement.bindString(5, entity.getNota());
        final int _tmp = entity.getCobrada() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getFechaCobro() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getFechaCobro());
        }
        statement.bindLong(8, entity.getFechaRegistro());
      }
    };
    this.__updateAdapterOfDeudaEntity = new EntityDeletionOrUpdateAdapter<DeudaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `deuda` SET `id` = ?,`nombre` = ?,`montoCentavos` = ?,`fecha` = ?,`nota` = ?,`cobrada` = ?,`fechaCobro` = ?,`fechaRegistro` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeudaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNombre());
        statement.bindLong(3, entity.getMontoCentavos());
        statement.bindLong(4, entity.getFecha());
        statement.bindString(5, entity.getNota());
        final int _tmp = entity.getCobrada() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getFechaCobro() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getFechaCobro());
        }
        statement.bindLong(8, entity.getFechaRegistro());
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfEliminar = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM deuda WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertar(final DeudaEntity entity, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfDeudaEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object actualizar(final DeudaEntity entity, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDeudaEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object eliminar(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfEliminar.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfEliminar.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DeudaEntity>> observarTodas() {
    final String _sql = "SELECT * FROM deuda ORDER BY cobrada ASC, fecha DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"deuda"}, new Callable<List<DeudaEntity>>() {
      @Override
      @NonNull
      public List<DeudaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfMontoCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "montoCentavos");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfNota = CursorUtil.getColumnIndexOrThrow(_cursor, "nota");
          final int _cursorIndexOfCobrada = CursorUtil.getColumnIndexOrThrow(_cursor, "cobrada");
          final int _cursorIndexOfFechaCobro = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCobro");
          final int _cursorIndexOfFechaRegistro = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaRegistro");
          final List<DeudaEntity> _result = new ArrayList<DeudaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeudaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final long _tmpMontoCentavos;
            _tmpMontoCentavos = _cursor.getLong(_cursorIndexOfMontoCentavos);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final String _tmpNota;
            _tmpNota = _cursor.getString(_cursorIndexOfNota);
            final boolean _tmpCobrada;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCobrada);
            _tmpCobrada = _tmp != 0;
            final Long _tmpFechaCobro;
            if (_cursor.isNull(_cursorIndexOfFechaCobro)) {
              _tmpFechaCobro = null;
            } else {
              _tmpFechaCobro = _cursor.getLong(_cursorIndexOfFechaCobro);
            }
            final long _tmpFechaRegistro;
            _tmpFechaRegistro = _cursor.getLong(_cursorIndexOfFechaRegistro);
            _item = new DeudaEntity(_tmpId,_tmpNombre,_tmpMontoCentavos,_tmpFecha,_tmpNota,_tmpCobrada,_tmpFechaCobro,_tmpFechaRegistro);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Long> sumaPendiente() {
    final String _sql = "SELECT COALESCE(SUM(montoCentavos), 0) FROM deuda WHERE cobrada = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"deuda"}, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> contarPendientes() {
    final String _sql = "SELECT COUNT(*) FROM deuda WHERE cobrada = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"deuda"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object obtener(final long id, final Continuation<? super DeudaEntity> $completion) {
    final String _sql = "SELECT * FROM deuda WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DeudaEntity>() {
      @Override
      @Nullable
      public DeudaEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfMontoCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "montoCentavos");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfNota = CursorUtil.getColumnIndexOrThrow(_cursor, "nota");
          final int _cursorIndexOfCobrada = CursorUtil.getColumnIndexOrThrow(_cursor, "cobrada");
          final int _cursorIndexOfFechaCobro = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCobro");
          final int _cursorIndexOfFechaRegistro = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaRegistro");
          final DeudaEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final long _tmpMontoCentavos;
            _tmpMontoCentavos = _cursor.getLong(_cursorIndexOfMontoCentavos);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final String _tmpNota;
            _tmpNota = _cursor.getString(_cursorIndexOfNota);
            final boolean _tmpCobrada;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCobrada);
            _tmpCobrada = _tmp != 0;
            final Long _tmpFechaCobro;
            if (_cursor.isNull(_cursorIndexOfFechaCobro)) {
              _tmpFechaCobro = null;
            } else {
              _tmpFechaCobro = _cursor.getLong(_cursorIndexOfFechaCobro);
            }
            final long _tmpFechaRegistro;
            _tmpFechaRegistro = _cursor.getLong(_cursorIndexOfFechaRegistro);
            _result = new DeudaEntity(_tmpId,_tmpNombre,_tmpMontoCentavos,_tmpFecha,_tmpNota,_tmpCobrada,_tmpFechaCobro,_tmpFechaRegistro);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
