package acz.dbmagic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.BaseStatementCustomizer;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class GeneratedKeysCustomizer<ResultType> extends BaseStatementCustomizer
{
    private final ResultSetMapper<ResultType> mapper;
    private List<ResultType> results;

    private GeneratedKeysCustomizer(ResultSetMapper<ResultType> mapper)
    {
        this.mapper = mapper;
    }

    @Override
    public void afterExecution(PreparedStatement stmt, StatementContext ctx) throws SQLException
    {
        ResultSet rs = stmt.getGeneratedKeys();
        results = new ArrayList<ResultType>();
        int i = 0;
        while (rs.next()) {
            results.add(mapper.map(i++, rs, ctx));
        }
        rs.close();
    }

    public List<ResultType> getAll()
    {
        return results;
    }

    public ResultType getFirst()
    {
        return results.get(0);
    }

    public static <ResultType> GeneratedKeysCustomizer<ResultType> forMapper(ResultSetMapper<ResultType> mapper)
    {
        return new GeneratedKeysCustomizer<ResultType>(mapper);
    }
}
