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
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.calendars.DayClustering;
import nbbrd.design.Development;

/**
 *
 * @author palatej
 */

@lombok.Value
@Development(status=Development.Status.Release)
public class StockTradingDays implements ITradingDaysVariable, ISystemVariable{
    /**
     * W-th day of the month. 0-based! When w is negative, the (-w) day before the end of the month is considered
     * See documentation of X12 Arima, for instance.
    */
    private int w;

    @Override
    public int dim() {
        return 6;
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return "td";
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context){
        return GenericTradingDaysVariable.description(DayClustering.TD7, idx);
    }
}
