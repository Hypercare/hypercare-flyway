/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.database.derby;

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.database.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Derby database specific support
 */
public class DerbyDatabase extends Database {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public DerbyDatabase(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    @Override
    protected final void ensureSupported() {
        String version = majorVersion + "." + minorVersion;

        if (majorVersion < 10 || (majorVersion == 10 && minorVersion < 8)) {
            throw new FlywayDbUpgradeRequiredException("Derby", version, "10.8.1.2");
        }
    }

    public String getDbName() {
        return "derby";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT_USER FROM SYSIBM.SYSDUMMY1");
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1");
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + quote(schema));
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "true";
    }

    public String getBooleanFalse() {
        return "false";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new DerbySqlStatementBuilder(getDefaultDelimiter());
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new DerbySchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }
}