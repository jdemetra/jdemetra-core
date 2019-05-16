/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.data.analysis;

import jdplus.data.DataBlock;
import demetra.design.Demo;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class TukeyHanningTaperTest {
    
    public TukeyHanningTaperTest() {
    }

    @Demo
    public void testRandom() {
        double[] x=new double[60];
        DataBlock X=DataBlock.of(x);
        Random rnd=new Random();
        X.set(rnd::nextGaussian);
        TukeyHanningTaper taper=new TukeyHanningTaper(.5);
        System.out.println(X);
        taper.process(x);
        System.out.println(X);
    }
    
}
