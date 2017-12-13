/*
 * Copyright 2013-2014 National Bank of Belgium
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
package demetra.dfm;

import static demetra.dfm.MeasurementType.getMeasurementType;
import static demetra.dfm.MeasurementType.measurement;


/**
 *
 * @author Jean Palate
 */
public class MeasurementDescriptor {

public static final double C_DEF = .2;    

/**
     * Type of the measurement equation
     */
    private final IDfmMeasurement type;

    private final double[] coeff;
    /**
     * Variance of the measurement equation (>=0)
     */
    private final double var; // DAVID: why not final?
    /**
     * Creates a new measurement descriptor
     *
     * @param type Type of the measurement equation
     * @param coeff Coefficients (1 by factor). Unused factors are identified by
     * a "Double.NaN" coefficient.
     * @param var Variance of the measurement equation (>=0)
     */
    public MeasurementDescriptor(final IDfmMeasurement type,
            final double[] coeff, final double var) {
        this.type = type;
        this.coeff = coeff.clone();
        this.var = var;
    }

    // WHY DO WE NEED THIS CONSTRUCTOR?
    public MeasurementDescriptor(final MeasurementStructure structure, final double var) {
        this.type = measurement(structure.type);
        this.coeff = new double[structure.used.length];
        for (int i = 0; i < coeff.length; ++i) {
            if (!structure.used[i]) {
                coeff[i] = Double.NaN;
            } else {
                coeff[i] = C_DEF;
            }
        }
        this.var = var;   // DAVID: why is equal to 1?  I think it must be initialized
    }

    public void seDefaultCoefficients() {
        for (int i = 0; i < coeff.length; ++i) {
            if (isUsed(i)) {
                coeff[i] = C_DEF;
            }
        }
    }
    
    public double getVar(){
        return var;
    }
    
    public double getCoefficient(int pos){
        return coeff[pos];
    }
    
    public IDfmMeasurement getType(){
        return type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < coeff.length; ++i) {
            if (Double.isNaN(coeff[i])) {
                builder.append('.');
            } else {
                builder.append(coeff[i]);
            }
            builder.append('\t');
        }
        builder.append(var);
        return builder.toString();
    }


    public boolean isUsed(int fac) {
        return !Double.isNaN(coeff[fac]);
    }

    public boolean[] getUsedFactors() {
        boolean[] used = new boolean[coeff.length]; // DAVID: I THINK THIS LINE SHOULD BE COMMENTED (OTHERWISE IT CREATES A NEW USED)
        for (int i = 0; i < used.length; ++i) {
            used[i] = !Double.isNaN(coeff[i]);
        }
        return used;
    }

    public int getUsedFactorsCount() {
        int n = 0;
        for (int i = 0; i < coeff.length; ++i) {
            if (!Double.isNaN(coeff[i])) {
                ++n;
            }
        }
        return n;
    }

    public MeasurementStructure getStructure() {
        return new MeasurementStructure(getMeasurementType(type), getUsedFactors());
    }

}
