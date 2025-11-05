/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.internal.database.dameng;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DMDatabase extends Database<DMConnection> {

    public DMDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected DMConnection doGetConnection(Connection connection) {
        return new DMConnection(this, connection);
    }


    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("8");
        recommendFlywayUpgradeIfNecessaryForMajorVersion("8.1");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String tablespace = configuration.getTablespace() == null
                ? ""
                : " TABLESPACE \"" + configuration.getTablespace() + "\"";

        return "CREATE TABLE " + table + " (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR2(50),\n" +
                "    \"description\" VARCHAR2(200) NOT NULL,\n" +
                "    \"type\" VARCHAR2(20) NOT NULL,\n" +
                "    \"script\" VARCHAR2(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR2(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" NUMBER(1) NOT NULL,\n" +
                "    CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY (\"installed_rank\")\n" +
                ")" + tablespace + ";\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "CREATE INDEX \"" + table.getSchema().getName() + "\".\"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");\n";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT USER FROM DUAL");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    /**
     * Checks whether the specified query returns rows or not. Wraps the query in EXISTS() SQL function and executes it.
     * This is more preferable to opening a cursor for the original query, because a query inside EXISTS() is implicitly
     * optimized to return the first row and because the client never fetches more than 1 row despite the fetch size
     * value.
     *
     * @param query The query to check.
     * @param params The query parameters.
     * @return {@code true} if the query returns rows, {@code false} if not.
     * @throws SQLException when the query execution failed.
     */
    boolean queryReturnsRows(String query, String... params) throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForBoolean("SELECT CASE WHEN EXISTS(" + query + ") THEN 1 ELSE 0 END FROM DUAL", params);
    }

    /**
     * Checks whether the specified privilege or role is granted to the current user.
     *
     * @return {@code true} if it is granted, {@code false} if not.
     * @throws SQLException if the check failed.
     */
    boolean isPrivOrRoleGranted(String name) throws SQLException {
        return queryReturnsRows("SELECT 1 FROM SESSION_PRIVS WHERE PRIVILEGE = ? UNION ALL " +
                                        "SELECT 1 FROM SESSION_ROLES WHERE ROLE = ?", name, name);
    }


    /**
     * Returns the set of Oracle options available on the target database.
     *
     * @return the set of option titles.
     * @throws SQLException if retrieving of options failed.
     */
    private Set<String> getAvailableOptions() throws SQLException {
        return new HashSet<>(getMainConnection().getJdbcTemplate()
                                     .queryForStringList("SELECT PARAMETER FROM V$OPTION WHERE VALUE = 'TRUE'"));
    }

    /**
     * Checks whether Flashback Data Archive option is available or not.
     *
     * @return {@code true} if it is available, {@code false} if not.
     * @throws SQLException when checking availability of the feature failed.
     */
    boolean isFlashbackDataArchiveAvailable() throws SQLException {
        return getAvailableOptions().contains("Flashback Data Archive");
    }
}