# Example

Create an interface for your result object:

    public interface Schema
    {
        int getId();

        String getCatalogName();

        String getSchemaName();
    }

Use the mapper to generate the result object from the query:

    handle.createQuery("select * from information_schema.schemata")
        .map(MagicMapper.forClass(Schema.class))
        .list();
