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

package ec.satoolkit.x13;

import ec.satoolkit.AbstractSaSpecification;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.IDefaultSeriesDecomposer;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.satoolkit.x11.X11Specification;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class X13Specification extends AbstractSaSpecification implements ISaSpecification, Cloneable {

    public static final String REGARIMA = "regarima", X11 = "x11", BENCH = "benchmarking", RSA = "method";
   private static final String SMETHOD = "X13";
     
    public static void fillDictionary(String prefix, Map<String, Class> dic){
        RegArimaSpecification.fillDictionary(InformationSet.item(prefix, REGARIMA), dic);
        X11Specification.fillDictionary(InformationSet.item(prefix, X11), dic);
        SaBenchmarkingSpec.fillDictionary(InformationSet.item(prefix, BENCH), dic);
    }
    
    private RegArimaSpecification regSpec_;
    private X11Specification x11Spec_;
    private SaBenchmarkingSpec benchSpec_;
    public static final X13Specification RSAX11, RSA0, RSA1, RSA2, RSA3, RSA4, RSA5;

    static {
        X11Specification xdef = new X11Specification();
        X11Specification x11 = new X11Specification();
        x11.setMode(DecompositionMode.Multiplicative);
        x11.setForecastHorizon(0);
        RSAX11 = new X13Specification(RegArimaSpecification.RGDISABLED, x11);
        RSA0 = new X13Specification(RegArimaSpecification.RG0, xdef);
        RSA1 = new X13Specification(RegArimaSpecification.RG1, xdef);
        RSA2 = new X13Specification(RegArimaSpecification.RG2, xdef);
        RSA3 = new X13Specification(RegArimaSpecification.RG3, xdef);
        RSA4 = new X13Specification(RegArimaSpecification.RG4, xdef);
        RSA5 = new X13Specification(RegArimaSpecification.RG5, xdef);
    }

    public X13Specification() {
        regSpec_ = new RegArimaSpecification();
        x11Spec_ = new X11Specification();
        benchSpec_ = new SaBenchmarkingSpec();
    }

    public X13Specification(final RegArimaSpecification regSpec, final X11Specification x11Spec) {
        regSpec_ = regSpec;
        x11Spec_ = x11Spec;
        benchSpec_ = new SaBenchmarkingSpec();
    }

    @Override
    public X13Specification clone() {
        try {
            X13Specification spec = (X13Specification) super.clone();
            spec.regSpec_ = regSpec_.clone();
            spec.x11Spec_ = x11Spec_.clone();
            spec.benchSpec_ = benchSpec_.clone();
            return spec;
        }
        catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     * @return the regSpec_
     */
    public RegArimaSpecification getRegArimaSpecification() {
        return regSpec_;
    }

    /**
     * @param regSpec_ the regSpec_ to set
     */
    public void setRegArimaSpecification(RegArimaSpecification regSpec_) {
        this.regSpec_ = regSpec_;
    }

    /**
     * @return the x11Spec_
     */
    public X11Specification getX11Specification() {
        return x11Spec_;
    }

    /**
     * @param x11Spec_ the x11Spec_ to set
     */
    public void setX11Specification(X11Specification x11Spec_) {
        this.x11Spec_ = x11Spec_;
    }

    public SaBenchmarkingSpec getBenchmarkingSpecification() {
        return benchSpec_;
    }

    public void setBenchmarkingSpecification(SaBenchmarkingSpec benchSpec) {
        if (benchSpec == null) {
            throw new java.lang.IllegalArgumentException(BENCH);
        }
        this.benchSpec_ = benchSpec;
    }

    public IPreprocessor buildPreprocessor() {
        return regSpec_.build();
    }

    public IDefaultSeriesDecomposer buildDecomposer() {
        return new X11Decomposer(x11Spec_);
    }

    @Override
    protected void checkContext(ProcessingContext context) {
        if (regSpec_.getRegression() == null) {
            return;
        }
        int nregs = regSpec_.getRegression().getUserDefinedVariablesCount();
        if (nregs > 0) {
            if (!checkVariables(regSpec_.getRegression().getUserDefinedVariables(), context)) {
                throw new X13Exception(X13Exception.ERR_CONTEXT);
            }
        }

        TradingDaysSpec td = regSpec_.getRegression().getTradingDays();
        if (td == null) {
            return;
        }
        if (!checkVariables(td.getUserVariables(), context)) {
            throw new X13Exception(X13Exception.ERR_CONTEXT);
        }
        if (!checkCalendar(td.getHolidays(), context)) {
            throw new X13Exception(X13Exception.ERR_CONTEXT);
        }
    }

    public static X13Specification fromString(String name) {
        if (name.equals("X11")) {
            return RSAX11;
        }
        if (name.equals("RSA0")) {
            return RSA0;
        }
        if (name.equals("RSA1")) {
            return RSA1;
        }
        if (name.equals("RSA2c")) {
            return RSA2;
        }
        if (name.equals("RSA3")) {
            return RSA3;
        }
        if (name.equals("RSA4c")) {
            return RSA4;
        }
        if (name.equals("RSA5c")) {
            return RSA5;
        }
        return new X13Specification();
    }

    public boolean isSystem() {
        return this == RSAX11 || this == RSA0 || this == RSA1 || this == RSA2
                || this == RSA3 || this == RSA4 || this == RSA5;
    }

    public X13Specification matchSystem() {
        if (isSystem()) {
            return this;
        }
        else if (equals(RSAX11)) {
            return RSAX11;
        }
        else if (equals(RSA0)) {
            return RSA0;
        }
        else if (equals(RSA1)) {
            return RSA1;
        }
        else if (equals(RSA2)) {
            return RSA2;
        }
        else if (equals(RSA3)) {
            return RSA3;
        }
        else if (equals(RSA4)) {
            return RSA4;
        }
        else if (equals(RSA5)) {
            return RSA5;
        }
        else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (this == RSAX11) {
            return "X11";
        }
        if (this == RSA0) {
            return "RSA0";
        }
        if (this == RSA1) {
            return "RSA1";
        }
        if (this == RSA2) {
            return "RSA2c";
        }
        if (this == RSA3) {
            return "RSA3";
        }
        if (this == RSA4) {
            return "RSA4c";
        }
        if (this == RSA5) {
            return "RSA5c";
        }
        if (equals(RSAX11)) {
            return "X11";
        }
        if (equals(RSA0)) {
            return "RSA0";
        }
        if (equals(RSA1)) {
            return "RSA1";
        }
        if (equals(RSA2)) {
            return "RSA2c";
        }
        if (equals(RSA3)) {
            return "RSA3";
        }
        if (equals(RSA4)) {
            return "RSA4c";
        }
        if (equals(RSA5)) {
            return "RSA5c";
        }
        return SMETHOD;
    }

    @Override
    public String toLongString() {
        String s = toString();
        if (s.length() == 0) {
            return SMETHOD;
        }
        else {
            StringBuilder builder = new StringBuilder();
            builder.append("X13[").append(s).append(']');
            return builder.toString();
        }
    }

//    @Override
//    public List<String> summary() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
    private boolean getrsa(InformationSet pinfo) {
        String rsa = pinfo.get(RSA, String.class);
        if (rsa != null) {
            RegArimaSpecification.Default option = RegArimaSpecification.Default.valueOf(rsa);
            switch (option) {
                case RG0:
                    regSpec_ = RegArimaSpecification.RG0.clone();
                    return true;
                case RG1:
                    regSpec_ = RegArimaSpecification.RG1.clone();
                    return true;
                case RG2:
                    regSpec_ = RegArimaSpecification.RG2.clone();
                    return true;
                case RG3:
                    regSpec_ = RegArimaSpecification.RG3.clone();
                    return true;
                case RG4:
                    regSpec_ = RegArimaSpecification.RG4.clone();
                    return true;
                case RG5:
                    regSpec_ = RegArimaSpecification.RG5.clone();
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof X13Specification && equals((X13Specification) obj));
    }

    private boolean equals(X13Specification spec) {
        return Objects.equals(spec.regSpec_, regSpec_)
                && Objects.equals(spec.x11Spec_, x11Spec_)
                && Objects.equals(spec.benchSpec_, benchSpec_);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.regSpec_);
        hash = 47 * hash + Objects.hashCode(this.x11Spec_);
        return hash;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dictionary
    @Override
    public InformationSet write(boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add(ALGORITHM, X13ProcessingFactory.DESCRIPTOR);
        InformationSet tinfo = regSpec_.write(verbose);
        if (tinfo != null) {
            specInfo.add(REGARIMA, tinfo);
        }
        InformationSet sinfo = x11Spec_.write(verbose);
        if (sinfo != null) {
            specInfo.add(X11, sinfo);
        }
        InformationSet binfo = benchSpec_.write(verbose);
        if (binfo != null) {
            specInfo.add(BENCH, binfo);
        }
        return specInfo;
    }

    @Override
    public boolean read(InformationSet info) {
        InformationSet tinfo = info.getSubSet(REGARIMA);
        if (tinfo != null) {
            boolean tok = regSpec_.read(tinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet sinfo = info.getSubSet(X11);
        if (sinfo != null) {
            boolean tok = x11Spec_.read(sinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet binfo = info.getSubSet(BENCH);
        if (binfo != null) {
            boolean tok = benchSpec_.read(binfo);
            if (!tok) {
                return false;
            }
        }
        return true;
    }

}
