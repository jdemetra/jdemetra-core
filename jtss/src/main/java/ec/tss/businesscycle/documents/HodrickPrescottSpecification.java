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

package ec.tss.businesscycle.documents;

import ec.tss.businesscycle.processors.HodrickPrescottProcessingFactory;
import ec.satoolkit.SaSpecification;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class HodrickPrescottSpecification implements IProcSpecification, Cloneable {

    public static final double DEF_LAMBDA = 1600;

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, TARGET), String.class);
        dic.put(InformationSet.item(prefix, LAMBDA), Double.class);
        dic.put(InformationSet.item(prefix, CYCLELENGTH), Double.class);
        SaSpecification.fillDictionary(InformationSet.item(prefix, SASPEC),dic);
    }
    
    private Target target = Target.Original;
    private SaSpecification saSpec = new SaSpecification();
    private double lambda = DEF_LAMBDA;
    private double cycleLength = 0;

    /**
     * @return the target
     */
    public Target getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * @return the saSpec
     */
    public SaSpecification getSaSpecification() {
        return saSpec;
    }

    /**
     * @param saSpec the saSpec to set
     */
    public void setSaSpecification(SaSpecification saSpec) {
        this.saSpec = saSpec;
    }

    /**
     * @return the lambda
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * @return the cycleLength
     */
    public double getCycleLength() {
        return cycleLength;
    }

    /**
     * @param cycleLength the cycleLength to set
     */
    public void setCycleLength(double cycleLength) {
        this.cycleLength = cycleLength;
    }

    public static enum Target {

        Original, Trend, Sa
    };

    public boolean isDefault() {
        return saSpec.isDefault() && cycleLength == 0 && target == Target.Original
                && lambda == DEF_LAMBDA;
    }

    @Override
    public HodrickPrescottSpecification clone() {
        try {
            HodrickPrescottSpecification spec = (HodrickPrescottSpecification) super.clone();
            spec.setSaSpecification(getSaSpecification().clone());
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(ALGORITHM, HodrickPrescottProcessingFactory.DESCRIPTOR);
        InformationSet saspec = saSpec.write(verbose);
        if (saspec != null) {
            info.set(SASPEC, saspec);
        }
        if (target != Target.Original || verbose) {
            info.set(TARGET, target.name());
        }
        if (lambda != DEF_LAMBDA || verbose) {
            info.set(LAMBDA, lambda);
        }
        if (cycleLength != 0 || verbose) {
            info.set(CYCLELENGTH, cycleLength);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            InformationSet spec = info.getSubSet(SASPEC);
            if (spec != null) {
                saSpec.read(spec);
            }
            String t = info.get(TARGET, String.class);
            if (t != null) {
                target = Target.valueOf(t);
            }
            Double l = info.get(LAMBDA, Double.class);
            if (l != null) {
                lambda = l;
            }
            Double c = info.get(CYCLELENGTH, Double.class);
            if (c != null) {
                cycleLength = c;
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }
    public static final String CYCLELENGTH = "cyclelength", LAMBDA = "lambda", SASPEC = "saspec", TARGET = "target";
}
