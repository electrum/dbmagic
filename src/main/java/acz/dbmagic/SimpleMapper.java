package acz.dbmagic;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class SimpleMapper<T> implements ResultSetMapper<T>
{
    private final Class<T> type;
    private final boolean requireAll;
    private final Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();

    public static <T> SimpleMapper<T> forClass(Class<T> type)
    {
        return new SimpleMapper<T>(type, true);
    }

    public static <T> SimpleMapper<T> forPartialClass(Class<T> type)
    {
        return new SimpleMapper<T>(type, false);
    }

    private SimpleMapper(Class<T> type, boolean requireAll)
    {
        this.type = type;
        this.requireAll = requireAll;
        try {
            BeanInfo info = Introspector.getBeanInfo(type, type.getSuperclass());
            for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
                if (descriptor.getReadMethod() == null) {
                    throw new IllegalArgumentException("No read method: " + descriptor.getName());
                }
                if (descriptor.getWriteMethod() == null) {
                    throw new IllegalArgumentException("No write method: " + descriptor.getName());
                }
                properties.put(descriptor.getName().toLowerCase(), descriptor);
            }
        }
        catch (IntrospectionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public T map(int index, ResultSet rs, StatementContext ctx) throws SQLException
    {
        Map<String, Integer> names = new HashMap<String, Integer>();

        T bean;
        try {
            bean = type.newInstance();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Failed to instantiate bean: " + type.getName(), e);
        }

        ResultSetMetaData metadata = rs.getMetaData();

        if (requireAll && (metadata.getColumnCount() < properties.size())) {
            throw new IllegalArgumentException(String.format(
                "Class has %d properties, but result set only has %d columns",
                properties.size(), metadata.getColumnCount()));
        }

        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            String name = metadata.getColumnLabel(i).toLowerCase().replace("_", "");

            if (index == 0) {
                if (names.containsKey(name)) {
                    int dup = names.get(name);
                    throw new IllegalArgumentException(String.format(
                        "Column %d '%s' name is duplicated by column %d '%s'",
                        i, metadata.getColumnLabel(i), dup, metadata.getColumnLabel(dup)));
                }
                names.put(name, i);
            }

            PropertyDescriptor descriptor = properties.get(name);
            if (descriptor == null) {
                throw new IllegalArgumentException("No property for column: " + name);
            }

            Object value = getValue(descriptor, rs, i);
            try {
                descriptor.getWriteMethod().invoke(bean, value);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to access setter for " + name, e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("Failed invoking setter for " + name, e);
            }
        }

        return bean;
    }

    private static Object getValue(PropertyDescriptor descriptor, ResultSet rs, int i) throws SQLException
    {
        Class type = descriptor.getPropertyType();

        Object value = rs.getObject(i);
        if (value == null) {
            if (type.isPrimitive()) {
                throw new IllegalArgumentException(String.format(
                    "Cannot assign null from column %d '%s' to property '%s' with primitive type '%s'",
                    i, rs.getMetaData().getColumnLabel(i),
                    descriptor.getName(), type.getName()));
            }
            return null;
        }

        if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
            return rs.getBoolean(i);
        }
        if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
            return rs.getByte(i);
        }
        if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
            return rs.getShort(i);
        }
        if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
            return rs.getInt(i);
        }
        if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
            return rs.getLong(i);
        }
        if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
            return rs.getFloat(i);
        }
        if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
            return rs.getDouble(i);
        }

        if (!type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(String.format(
                "Value type '%s' from column %d '%s' is not assignable to property '%s' type '%s'",
                value.getClass().getName(),
                i, rs.getMetaData().getColumnLabel(i),
                descriptor.getName(), type.getName()));
        }
        return value;
    }
}
