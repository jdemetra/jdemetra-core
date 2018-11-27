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
package demetra.random;

import demetra.design.AlgorithmImplementation;
import java.util.Random;

/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm=RandomNumberGenerator.class)
public class SystemRNG extends AbstractRNG{
    
    private final Random rnd;

    public SystemRNG(final long seed) {
        rnd=new Random(seed);
    }

    @Override
    public int nextInt() {
        return rnd.nextInt();
    }
    
    @Override
    public int nextInt(int bound) {
        return rnd.nextInt(bound);
    }
    
    @Override
    public long nextLong() {
        return rnd.nextLong();
    }
    
    @Override
    public float nextFloat() {
        return rnd.nextFloat();
    }
    
    @Override
    public double nextDouble() {
        return rnd.nextDouble();
    }
    
    
}
