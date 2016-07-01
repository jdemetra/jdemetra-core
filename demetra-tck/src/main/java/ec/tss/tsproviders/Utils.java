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
package ec.tss.tsproviders;

import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import java.io.IOException;
import java.util.Collections;
import org.assertj.core.api.SoftAssertions;

/**
 *
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

    static final DataSource BAD_DATA_SOURCE = DataSource.deepCopyOf("BAD", "", Collections.emptyMap());
    static final DataSet BAD_DATA_SET = DataSet.deepCopyOf(BAD_DATA_SOURCE, DataSet.Kind.SERIES, Collections.emptyMap());
    static final TsMoniker BAD_MONIKER = new TsMoniker("BAD", "");
    static final Object BAD_BEAN = new Object();

    static String throwDescription(Object o, String code, Class<? extends Throwable> exClass) {
        return throwDescription(o.getClass(), code, exClass);
    }

    static String throwDescription(Class<?> codeClass, String code, Class<? extends Throwable> exClass) {
        return String.format("Expecting '%s#%s' to raise '%s'", codeClass.getName(), code, exClass.getName());
    }

    //<editor-fold defaultstate="collapsed" desc="Equivalence implementation">
    static void assertFileLoaderEquivalence(SoftAssertions s, IFileLoader actual, IFileLoader expected, DataSource dataSource) throws IOException {
        assertDataSourceLoaderEquivalence(s, actual, expected, dataSource);
        s.assertThat(expected.getFileDescription()).isEqualTo(actual.getFileDescription());
    }

    static void assertDataSourceLoaderEquivalence(SoftAssertions s, IDataSourceLoader actual, IDataSourceLoader expected, DataSource dataSource) throws IOException {
        s.assertThat(expected.getSource()).isEqualTo(actual.getSource());
        s.assertThat(expected.getDisplayName()).isEqualTo(actual.getDisplayName());

        s.assertThat(expected.open(dataSource)).isTrue();
        s.assertThat(actual.open(dataSource)).isTrue();

        s.assertThat(expected.getDataSources()).containsExactlyElementsOf(actual.getDataSources());

        s.assertThat(expected.toMoniker(dataSource)).isEqualTo(actual.toMoniker(dataSource));
        s.assertThat(expected.getDisplayName(dataSource)).isEqualTo(actual.getDisplayName(dataSource));
        s.assertThat(expected.toDataSource(expected.toMoniker(dataSource))).isEqualTo(actual.toDataSource(actual.toMoniker(dataSource)));
        s.assertThat(expected.children(dataSource)).containsExactlyElementsOf(actual.children(dataSource));

        for (DataSet o : expected.children(dataSource)) {
            assertEquivalent(s, actual, expected, o);
        }
    }

    private static void assertEquivalent(SoftAssertions s, IDataSourceLoader actual, IDataSourceLoader expected, DataSet dataSet) throws IOException {
        switch (dataSet.getKind()) {
            case COLLECTION:
                s.assertThat(expected.toMoniker(dataSet)).isEqualTo(actual.toMoniker(dataSet));
                s.assertThat(expected.getDisplayName(dataSet)).isEqualTo(actual.getDisplayName(dataSet));
                s.assertThat(expected.getDisplayNodeName(dataSet)).isEqualTo(actual.getDisplayNodeName(dataSet));
                s.assertThat(expected.toDataSet(expected.toMoniker(dataSet))).isEqualTo(actual.toDataSet(actual.toMoniker(dataSet)));
                s.assertThat(expected.children(dataSet)).containsExactlyElementsOf(actual.children(dataSet));
                for (DataSet o : expected.children(dataSet)) {
                    assertEquivalent(s, actual, expected, o);
                }
                break;
            case SERIES:
                s.assertThat(dataSet).isEqualTo(dataSet);
                s.assertThat(expected.toMoniker(dataSet)).isEqualTo(actual.toMoniker(dataSet));
                s.assertThat(expected.getDisplayName(dataSet)).isEqualTo(actual.getDisplayName(dataSet));
                s.assertThat(expected.getDisplayNodeName(dataSet)).isEqualTo(actual.getDisplayNodeName(dataSet));
                s.assertThat(expected.toDataSet(expected.toMoniker(dataSet))).isEqualTo(actual.toDataSet(actual.toMoniker(dataSet)));
                s.assertThat(getTsInformation(actual, dataSet)).isEqualToIgnoringNullFields(getTsInformation(expected, dataSet));
                break;
        }
    }

    private static TsInformation getTsInformation(IDataSourceProvider p, DataSet dataSet) {
        TsInformation info = new TsInformation();
        info.type = TsInformationType.All;
        info.moniker = p.toMoniker(dataSet);
        p.get(info);
        return info;
    }
    //</editor-fold>
}
