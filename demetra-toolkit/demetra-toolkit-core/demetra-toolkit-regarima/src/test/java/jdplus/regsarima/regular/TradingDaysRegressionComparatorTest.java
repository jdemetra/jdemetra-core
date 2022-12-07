package jdplus.regsarima.regular;

import demetra.data.Data;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import jdplus.regarima.RegArimaEstimation;
import jdplus.sarima.SarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author palatej
 */
public class TradingDaysRegressionComparatorTest {

    public TradingDaysRegressionComparatorTest() {
    }

    @Test
    public void testProd() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.RETAIL_BOOKSTORES);
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator.test(model, TradingDaysRegressionComparator.ALL, 1e-5);
//        for (int i=0; i<test.length; ++i){
//            System.out.println(test[i].statistics().getBIC());
//        }
        int bestModel = TradingDaysRegressionComparator.bestModel(test, TradingDaysRegressionComparator.bicComparator());
        assertTrue(bestModel == 2);
    }

}
