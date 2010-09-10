package acz.dbmagic;

import java.util.List;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

public class DemoBeanMapper
{
    @SuppressWarnings({"UnusedDeclaration"})
    public static class Schema
    {
        private int id;
        private String catalog_name;
        private String schema_name;

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public String getCatalog_name()
        {
            return catalog_name;
        }

        public void setCatalog_name(String catalog_name)
        {
            this.catalog_name = catalog_name;
        }

        public String getSchema_name()
        {
            return schema_name;
        }

        public void setSchema_name(String schema_name)
        {
            this.schema_name = schema_name;
        }
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
                        .map(Schema.class)
                        .list();
                for (Schema i : list) {
                    System.out.printf("%s %s %s%n", i.getId(), i.getCatalog_name(), i.getSchema_name());
                }
                return null;
            }
        });
    }
}
