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
package ec.tss.tsproviders.odbc.registry;

import ec.tstoolkit.design.Immutable;

@Immutable
public final class OdbcDataSource {

    public enum Type {

        SYSTEM, USER, FILE
    };
    //      
    private final Type type;
    private final String name;
    private final String description;
    private final String driverDescription;
    private final String driver;
    private final String serverName;

    public OdbcDataSource(Type type, String name, String description, String driverDescription, String driver, String serverName) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.driverDescription = driverDescription;
        this.driver = driver;
        this.serverName = serverName;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDriverDescription() {
        return driverDescription;
    }

    public String getDriver() {
        return driver;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public String toString() {
        return name;
    }
}
