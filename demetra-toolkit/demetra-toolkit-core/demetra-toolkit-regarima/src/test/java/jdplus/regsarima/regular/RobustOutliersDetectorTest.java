package jdplus.regsarima.regular;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class RobustOutliersDetectorTest {
    
    public RobustOutliersDetectorTest() {
    }

    @Test
    public void testProd() {
        double[] data = Data.PROD.clone();
        data[125]=400;
        data[12]=6;
        TsData s=TsData.ofInternal(TsPeriod.monthly(1967, 1), data);
        
        RobustOutliersDetector tool = RobustOutliersDetector.builder()
                .build();
        DoubleSeq sc=tool.process(s.getValues(), s.getAnnualFrequency(), null);
        System.out.println(sc);
    }
    
}
