/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tss.sa.processors;

import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class X13ProcessorTest {
    
    public X13ProcessorTest() {
    }

    @Test
    @Ignore
    public void testOutputDictionary(){
        Map<String, Class> dic = new X13Processor().getOutputDictionary(true);
        dic.forEach((s,c)->{
            System.out.print(s);
            System.out.print('\t');
            System.out.println(c.getCanonicalName());
        });
        System.out.println("");
        dic = new X13Processor().getOutputDictionary(false);
        dic.forEach((s,c)->{
            System.out.print(s);
            System.out.print('\t');
            System.out.println(c.getCanonicalName());
        });
    }
    
}
