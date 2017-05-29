/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries.simplets;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.timeseries.IDateDomain;
import java.time.LocalDate;
import java.time.Period;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
@lombok.EqualsAndHashCode
public final class TsDomain implements IDateDomain<TsPeriod> {

    /**
     * Creates a new time domain, identified by its frequency, the year and the
     * position of the first period and the length of the domain.
     *
     * @param freq The frequency.
     * @param firstyear Year of the first period
     * @param firstperiod (0-based) position in the year of the first period.
     * @param count Length of the domain (number of periods).
     * @return
     */
    public static TsDomain of(TsFrequency freq, int firstyear, int firstperiod, int count) {
        return new TsDomain(TsPeriod.of(freq, firstyear, firstperiod), count);
    }

    static TsDomain of(TsPeriod start, int length) {
        return new TsDomain(start, length);
    }

    private final TsPeriod start;
    private final int length;

    private TsDomain(TsPeriod start, int length) {
        this.start = start;
        this.length = length;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public TsPeriod get(int index) {
        return start.plus(index);
    }

    @Override
    public int search(final LocalDate day) {
        TsPeriod p = TsPeriod.of(start.getFrequency(), day);
        return search(p);
    }

    @Override
    public Period toPeriod() {
        return getFrequency().toPeriod();
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    /**
     * Returns the frequency of each periods.
     *
     * @return The frequency of the domain. Even an empty domain must have a
     * frequency.
     */
    public TsFrequency getFrequency() {
        return start.getFrequency();
    }

    /**
     * Returns the first period of the domain.
     *
     * @return A new period is returned, even for empty domain,
     */
    public TsPeriod getStart() {
        return start;
    }

    /**
     * Returns the last period of the domain (which is just before getEnd().
     *
     * @return A new period is returned. Should not be used on empty domain,
     */
    public TsPeriod getLast() {
        return start.plus(length - 1);
    }

    /**
     * Returns the last period of the domain (which is just before getEnd().
     *
     * @return A new period is returned. Should not be used on empty domain,
     */
    public TsPeriod getEnd() {
        return start.plus(length);
    }

    /**
     * Searches the place of a given period.
     *
     * @param p The period searched in the domain.
     * @return The index of the period is returned if it is found (exact match),
     * -1 otherwise.
     */
    public int search(final TsPeriod p) {
        if (p.getFrequency() != start.getFrequency()) {
            return -1;
        }
        int id = p.id();
        id -= start.id();
        if ((id < 0) || (id >= length)) {
            return -1;
        } else {
            return id;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getStart()).append((" - ")).append(getLast());
        return builder.toString();
    }
}
