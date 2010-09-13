package acz.dbmagic;

import java.sql.SQLException;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.LongMapper;

public class DemoGeneratedKeys
{
    public static void main(String[] args) throws SQLException
    {
        DBI dbi = new DBI("jdbc:h2:mem:test");

        dbi.withHandle(new HandleCallback<Void>()
        {
            @Override
            public Void withHandle(Handle handle) throws Exception
            {
                handle.execute("create sequence foo_seq start with 123");
                handle.execute("create table foo (foo_id int)");

                GeneratedKeysCustomizer<Long> gk = GeneratedKeysCustomizer.forMapper(LongMapper.FIRST);
                handle.setStatementBuilder(new GeneratedKeysStatementBuilder("foo_id"));
                handle.createStatement("insert into foo values (foo_seq.nextval)")
                    .addStatementCustomizer(gk)
                    .execute();
                System.out.println(gk.getFirst());

                return null;
            }
        });
    }
}
