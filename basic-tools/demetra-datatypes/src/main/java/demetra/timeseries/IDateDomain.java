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
package demetra.timeseries;

import demetra.design.Development;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import javax.annotation.Nonnegative;

/**
 * Represents a time domain, which is a collection of contiguous periods.
 * Implementations of a time domain should be immutable objects.
 *
 * @author Jean Palate
 * @param <E>
 */
@Development(status = Development.Status.Preliminary)
public interface IDateDomain<E extends IDatePeriod> extends ITimeDomain<E> {

    @Override
    default int search(LocalDateTime time) {
        return search(time.toLocalDate());
    }

    /**
     * @param time
     * @return -1 if not found.
     */
    int search(LocalDate time);

    /**
     * First included, end excluded
     *
     * @param firstPeriod
     * @param endPeriod
     * @return
     */
    IDateDomain<E> range(@Nonnegative int firstPeriod, @Nonnegative int endPeriod);
    
    default IDateDomain<E> drop(@Nonnegative int nbeg, @Nonnegative int nend){
        return range(nbeg, length()-nend);
    }
 
    IDateDomain<E> intersection(final IDateDomain<E> d2);

    /**
     * Makes a new domain from this domain and a period selector.
     *
     * @param selector The selector.
     * @return The corresponding domain. May be Empty.
     */
    default IDateDomain<E> select(final TsPeriodSelector selector) {
        if (isEmpty()) {
            return this;
        }
        // throw new ArgumentNullException("ps");

        int len = length();
        TsPeriodSelector.SelectionType type = selector.getType();
        switch (type) {
            case None:
                return range(0, 0);
            case All:
                return this;
            case First:
                return range(0, selector.getN0());
            case Last:
                return range(len - selector.getN1(), len);
            case Excluding:
                return range(selector.getN0(), len - selector.getN1());
            default:
                int nf = 0,
                 nl = 0;
                if ((type == TsPeriodSelector.SelectionType.From)
                        || (type == TsPeriodSelector.SelectionType.Between)) {
                    LocalDateTime d = selector.getD0();
                    int pos = search(d);
                    if (pos < -1) {
                        nf = len;
                    } else if (pos >= 0) {
                        if (get(pos).start().isBefore(d)) {
                            nf = pos + 1;
                        } else {
                            nf = pos;
                        }
                    }
                }
                if ((type == TsPeriodSelector.SelectionType.To)
                        || (type == TsPeriodSelector.SelectionType.Between)) {
                    LocalDateTime d = selector.getD1();
                    int pos = search(d);
                    if (pos == -1) {
                        nl = len; // on ne garde rien
                    } else if (pos >= 0) {
                        if (get(pos + 1).start().isBefore(d)) {
                            nl = len - pos;
                        } else {
                            nl = len - pos - 1;
                        }
                    }
                }
                if (nf < 0) {
                    nf = 0;
                }
                if (nl < 0) {
                    nl = 0;
                }
                return range(nf, len - nl);
        }
    }

    /**
     * Returns the union between this domain and another one.
     *
     * @param d2 Another domain. Should have the same frequency.
     * @return <I>null</I> if the periodicity is not the same. If the actual
     * union contains a hole, it is removed in the returned domain.
     *
     */
    IDateDomain<E> union(final IDateDomain<E> d2);

    /**
     * Returns the first period of the domain.
     *
     * @return A new period is returned, even for empty domain,
     */
    E getStart();

    /**
     * Returns the last period of the domain (not included).
     *
     * @return A new period is returned. Should not be used on empty domain,
     */
    E getEnd();

    /**
     * Returns the last period of the domain (which is just before getEnd().
     *
     * @return A new period is returned. Should not be used on empty domain,
     */
    E getLast();

}
