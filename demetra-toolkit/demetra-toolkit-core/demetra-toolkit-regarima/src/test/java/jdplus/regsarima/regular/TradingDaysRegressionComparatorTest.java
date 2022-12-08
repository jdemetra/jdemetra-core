package jdplus.regsarima.regular;

import demetra.data.Data;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.LengthOfPeriod;
import jdplus.regarima.RegArimaEstimation;
import jdplus.sarima.SarimaModel;
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
    public void testRetail() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.RETAIL_BOOKSTORES);
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .test(model, TradingDaysRegressionComparator.ALL, 
                        new LengthOfPeriod(LengthOfPeriodType.LeapYear), 1e-5);
//        for (int i=0; i<test.length; ++i){
//            System.out.println(test[i].statistics().getAIC());
//        }
        int bestModel = TradingDaysRegressionComparator.bestModel(test, TradingDaysRegressionComparator.bicComparator());
        assertTrue(bestModel == 4);
    }

    @Test
    public void testWaldRetail() {
        TsData s = TsData.ofInternal(TsPeriod.monthly(1992, 1), Data.RETAIL_BOOKSTORES);
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .testRestrictions(model, TradingDaysRegressionComparator.ALL_NESTED, 
                        new LengthOfPeriod(LengthOfPeriodType.LeapYear), 1e-5);
        
        int waldTest = TradingDaysRegressionComparator.waldTest(test, 0.01, 0.1);
        assertTrue(waldTest == 4);
    }
    
    @Test
    public void testWaldProd() {
        TsData s = Data.TS_PROD;
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .testRestrictions(model, TradingDaysRegressionComparator.ALL_NESTED, 
                        new LengthOfPeriod(LengthOfPeriodType.LeapYear), 1e-5);
        
        int waldTest = TradingDaysRegressionComparator.waldTest(test, 0.01, 0.1);
        assertTrue(waldTest == 5);
    }
    
   @Test
    public void testWaldABS() {
        TsData s = Data.TS_ABS_RETAIL;
        ModelDescription model = new ModelDescription(s, null);
        model.setLogTransformation(true);

        RegArimaEstimation<SarimaModel>[] test = TradingDaysRegressionComparator
                .testRestrictions(model, TradingDaysRegressionComparator.ALL_NESTED, 
                        new LengthOfPeriod(LengthOfPeriodType.LeapYear), 1e-5);
        
        int waldTest = TradingDaysRegressionComparator.waldTest(test, 0.01, 0.1);
        assertTrue(waldTest == 5);
    }
}
