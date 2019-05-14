/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.r;

import demetra.data.Data;
import demetra.data.MatrixSerializer;
import demetra.data.WeeklyData;
import static demetra.timeseries.simplets.TsDataToolkit.log;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FractionalAirlineDecompositionTest {
    
    public FractionalAirlineDecompositionTest() {
    }
    
    public static void main(String[] cmds) throws IOException{
        MatrixType M = MatrixSerializer.read(new File("c://highfreq/data/births.txt"));
        FractionalAirlineDecomposition.Results rslt = FractionalAirlineDecomposition.process(M.column(0).toArray(), 365.25, true, true);
        double[] s = rslt.getData("s", double[].class);
        System.out.println(DoubleSeq.of(s));
        double[] n = rslt.getData("n", double[].class);
        System.out.println(DoubleSeq.of(n));
    }

    @Test
    public void testWeekly() {
        FractionalAirlineDecomposition.Results rslt = FractionalAirlineDecomposition.process(WeeklyData.US_CLAIMS, 365.25/7, true, false);
//        System.out.println(rslt.getUcarima());
//        System.out.println(DoubleSequence.ofInternal(rslt.getData("sa", double[].class)));
        assertTrue(null != rslt.getData("sa", double[].class));
        Map<String, Class> dictionary=new LinkedHashMap<>();
        FractionalAirlineDecomposition.Results.getMapping().fillDictionary(null, dictionary, true);
        
//        dictionary.keySet().forEach(s->System.out.println(s));
        
//        System.out.println(rslt.getData("ucarima.component(2).var", Double.class));
    }
    
}
