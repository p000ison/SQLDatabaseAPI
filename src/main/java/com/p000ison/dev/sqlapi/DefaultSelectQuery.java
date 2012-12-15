package com.p000ison.dev.sqlapi;

import com.p000ison.dev.sqlapi.exception.QueryException;
import com.p000ison.dev.sqlapi.query.SelectQuery;
import com.p000ison.dev.sqlapi.query.WhereQuery;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a DefaultSelectQuery
 */
public class DefaultSelectQuery<T extends TableObject> implements SelectQuery<T> {

    private RegisteredTable table;
    private boolean descending;
    private DefaultWhereQuery<T> whereQuery;
    private Database database;

    public DefaultSelectQuery(Database database)
    {
        this.database = database;
    }

    @Override
    public SelectQuery<T> from(Class<T> object)
    {
        table = database.getRegisteredTable(object);
        return this;
    }

    @Override
    public WhereQuery<T> where()
    {
        return whereQuery = new DefaultWhereQuery<T>(this);
    }

    @Override
    public SelectQuery<T> descending()
    {
        this.descending = true;
        return this;
    }

    @Override
    public SelectQuery<T> orderBy(Column order)
    {
        return this;
    }

    @Override
    public SelectQuery<T> orderBy(String order)
    {
        return this;
    }

    @Override
    public SelectQuery<T> groupBy(Column group)
    {
        return this;
    }

    @Override
    public SelectQuery<T> groupBy(String group)
    {
        return this;
    }

    protected DefaultWhereQuery<T> getWhereQuery()
    {
        return whereQuery;
    }

    @Override
    public List<T> list()
    {
        List<T> objects = new ArrayList<T>();

        ResultSet result = database.executeDirectQuery(getQuery());
        List<Column> columns = table.getRegisteredColumns();
        try {
            while (result.next()) {
                T object = table.createNewInstance();

                for (int i = 0; i < columns.size(); i++) {
                    Column column = columns.get(i);

                    Object obj = null;

                    if (column.isSerializable()) {
                        try {
                            ObjectInputStream inputStream = new ObjectInputStream(result.getBlob(i + 1).getBinaryStream());
                            obj = inputStream.readObject();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        obj = result.getObject(i + 1);
                    }

                    column.setValue(object, obj);
                }

                objects.add(object);
            }
        } catch (SQLException e) {
            throw new QueryException(e);
        }
        return objects;
    }

    @Override
    public String getQuery()
    {
        StringBuilder query = new StringBuilder("SELECT ");
        List<Column> columns = table.getRegisteredColumns();

        int end = columns.size() - 1;
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            query.append(column.getColumnName());
            if (i != end) {
                query.append(',');
            }
        }

        query.append(" FROM ").append(table.getName());

        if (descending) {
            query.append(" ORDER BY DESC");
        }

        if (getWhereQuery() != null) {
            query.append(" WHERE ");
            List<DefaultWhereComparator> comparators = whereQuery.getComparators();

            if (!comparators.isEmpty()) {
                for (DefaultWhereComparator comparator : comparators) {
                    query.append(comparator.getColumn()).append(comparator.getOperator()).append(comparator.getExpectedValue());

                    if (comparator.isAnd()) {
                        query.append(" AND ");
                    } else if (comparator.isOr()) {
                        query.append(" OR ");
                    } else {
                        break;
                    }

                }
            }
        }

        query.append(';');

        return query.toString();
    }
}