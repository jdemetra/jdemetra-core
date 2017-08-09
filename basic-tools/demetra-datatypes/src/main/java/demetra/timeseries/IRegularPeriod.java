/*
 * Copyright 2017 National Bank of Belgium
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

import java.time.LocalDateTime;

/**
 * 
 * @author Jean Palate
 */
public interface IRegularPeriod extends ITimePeriod {
    /**
     * Computes the number of periods between two periods. 
     * We have "plus(until(period) == period"
     * @param period The second period. Should have the same type as this object
     * @return The number of periods until the given period. May be negative if
     * the given period is before this period
     */
    long until(IRegularPeriod period);
    
    /**
     * Add a given number of periods to this period
     * @param nperiods The number of periods. Can be negative.
     * @return The returned period should have the same type as this period
     */
    IRegularPeriod plus(long nperiods);     
    
    /**
     * Creates a new period that contains the given time
     * @param dt The datetime which will be inside the returned period
     * @return The returned period should have the same type as this period
     */
    IRegularPeriod moveTo(LocalDateTime dt);
}
