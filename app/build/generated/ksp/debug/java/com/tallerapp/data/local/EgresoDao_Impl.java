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
public final class EgresoDao_Impl implements EgresoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EgresoEntity> __insertionAdapterOfEgresoEntity;

  private final EntityDeletionOrUpdateAdapter<EgresoEntity> __updateAdapterOfEgresoEntity;

  private final SharedSQLiteStatement __preparedStmtOfEliminar;

  public EgresoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEgresoEntity = new EntityInsertionAdapter<EgresoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `egreso` (`id`,`montoCentavos`,`categoria`,`concepto`,`fecha`,`fechaRegistro`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EgresoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMontoCentavos());
        statement.bindString(3, entity.getCategoria());
        statement.bindString(4, entity.getConcepto());
        statement.bindLong(5, entity.getFecha());
        statement.bindLong(6, entity.getFechaRegistro());
      }
    };
    this.__updateAdapterOfEgresoEntity = new EntityDeletionOrUpdateAdapter<EgresoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `egreso` SET `id` = ?,`montoCentavos` = ?,`categoria` = ?,`concepto` = ?,`fecha` = ?,`fechaRegistro` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EgresoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMontoCentavos());
        statement.bindString(3, entity.getCategoria());
        statement.bindString(4, entity.getConcepto());
        statement.bindLong(5, entity.getFecha());
        statement.bindLong(6, entity.getFechaRegistro());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfEliminar = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM egreso WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertar(final EgresoEntity entity, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfEgresoEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object actualizar(final EgresoEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEgresoEntity.handle(entity);
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
  public Flow<List<EgresoEntity>> observarRango(final long inicio, final long fin) {
    final String _sql = "SELECT * FROM egreso WHERE fecha >= ? AND fecha < ? ORDER BY fechaRegistro DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, inicio);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fin);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"egreso"}, new Callable<List<EgresoEntity>>() {
      @Override
      @NonNull
      public List<EgresoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMontoCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "montoCentavos");
          final int _cursorIndexOfCategoria = CursorUtil.getColumnIndexOrThrow(_cursor, "categoria");
          final int _cursorIndexOfConcepto = CursorUtil.getColumnIndexOrThrow(_cursor, "concepto");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfFechaRegistro = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaRegistro");
          final List<EgresoEntity> _result = new ArrayList<EgresoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EgresoEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMontoCentavos;
            _tmpMontoCentavos = _cursor.getLong(_cursorIndexOfMontoCentavos);
            final String _tmpCategoria;
            _tmpCategoria = _cursor.getString(_cursorIndexOfCategoria);
            final String _tmpConcepto;
            _tmpConcepto = _cursor.getString(_cursorIndexOfConcepto);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final long _tmpFechaRegistro;
            _tmpFechaRegistro = _cursor.getLong(_cursorIndexOfFechaRegistro);
            _item = new EgresoEntity(_tmpId,_tmpMontoCentavos,_tmpCategoria,_tmpConcepto,_tmpFecha,_tmpFechaRegistro);
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
  public Flow<Long> sumaRango(final long inicio, final long fin) {
    final String _sql = "SELECT COALESCE(SUM(montoCentavos), 0) FROM egreso WHERE fecha >= ? AND fecha < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, inicio);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fin);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"egreso"}, new Callable<Long>() {
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
  public Object obtener(final long id, final Continuation<? super EgresoEntity> $completion) {
    final String _sql = "SELECT * FROM egreso WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EgresoEntity>() {
      @Override
      @Nullable
      public EgresoEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMontoCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "montoCentavos");
          final int _cursorIndexOfCategoria = CursorUtil.getColumnIndexOrThrow(_cursor, "categoria");
          final int _cursorIndexOfConcepto = CursorUtil.getColumnIndexOrThrow(_cursor, "concepto");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfFechaRegistro = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaRegistro");
          final EgresoEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMontoCentavos;
            _tmpMontoCentavos = _cursor.getLong(_cursorIndexOfMontoCentavos);
            final String _tmpCategoria;
            _tmpCategoria = _cursor.getString(_cursorIndexOfCategoria);
            final String _tmpConcepto;
            _tmpConcepto = _cursor.getString(_cursorIndexOfConcepto);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final long _tmpFechaRegistro;
            _tmpFechaRegistro = _cursor.getLong(_cursorIndexOfFechaRegistro);
            _result = new EgresoEntity(_tmpId,_tmpMontoCentavos,_tmpCategoria,_tmpConcepto,_tmpFecha,_tmpFechaRegistro);
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
