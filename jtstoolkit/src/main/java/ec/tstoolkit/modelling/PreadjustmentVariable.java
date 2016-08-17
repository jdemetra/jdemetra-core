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

package ec.tstoolkit.modelling;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.regression.AbstractTsVariableBox;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Jean Palate
 */
public class PreadjustmentVariable {
    
    private final UserVariable variable;
    private final double[] coefficients;
    
    public PreadjustmentVariable(UserVariable var, double[] coef){
        if (var.getDim() != coef.length)
            throw new TsException("Invalid preadjustment variable");
        variable=var;
        coefficients=coef;
    }
    
    public ComponentType getType(){
        return variable.getType();
    }
    
    public void addTo(TsData sum){
        Matrix M=new Matrix(sum.getLength(), coefficients.length);
        List<DataBlock> cols = M.columnList();
        variable.data(sum.getDomain(), cols);
        TsDataBlock all=TsDataBlock.all(sum);
        for (int i=0; i<coefficients.length; ++i){
            double c=coefficients[i];
            all.data.addAY(c, cols.get(i));
        }
    }
    
    public TsData data(TsDomain domain){
        TsData sum=new TsData(domain, 0);
        addTo(sum);
        return sum;
    }
    
    public static TsData sum(List<PreadjustmentVariable> vars, TsDomain domain, Predicate<UserVariable> pred){
        TsData sum=new TsData(domain, 0);
        for (PreadjustmentVariable var:vars){
            if(pred.test(var.variable)){
                var.addTo(sum);
            }
        }
        return sum;
    }
    
    public static TsData sum(List<PreadjustmentVariable> vars, TsDomain domain, ComponentType type){
        return sum(vars, domain, var->var.getType() == type);
    }
    
   public static TsData sum(List<PreadjustmentVariable> vars, TsDomain domain){
        return sum(vars, domain, var->true);
    }
}
