/*
 * Copyright 2013 National Bank of Belgium
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
package jdplus.regsarima.regular;

import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.Variable;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaUtility;
import jdplus.sarima.SarimaModel;
import demetra.timeseries.regression.ITradingDaysVariable;
import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import jdplus.regarima.IRegArimaComputer;

/**
 */
@lombok.experimental.UtilityClass
public class TradingDaysRegressionComparator {

    public static final ITradingDaysVariable[] ALL = new ITradingDaysVariable[]{
        new GenericTradingDaysVariable(DayClustering.TD2, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD3, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD3c, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD4, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD7, GenericTradingDays.Type.CONTRAST)};

    public static final ITradingDaysVariable[] ALL_NESTED = new ITradingDaysVariable[]{
        new GenericTradingDaysVariable(DayClustering.TD2, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD3, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD4, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD7, GenericTradingDays.Type.CONTRAST)};
    
    public static final ITradingDaysVariable[] DEFAULT = new ITradingDaysVariable[]{
        new GenericTradingDaysVariable(DayClustering.TD2, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD3, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD7, GenericTradingDays.Type.CONTRAST)};

    public RegArimaEstimation<SarimaModel>[] test(ModelDescription description, ITradingDaysVariable[] td, double eps) {

        RegArimaEstimation<SarimaModel>[] rslt = new RegArimaEstimation[td.length];

        ModelDescription refdesc = ModelDescription.copyOf(description);
        refdesc.setAirline(true);
        refdesc.setMean(true);
        refdesc.remove("td");

        IRegArimaComputer<SarimaModel> processor = RegArimaUtility.processor(true, eps);
        for (int i = 0; i < td.length; ++i) {
            ModelDescription cdesc = ModelDescription.copyOf(refdesc);
            cdesc.addVariable(Variable.variable("td", td[i]));
            cdesc.estimate(processor);
            try {
                rslt[i] = processor.process(cdesc.regarima(), null);
            } catch (Exception err) {
                rslt[i] = null;
            }
        }

        return rslt;
    }

    public int bestModel(RegArimaEstimation<SarimaModel>[] rslt, Comparator<RegArimaEstimation<SarimaModel>> cmp) {
        int best = 0;
        while (rslt[best] == null && best < rslt.length) {
            ++best;
        }
        if (best == rslt.length) {
            return -1;
        }

        for (int i = best + 1; i < rslt.length; ++i) {
            if (rslt[i] != null && cmp.compare(rslt[best], rslt[i]) > 0) {
                best = i;
            }
        }
        return best;
    }

    public Comparator<RegArimaEstimation<SarimaModel>> aicComparator() {
        return comparator(e -> e.statistics().getAIC());
    }

    public Comparator<RegArimaEstimation<SarimaModel>> aiccComparator() {
        return comparator(e -> e.statistics().getAICC());
    }

    public Comparator<RegArimaEstimation<SarimaModel>> biccComparator() {
        return comparator(e -> e.statistics().getBICC());
    }

    public Comparator<RegArimaEstimation<SarimaModel>> bicComparator() {
        return comparator(e -> e.statistics().getBIC());
    }

    private Comparator<RegArimaEstimation<SarimaModel>> comparator(ToDoubleFunction<RegArimaEstimation<SarimaModel>> fn) {
        return (RegArimaEstimation<SarimaModel> e1, RegArimaEstimation<SarimaModel> e2) -> {
            if (e1 == null) {
                return 1;
            } else if (e2 == null) {
                return -1;
            } else {
                return Double.compare(fn.applyAsDouble(e1), fn.applyAsDouble(e2));
            }
        };
    }
}
