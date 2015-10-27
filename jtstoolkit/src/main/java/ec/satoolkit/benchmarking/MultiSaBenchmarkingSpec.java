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

package ec.satoolkit.benchmarking;

import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;

/**
 *
 * @author Jean Palate
 */
public class MultiSaBenchmarkingSpec implements IProcSpecification, Cloneable {
    
    public static enum ConstraintType{
        None,Fixed,Free
    }
    
    public boolean isEnabled(){
        return annualConstraints || contemporaneousConstraints != ConstraintType.None;
    }
    
    /**
     * @return the target_
     */
    public SaBenchmarkingSpec.Target getTarget() {
        return target_;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(SaBenchmarkingSpec.Target target) {
        this.target_ = target;
    }

    /**
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

    /**
     * @return the bias_
     */
    public boolean isAnnualConstraint() {
        return annualConstraints;
    }

    /**
     * @return the bias_
     */
    public ConstraintType getContemporaneousConstraintType() {
        return contemporaneousConstraints;
    }
 
    public void setAnnualConstraint(boolean type) {
        this.annualConstraints=type;
    }

    public void setContemporaneousConstraintType(ConstraintType type) {
        this.contemporaneousConstraints=type;
    }
    //////////////////////////////////////////////////////////////////////////
    @Override
    public InformationSet write(boolean verbose) {
        if (! verbose && ! isEnabled())
            return null;
        InformationSet info = new InformationSet();
        if (verbose || target_ != SaBenchmarkingSpec.Target.Original) {
            info.add(TARGET, target_.name());
        }
        if (verbose || lambda_ != DEF_LAMBDA) {
            info.add(LAMBDA, lambda_);
        }
        if (verbose || rho_ != DEF_RHO) {
            info.add(RHO, rho_);
        }
        if (verbose || contemporaneousConstraints != ConstraintType.None) {
            info.add(CONTEMPORANEOUSCONSTRAINT, contemporaneousConstraints.name());
        }
        if (verbose || annualConstraints) {
            info.add(ANNUALCONSTRAINT, annualConstraints);
        }
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
            Boolean enabled = info.get(ENABLED, Boolean.class);
            String target = info.get(TARGET, String.class);
            if (target != null) {
                target_=SaBenchmarkingSpec.Target.valueOf(target);
            }
            Boolean tcnt = info.get(ANNUALCONSTRAINT, Boolean.class);
            if (tcnt != null) {
                annualConstraints=tcnt;
            }
            String ccnt = info.get(CONTEMPORANEOUSCONSTRAINT, String.class);
            if (ccnt != null) {
                contemporaneousConstraints=ConstraintType.valueOf(ccnt);
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

//    @Override
//    public void fillDictionary(String prefix, List<String> dic) {
//        for (int i = 0; i < DICTIONARY.length; ++i) {
//            dic.add(InformationSet.item(prefix, DICTIONARY[i]));
//        }
//    }
    
    public static final String 
            ENABLED = "enabled",
            TARGET="target",
            LAMBDA = "lambda",
            RHO = "rho",
            ANNUALCONSTRAINT = "tcnts",
            CONTEMPORANEOUSCONSTRAINT = "ccnts"
            ;
    

    public static enum Target{
      Original,
      CalendarAdjusted
    }
    
    @Override
    public MultiSaBenchmarkingSpec clone(){
        try {
            return (MultiSaBenchmarkingSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
    
    public boolean equals(MultiSaBenchmarkingSpec other){
        return  target_ == other.target_ && rho_==other.rho_ &&
                lambda_ == other.lambda_ && annualConstraints == other.annualConstraints 
                && contemporaneousConstraints == other.contemporaneousConstraints;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof MultiSaBenchmarkingSpec && equals((MultiSaBenchmarkingSpec) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.rho_) ^ (Double.doubleToLongBits(this.rho_) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.lambda_) ^ (Double.doubleToLongBits(this.lambda_) >>> 32));
        return hash;
    }
   
    
    public static double DEF_LAMBDA=1, DEF_RHO=1;
    
    private boolean annualConstraints=false;
    private SaBenchmarkingSpec.Target target_=SaBenchmarkingSpec.Target.Original;
    private double rho_=DEF_RHO;
    private double lambda_=DEF_LAMBDA;
    private ConstraintType contemporaneousConstraints=ConstraintType.Fixed;
    
}
