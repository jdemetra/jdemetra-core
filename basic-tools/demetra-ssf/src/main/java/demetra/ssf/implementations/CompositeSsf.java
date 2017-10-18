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
package demetra.ssf.implementations;

import demetra.ssf.ISsfDynamics;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;
import demetra.ssf.ISsfInitialization;

/**
 *
 * @author Jean Palate
 */
public class CompositeSsf extends Ssf{
    
    public static int[] dimensions(ISsf... ssf){
        int[] dim=new int[ssf.length];
        for (int i=0; i<dim.length; ++i){
            dim[i]=ssf[i].getStateDim();
        }
        return dim;
    }
    
    public static int[] dimensions(IMultivariateSsf... ssf){
        int[] dim=new int[ssf.length];
        for (int i=0; i<dim.length; ++i){
            dim[i]=ssf[i].getStateDim();
        }
        return dim;
    }

     CompositeSsf(ISsfInitialization initializer, ISsfDynamics dyn, ISsfMeasurement m){
        super(initializer, dyn, m);
    }
    
    public static CompositeSsf of(double var, ISsf... ssf ){
        int[] dim=dimensions(ssf);
        int n=0;
        ISsfInitialization[] initializer =new ISsfInitialization[ssf.length];
        ISsfMeasurement[] measurement =new ISsfMeasurement[ssf.length];
        ISsfDynamics[] dynamics =new ISsfDynamics[ssf.length];
        for (int i=0; i<ssf.length; ++i){
            ISsf cur=ssf[i];
            n+=cur.getStateDim();
            initializer[i]=cur.getInitialization();
            measurement[i]=cur.getMeasurement();
            dynamics[i]=cur.getDynamics();
        }
        return new CompositeSsf(new CompositeInitialization(dim, initializer), 
                new CompositeDynamics(dim, dynamics), new CompositeMeasurement(dim, measurement, var));
    }
    
    public static CompositeSsf of(WeightedCompositeMeasurement.IWeights weights, ISsf... ssf ){
        ISsfMeasurement m=WeightedCompositeMeasurement.of(weights, ssf);
        if (m == null)
            return null;
        int[] dim=dimensions(ssf);
        int n=0;
        ISsfInitialization[] initializer =new ISsfInitialization[ssf.length];
        ISsfDynamics[] dynamics =new ISsfDynamics[ssf.length];
        for (int i=0; i<ssf.length; ++i){
            ISsf cur=ssf[i];
            n+=cur.getStateDim();
            initializer[i]=cur.getInitialization();
            dynamics[i]=cur.getDynamics();
        }
        return new CompositeSsf(new CompositeInitialization(dim, initializer), 
                new CompositeDynamics(dim, dynamics), m);
    }
}
