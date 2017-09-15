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
package internal.spreadsheet;

import demetra.data.AggregationType;
import demetra.spreadsheet.SpreadSheetBean;
import demetra.timeseries.TsUnit;
import demetra.tsprovider.HasDataSourceBean;
import demetra.tsprovider.util.ObsFormat;
import demetra.util.Parser;
import java.io.File;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetParamTest {

    @Test
    public void testV1() {
        HasDataSourceBean<SpreadSheetBean> v1 = beanSupportOf(new SpreadSheetParam.V1());

        assertCompliance(v1, SpreadSheetParamTest::getBeanSample);

        assertThat(v1.encodeBean(getBeanSample()))
                .satisfies(o -> {
                    assertThat(o.getParams().keySet())
                            .containsExactly("aggregationType", "cleanMissing", "datePattern", "file", "frequency", "locale", "numberPattern");
                    assertThat(o.getParams().values())
                            .containsExactly("Average", "false", "yyyy", "1234", "Yearly", "fr_BE", "#");
                });

    }

    static HasDataSourceBean<SpreadSheetBean> beanSupportOf(SpreadSheetParam param) {
        return HasDataSourceBean.of("XCLPRVDR", param, param.getVersion());
    }

    static SpreadSheetBean getBeanSample() {
        SpreadSheetBean result = new SpreadSheetBean();
        result.setFile(new File("1234"));
        result.setObsFormat(ObsFormat.of(Parser.onLocale().parse("fr_BE"), "yyyy", "#"));
        result.setObsGathering(demetra.tsprovider.util.ObsGathering.builder().unit(TsUnit.YEARLY).aggregationType(AggregationType.Average).skipMissingValues(false).build());
        return result;
    }

    static <T> void assertCompliance(HasDataSourceBean<T> v1, Supplier<T> sampler) {
        T sample = sampler.get();

        assertThat(v1.newBean())
                .isNotSameAs(v1.newBean())
                .isEqualToComparingFieldByField(v1.newBean());

        assertThat(v1.encodeBean(v1.newBean()))
                .isNotSameAs(v1.encodeBean(v1.newBean()))
                .isEqualTo(v1.encodeBean(v1.newBean()))
                .satisfies(o -> {
                    assertThat(o.getParams().isEmpty()).isTrue();
                });

        assertThat(v1.encodeBean(sample))
                .isEqualTo(v1.encodeBean(sample))
                .satisfies(o -> {
                    assertThat(o.getParams().isEmpty()).isFalse();
                });

        assertThat(v1.decodeBean(v1.encodeBean(v1.newBean())))
                .isNotSameAs(v1.newBean())
                .isEqualToComparingFieldByField(v1.newBean());

        assertThat(v1.decodeBean(v1.encodeBean(sample)))
                .isNotSameAs(sample)
                .isEqualToComparingFieldByField(sample);
    }
}
