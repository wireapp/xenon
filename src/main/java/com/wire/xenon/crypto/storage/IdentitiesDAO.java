package com.wire.xenon.crypto.storage;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IdentitiesDAO {
    @SqlUpdate("INSERT INTO Identities (id, data) " +
            "VALUES (:id, :data) " +
            "ON CONFLICT (id) DO UPDATE SET data = EXCLUDED.data")
    int insert(@Bind("id") String id,
               @Bind("data") byte[] data);

    @SqlQuery("SELECT data FROM Identities WHERE id = :id")
    byte[] get(@Bind("id") String id);

    @SqlUpdate("DELETE FROM Identities WHERE id = :id")
    int delete(@Bind("id") String id);

    class _Mapper implements ColumnMapper<byte[]> {
        @Override
        public byte[] map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
            return r.getBytes("data");
        }
    }
}
