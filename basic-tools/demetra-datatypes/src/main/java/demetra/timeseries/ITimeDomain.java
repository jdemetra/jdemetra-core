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

import demetra.data.Sequence;
import demetra.design.Development;
import java.time.LocalDateTime;
import java.time.Period;
import javax.annotation.Nonnegative;

/**
 * Represents a time domain, which is an indexed collection of periods.
 * Implementations of a time domain should be immutable objects.
 *
 * @author Jean Palate
 * @param <E>
 */
@Development(status = Development.Status.Preliminary)
public interface ITimeDomain<E extends ITimePeriod> extends Sequence<E> {

    /**
     * @param time
     * @return -(insertion point)-1 if not found. The insertion point is the
     * position of the first period which is after the given time. -getLength()
     * is returned if the point is after the domain
     */
    int search(LocalDateTime time);

    /**
     * Gets the period of this domain, if any
     *
     * @return null when the domain has irregular periods
     */
    Period getPeriod();

    /**
     * Checks that the domain is continuous
     *
     * @return True if end(t)=start(t+1), false otherwise
     */
    boolean isContinuous();
    
    /**
     * First included, end excluded
     *
     * @param firstPeriod
     * @param endPeriod
     * @return
     */
    ITimeDomain<E> range(@Nonnegative int firstPeriod, @Nonnegative int endPeriod);

}
