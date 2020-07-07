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
package ec.tss.tsproviders.jdbc;

import ec.tss.TsAsyncMode;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.*;
import ec.tss.tsproviders.db.DbProvider;
import ec.tss.tsproviders.utils.Parsers;
import ec.tstoolkit.design.VisibleForTesting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

/**
 *
 * @author Demortier Jeremy
 */
public abstract class JdbcProvider<BEAN extends JdbcBean> extends DbProvider<BEAN> {

    protected final String version;
    protected final Parsers.Parser<DataSource> legacyDataSourceParser;
    protected final Parsers.Parser<DataSet> legacyDataSetParser;

    protected JdbcProvider(@NonNull String source, @NonNull String version, @NonNull Logger logger, @NonNull TsAsyncMode asyncMode) {
        this(source, version, logger, asyncMode, JdbcLegacy.dbParser(source, version));
    }

    protected JdbcProvider(@NonNull String source, @NonNull String version, @NonNull Logger logger, @NonNull TsAsyncMode asyncMode, Parsers.@NonNull Parser<DataSource> legacyDbParser) {
        this(source, version, logger, asyncMode, legacyDbParser, JdbcLegacy.domainSeriesParser(legacyDbParser));
    }

    @VisibleForTesting
    JdbcProvider(@NonNull String providerName, @NonNull String version, @NonNull Logger logger, @NonNull TsAsyncMode asyncMode, Parsers.@NonNull Parser<DataSource> legacyDataSourceParser, Parsers.@NonNull Parser<DataSet> legacyDataSetParser) {
        super(logger, providerName, asyncMode);
        this.version = version;
        this.legacyDataSourceParser = legacyDataSourceParser;
        this.legacyDataSetParser = legacyDataSetParser;
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) {
        DataSet result = super.toDataSet(moniker);
        return result != null ? result : legacyDataSetParser.parse(moniker.getId());
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) {
        DataSource result = super.toDataSource(moniker);
        return result != null ? result : legacyDataSourceParser.parse(moniker.getId());
    }

    @Override
    public DataSource encodeBean(Object bean) throws IllegalArgumentException {
        return support.checkBean(bean, JdbcBean.class).toDataSource(getSource(), version);
    }
}
