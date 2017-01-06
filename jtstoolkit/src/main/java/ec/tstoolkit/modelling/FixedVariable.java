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
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FixedVariable{

    public FixedVariable(ITsVariable var, double[] coeff) {
        variable = var;
        type = ComponentType.Undefined;
        this.coefficients=coeff;
    }

    public FixedVariable(ITsVariable var, ComponentType cmp, double[] coeff) {
        variable = var;
        type = cmp;
        this.coefficients=coeff;
    }

    public ITsVariable getVariable() {
        return variable;
    }

    public ITsVariable getRootVariable() {
        return TsVariableList.getRoot(variable);
    }

    public <T extends ITsVariable> boolean isCompatible(Class<T> tclass) {
        return tclass.isAssignableFrom(TsVariableList.getRoot(variable).getClass());
    }
    
    private static DataBlock internalRegressionEffect(Stream<FixedVariable> regs, TsDomain domain, Predicate<FixedVariable> pred){
        DataBlock z=new DataBlock(domain.getLength());
        regs.filter(reg -> pred.test(reg)).forEach(reg -> {
            Matrix m=RegressionUtilities.matrix(reg.getVariable(), domain);
            for (int i=0; i<reg.coefficients.length; ++i){
                z.addAY(reg.coefficients[i], m.column(i));
            }
        });
        return z;
    }
    
    public static DataBlock regressionEffect(Stream<FixedVariable> regs, TsDomain domain, Predicate<ITsVariable> pred){
        DataBlock z=new DataBlock(domain.getLength());
        regs.filter(reg -> pred.test(reg.variable)).forEach(reg -> {
            Matrix m=RegressionUtilities.matrix(reg.getVariable(), domain);
            for (int i=0; i<reg.coefficients.length; ++i){
                z.addAY(reg.coefficients[i], m.column(i));
            }
        });
        return z;
    }

    public static DataBlock regressionEffect(Stream<FixedVariable> regs, TsDomain domain, ComponentType type){
        return internalRegressionEffect(regs, domain, reg->reg.getType()==type);
    }
    
    public static DataBlock regressionEffect(Stream<FixedVariable> regs, TsDomain domain){
        return internalRegressionEffect(regs, domain, reg->true);
    }

    public static <T extends ITsVariable> DataBlock regressionEffect(Stream<FixedVariable> regs, TsDomain domain, Class<T> tclass){
        return internalRegressionEffect(regs, domain, reg->tclass.isInstance(TsVariableList.getRoot(reg.variable)));
    }

    private final ITsVariable variable;
    private final ComponentType type;
    private final double[] coefficients;

    /**
     * @return the type
     */
    public ComponentType getType() {
        return type;
    }

    /**
     * @return the coefficients
     */
    public double[] getCoefficients() {
        return coefficients;
    }
}
