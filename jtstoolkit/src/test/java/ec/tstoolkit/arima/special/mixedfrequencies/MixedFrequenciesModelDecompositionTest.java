/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.DataType;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequenciesModelDecompositionTest {

    public MixedFrequenciesModelDecompositionTest() {
    }

    //@Test
    public void demoSomeMethod() {
        MixedFrequenciesSpecification spec = new MixedFrequenciesSpecification();
        MixedFrequenciesMonitor monitor = new MixedFrequenciesMonitor();
        spec.getRegression().getTradingDays().setTradingDaysType(TradingDaysType.TradingDays);
        spec.getBasic().setLog(true);
        int ny = 5;
        TsData s = data.Data.X;
        TsData Q = s.drop(0, s.getLength() - ny * 12).changeFrequency(TsFrequency.Quarterly, TsAggregationType.Sum, true);
        TsData M = s.drop(ny * 12, 0);

        boolean rslt = monitor.process(Q, M, spec);
        MixedFrequenciesModelDecomposition decomposer = new MixedFrequenciesModelDecomposition();
        decomposer.decompose(M, Q, monitor.getArima(), true, DataType.Flow, true);

        TsDataTable table = new TsDataTable();
        table.insert(-1, monitor.getInterpolatedSeries());
        table.insert(-1, monitor.getInterpolationErrors());
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.Series, ComponentInformation.Value));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Value));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.Series, ComponentInformation.Stdev));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Stdev));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Stdev));
        table.insert(-1, decomposer.getDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Stdev));

        System.out.println(table);
        System.out.println(monitor.getArima());
        System.out.println(decomposer.getUcarimaModel());

    }

}
