/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package demetra.timeseries;

import _util.MockedTsProvider;
import demetra.data.Doubles;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static demetra.timeseries.TsInformationType.BaseInformation;
import static demetra.timeseries.TsInformationType.Data;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author PALATEJ
 */
public class TsTest {

    @Test
    public void testWithName() {
        Ts original = Ts.of(TsData.empty("abc"));
        Ts renamed = original.withName("xyz");
        assertThat(original).isNotEqualTo(renamed);
        assertThat(renamed.getName()).isEqualTo("xyz");
        assertThat(original.withName("")).isSameAs(original);
    }

    @Test
    public void testLoadOfProvided() {
        Ts provided = factory.makeTs(tsMoniker, BaseInformation);

        assertThatNullPointerException()
                .isThrownBy(() -> provided.load(null, factory));

        for (TsInformationType info : TsInformationType.values()) {
            assertThatNullPointerException()
                    .isThrownBy(() -> provided.load(info, null));
            if (provided.getType().encompass(info)) {
                assertThat(provided.load(info, factory))
                        .describedAs("Provided TS can be modified by 'load' if old type encompasses new one")
                        .isSameAs(provided);
            } else {
                assertThat(provided.load(info, factory))
                        .describedAs("Provided TS must not be modified by 'load' if old type doesn't not encompass new one")
                        .isNotSameAs(provided);
            }
        }
    }

    @Test
    public void testLoadOfAnonymous() {
        Ts anonymous = Ts.of(TsData.empty("abc"));

        assertThatNullPointerException()
                .isThrownBy(() -> anonymous.load(null, factory));

        assertThatNullPointerException()
                .isThrownBy(() -> anonymous.load(Data, null));

        for (TsInformationType info : TsInformationType.values()) {
            assertThat(anonymous.load(info, factory))
                    .describedAs("Anonymous TS must not be modified by 'load'")
                    .isEqualTo(anonymous);
        }
    }

    @Test
    public void testFreeze() {
        Ts original = factory.makeTs(tsMoniker, BaseInformation);
        Ts frozen = original.freeze();
        Ts unfrozen = frozen.unfreeze(factory, TsInformationType.All);

        assertThat(original.getData()).isEmpty();

        assertThat(frozen.getMoniker()).isNotEqualTo(original.getMoniker());
        assertThat(frozen.getData()).isEmpty();

        assertThat(unfrozen.getMoniker()).isEqualTo(original.getMoniker());
        assertThat(unfrozen.getData()).isNotEmpty();

        assertThatNullPointerException().isThrownBy(() -> frozen.unfreeze(null, TsInformationType.All));
    }

    private final TsMoniker tsMoniker = TsMoniker.of(MockedTsProvider.NAME, "0:300");

    private final List<TsProvider> providers = Collections.singletonList(
            MockedTsProvider
                    .builder()
                    .ts(Ts
                            .builder()
                            .moniker(tsMoniker)
                            .data(TsData.of(TsPeriod.monthly(2010, 1), Doubles.of(Math.PI)))
                            .build())
                    .build()
    );

    private final TsFactory factory = TsFactory.of(providers);
}
