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
public final class IngresoDao_Impl implements IngresoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<IngresoEntity> __insertionAdapterOfIngresoEntity;

  private final EntityDeletionOrUpdateAdapter<IngresoEntity> __updateAdapterOfIngresoEntity;

  private final SharedSQLiteStatement __preparedStmtOfEliminar;

  public IngresoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfIngresoEntity = new EntityInsertionAdapter<IngresoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `ingreso` (`id`,`montoCentavos`,`concepto`,`metodo`,`repEfectivo`,`repTransferencia`,`repTarjeta`,`repMercadoPago`,`fecha`,`fechaRegistro`,`origen`,`trabajoId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final IngresoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMontoCentavos());
        statement.bindString(3, entity.getConcepto());
        statement.bindString(4, entity.getMetodo());
        if (entity.getRepEfectivo() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getRepEfectivo());
        }
        if (entity.getRepTransferencia() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getRepTransferencia());
        }
        if (entity.getRepTarjeta() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getRepTarjeta());
        }
        if (entity.getRepMercadoPago() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getRepMercadoPago());
        }
        statement.bindLong(9, entity.getFecha());
        statement.bindLong(10, entity.getFechaRegistro());
        statement.bindString(11, entity.getOrigen());
        if (entity.getTrabajoId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getTrabajoId());
        }
      }
    };
    this.__updateAdapterOfIngresoEntity = new EntityDeletionOrUpdateAdapter<IngresoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `ingreso` SET `id` = ?,`montoCentavos` = ?,`concepto` = ?,`metodo` = ?,`repEfectivo` = ?,`repTransferencia` = ?,`repTarjeta` = ?,`repMercadoPago` = ?,`fecha` = ?,`fechaRegistro` = ?,`origen` = ?,`trabajoId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final IngresoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMontoCentavos());
        statement.bindString(3, entity.getConcepto());
        statement.bindString(4, entity.getMetodo());
        if (entity.getRepEfectivo() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getRepEfectivo());
        }
        if (entity.getRepTransferencia() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getRepTransferencia());
        }
        if (entity.getRepTarjeta() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getRepTarjeta());
        }
        if (entity.getRepMercadoPago() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getRepMercadoPago());
        }
        statement.bindLong(9, entity.getFecha());
        statement.bindLong(10, entity.getFechaRegistro());
        statement.bindString(11, entity.getOrigen());
        if (entity.getTrabajoId() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getTrabajoId());
        }
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfEliminar = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ingreso WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertar(final IngresoEntity entity, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfIngresoEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object actualizar(final IngresoEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfIngresoEntity.handle(entity);
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
  public Flow<List<IngresoEntity>> observarRango(final long inicio, final long fin) {
    final String _sql = "SELECT * FROM ingreso WHERE fecha >= ? AND fecha < ? ORDER BY fechaRegistro DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, inicio);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fin);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ingreso"}, new Callable<List<IngresoEntity>>() {
      @Override
      @NonNull
      public List<IngresoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMontoCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "montoCentavos");
          final int _cursorIndexOfConcepto = CursorUtil.getColumnIndexOrThrow(_cursor, "concepto");
          final int _cursorIndexOfMetodo = CursorUtil.getColumnIndexOrThrow(_cursor, "metodo");
          final int _cursorIndexOfRepEfectivo = CursorUtil.getColumnIndexOrThrow(_cursor, "repEfectivo");
          final int _cursorIndexOfRepTransferencia = CursorUtil.getColumnIndexOrThrow(_cursor, "repTransferencia");
          final int _cursorIndexOfRepTarjeta = CursorUtil.getColumnIndexOrThrow(_cursor, "repTarjeta");
          final int _cursorIndexOfRepMercadoPago = CursorUtil.getColumnIndexOrThrow(_cursor, "repMercadoPago");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfFechaRegistro = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaRegistro");
          final int _cursorIndexOfOrigen = CursorUtil.getColumnIndexOrThrow(_cursor, "origen");
          final int _cursorIndexOfTrabajoId = CursorUtil.getColumnIndexOrThrow(_cursor, "trabajoId");
          final List<IngresoEntity> _result = new ArrayList<IngresoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final IngresoEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMontoCentavos;
            _tmpMontoCentavos = _cursor.getLong(_cursorIndexOfMontoCentavos);
            final String _tmpConcepto;
            _tmpConcepto = _cursor.getString(_cursorIndexOfConcepto);
            final String _tmpMetodo;
            _tmpMetodo = _cursor.getString(_cursorIndexOfMetodo);
            final Long _tmpRepEfectivo;
            if (_cursor.isNull(_cursorIndexOfRepEfectivo)) {
              _tmpRepEfectivo = null;
            } else {
              _tmpRepEfectivo = _cursor.getLong(_cursorIndexOfRepEfectivo);
            }
            final Long _tmpRepTransferencia;
            if (_cursor.isNull(_cursorIndexOfRepTransferencia)) {
              _tmpRepTransferencia = null;
            } else {
              _tmpRepTransferencia = _cursor.getLong(_cursorIndexOfRepTransferencia);
            }
            final Long _tmpRepTarjeta;
            if (_cursor.isNull(_cursorIndexOfRepTarjeta)) {
              _tmpRepTarjeta = null;
            } else {
              _tmpRepTarjeta = _cursor.getLong(_cursorIndexOfRepTarjeta);
            }
            final Long _tmpRepMercadoPago;
            if (_cursor.isNull(_cursorIndexOfRepMercadoPago)) {
              _tmpRepMercadoPago = null;
            } else {
              _tmpRepMercadoPago = _cursor.getLong(_cursorIndexOfRepMercadoPago);
            }
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final long _tmpFechaRegistro;
            _tmpFechaRegistro = _cursor.getLong(_cursorIndexOfFechaRegistro);
            final String _tmpOrigen;
            _tmpOrigen = _cursor.getString(_cursorIndexOfOrigen);
            final Long _tmpTrabajoId;
            if (_cursor.isNull(_cursorIndexOfTrabajoId)) {
              _tmpTrabajoId = null;
            } else {
              _tmpTrabajoId = _cursor.getLong(_cursorIndexOfTrabajoId);
            }
            _item = new IngresoEntity(_tmpId,_tmpMontoCentavos,_tmpConcepto,_tmpMetodo,_tmpRepEfectivo,_tmpRepTransferencia,_tmpRepTarjeta,_tmpRepMercadoPago,_tmpFecha,_tmpFechaRegistro,_tmpOrigen,_tmpTrabajoId);
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
    final String _sql = "SELECT COALESCE(SUM(montoCentavos), 0) FROM ingreso WHERE fecha >= ? AND fecha < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, inicio);
    _argIndex = 2;
    _statement.bindLong(_argIndex, fin);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ingreso"}, new Callable<Long>() {
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
  public Object obtener(final long id, final Continuation<? super IngresoEntity> $completion) {
    final String _sql = "SELECT * FROM ingreso WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<IngresoEntity>() {
      @Override
      @Nullable
      public IngresoEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMontoCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "montoCentavos");
          final int _cursorIndexOfConcepto = CursorUtil.getColumnIndexOrThrow(_cursor, "concepto");
          final int _cursorIndexOfMetodo = CursorUtil.getColumnIndexOrThrow(_cursor, "metodo");
          final int _cursorIndexOfRepEfectivo = CursorUtil.getColumnIndexOrThrow(_cursor, "repEfectivo");
          final int _cursorIndexOfRepTransferencia = CursorUtil.getColumnIndexOrThrow(_cursor, "repTransferencia");
          final int _cursorIndexOfRepTarjeta = CursorUtil.getColumnIndexOrThrow(_cursor, "repTarjeta");
          final int _cursorIndexOfRepMercadoPago = CursorUtil.getColumnIndexOrThrow(_cursor, "repMercadoPago");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfFechaRegistro = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaRegistro");
          final int _cursorIndexOfOrigen = CursorUtil.getColumnIndexOrThrow(_cursor, "origen");
          final int _cursorIndexOfTrabajoId = CursorUtil.getColumnIndexOrThrow(_cursor, "trabajoId");
          final IngresoEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMontoCentavos;
            _tmpMontoCentavos = _cursor.getLong(_cursorIndexOfMontoCentavos);
            final String _tmpConcepto;
            _tmpConcepto = _cursor.getString(_cursorIndexOfConcepto);
            final String _tmpMetodo;
            _tmpMetodo = _cursor.getString(_cursorIndexOfMetodo);
            final Long _tmpRepEfectivo;
            if (_cursor.isNull(_cursorIndexOfRepEfectivo)) {
              _tmpRepEfectivo = null;
            } else {
              _tmpRepEfectivo = _cursor.getLong(_cursorIndexOfRepEfectivo);
            }
            final Long _tmpRepTransferencia;
            if (_cursor.isNull(_cursorIndexOfRepTransferencia)) {
              _tmpRepTransferencia = null;
            } else {
              _tmpRepTransferencia = _cursor.getLong(_cursorIndexOfRepTransferencia);
            }
            final Long _tmpRepTarjeta;
            if (_cursor.isNull(_cursorIndexOfRepTarjeta)) {
              _tmpRepTarjeta = null;
            } else {
              _tmpRepTarjeta = _cursor.getLong(_cursorIndexOfRepTarjeta);
            }
            final Long _tmpRepMercadoPago;
            if (_cursor.isNull(_cursorIndexOfRepMercadoPago)) {
              _tmpRepMercadoPago = null;
            } else {
              _tmpRepMercadoPago = _cursor.getLong(_cursorIndexOfRepMercadoPago);
            }
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final long _tmpFechaRegistro;
            _tmpFechaRegistro = _cursor.getLong(_cursorIndexOfFechaRegistro);
            final String _tmpOrigen;
            _tmpOrigen = _cursor.getString(_cursorIndexOfOrigen);
            final Long _tmpTrabajoId;
            if (_cursor.isNull(_cursorIndexOfTrabajoId)) {
              _tmpTrabajoId = null;
            } else {
              _tmpTrabajoId = _cursor.getLong(_cursorIndexOfTrabajoId);
            }
            _result = new IngresoEntity(_tmpId,_tmpMontoCentavos,_tmpConcepto,_tmpMetodo,_tmpRepEfectivo,_tmpRepTransferencia,_tmpRepTarjeta,_tmpRepMercadoPago,_tmpFecha,_tmpFechaRegistro,_tmpOrigen,_tmpTrabajoId);
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
