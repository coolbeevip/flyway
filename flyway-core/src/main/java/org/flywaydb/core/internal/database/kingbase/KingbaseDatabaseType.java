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
package org.flywaydb.core.internal.database.kingbase;

import lombok.CustomLog;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.BaseDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;

import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;

@CustomLog
public class KingbaseDatabaseType extends BaseDatabaseType {

    @Override
    public String getName() {
        return "KingbaseES";
    }

    @Override
    public int getNullType() {
        return Types.VARCHAR;
    }

    @Override
    public boolean handlesJDBCUrl(String url) {
        return url.startsWith("jdbc:kingbase8:");
    }

    @Override
    public String getDriverClass(String url, ClassLoader classLoader) {
        if (url.startsWith("jdbc:kingbase8:")) {
            return "com.kingbase8.Driver";
        } else {
            throw new IllegalArgumentException("Unsupported JDBC URL: " + url);
        }
    }

    @Override
    public boolean handlesDatabaseProductNameAndVersion(String databaseProductName, String databaseProductVersion, Connection connection) {
        return databaseProductName.startsWith("KingbaseES");
    }

    @Override
    public Database createDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory, StatementInterceptor statementInterceptor) {
        return new KingbaseDatabase(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    public Parser createParser(Configuration configuration, ResourceProvider resourceProvider, ParsingContext parsingContext) {
        return new KingbaseParser(configuration, parsingContext);
    }

    @Override
    public void setDefaultConnectionProps(String url, Properties props, ClassLoader classLoader) {
        // 金仓数据库驱动再读取时没有嵌套读取程序名属性，因此这里不设置程序名
        //  props.put("connectionAttributes", "program_name:" + APPLICATION_NAME);
    }

    @Override
    public boolean detectPasswordRequiredByUrl(String url) {
        return super.detectPasswordRequiredByUrl(url);
    }

    @Override
    public boolean externalAuthPropertiesRequired(String url, String username, String password) {
        return super.externalAuthPropertiesRequired(url, username, password);
    }

    @Override
    public Properties getExternalAuthProperties(String url, String username) {
        KingbaseOptionFileReader kingbaseOptionFileReader = new KingbaseOptionFileReader();

        kingbaseOptionFileReader.populateOptionFiles();
        if (!kingbaseOptionFileReader.optionFiles.isEmpty()) {
            LOG.info(org.flywaydb.core.internal.license.FlywayTeamsUpgradeMessage.generate("a MySQL option file", "use this for database authentication"));
        }
        return super.getExternalAuthProperties(url, username);
    }
}