/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.stats.tests;

import jd.data.DataBlock;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TestOfUpDownRunsTest {
    
    public TestOfUpDownRunsTest() {
    }

    @Test
    public void testLegacy() {
        int N=100;
        DataBlock X=DataBlock.make(N);
        Random rnd=new Random(0);
        X.set(rnd::nextDouble);
        
        TestOfUpDownRuns runs=new TestOfUpDownRuns(X);
        
        double lvalue = runs.testLength().getValue();
        double nvalue = runs.testNumber().getValue();
        
        ec.tstoolkit.stats.TestofUpDownRuns oruns=new ec.tstoolkit.stats.TestofUpDownRuns();
        oruns.test(new ec.tstoolkit.data.ReadDataBlock(X.getStorage()));
        oruns.setKind(ec.tstoolkit.stats.RunsTestKind.Length);
        double olvalue = oruns.getValue();
        
        oruns.setKind(ec.tstoolkit.stats.RunsTestKind.Number);
        double onvalue = oruns.getValue();
        
        assertEquals(lvalue, olvalue, 1e-9);
        assertEquals(nvalue, onvalue, 1e-9);
    }
    
    @Test
    public void testLegacyWithMissing() {
        int N=100;
        DataBlock X=DataBlock.make(N);
        Random rnd=new Random(0);
        X.set(rnd::nextDouble);
        
        X.set(1, Double.NaN);
        X.range(56, 63).set(Double.NaN);
        
        TestOfUpDownRuns runs=new TestOfUpDownRuns(X);
        
        double lvalue = runs.testLength().getValue();
        double nvalue = runs.testNumber().getValue();
        
        ec.tstoolkit.stats.TestofUpDownRuns oruns=new ec.tstoolkit.stats.TestofUpDownRuns();
        oruns.test(new ec.tstoolkit.data.ReadDataBlock(X.getStorage()));
        oruns.setKind(ec.tstoolkit.stats.RunsTestKind.Length);
        double olvalue = oruns.getValue();
        
        oruns.setKind(ec.tstoolkit.stats.RunsTestKind.Number);
        double onvalue = oruns.getValue();
        
        assertEquals(lvalue, olvalue, 1e-9);
        assertEquals(nvalue, onvalue, 1e-9);
    }
}
