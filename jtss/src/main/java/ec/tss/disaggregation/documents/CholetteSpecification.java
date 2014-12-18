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

package ec.tss.disaggregation.documents;

import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import java.util.Map;

/**
 *
 * @author pcuser
 */
public class CholetteSpecification implements IProcSpecification, Cloneable {

    public static double DEF_LAMBDA = 1, DEF_RHO = 1;
    private double rho_ = DEF_RHO;
    private double lambda_ = DEF_LAMBDA;

    /**
     *
     * @return the rho_
     */
    public double getRho() {
        return rho_;
    }

    /**
     * @param rho the rho to set
     */
    public void setRho(double rho) {
        this.rho_ = rho;
    }

    /**
     * @return the lambda_
     */
    public double getLambda() {
        return lambda_;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda_ = lambda;
    }

    //////////////////////////////////////////////////////////////////////////
    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(LAMBDA, lambda_);
        info.add(RHO, rho_);
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            Double rho = info.get(RHO, Double.class);
            if (rho != null) {
                rho_ = rho;
            }
            Double lambda = info.get(LAMBDA, Double.class);
            if (lambda != null) {
                lambda_ = lambda;
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }
    
    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(RHO, Double.class);
        dic.put(LAMBDA, Double.class);
    }
    
    
    public static final String LAMBDA = "lambda",
            RHO = "rho";
    private static final String[] DICTIONARY = new String[]{
        LAMBDA, RHO
    };

    @Override
    public CholetteSpecification clone() {
        try {
            return (CholetteSpecification) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public boolean equals(CholetteSpecification other) {
        return rho_ == other.rho_
                && lambda_ == other.lambda_;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof CholetteSpecification && equals((CholetteSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.rho_) ^ (Double.doubleToLongBits(this.rho_) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.lambda_) ^ (Double.doubleToLongBits(this.lambda_) >>> 32));
        return hash;
    }
}
