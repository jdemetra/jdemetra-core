/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.bridge;

import static demetra.bridge.TsConverter.*;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsInformationType;
import demetra.timeseries.TsMoniker;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsConverterTest {

    @Test
    public void testTsUnit() {
        for (TsFrequency freq : TsFrequency.values()) {
            assertThat(fromTsUnit(toTsUnit(freq))).isEqualTo(freq);
        }
        assertThatThrownBy(() -> fromTsUnit(TsUnit.DAY)).isInstanceOf(ConverterException.class);
    }

    @Test
    public void testDateTime() throws ParseException {
        assertThat(fromDateTime(LocalDateTime.of(2010, 1, 1, 0, 0))).isEqualTo(Day.fromString("2010-01-01"));

        assertThat(toDateTime(Day.fromString("2010-01-01"))).isEqualTo(LocalDateTime.of(2010, 1, 1, 0, 0));
    }

    @Test
    public void testTsPeriod() {
        for (TsUnit unit : supportedUnits) {
            TsPeriod normal = TsPeriod.of(unit, 0);
            assertThat(toTsPeriod(fromTsPeriod(normal))).isEqualTo(normal);
        }
        assertThatThrownBy(() -> fromTsPeriod(TsPeriod.of(TsUnit.DAY, 0))).isInstanceOf(ConverterException.class);
    }

    @Test
    public void testTsDomain() {
        for (TsUnit unit : supportedUnits) {
            TsDomain normal = TsDomain.of(TsPeriod.of(unit, 0), 4);
            assertThat(toTsDomain(fromTsDomain(normal))).isEqualTo(normal);
        }
        assertThatThrownBy(() -> fromTsDomain(TsDomain.of(TsPeriod.of(TsUnit.DAY, 0), 4))).isInstanceOf(ConverterException.class);
    }

    @Test
    public void testTsData() {
        for (TsUnit unit : supportedUnits) {
            TsData normal = TsData.random(unit, 0);
            assertThat(toTsData(fromTsData(normal))).isEqualTo(normal);
        }
        assertThat(toTsData(fromTsData(emptyTsData))).isEqualTo(emptyTsData);
    }

    @Test
    public void testTsMoniker() {
        for (TsMoniker o : new TsMoniker[]{TsMoniker.NULL, TsMoniker.of(), TsMoniker.of("hello", "world")}) {
            assertThat(toTsMoniker(fromTsMoniker(o))).isEqualTo(o);
        }

        for (ec.tss.TsMoniker o : new ec.tss.TsMoniker[]{ec.tss.TsMoniker.create(null, null), ec.tss.TsMoniker.create("", ""), ec.tss.TsMoniker.create("hello", "world")}) {
            assertThat(fromTsMoniker(toTsMoniker(o))).isEqualTo(o);
        }
    }

    @Test
    public void testTs() {
        for (String name : names) {
            for (Map meta : metaArray) {
                for (TsMoniker moniker : monikers) {
                    for (TsData data : dataArray) {
                        // types are restricted by contructors !
                        Ts x = Ts
                                .builder()
                                .name(name)
                                .moniker(moniker)
                                .meta(meta)
                                .data(data)
                                .type(TsInformationType.UserDefined)
                                .build();
                        assertThat(toTs(fromTs(x))).isEqualTo(x);
                    }
                }
            }
        }
    }

    @Test
    public void testTsBuilder() {
        for (String name : names) {
            for (Map meta : metaArray) {
                for (TsMoniker moniker : monikers) {
                    for (TsData data : dataArray) {
                        for (TsInformationType type : TsInformationType.values()) {
                            Ts.Builder x = Ts
                                    .builder()
                                    .name(name)
                                    .moniker(moniker)
                                    .meta(meta)
                                    .data(data)
                                    .type(type);
                            assertThat(toTsBuilder(fromTsBuilder(x))).isEqualToComparingFieldByField(x);
                        }
                    }
                }
            }
        }

        Ts.Builder invalidData = Ts.builder().type(TsInformationType.All);
        assertThat(invalidData.getType().hasData()).isTrue();
        assertThat(fromTsBuilder(invalidData).hasData()).isTrue();

        Ts.Builder validData = Ts.builder().type(TsInformationType.All).data(TsData.random(TsUnit.MONTH, 0));
        assertThat(validData.getType().hasData()).isTrue();
        assertThat(fromTsBuilder(validData).hasData()).isTrue();

        Ts.Builder undefinedData = Ts.builder().type(TsInformationType.MetaData);
        assertThat(undefinedData.getType().hasData()).isFalse();
        assertThat(fromTsBuilder(undefinedData).hasData()).isFalse();
    }

    @Test
    public void testTsCollection() {
        for (String name : names) {
            for (Map meta : metaArray) {
                for (TsMoniker moniker : monikers) {
                    for (List<Ts> items : tsArray) {
                        // types are restricted by contructors !
                        TsCollection x = TsCollection
                                .builder()
                                .name(name)
                                .moniker(moniker)
                                .meta(meta)
                                .data(items)
                                .type(TsInformationType.UserDefined)
                                .build();
                        assertThat(toTsCollection(fromTsCollection(x))).isEqualTo(x);
                    }
                }
            }
        }
    }

    @Test
    public void testTsCollectionBuilder() {
        for (String name : names) {
            for (Map meta : metaArray) {
                for (TsMoniker moniker : monikers) {
                    for (List<Ts> items : tsArray) {
                        for (TsInformationType type : TsInformationType.values()) {
                            TsCollection.Builder x = TsCollection
                                    .builder()
                                    .name(name)
                                    .moniker(moniker)
                                    .meta(meta)
                                    .data(items)
                                    .type(type);
                            assertThat(toTsCollectionBuilder(fromTsCollectionBuilder(x))).isEqualToComparingFieldByField(x);
                        }
                    }
                }
            }
        }
    }

    private final TsUnit[] supportedUnits = Stream.of(TsFrequency.values()).map(o -> toTsUnit(o)).filter(o -> !o.equals(TsUnit.UNDEFINED)).toArray(TsUnit[]::new);

    private final TsData emptyTsData = TsData.empty("boom");
    private final TsData montlyTsData = TsData.random(TsUnit.MONTH, 0);
    private final Ts emptyTs = Ts.builder().name("x").data(emptyTsData).meta("k", "v").moniker(TsMoniker.of("a", "b")).type(TsInformationType.UserDefined).build();
    private final Ts montlyTs = Ts.builder().name("x").data(montlyTsData).meta("k", "v").moniker(TsMoniker.of("a", "b")).type(TsInformationType.UserDefined).build();

    private final String[] names = {"", "not_blank"};
    private final Map<String, String>[] metaArray = new Map[]{Collections.emptyMap(), Collections.singletonMap("hello", "world")};
    private final TsMoniker[] monikers = new TsMoniker[]{TsMoniker.of("p", "id")};
    private final TsData[] dataArray = new TsData[]{emptyTsData, montlyTsData};
    private final List<Ts>[] tsArray = new List[]{Collections.emptyList(), Arrays.asList(emptyTs, montlyTs)};
}
