package acz.dbmagic;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.StatementBuilder;

public class GeneratedKeysStatementBuilder implements StatementBuilder
{
    private final String[] columnNames;

    public GeneratedKeysStatementBuilder(String... columnNames)
    {
        this.columnNames = columnNames;
    }

    @Override
    public PreparedStatement create(Connection conn, String sql, StatementContext ctx) throws SQLException
    {
        return conn.prepareStatement(sql, columnNames);
    }

    @Override
    public CallableStatement createCall(Connection conn, String sql, StatementContext ctx) throws SQLException
    {
        return conn.prepareCall(sql);
    }

    @Override
    public void close(Connection conn, String sql, Statement stmt) throws SQLException
    {
        if (stmt != null) {
            stmt.close();
        }
    }

    @Override
    public void close(Connection conn)
    {
        // no-op
    }
}
