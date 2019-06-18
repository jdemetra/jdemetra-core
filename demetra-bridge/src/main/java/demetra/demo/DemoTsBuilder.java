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
package demetra.demo;

import demetra.design.BuilderPattern;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsMeta;
import demetra.tsprovider.TsMoniker;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.XorshiftRNG;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@BuilderPattern(Ts.class)
public final class DemoTsBuilder {

    @NonNull
    public static TsCollection randomTsCollection(int nSeries) {
        XorshiftRNG rng = new XorshiftRNG(0);
        DemoTsBuilder builder = new DemoTsBuilder().obsCount(24).rng(rng);
        TsCollection.Builder result = TsCollection.builder().moniker(TsMoniker.of());
        IntStream.range(0, nSeries)
                .mapToObj(o -> builder.name("S" + o).build())
                .forEach(result::data);
        return result.build();
    }

    private String name;
    private BiFunction<Integer, IRandomNumberGenerator, double[]> generator;
    private int forecastCount;
    private int missingCount;
    private int obsCount;
    private IRandomNumberGenerator rng;
    private TsPeriod start;

    public DemoTsBuilder() {
        this.name = "";
        this.forecastCount = 0;
        this.missingCount = 0;
        this.obsCount = 24;
        this.rng = new XorshiftRNG(0);
        this.start = TsPeriod.monthly(2010, 1);
        this.generator = (x, y) -> generateValues(x, y, start.start().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @NonNull
    public DemoTsBuilder name(@NonNull String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    @NonNull
    public DemoTsBuilder start(@NonNull TsPeriod start) {
        this.start = Objects.requireNonNull(start);
        return this;
    }

    @NonNull
    public DemoTsBuilder generator(@NonNull BiFunction<Integer, IRandomNumberGenerator, double[]> generator) {
        this.generator = Objects.requireNonNull(generator);
        return this;
    }

    @NonNull
    public DemoTsBuilder forecastCount(int forecastCount) {
        this.forecastCount = forecastCount;
        return this;
    }

    @NonNull
    public DemoTsBuilder missingCount(int missingCount) {
        this.missingCount = missingCount;
        return this;
    }

    @NonNull
    public DemoTsBuilder obsCount(int obsCount) {
        this.obsCount = obsCount;
        return this;
    }

    @NonNull
    public DemoTsBuilder rng(@NonNull IRandomNumberGenerator rng) {
        this.rng = Objects.requireNonNull(rng);
        return this;
    }

    public Ts build() {
        Ts.Builder result = Ts.builder().name(name).moniker(TsMoniker.of());
        double[] values = generator.apply(obsCount, rng);
        if (missingCount > 0 && values.length > 0) {
            for (int x = 0; x < missingCount; x++) {
                values[rng.nextInt(values.length)] = Double.NaN;
            }
        }
        TsData data = TsData.ofInternal(start, values);
        if (forecastCount > 0) {
            TsMeta.END.store(result::meta, data.getDomain().get(data.length() - forecastCount - 1).end());
        }
        result.data(data);
        return result.build();
    }

    private static double[] generateValues(int obsCount, IRandomNumberGenerator rng, long startTimeMillis) {
        int seriesIndex = rng.nextInt();
        double[] result = new double[obsCount];
        for (int j = 0; j < obsCount; j++) {
            result[j] = Math.abs((100 * (Math.cos(startTimeMillis * seriesIndex))) + (100 * (Math.sin(startTimeMillis) - Math.cos(rng.nextDouble()) + Math.tan(rng.nextDouble()))));
        }
        return result;
    }
}
