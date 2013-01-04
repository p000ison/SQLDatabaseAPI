/*
 * This file is part of SQLDatabaseAPI (2012).
 *
 * SQLDatabaseAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQLDatabaseAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SQLDatabaseAPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Last modified: 29.12.12 16:45
 */

package com.p000ison.dev.sqlapi.sqlite;

import com.p000ison.dev.sqlapi.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

/**
 * Represents a SQLiteTableBuilder
 */
//todo add modify statements
public final class SQLiteTableBuilder extends TableBuilder {

    public SQLiteTableBuilder(TableObject object, Database database)
    {
        super(object, database);
    }

    public SQLiteTableBuilder(Class<? extends TableObject> object, Database database)
    {
        super(object, database);
    }

    @Override
    protected StringBuilder buildColumn(Column column)
    {
        Class<?> type = column.getType();
        StringBuilder query = new StringBuilder();
        query.append(column.getName()).append(' ');

        if (column.isID()) {
            query.append("INTEGER");
        } else {
            boolean allowModifyLength = true;

            if (type == boolean.class || type == Boolean.class) {
                query.append("TINYINT(1)");
                allowModifyLength = false;
            } else if (type == byte.class || type == Byte.class) {
                query.append("TINYINT");
                allowModifyLength = false;
            } else if (type == short.class || type == Short.class) {
                query.append("SMALLINT");
                allowModifyLength = false;
            } else if (type == int.class || type == Integer.class || type == long.class || type == Long.class) {
                query.append("INTEGER");
            } else if (type == float.class || type == Float.class) {
                query.append("FLOAT");
            } else if (type == double.class || type == Double.class) {
                query.append("DOUBLE");
            } else if (type == char.class || type == Character.class) {
                query.append("CHAR");
            } else if (type == Date.class || type == Timestamp.class) {
                query.append("DATETIME");
            } else if (type == String.class) {
                if (column.getLength().length != 0) {
                    query.append("VARCHAR");
                } else {
                    query.append("TEXT");
                }
            } else if (RegisteredTable.isSerializable(type)) {
                query.append("BLOB");
                allowModifyLength = false;
            }


            if (column.getLength().length != 0 && allowModifyLength) {
                query.append('(');
                for (int singleLength : column.getLength()) {
                    query.append(singleLength).append(',');
                }

                query.deleteCharAt(query.length() - 1);
                query.append(')');
            }
        }

        if (column.isNotNull()) {
            query.append(" NOT NULL");
        } else {
            query.append(" NULL");
        }

        if (column.isUnique()) {
            query.append(" UNIQUE");
        }

        if (column.isAutoIncrementing()) {
            throw new IllegalArgumentException("AutoIncrement is not supported by SQLite.");
        }

        if (column.isID()) {
            query.append(" PRIMARY KEY AUTOINCREMENT");
            //no autoincrement
        }

        if (!column.getDefaultValue().isEmpty()) {
            query.append(" DEFAULT ").append(column.getDefaultValue());
        }

        return query;
    }

    private static StringBuilder buildModifyColumn(Column column)
    {
        Class<?> type = column.getType();
        StringBuilder query = new StringBuilder();
        query.append(column.getName()).append(' ');

        boolean allowModifyLength = true;

        if (type == boolean.class || type == Boolean.class) {
            query.append("TINYINT(1)");
            allowModifyLength = false;
        } else if (type == byte.class || type == Byte.class) {
            query.append("TINYINT");
            allowModifyLength = false;
        } else if (type == short.class || type == Short.class) {
            query.append("SMALLINT");
            allowModifyLength = false;
        } else if (type == int.class || type == Integer.class) {
            query.append("INTEGER");
        } else if (type == float.class || type == Float.class) {
            query.append("FLOAT");
        } else if (type == double.class || type == Double.class) {
            query.append("DOUBLE");
        } else if (type == long.class || type == Long.class) {
            query.append("LONG");
        } else if (type == char.class || type == Character.class) {
            query.append("CHAR");
        } else if (type == Date.class || type == Timestamp.class) {
            query.append("DATETIME");
        } else if (type == String.class) {
            if (column.getLength().length != 0) {
                query.append("VARCHAR");
            } else {
                query.append("TEXT");
            }
        } else if (RegisteredTable.isSerializable(type)) {
            query.append("BLOB");
            allowModifyLength = false;
        }

        if (column.getLength().length != 0 && allowModifyLength) {
            query.append('(');
            for (int singleLength : column.getLength()) {
                query.append(singleLength).append(',');
            }

            query.deleteCharAt(query.length() - 1);
            query.append(')');
        }

        return query;
    }

    @Override
    protected void buildModifyColumns()
    {
        Set<Column> toAdd = super.getColumnsToAdd();

        for (Column column : toAdd) {
            StringBuilder query = new StringBuilder("ALTER TABLE ").append(getTableName()).append(" ADD COLUMN ");
            query.append(buildModifyColumn(column)).append(';');

            super.addQuery(query);
        }
    }

    @Override
    protected boolean isSupportAddColumns()
    {
        return true;
    }

    @Override
    protected boolean isSupportRemoveColumns()
    {
        return false;
    }

    @Override
    protected boolean isSupportModifyColumns()
    {
        return false;
    }
}
