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
package ec.tss.tsproviders.utils;

import ec.tss.tsproviders.DataSource;
import java.io.File;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DataSourceBeanSupportTest {

    private final String providerName = "myprovider";
    private final String version = "1234";

    private static final class CustomBean {

        static CustomBean of(File file, String details) {
            CustomBean result = new CustomBean();
            result.file = file;
            result.details = details;
            return result;
        }

        File file;
        String details;

        @Override
        public int hashCode() {
            return Objects.hash(file, details);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CustomBean
                    && ((CustomBean) obj).file.equals(file)
                    && ((CustomBean) obj).details.equals(details);
        }
    }

    private final VersionedParam<DataSource, CustomBean> param = VersionedParam.of(version, new IParam<DataSource, CustomBean>() {

        private final IParam<DataSource, File> fileParam = Params.onFile(new File("defaultFile"), "f");
        private final IParam<DataSource, String> detailsParam = Params.onString("defaultValue", "d");

        @Override
        public CustomBean defaultValue() {
            CustomBean result = new CustomBean();
            result.file = fileParam.defaultValue();
            result.details = detailsParam.defaultValue();
            return result;
        }

        @Override
        public CustomBean get(DataSource config) {
            CustomBean result = new CustomBean();
            result.file = fileParam.get(config);
            result.details = detailsParam.get(config);
            return result;
        }

        @Override
        public void set(IConfig.Builder<?, DataSource> builder, CustomBean value) {
            fileParam.set(builder, value.file);
            detailsParam.set(builder, value.details);
        }
    });

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThatThrownBy(() -> DataSourceBeanSupport.of(null, param)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataSourceBeanSupport.of(providerName, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testNewBean() {
        DataSourceBeanSupport support = DataSourceBeanSupport.of(providerName, param);
        assertThat(support.newBean())
                .isNotNull()
                .isNotSameAs(support.newBean())
                .isEqualToComparingFieldByField(support.newBean())
                .extracting("file", "details")
                .containsExactly(new File("defaultFile"), "defaultValue");
    }

    @Test
    public void testEncodeBean() {
        DataSourceBeanSupport support = DataSourceBeanSupport.of(providerName, param);
        DataSource.Builder b = DataSource.builder(providerName, version);
        assertThat(support.encodeBean(support.newBean()))
                .isEqualTo(b.clear().build());
        assertThat(support.encodeBean(CustomBean.of(new File("hello"), "world")))
                .isEqualTo(b.clear().put("f", "hello").put("d", "world").build());
        assertThatThrownBy(() -> support.encodeBean(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.encodeBean("string")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDecodeBean() {
        DataSourceBeanSupport support = DataSourceBeanSupport.of(providerName, param);
        DataSource.Builder b = DataSource.builder(providerName, version);
        assertThat(support.decodeBean(b.clear().build()))
                .isEqualTo(support.newBean());
        assertThat(support.decodeBean(b.clear().put("f", "hello").put("d", "world").build()))
                .isEqualTo(CustomBean.of(new File("hello"), "world"));
        assertThatThrownBy(() -> support.decodeBean(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> support.decodeBean(DataSource.builder("xxx", version).build())).isInstanceOf(IllegalArgumentException.class);
    }
}
