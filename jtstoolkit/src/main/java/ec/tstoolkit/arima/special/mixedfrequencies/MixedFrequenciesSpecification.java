/*
 * Copyright 2014 National Bank of Belgium
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
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.arima.special.RegressionSpec;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.IModellingSpecification;
import ec.tstoolkit.modelling.arima.DefaultArimaSpec;
import ec.tstoolkit.modelling.arima.IRegArimaSpecification;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequenciesSpecification implements IRegArimaSpecification, Cloneable {

    public static final String BASIC = "basic", ARIMA = "arima", REGRESSION = "regression", ESTIMATE="estimate";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        BasicSpec.fillDictionary(InformationSet.item(prefix, BASIC), dic);
        DefaultArimaSpec.fillDictionary(InformationSet.item(prefix, ARIMA), dic);
        RegressionSpec.fillDictionary(InformationSet.item(prefix, REGRESSION), dic);
        EstimateSpec.fillDictionary(InformationSet.item(prefix, ESTIMATE), dic);
    }

    private BasicSpec basic_ = new BasicSpec();
    private DefaultArimaSpec arima_ = new DefaultArimaSpec();
    private RegressionSpec regs_ = new RegressionSpec();
    private EstimateSpec estimate_=new EstimateSpec();

    public MixedFrequenciesSpecification() {
        arima_.airline();
    }
    
    public BasicSpec getBasic(){
        return basic_;
    }
    
    public void setBasic(BasicSpec spec){
        basic_=spec;
    }

    public DefaultArimaSpec getArima(){
        return arima_;
    }
    
    public void setArima(DefaultArimaSpec spec){
        arima_=spec;
    }

     public RegressionSpec getRegression(){
        return regs_;
    }
    
    public void setRegression(RegressionSpec spec){
        regs_=spec;
    }

   public EstimateSpec getEstimate(){
        return estimate_;
    }
    
    public void setEstimate(EstimateSpec spec){
        estimate_=spec;
    }
    
    @Override
    public MixedFrequenciesSpecification clone() {
        try {
            MixedFrequenciesSpecification cl = (MixedFrequenciesSpecification) super.clone();
            cl.basic_ = basic_.clone();
            cl.arima_ = arima_.clone();
            cl.regs_ = regs_.clone();
            cl.estimate_=estimate_.clone();
            return cl;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(BASIC, basic_.write(verbose));
        info.add(ARIMA, arima_.write(verbose));
        if (verbose || regs_.isUsed()) {
            info.add(REGRESSION, regs_.write(verbose));
        }
        info.add(ESTIMATE, estimate_.write(verbose));
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        InformationSet cinfo = info.getSubSet(BASIC);
        if (cinfo != null) {
            basic_.read(info);
        } else {
            basic_.reset();
        }
        cinfo = info.getSubSet(ARIMA);
        if (cinfo != null) {
            arima_.read(info);
        } else {
            arima_.airline();
        }
        cinfo = info.getSubSet(REGRESSION);
        if (cinfo != null) {
            regs_.read(info);
        } else {
            regs_.reset();
        }
       cinfo = info.getSubSet(ESTIMATE);
        if (cinfo != null) {
            estimate_.read(info);
        } else {
            estimate_.reset();
        }
        return true;
    }

    public boolean equals(MixedFrequenciesSpecification other) {
         return Objects.equals(basic_, other.basic_) && Objects.equals(arima_, other.arima_) && 
                 Objects.equals(regs_, other.regs_) && Objects.equals(estimate_, other.estimate_) ;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof MixedFrequenciesSpecification && equals((MixedFrequenciesSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.basic_);
        hash = 73 * hash + Objects.hashCode(this.arima_);
        return hash;
    }

    @Override
    public String toString(){
        StringBuilder builder =new StringBuilder();
        builder.append(basic_.getDataType());
        return builder.toString();
    }
}
