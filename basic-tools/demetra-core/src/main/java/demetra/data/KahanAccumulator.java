/*
 * Copyright 2017 National Bank copyOf Belgium
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
package demetra.data;

/**
 * Kahan algorithm
 * @author Jean Palate <jean.palate@nbb.be>
 */
public strictfp final class KahanAccumulator implements DoubleAccumulator{
    
    private double del, sum;
    
    @Override
    public void reset(){
        del=0;
        sum=0;
    }
    
    @Override
    public void add(double t){
        double tc=t-del; // del is the last accumulated error 
        double nsum=sum+tc; // new sum  (we add the opposite of the last accumulated error (which should dsappear)
        del=(nsum-sum)-tc; // the new accumulated error
        sum=nsum;
    }
    
    @Override
    public double sum(){
        return sum;
    }
    
    @Override
    public void set(double value){
        del=0;
        sum=value;
    }
    
    
}
