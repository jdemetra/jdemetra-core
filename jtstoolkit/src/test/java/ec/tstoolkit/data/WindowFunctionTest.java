/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.data;

import ec.tstoolkit.arima.AutoCovarianceFunction;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class WindowFunctionTest {

    private AutoCovarianceFunction fn;

    public WindowFunctionTest() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setP(3);
        spec.setQ(3);
        spec.setBP(1);
        spec.setBQ(1);
        fn = new SarimaModel(spec).getAutoCovarianceFunction();
    }

    @Test
    
    public void testParzenMethod() {
        for (int len = 1; len < 12; ++len) {
//            System.out.println(new WindowFunction(WindowType.Parzen, len).compute(i -> fn.get(Math.abs(i))));
            new WindowFunction(WindowType.Parzen, len).compute(i -> fn.get(Math.abs(i)));
        }
    }

   @Test
    public void testBartlettMethod() {
        for (int len = 1; len < 12; ++len) {
//            System.out.println(new WindowFunction(WindowType.Bartlett, len).compute(i -> fn.get(Math.abs(i))));
            new WindowFunction(WindowType.Bartlett, len).compute(i -> fn.get(Math.abs(i)));
        }
    }
}
