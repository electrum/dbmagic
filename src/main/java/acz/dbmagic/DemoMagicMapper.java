package acz.dbmagic;

import java.util.List;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

public class DemoMagicMapper
{
    public interface Schema
    {
        int getId();

        String getCatalogName();

        String getSchemaName();
    }

    public static void main(String[] args)
    {
        DBI dbi = new DBI("jdbc:h2:mem:test");

        dbi.withHandle(new HandleCallback<Void>()
        {
            @Override
            public Void withHandle(Handle handle) throws Exception
            {
                List<Schema> list =
                    handle.createQuery("select * from information_schema.schemata")
                        .map(MagicMapper.forClass(Schema.class))
                        .list();
                for (Schema i : list) {
                    System.out.printf("%s %s %s%n", i.getId(), i.getCatalogName(), i.getSchemaName());
                }
                return null;
            }
        });
    }
}
