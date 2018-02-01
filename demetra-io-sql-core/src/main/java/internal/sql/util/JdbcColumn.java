/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package internal.sql.util;

import ec.tstoolkit.design.Immutable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@Immutable
public final class JdbcColumn {

    @Nonnull
    public static List<JdbcColumn> ofAll(@Nonnull ResultSetMetaData md) throws SQLException {
        JdbcColumn[] result = new JdbcColumn[md.getColumnCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = of(md, i + 1);
        }
        return Arrays.asList(result);
    }

    @Nonnull
    public static JdbcColumn of(@Nonnull ResultSetMetaData md, int columnIndex) throws SQLException {
        return new JdbcColumn(
                md.getColumnClassName(columnIndex),
                md.getColumnDisplaySize(columnIndex),
                md.getColumnLabel(columnIndex),
                md.getColumnName(columnIndex),
                md.getColumnType(columnIndex),
                md.getColumnTypeName(columnIndex));
    }
    //
    final String className;
    final int displaySize;
    final String label;
    final String name;
    final int type;
    final String typeName;

    public JdbcColumn(String className, int displaySize, String label, String name, int type, String typeName) {
        this.className = className;
        this.displaySize = displaySize;
        this.label = label;
        this.name = name;
        this.type = type;
        this.typeName = typeName;
    }

    /**
     * Gets the column's table's catalog name.
     *
     * @see ResultSetMetaData#getColumnClassName(int)
     * @return
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the column's normal maximum width in characters.
     *
     * @see ResultSetMetaData#getColumnDisplaySize(int)
     * @return
     */
    public int getDisplaySize() {
        return displaySize;
    }

    /**
     * Gets the column's suggested title for use in printouts and displays. The
     * suggested title is usually specified by the SQL
     * <code>AS</code> clause. If a SQL
     * <code>AS</code> is not specified, the value returned from
     * <code>getColumnLabel</code> will be the same as the value returned by the
     * <code>getColumnName</code> method.
     *
     * @see ResultSetMetaData#getColumnLabel(int)
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the column's name.
     *
     * @see ResultSetMetaData#getColumnName(int)
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the column's SQL type.
     *
     * @see ResultSetMetaData#getColumnType(int)
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * Retrieves the column's database-specific type name.
     *
     * @see ResultSetMetaData#getColumnTypeName(int)
     * @return
     */
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return name;
    }
}
