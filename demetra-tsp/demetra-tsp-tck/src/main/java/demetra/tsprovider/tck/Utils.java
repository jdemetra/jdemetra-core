/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package demetra.tsprovider.tck;

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsMoniker;
import demetra.tsprovider.*;
import org.assertj.core.api.SoftAssertions;

import java.io.IOException;

import static demetra.timeseries.TsInformationType.All;

/**
 * @author Philippe Charles
 * @since 2.2.0
 */
final class Utils {

    private Utils() {
        // static class
    }

    static final DataSource NULL_DATA_SOURCE = null;
    static final DataSet NULL_DATA_SET = null;
    static final TsMoniker NULL_MONIKER = null;
    static final Object NULL_BEAN = null;
    static final IOException NULL_IO_EXCEPTION = null;

    static final DataSource BAD_DATA_SOURCE = DataSource.of("BAD", "");
    static final DataSet BAD_DATA_SET = DataSet.of(BAD_DATA_SOURCE, DataSet.Kind.SERIES);
    static final TsMoniker BAD_MONIKER = TsMoniker.of("BAD", "");
    static final Object BAD_BEAN = new Object();

    static String throwDescription(Object o, String code, Class<? extends Throwable> exClass) {
        return throwDescription(o.getClass(), code, exClass);
    }

    static String throwDescription(Class<?> codeClass, String code, Class<? extends Throwable> exClass) {
        return String.format("Expecting '%s#%s' to raise '%s'", codeClass.getName(), code, exClass.getName());
    }

    static final TsCollection.Builder NULL_TS_COLLECTION_INFO = null;
    static final Ts.Builder NULL_TS_INFO = null;

    static void assertFileLoaderEquivalence(SoftAssertions s, FileLoader actual, FileLoader expected, DataSource dataSource) throws IOException {
        s.assertThat(actual.newBean().getFile())
                .isEqualTo(expected.newBean().getFile());

        s.assertThat(actual.encodeBean(actual.decodeBean(dataSource)))
                .isEqualTo(expected.encodeBean(expected.decodeBean(dataSource)));

        s.assertThat(actual.getFileDescription())
                .isEqualTo(expected.getFileDescription());

        assertDataSourceLoaderEquivalence(s, actual, expected, dataSource);
    }

    static void assertDataSourceLoaderEquivalence(SoftAssertions s, DataSourceLoader actual, DataSourceLoader expected, DataSource dataSource) throws IOException {
        s.assertThat(actual.open(dataSource))
                .isTrue();

        s.assertThat(expected.open(dataSource))
                .isTrue();

        assertDataSourceProviderEquivalence(s, actual, expected, dataSource);
    }

    static void assertDataSourceProviderEquivalence(SoftAssertions s, DataSourceProvider actual, DataSourceProvider expected, DataSource dataSource) throws IOException {
        s.assertThat(actual.getSource())
                .isEqualTo(expected.getSource());

        s.assertThat(actual.getDisplayName())
                .isEqualTo(expected.getDisplayName());

        s.assertThat(actual.getDataSources())
                .containsExactlyElementsOf(expected.getDataSources());

        s.assertThat(actual.toMoniker(dataSource))
                .isEqualTo(expected.toMoniker(dataSource));

        s.assertThat(actual.getDisplayName(dataSource))
                .isEqualTo(expected.getDisplayName(dataSource));

        s.assertThat(actual.toDataSource(actual.toMoniker(dataSource)))
                .isEqualTo(expected.toDataSource(expected.toMoniker(dataSource)));

        s.assertThat(actual.children(dataSource))
                .containsExactlyElementsOf(expected.children(dataSource));

        for (DataSet o : actual.children(dataSource)) {
            assertEquivalent(s, actual, expected, o);
        }
    }

    private static void assertEquivalent(SoftAssertions s, DataSourceProvider actual, DataSourceProvider expected, DataSet dataSet) throws IOException {
        s.assertThat(actual.toMoniker(dataSet))
                .isEqualTo(expected.toMoniker(dataSet));

        s.assertThat(actual.getDisplayName(dataSet))
                .isEqualTo(expected.getDisplayName(dataSet));

        s.assertThat(actual.getDisplayNodeName(dataSet))
                .isEqualTo(expected.getDisplayNodeName(dataSet));

        s.assertThat(actual.toDataSet(actual.toMoniker(dataSet)))
                .isEqualTo(expected.toDataSet(expected.toMoniker(dataSet)));

        switch (dataSet.getKind()) {
            case COLLECTION: {
                s.assertThat(actual.getTsCollection(actual.toMoniker(dataSet), All))
                        .usingRecursiveComparison()
                        .withComparatorForType(Double::compare, Double.class)
                        .ignoringFields("meta")
                        .isEqualTo(expected.getTsCollection(expected.toMoniker(dataSet), All));

                s.assertThat(actual.children(dataSet))
                        .containsExactlyElementsOf(expected.children(dataSet));

                for (DataSet o : actual.children(dataSet)) {
                    assertEquivalent(s, actual, expected, o);
                }
                break;
            }
            case SERIES: {
                s.assertThat(actual.getTs(actual.toMoniker(dataSet), All))
                        .usingRecursiveComparison()
                        .withComparatorForType(Double::compare, Double.class)
                        .ignoringFields("meta")
                        .isEqualTo(expected.getTs(expected.toMoniker(dataSet), All));
                break;
            }
        }
    }
}
