/*
* Copyright 2013 National Bank of Belgium
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

package ec.tstoolkit.eco.discrete;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 *
 * @author pcuser
 */
public class DiscreteModelTest {
    
    public DiscreteModelTest() {
    }

    //@Test
    public void demoSomeMethod() {
        int n=10000;
        int[] y=new int[n];
        for (int i=0; i<y.length; ++i){
            y[i]=i<n/2 ? 0 :1;
        }
        
        Matrix M =new Matrix(n, 100);
        M.randomize();
        M.add(-.5);
        M.column(0).set(1);
        
        DiscreteModel model=new DiscreteModel(new Probit());
        //LbfgsMinimizer min=new LbfgsMinimizer();
        //min.setMemoryLength(3);
        //model.setMinimizer(min);
        DiscreteModelEvaluation rslt = model.process(y, M);
        IReadDataBlock parameters = rslt.getParameters();
        System.out.println(parameters);
        System.out.println(new DataBlock(rslt.gradient()));
        model=new DiscreteModel(new Logit());
        rslt = model.process(y, M);
        parameters = rslt.getParameters();
        System.out.println(parameters);
        System.out.println(new DataBlock(rslt.gradient()));
    }
}
