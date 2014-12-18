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

package ec.tss.sa.composite;

import ec.satoolkit.ISaSpecification;
import ec.satoolkit.benchmarking.MultiSaBenchmarkingSpec;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author pcuser
 */
public class MultiSaSpecification implements IProcSpecification, Cloneable {

    public static final String DIRECT = "direct", INDIRECT = "indirect", DEFAULT = "__default", COMPONENT = "component", BENCHMARKING = "benchmarking";

    public static void fillDictionary(String prefix, HashMap<String, Class> dic) {
        // TODO
    }
    private ISaSpecification totalSpec_, defSpec_;
    private List<ISaSpecification> specs_ = new ArrayList<>();
    private MultiSaBenchmarkingSpec benchSpec_=new MultiSaBenchmarkingSpec();

    public void set(int pos, ISaSpecification spec) {
        specs_.set(pos, spec);
    }

    public void add(ISaSpecification spec) {
        specs_.add(spec);
    }
    
    public MultiSaBenchmarkingSpec getBenchmarkingSpecification(){
        return benchSpec_;
    }

    public void setBenchmarkingSpecification(MultiSaBenchmarkingSpec spec){
        if (spec == null)
            throw new IllegalArgumentException();
        benchSpec_=spec;
    }

    public ISaSpecification getSpecification(int pos) {
        int nspecs = specs_.size();
        if (pos >= nspecs) {
            return defSpec_;
        }
        else {
            return specs_.get(pos);
        }
    }

    public ISaSpecification getTotalSpecification() {
        return totalSpec_;
    }

    public void setTotalSpecification(ISaSpecification spec) {
        totalSpec_ = spec;
    }

    public ISaSpecification getDefaultSpecification() {
        return defSpec_;
    }

    public void setDefaultSpecification(ISaSpecification spec) {
        defSpec_ = spec;
        specs_.clear();
    }

    @Override
    public MultiSaSpecification clone() {
        try {
            MultiSaSpecification c = (MultiSaSpecification) super.clone();
            c.benchSpec_=benchSpec_.clone();
            c.specs_.addAll(specs_);
            return c;
        }
        catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(ALGORITHM, MultiSaProcessingFactory.DESCRIPTOR);

        info.add(DIRECT, totalSpec_.write(verbose));
        info.add(DEFAULT, defSpec_.write(verbose));

        for (int i = 0; i < specs_.size(); ++i) {
            String name = COMPONENT + (i + 1);
            info.add(name, specs_.get(i).write(verbose));
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
