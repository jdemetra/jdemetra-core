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
package demetra.timeseries;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsDataTable {

    public enum DistributionType {
        FIRST, LAST, MIDDLE
    }

    public enum ValueStatus {
        PRESENT, UNUSED, BEFORE, AFTER, EMPTY;
    }

    @Nonnull
    public static TsDataTable of(@Nonnull List<TsData> col) {
        TsDomain domain = computeDomain(col.stream().map(TsData::getDomain).iterator());
        return new TsDataTable(domain, Collections.unmodifiableList(new ArrayList<>(col)));
    }

    @lombok.NonNull
    @lombok.Getter
    private final TsDomain domain;

    @lombok.NonNull
    @lombok.Getter
    private final List<TsData> data;

    @Nonnull
    public Cursor cursor(@Nonnull DistributionType distribution) {
        return cursor(i -> distribution);
    }

    @Nonnull
    public Cursor cursor(@Nonnull IntFunction<DistributionType> distribution) {
        return new Cursor(IntStream
                .range(0, data.size())
                .mapToObj(distribution)
                .map(TsDataTable::getDistributor)
                .collect(Collectors.toList())
        );
    }

    @lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public final class Cursor {

        private final List<Distributor> distributors;

        @lombok.Getter
        private int index = -1;

        @lombok.Getter
        private int windowLength = -1;

        @lombok.Getter
        private int windowIndex = -1;

        @lombok.NonNull
        @lombok.Getter
        private ValueStatus status = ValueStatus.EMPTY;

        @lombok.Getter
        private double value = Double.NaN;

        @Nonnull
        public Cursor moveTo(int period, int series) {
            TsData ts = data.get(series);
            if (ts.isEmpty()) {
                index = -1;
                windowLength = -1;
                windowIndex = -1;
                status = ValueStatus.EMPTY;
                value = Double.NaN;
            } else {
                TsPeriod current = domain.getStartPeriod().plus(period);
                TsPeriod valuePeriod = current.withUnit(ts.getDomain().getTsUnit());
                index = ts.getDomain().position(valuePeriod);

                if (isInBounds(ts, index)) {
                    TsPeriod start = valuePeriod.withUnit(current.getUnit());
                    TsPeriod end = valuePeriod.next().withUnit(current.getUnit());

                    windowLength = start.until(end);
                    windowIndex = start.until(current);

                    if (distributors.get(series).test(windowIndex, windowLength)) {
                        status = ValueStatus.PRESENT;
                        value = ts.getValue(index);
                    } else {
                        status = ValueStatus.UNUSED;
                        value = Double.NaN;
                    }
                } else {
                    windowLength = -1;
                    windowIndex = -1;
                    status = index < 0 ? ValueStatus.BEFORE : ValueStatus.AFTER;
                    value = Double.NaN;
                }
            }

            return this;
        }

        private boolean isInBounds(TsData ts, int index) {
            return index >= 0 && index < ts.length();
        }

        @Nonnegative
        public int getPeriodCount() {
            return domain.getLength();
        }

        @Nonnegative
        public int getSeriesCount() {
            return data.size();
        }

        public void forEachByPeriod(@Nonnull Consumer consumer) {
            for (int i = 0; i < getPeriodCount(); i++) {
                for (int j = 0; j < getSeriesCount(); j++) {
                    moveTo(i, j);
                    consumer.accept(i, j, this);
                }
            }
        }
    }

    @FunctionalInterface
    public interface Consumer {

        void accept(int period, int series, Cursor cursor);
    }

    @FunctionalInterface
    private interface Distributor {

        boolean test(int pos, int size);
    }

    private static Distributor getDistributor(DistributionType type) {
        switch (type) {
            case FIRST:
                return (pos, size) -> pos % size == 0;
            case LAST:
                return (pos, size) -> pos % size == size - 1;
            case MIDDLE:
                return (pos, size) -> pos % size == size / 2;
            default:
                throw new RuntimeException();
        }
    }

    static TsDomain computeDomain(Iterator<TsDomain> domains) {
        if (!domains.hasNext()) {
            throw new IllegalArgumentException();
        }

        TsDomain o = domains.next();

        long lowestAmount = o.getTsUnit().getAmount();
        ChronoUnit lowestChronoUnit = o.getTsUnit().getChronoUnit();
        LocalDateTime minDate = o.start();
        LocalDateTime maxDate = o.end();

        while (domains.hasNext()) {
            o = domains.next();

            if (o.getTsUnit().getChronoUnit().compareTo(lowestChronoUnit) < 0) {
                lowestAmount = getLowestAmount(lowestAmount, lowestChronoUnit, o.getTsUnit().getChronoUnit());
                lowestChronoUnit = o.getTsUnit().getChronoUnit();
            }
            lowestAmount = gcd(lowestAmount, o.getTsUnit().getAmount());

            if (minDate.isAfter(o.start())) {
                minDate = o.start();
            }
            if (maxDate.isBefore(o.end())) {
                maxDate = o.end();
            }
        }

        TsUnit unit = TsUnit.of(lowestAmount, lowestChronoUnit);
        TsPeriod startPeriod = TsPeriod.of(unit, minDate);
        TsPeriod endPeriod = TsPeriod.of(unit, maxDate);
        // FIXME: default epoch?
        return TsDomain.of(startPeriod, startPeriod.until(endPeriod));
    }

    private static long getLowestAmount(long lowestAmount, ChronoUnit oldUnit, ChronoUnit newUnit) {
        return oldUnit.compareTo(ChronoUnit.DAYS) > 0 && newUnit.compareTo(ChronoUnit.DAYS) <= 0
                ? 1
                : lowestAmount * CHRONO_UNIT_RATIOS_ON_SECONDS[oldUnit.ordinal()][newUnit.ordinal()];
    }

    private static final long[][] CHRONO_UNIT_RATIOS_ON_SECONDS = computeChronoUnitRatiosOnSeconds();

    private static long[][] computeChronoUnitRatiosOnSeconds() {
        Predicate<ChronoUnit> hasSeconds = o -> o.getDuration().getSeconds() > 0;
        ChronoUnit[] units = ChronoUnit.values();
        long[][] result = new long[units.length][units.length];
        for (ChronoUnit i : units) {
            for (ChronoUnit j : units) {
                result[i.ordinal()][j.ordinal()]
                        = hasSeconds.test(i) && hasSeconds.test(j)
                        ? i.getDuration().dividedBy(j.getDuration().getSeconds()).getSeconds()
                        : 0;
            }
        }
        return result;
    }

    /**
     * Computes the greatest common divisor of two integers.
     *
     * @param a
     * @param b
     * @return
     */
    private static long gcd(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b; // % is remainder  
            a = temp;
        }
        return a;
    }
}
