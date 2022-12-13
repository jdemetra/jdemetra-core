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

import demetra.data.DoubleSeq;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.TimeSeriesDomain;
import demetra.math.matrices.Matrix;
import demetra.timeseries.calendars.TradingDaysType;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.AllArgsConstructor
@lombok.Builder(builderClassName="Builder", toBuilder=true)
public class HolidaysCorrectedTradingDays implements ITradingDaysVariable, ISystemVariable {

    public static interface HolidaysCorrector {

        /**
         * Gets the corrections (in days) to be applied on normal calendars.For each period, the sum of the correction should be 0.
         *
         * @param domain
         * @param meanCorrected
         * @return The corrections for each period of the
         * domain. The different columns of the matrix correspond to
         * Mondays...Sundays. The dimensions of the matrix are (domain.length() x 7)
         */
        Matrix holidaysCorrection(TsDomain domain, boolean meanCorrected);
        
        /**
         * Gets the average annual corrections (in days) to be applied on Mondays...Sundays
         * The sum should be 0
         * @return An array of 7 elements
         */
        DoubleSeq longTermYearlyCorrection();

    }

    private DayClustering clustering;
    private boolean contrast;
    private boolean meanCorrection;
    private boolean weighted;
    private HolidaysCorrector corrector;

    public HolidaysCorrectedTradingDays(GenericTradingDays td, HolidaysCorrector corrector) {
        this.clustering = td.getClustering();
        this.contrast=td.getType() == GenericTradingDays.Type.CONTRAST;
        this.meanCorrection=contrast ? true : td.getType() == GenericTradingDays.Type.MEANCORRECTED;
        this.corrector = corrector;
        this.weighted=false;
    }

    @Override
    public int dim() {
        int n = clustering.getGroupsCount();
        return contrast ? n - 1 : n;
    }
    
    @Override
    public TradingDaysType getTradingDaysType(){
        return clustering.getType();
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context){
        return "Trading days";
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context){
        return GenericTradingDaysVariable.description(clustering, idx);
    }
    

}
