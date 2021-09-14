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

import _util.*;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.*;
import demetra.tsprovider.FileBean;
import demetra.tsprovider.FileLoader;
import demetra.tsprovider.util.ObsFormat;
import demetra.util.Table;
import ec.tss.TsBypass;
import ec.tss.TsCollectionInformation;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static demetra.bridge.TsConverter.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class TsConverterTest {

    @Test
    public void testTsProvider() {
        assertThat(toTsProvider(fromTsProvider(new MockedDataSourceProvider()))).isInstanceOf(MockedDataSourceProvider.class);
        assertThat(toTsProvider(fromTsProvider(new MockedDataSourceLoader()))).isInstanceOf(MockedDataSourceLoader.class);
        assertThat(toTsProvider(fromTsProvider(new MockedFileLoader()))).isInstanceOf(MockedFileLoader.class);

        assertThat(fromTsProvider(toTsProvider(new MockedDataSourceProviderV2()))).isInstanceOf(MockedDataSourceProviderV2.class);
        assertThat(fromTsProvider(toTsProvider(new MockedDataSourceLoaderV2()))).isInstanceOf(MockedDataSourceLoaderV2.class);
        assertThat(fromTsProvider(toTsProvider(new MockedFileLoaderV2()))).isInstanceOf(MockedFileLoaderV2.class);

        FileLoader<?> x = (ToFileLoader) toTsProvider(new MockedFileLoaderV2());
        FileBean fileBean = x.newBean();
        assertThat(x.decodeBean(x.encodeBean(fileBean))).isEqualTo(fileBean);
    }

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
                        // types are restricted by constructors !
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
                            assertThat(toTsBuilder(fromTsBuilder(x.build())))
                                    .usingRecursiveComparison()
                                    .isEqualTo(x);
                        }
                    }
                }
            }
        }

        Ts.Builder invalidData = Ts.builder().type(TsInformationType.All);
        assertThat(fromTsBuilder(invalidData.build()).hasData()).isTrue();

        Ts.Builder validData = Ts.builder().type(TsInformationType.All).data(TsData.random(TsUnit.MONTH, 0));
        assertThat(fromTsBuilder(validData.build()).hasData()).isTrue();

        Ts.Builder undefinedData = Ts.builder().type(TsInformationType.MetaData);
        assertThat(fromTsBuilder(undefinedData.build()).hasData()).isFalse();
    }

    @Test
    public void testTsCollection() {
        for (String name : names) {
            for (Map meta : metaArray) {
                for (TsMoniker moniker : monikers) {
                    // types are restricted by constructors !
                    for (TsInformationType type : new TsInformationType[]{TsInformationType.UserDefined}) {
                        for (List<Ts> items : tsArray) {
                            TsCollection x = TsCollection
                                    .builder()
                                    .name(name)
                                    .moniker(moniker)
                                    .meta(meta)
                                    .items(items)
                                    .type(type)
                                    .build();
                            assertThat(toTsCollection(fromTsCollection(x))).isEqualTo(x);
                        }

                        TsCollection empty = TsCollection
                                .builder()
                                .name(name)
                                .moniker(moniker)
                                .meta(meta)
                                .emptyCause("some specific cause")
                                .type(type)
                                .build();
                        assertThat(toTsCollection(fromTsCollection(empty))).isEqualTo(empty);
                    }
                }
            }
        }

        assertThat(toTsCollection(TsBypass.col(null)).toBuilder().moniker(TsMoniker.NULL).build())
                .describedAs("Empty TsCollection with nulls should not fail")
                .isEqualTo(TsCollection.EMPTY);
    }

    @Test
    public void testTsCollectionBuilder() {
        for (String name : names) {
            for (Map meta : metaArray) {
                for (TsMoniker moniker : monikers) {
                    for (TsInformationType type : TsInformationType.values()) {
                        for (List<Ts> items : tsArray) {
                            TsCollection.Builder x = TsCollection
                                    .builder()
                                    .name(name)
                                    .moniker(moniker)
                                    .meta(meta)
                                    .items(items)
                                    .type(type);
                            assertThat(toTsCollectionBuilder(fromTsCollectionBuilder(x.build())).build())
                                    .isEqualTo(x.build());
                        }

                        TsCollection.Builder empty = TsCollection
                                .builder()
                                .name(name)
                                .moniker(moniker)
                                .meta(meta)
                                .emptyCause("some specific cause")
                                .type(type);
                        assertThat(toTsCollectionBuilder(fromTsCollectionBuilder(empty.build())).build())
                                .isEqualTo(empty.build());
                    }
                }
            }
        }

        assertThat(toTsCollectionBuilder(new TsCollectionInformation()).moniker(TsMoniker.NULL).build())
                .describedAs("Empty TsCollectionInformation with nulls should not fail")
                .isEqualTo(TsCollection.EMPTY);
    }

    @Test
    public void testObsFormat() {
        assertThat(TsConverter.fromObsFormat(ObsFormat.DEFAULT))
                .isEqualTo(DataFormat.DEFAULT);

        assertThat(TsConverter.fromObsFormat(ObsFormat.ROOT))
                .isEqualTo(DataFormat.ROOT);

        assertThat(TsConverter.fromObsFormat(ObsFormat.of(Locale.FRENCH, "yyyy-MMM", "#")))
                .isEqualTo(DataFormat.of(Locale.FRENCH, "yyyy-MMM", "#"));

        assertThat(TsConverter.toObsFormat(DataFormat.DEFAULT))
                .isEqualTo(ObsFormat.DEFAULT);

        assertThat(TsConverter.toObsFormat(DataFormat.ROOT))
                .isEqualTo(ObsFormat.ROOT);

        assertThat(TsConverter.toObsFormat(DataFormat.of(Locale.FRENCH, "yyyy-MMM", "#")))
                .isEqualTo(ObsFormat.of(Locale.FRENCH, "yyyy-MMM", "#"));
    }

    @Test
    public void testTimeSelector() {
        assertThatNullPointerException()
                .isThrownBy(() -> TsConverter.fromTimeSelector(null));

        assertThatNullPointerException()
                .isThrownBy(() -> TsConverter.toTimeSelector(null));

        LocalDateTime d0 = LocalDate.of(2009, 1, 1).atStartOfDay();
        LocalDateTime d1 = LocalDate.of(2009, 1, 1).atStartOfDay();

        TimeSelector[] z = {
                TimeSelector.all(),
                TimeSelector.none(),
                TimeSelector.from(d0),
                TimeSelector.to(d1),
                TimeSelector.between(d0, d1),
                TimeSelector.first(10),
                TimeSelector.last(5),
                TimeSelector.excluding(10, 5)
        };

        for (TimeSelector x : z) {
            assertThat(TsConverter.toTimeSelector(TsConverter.fromTimeSelector(x)))
                    .isEqualTo(x);
        }
    }

    @Test
    public void testMatrix() {
        MatrixType[] samples = {
                MatrixType.of(new double[]{1, 2, 3, 4, 5, 6}, 2, 3),
                MatrixType.of(new double[]{1, 2, 3, 4, 5, 6}, 3, 2),
                MatrixType.EMPTY
        };
        for (MatrixType x : samples) {
            assertThat(TsConverter.toMatrix(TsConverter.fromMatrix(x))).isEqualTo(x);
        }
    }

    @Test
    public void testTable() {
        List<Table<String>> samples = new ArrayList<>();
        samples.add(tableOf(new String[]{"1", "2", "3", "4", "5", "6"}, 2, 3));
        samples.add(tableOf(new String[]{"1", "2", "3", "4", "5", "6"}, 3, 2));
        samples.add(tableOf(new String[0], 0, 0));
        for (Table<String> x : samples) {
            assertThat(TsConverter.toTable(TsConverter.fromTable(x))).isEqualTo(x);
        }
    }

    private Table<String> tableOf(String[] data, int rows, int columns) {
        Table<String> result = new Table<>(rows, columns);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                result.set(row, column, data[row + column * rows]);
            }
        }
        return result;
    }

    private final TsUnit[] supportedUnits = Stream.of(TsFrequency.values()).map(o -> toTsUnit(o)).filter(o -> !o.equals(TsUnit.UNDEFINED)).toArray(TsUnit[]::new);

    private final TsData emptyTsData = TsData.empty("boom");
    private final TsData monthlyTsData = TsData.random(TsUnit.MONTH, 0);
    private final Ts emptyTs = Ts.builder().name("x").data(emptyTsData).meta("k", "v").moniker(TsMoniker.of("a", "b")).type(TsInformationType.UserDefined).build();
    private final Ts monthlyTs = Ts.builder().name("x").data(monthlyTsData).meta("k", "v").moniker(TsMoniker.of("a", "b")).type(TsInformationType.UserDefined).build();

    private final String[] names = {"", "not_blank"};
    private final Map<String, String>[] metaArray = new Map[]{Collections.emptyMap(), Collections.singletonMap("hello", "world")};
    private final TsMoniker[] monikers = new TsMoniker[]{TsMoniker.of("p", "id")};
    private final TsData[] dataArray = new TsData[]{emptyTsData, monthlyTsData};
    private final List<Ts>[] tsArray = new List[]{Collections.emptyList(), Arrays.asList(emptyTs, monthlyTs)};
}
