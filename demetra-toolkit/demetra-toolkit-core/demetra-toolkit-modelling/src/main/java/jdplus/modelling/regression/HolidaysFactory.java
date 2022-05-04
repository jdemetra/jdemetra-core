/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.modelling.regression;

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TimeSeriesInterval;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.HolidaysVariable;
import jdplus.math.matrices.FastMatrix;
import jdplus.timeseries.calendars.HolidaysUtility;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class HolidaysFactory implements RegressionVariableFactory<HolidaysVariable> {
    
    static HolidaysFactory FACTORY=new HolidaysFactory();

    @Override
    public boolean fill(HolidaysVariable var, TsPeriod start, FastMatrix buffer) {
        return HolidaysUtility.fill(var.getHolidays(), start,var.getHolidaysOption(), var.getNonworking(), buffer);
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(HolidaysVariable var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
