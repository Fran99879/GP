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
public final class TrabajoDao_Impl implements TrabajoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TrabajoEntity> __insertionAdapterOfTrabajoEntity;

  private final EntityDeletionOrUpdateAdapter<TrabajoEntity> __updateAdapterOfTrabajoEntity;

  private final SharedSQLiteStatement __preparedStmtOfEliminar;

  public TrabajoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTrabajoEntity = new EntityInsertionAdapter<TrabajoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `trabajo` (`id`,`cliente`,`telefono`,`patente`,`marca`,`modelo`,`servicio`,`fechaIngreso`,`estadoReparacion`,`estadoCobro`,`problema`,`diagnostico`,`precioCentavos`,`fechaEntrega`,`cobroId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrabajoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCliente());
        if (entity.getTelefono() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTelefono());
        }
        if (entity.getPatente() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPatente());
        }
        statement.bindString(5, entity.getMarca());
        statement.bindString(6, entity.getModelo());
        statement.bindString(7, entity.getServicio());
        statement.bindLong(8, entity.getFechaIngreso());
        statement.bindString(9, entity.getEstadoReparacion());
        statement.bindString(10, entity.getEstadoCobro());
        if (entity.getProblema() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getProblema());
        }
        if (entity.getDiagnostico() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getDiagnostico());
        }
        statement.bindLong(13, entity.getPrecioCentavos());
        if (entity.getFechaEntrega() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getFechaEntrega());
        }
        if (entity.getCobroId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getCobroId());
        }
      }
    };
    this.__updateAdapterOfTrabajoEntity = new EntityDeletionOrUpdateAdapter<TrabajoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `trabajo` SET `id` = ?,`cliente` = ?,`telefono` = ?,`patente` = ?,`marca` = ?,`modelo` = ?,`servicio` = ?,`fechaIngreso` = ?,`estadoReparacion` = ?,`estadoCobro` = ?,`problema` = ?,`diagnostico` = ?,`precioCentavos` = ?,`fechaEntrega` = ?,`cobroId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrabajoEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCliente());
        if (entity.getTelefono() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTelefono());
        }
        if (entity.getPatente() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPatente());
        }
        statement.bindString(5, entity.getMarca());
        statement.bindString(6, entity.getModelo());
        statement.bindString(7, entity.getServicio());
        statement.bindLong(8, entity.getFechaIngreso());
        statement.bindString(9, entity.getEstadoReparacion());
        statement.bindString(10, entity.getEstadoCobro());
        if (entity.getProblema() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getProblema());
        }
        if (entity.getDiagnostico() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getDiagnostico());
        }
        statement.bindLong(13, entity.getPrecioCentavos());
        if (entity.getFechaEntrega() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getFechaEntrega());
        }
        if (entity.getCobroId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getCobroId());
        }
        statement.bindLong(16, entity.getId());
      }
    };
    this.__preparedStmtOfEliminar = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM trabajo WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertar(final TrabajoEntity entity, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTrabajoEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object actualizar(final TrabajoEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTrabajoEntity.handle(entity);
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
  public Flow<List<TrabajoEntity>> observarTodos() {
    final String _sql = "SELECT * FROM trabajo ORDER BY fechaIngreso DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trabajo"}, new Callable<List<TrabajoEntity>>() {
      @Override
      @NonNull
      public List<TrabajoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCliente = CursorUtil.getColumnIndexOrThrow(_cursor, "cliente");
          final int _cursorIndexOfTelefono = CursorUtil.getColumnIndexOrThrow(_cursor, "telefono");
          final int _cursorIndexOfPatente = CursorUtil.getColumnIndexOrThrow(_cursor, "patente");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfServicio = CursorUtil.getColumnIndexOrThrow(_cursor, "servicio");
          final int _cursorIndexOfFechaIngreso = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaIngreso");
          final int _cursorIndexOfEstadoReparacion = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoReparacion");
          final int _cursorIndexOfEstadoCobro = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoCobro");
          final int _cursorIndexOfProblema = CursorUtil.getColumnIndexOrThrow(_cursor, "problema");
          final int _cursorIndexOfDiagnostico = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnostico");
          final int _cursorIndexOfPrecioCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "precioCentavos");
          final int _cursorIndexOfFechaEntrega = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaEntrega");
          final int _cursorIndexOfCobroId = CursorUtil.getColumnIndexOrThrow(_cursor, "cobroId");
          final List<TrabajoEntity> _result = new ArrayList<TrabajoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrabajoEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCliente;
            _tmpCliente = _cursor.getString(_cursorIndexOfCliente);
            final String _tmpTelefono;
            if (_cursor.isNull(_cursorIndexOfTelefono)) {
              _tmpTelefono = null;
            } else {
              _tmpTelefono = _cursor.getString(_cursorIndexOfTelefono);
            }
            final String _tmpPatente;
            if (_cursor.isNull(_cursorIndexOfPatente)) {
              _tmpPatente = null;
            } else {
              _tmpPatente = _cursor.getString(_cursorIndexOfPatente);
            }
            final String _tmpMarca;
            _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            final String _tmpModelo;
            _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            final String _tmpServicio;
            _tmpServicio = _cursor.getString(_cursorIndexOfServicio);
            final long _tmpFechaIngreso;
            _tmpFechaIngreso = _cursor.getLong(_cursorIndexOfFechaIngreso);
            final String _tmpEstadoReparacion;
            _tmpEstadoReparacion = _cursor.getString(_cursorIndexOfEstadoReparacion);
            final String _tmpEstadoCobro;
            _tmpEstadoCobro = _cursor.getString(_cursorIndexOfEstadoCobro);
            final String _tmpProblema;
            if (_cursor.isNull(_cursorIndexOfProblema)) {
              _tmpProblema = null;
            } else {
              _tmpProblema = _cursor.getString(_cursorIndexOfProblema);
            }
            final String _tmpDiagnostico;
            if (_cursor.isNull(_cursorIndexOfDiagnostico)) {
              _tmpDiagnostico = null;
            } else {
              _tmpDiagnostico = _cursor.getString(_cursorIndexOfDiagnostico);
            }
            final long _tmpPrecioCentavos;
            _tmpPrecioCentavos = _cursor.getLong(_cursorIndexOfPrecioCentavos);
            final Long _tmpFechaEntrega;
            if (_cursor.isNull(_cursorIndexOfFechaEntrega)) {
              _tmpFechaEntrega = null;
            } else {
              _tmpFechaEntrega = _cursor.getLong(_cursorIndexOfFechaEntrega);
            }
            final Long _tmpCobroId;
            if (_cursor.isNull(_cursorIndexOfCobroId)) {
              _tmpCobroId = null;
            } else {
              _tmpCobroId = _cursor.getLong(_cursorIndexOfCobroId);
            }
            _item = new TrabajoEntity(_tmpId,_tmpCliente,_tmpTelefono,_tmpPatente,_tmpMarca,_tmpModelo,_tmpServicio,_tmpFechaIngreso,_tmpEstadoReparacion,_tmpEstadoCobro,_tmpProblema,_tmpDiagnostico,_tmpPrecioCentavos,_tmpFechaEntrega,_tmpCobroId);
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
  public Flow<List<TrabajoEntity>> buscar(final String query) {
    final String _sql = "\n"
            + "        SELECT * FROM trabajo\n"
            + "        WHERE patente LIKE '%' || ? || '%'\n"
            + "           OR cliente LIKE '%' || ? || '%'\n"
            + "        ORDER BY fechaIngreso DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"trabajo"}, new Callable<List<TrabajoEntity>>() {
      @Override
      @NonNull
      public List<TrabajoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCliente = CursorUtil.getColumnIndexOrThrow(_cursor, "cliente");
          final int _cursorIndexOfTelefono = CursorUtil.getColumnIndexOrThrow(_cursor, "telefono");
          final int _cursorIndexOfPatente = CursorUtil.getColumnIndexOrThrow(_cursor, "patente");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfServicio = CursorUtil.getColumnIndexOrThrow(_cursor, "servicio");
          final int _cursorIndexOfFechaIngreso = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaIngreso");
          final int _cursorIndexOfEstadoReparacion = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoReparacion");
          final int _cursorIndexOfEstadoCobro = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoCobro");
          final int _cursorIndexOfProblema = CursorUtil.getColumnIndexOrThrow(_cursor, "problema");
          final int _cursorIndexOfDiagnostico = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnostico");
          final int _cursorIndexOfPrecioCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "precioCentavos");
          final int _cursorIndexOfFechaEntrega = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaEntrega");
          final int _cursorIndexOfCobroId = CursorUtil.getColumnIndexOrThrow(_cursor, "cobroId");
          final List<TrabajoEntity> _result = new ArrayList<TrabajoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrabajoEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCliente;
            _tmpCliente = _cursor.getString(_cursorIndexOfCliente);
            final String _tmpTelefono;
            if (_cursor.isNull(_cursorIndexOfTelefono)) {
              _tmpTelefono = null;
            } else {
              _tmpTelefono = _cursor.getString(_cursorIndexOfTelefono);
            }
            final String _tmpPatente;
            if (_cursor.isNull(_cursorIndexOfPatente)) {
              _tmpPatente = null;
            } else {
              _tmpPatente = _cursor.getString(_cursorIndexOfPatente);
            }
            final String _tmpMarca;
            _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            final String _tmpModelo;
            _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            final String _tmpServicio;
            _tmpServicio = _cursor.getString(_cursorIndexOfServicio);
            final long _tmpFechaIngreso;
            _tmpFechaIngreso = _cursor.getLong(_cursorIndexOfFechaIngreso);
            final String _tmpEstadoReparacion;
            _tmpEstadoReparacion = _cursor.getString(_cursorIndexOfEstadoReparacion);
            final String _tmpEstadoCobro;
            _tmpEstadoCobro = _cursor.getString(_cursorIndexOfEstadoCobro);
            final String _tmpProblema;
            if (_cursor.isNull(_cursorIndexOfProblema)) {
              _tmpProblema = null;
            } else {
              _tmpProblema = _cursor.getString(_cursorIndexOfProblema);
            }
            final String _tmpDiagnostico;
            if (_cursor.isNull(_cursorIndexOfDiagnostico)) {
              _tmpDiagnostico = null;
            } else {
              _tmpDiagnostico = _cursor.getString(_cursorIndexOfDiagnostico);
            }
            final long _tmpPrecioCentavos;
            _tmpPrecioCentavos = _cursor.getLong(_cursorIndexOfPrecioCentavos);
            final Long _tmpFechaEntrega;
            if (_cursor.isNull(_cursorIndexOfFechaEntrega)) {
              _tmpFechaEntrega = null;
            } else {
              _tmpFechaEntrega = _cursor.getLong(_cursorIndexOfFechaEntrega);
            }
            final Long _tmpCobroId;
            if (_cursor.isNull(_cursorIndexOfCobroId)) {
              _tmpCobroId = null;
            } else {
              _tmpCobroId = _cursor.getLong(_cursorIndexOfCobroId);
            }
            _item = new TrabajoEntity(_tmpId,_tmpCliente,_tmpTelefono,_tmpPatente,_tmpMarca,_tmpModelo,_tmpServicio,_tmpFechaIngreso,_tmpEstadoReparacion,_tmpEstadoCobro,_tmpProblema,_tmpDiagnostico,_tmpPrecioCentavos,_tmpFechaEntrega,_tmpCobroId);
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
  public Object obtener(final long id, final Continuation<? super TrabajoEntity> $completion) {
    final String _sql = "SELECT * FROM trabajo WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TrabajoEntity>() {
      @Override
      @Nullable
      public TrabajoEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCliente = CursorUtil.getColumnIndexOrThrow(_cursor, "cliente");
          final int _cursorIndexOfTelefono = CursorUtil.getColumnIndexOrThrow(_cursor, "telefono");
          final int _cursorIndexOfPatente = CursorUtil.getColumnIndexOrThrow(_cursor, "patente");
          final int _cursorIndexOfMarca = CursorUtil.getColumnIndexOrThrow(_cursor, "marca");
          final int _cursorIndexOfModelo = CursorUtil.getColumnIndexOrThrow(_cursor, "modelo");
          final int _cursorIndexOfServicio = CursorUtil.getColumnIndexOrThrow(_cursor, "servicio");
          final int _cursorIndexOfFechaIngreso = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaIngreso");
          final int _cursorIndexOfEstadoReparacion = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoReparacion");
          final int _cursorIndexOfEstadoCobro = CursorUtil.getColumnIndexOrThrow(_cursor, "estadoCobro");
          final int _cursorIndexOfProblema = CursorUtil.getColumnIndexOrThrow(_cursor, "problema");
          final int _cursorIndexOfDiagnostico = CursorUtil.getColumnIndexOrThrow(_cursor, "diagnostico");
          final int _cursorIndexOfPrecioCentavos = CursorUtil.getColumnIndexOrThrow(_cursor, "precioCentavos");
          final int _cursorIndexOfFechaEntrega = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaEntrega");
          final int _cursorIndexOfCobroId = CursorUtil.getColumnIndexOrThrow(_cursor, "cobroId");
          final TrabajoEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCliente;
            _tmpCliente = _cursor.getString(_cursorIndexOfCliente);
            final String _tmpTelefono;
            if (_cursor.isNull(_cursorIndexOfTelefono)) {
              _tmpTelefono = null;
            } else {
              _tmpTelefono = _cursor.getString(_cursorIndexOfTelefono);
            }
            final String _tmpPatente;
            if (_cursor.isNull(_cursorIndexOfPatente)) {
              _tmpPatente = null;
            } else {
              _tmpPatente = _cursor.getString(_cursorIndexOfPatente);
            }
            final String _tmpMarca;
            _tmpMarca = _cursor.getString(_cursorIndexOfMarca);
            final String _tmpModelo;
            _tmpModelo = _cursor.getString(_cursorIndexOfModelo);
            final String _tmpServicio;
            _tmpServicio = _cursor.getString(_cursorIndexOfServicio);
            final long _tmpFechaIngreso;
            _tmpFechaIngreso = _cursor.getLong(_cursorIndexOfFechaIngreso);
            final String _tmpEstadoReparacion;
            _tmpEstadoReparacion = _cursor.getString(_cursorIndexOfEstadoReparacion);
            final String _tmpEstadoCobro;
            _tmpEstadoCobro = _cursor.getString(_cursorIndexOfEstadoCobro);
            final String _tmpProblema;
            if (_cursor.isNull(_cursorIndexOfProblema)) {
              _tmpProblema = null;
            } else {
              _tmpProblema = _cursor.getString(_cursorIndexOfProblema);
            }
            final String _tmpDiagnostico;
            if (_cursor.isNull(_cursorIndexOfDiagnostico)) {
              _tmpDiagnostico = null;
            } else {
              _tmpDiagnostico = _cursor.getString(_cursorIndexOfDiagnostico);
            }
            final long _tmpPrecioCentavos;
            _tmpPrecioCentavos = _cursor.getLong(_cursorIndexOfPrecioCentavos);
            final Long _tmpFechaEntrega;
            if (_cursor.isNull(_cursorIndexOfFechaEntrega)) {
              _tmpFechaEntrega = null;
            } else {
              _tmpFechaEntrega = _cursor.getLong(_cursorIndexOfFechaEntrega);
            }
            final Long _tmpCobroId;
            if (_cursor.isNull(_cursorIndexOfCobroId)) {
              _tmpCobroId = null;
            } else {
              _tmpCobroId = _cursor.getLong(_cursorIndexOfCobroId);
            }
            _result = new TrabajoEntity(_tmpId,_tmpCliente,_tmpTelefono,_tmpPatente,_tmpMarca,_tmpModelo,_tmpServicio,_tmpFechaIngreso,_tmpEstadoReparacion,_tmpEstadoCobro,_tmpProblema,_tmpDiagnostico,_tmpPrecioCentavos,_tmpFechaEntrega,_tmpCobroId);
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
