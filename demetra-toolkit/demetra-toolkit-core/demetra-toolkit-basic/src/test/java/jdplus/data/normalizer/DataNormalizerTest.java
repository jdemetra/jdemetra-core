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
package jdplus.data.normalizer;

import jdplus.data.DataBlock;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DataNormalizerTest {
    
    public DataNormalizerTest() {
    }

    @Test
    public void testAll() {
        DataBlock z=DataBlock.make(100);
        Random rnd=new Random(0);
        z.set(i->rnd.nextDouble()*1000000);
        
        new ThousandNormalizer().normalize(z.deepClone());
        new RootMeanSquaresNormalizer().normalize(z.deepClone());
        new AbsMeanNormalizer().normalize(z.deepClone());
        new DecimalNormalizer().normalize(z.deepClone());
        
        z.set(i->rnd.nextDouble()/1000000);
        
        new ThousandNormalizer().normalize(z.deepClone());
        new RootMeanSquaresNormalizer().normalize(z.deepClone());
        new AbsMeanNormalizer().normalize(z.deepClone());
        new DecimalNormalizer().normalize(z.deepClone());
    }
    
}
