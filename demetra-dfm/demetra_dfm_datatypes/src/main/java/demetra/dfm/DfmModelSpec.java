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
package demetra.dfm;

import demetra.var.VarSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class DfmModelSpec implements Cloneable {

//    private VarSpecification vspec;
//    private List<MeasurementSpec> mspecs = new ArrayList<>();
//    private int fh;
//
//    private static final int DEF_FH = 12;
//
//    public DfmModelSpec() {
//        vspec = new VarSpecification();
//        vspec.setSize(2, 2);
//        fh = -1;
//    }
//
//    /**
//     * Gets the forecast horizon (in years)
//     *
//     * @return
//     */
//    public int getForecastHorizon() {
//        return fh;
//    }
//
//    public void setForecastHorizon(final int fh) {
//        this.fh = fh;
//    }
//
//    public VarSpec getVarSpec() {
//        return vspec;
//    }
//
//    public void setVarSpec(VarSpec spec) {
//        vspec = spec;
//    }
//
//    public List<MeasurementSpec> getMeasurements() {
//        return mspecs;
//    }
//    
//    public List<Integer> getPublicationDelays() {
//        List<Integer> delays = new ArrayList<>();
//        for (MeasurementSpec m : getMeasurements()) {
//            delays.add(m.getDelay());
//        }
//        
//        return delays;
//    }
//
//    public boolean isSpecified() {
//        for (MeasurementSpec mspec : mspecs) {
//            if (!mspec.isSpecified()) {
//                return false;
//            }
//        }
//        return vspec.isSpecified();
//    }
//
//    public boolean isDefined() {
//        for (MeasurementSpec mspec : mspecs) {
//            if (!mspec.isDefined()) {
//                return false;
//            }
//        }
//        return vspec.isDefined();
//    }
//
//    public boolean setParameterType(ParameterType type) {
//        for (MeasurementSpec mspec : mspecs) {
//            if (!mspec.setParameterType(type)) {
//                return false;
//            }
//        }
//        return vspec.setParameterType(type);
//    }
//
//    @Override
//    public DfmModelSpec clone() {
//        try {
//            DfmModelSpec spec = (DfmModelSpec) super.clone();
//            spec.vspec = vspec.clone();
//            spec.mspecs = new ArrayList<>();
//            for (MeasurementSpec mspec : mspecs) {
//                spec.mspecs.add(mspec.clone());
//            }
//
//            return spec;
//        } catch (CloneNotSupportedException ex) {
//            throw new AssertionError();
//        }
//    }
//
//    @Override
//    public InformationSet write(boolean verbose) {
//        InformationSet info = new InformationSet();
//        info.add(VSPEC, vspec.write(verbose));
//        int i = 0;
//        for (MeasurementSpec mspec : mspecs) {
//            info.add(MSPEC + (i++), mspec.write(verbose));
//        }
//        
//        if (verbose || fh != DEF_FH) {
//            info.add(FHORIZON, fh);
//        }
//        
//        return info;
//    }
//
//    @Override
//    public boolean read(InformationSet info) {
//        if (info == null) {
//            return true;
//        }
//        vspec = new VarSpec();
//        if (!vspec.read(info.getSubSet(VSPEC))) {
//            return false;
//        }
//        mspecs.clear();
//        List<Information<InformationSet>> sel = info.select(MSPEC + "*", InformationSet.class);
//        for (Information<InformationSet> m : sel) {
//            MeasurementSpec x = new MeasurementSpec(vspec.getEquationsCount());
//            if (x.read(m.value)) {
//                mspecs.add(x);
//            }
//        }
//        Integer f = info.get(FHORIZON, Integer.class);
//        if (f != null) {
//            fh = f;
//        }
//
//        return true;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return this == obj || (obj instanceof DfmModelSpec && equals((DfmModelSpec) obj));
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 89 * hash + Objects.hashCode(this.vspec);
//        return hash;
//    }
//
//    public boolean equals(DfmModelSpec spec) {
//        if (spec.fh != fh) {
//            return false;
//        }
//        if (!vspec.equals(spec.vspec)) {
//            return false;
//        }
//        if (mspecs.size() != spec.mspecs.size()) {
//            return false;
//        }
//        for (int i = 0; i < mspecs.size(); ++i) {
//            if (!mspecs.get(i).equals(spec.mspecs.get(i))) {
//                return false;
//            }
//        }
//        
//        return true;
//    }
//
//    public boolean copyParameters(DynamicFactorModel m) {
//        if (vspec.getEquationsCount() != m.getFactorsCount()) {
//            return false;
//        }
//        if (vspec.getLagsCount() != m.getTransition().nlags) {
//            return false;
//        }
//        if (mspecs.size() != m.getMeasurementsCount()) {
//            return false;
//        }
//        // fill the transition equation
//        Table<Parameter> v = vspec.getVarParams();
//        Matrix vparams = m.getTransition().varParams;
//        for (int r = 0; r < v.getRowsCount(); ++r) {
//            for (int c = 0; c < v.getColumnsCount(); ++c) {
//                v.set(r, c, convert(vparams.get(r, c)));
//            }
//        }
//        // copy noises
//        Table<Parameter> n = vspec.getNoiseParams();
//        Matrix tvar = m.getTransition().covar;
//        for (int r = 0; r < n.getRowsCount(); ++r) {
//            n.set(r, r, convert(tvar.get(r, r)));
//            for (int c = 0; c < r; ++c) {
//                n.set(r, c, convert(tvar.get(r, c)));
//                n.set(c, r, convert(tvar.get(r, c)));
//            }
//        }
//        // copy measurements
//        int nf = m.getFactorsCount();
//        int i = 0;
//        for (MeasurementDescriptor mdesc : m.getMeasurements()) {
//            MeasurementSpec mspec = mspecs.get(i++);
//            mspec.setVariance(convert(mdesc.var));
//            for (int j = 0; j < nf; ++j) {
//                mspec.setCoefficient(j, convertCoeff(mdesc.coeff[j]));
//            }
//        }
//        return true;
//    }
//
//    private static Parameter convertCoeff(double v) {
//        if (Double.isNaN(v)) {
//            return new Parameter(0, ParameterType.Fixed);
//        } else {
//            return new Parameter(v, ParameterType.Estimated);
//        }
//    }
//
//    private static Parameter convert(double v) {
//        if (Double.isNaN(v)) {
//            return new Parameter();
//        } else {
//            return new Parameter(v, ParameterType.Estimated);
//        }
//    }
//
//    public DynamicFactorModel build() {
//        int nb = vspec.getEquationsCount(), nl = vspec.getLagsCount();
//        int blocksize = 0;
//        for (MeasurementSpec m : mspecs) {
//            IMeasurement type = DynamicFactorModel.measurement(m.getFactorsTransformation());
//            int len = type.getLength();
//            if (len > blocksize) {
//                blocksize = len;
//            }
//        }
//        if (blocksize < nl) {
//            blocksize = nl;
//        }
//        DynamicFactorModel dfm = new DynamicFactorModel(blocksize, nb);
//        DynamicFactorModel.TransitionDescriptor tdesc
//                = new DynamicFactorModel.TransitionDescriptor(nb, nl);
//        // copy equations
//        Table<Parameter> v = vspec.getVarParams();
//        for (int r = 0; r < v.getRowsCount(); ++r) {
//            for (int c = 0; c < v.getColumnsCount(); ++c) {
//                Parameter p = v.get(r, c);
//                if (Parameter.isDefined(p)) {
//                    tdesc.varParams.set(r, c, p.getValue());
//                }
//            }
//        }
//        // copy noises
//        Table<Parameter> n = vspec.getNoiseParams();
//        for (int r = 0; r < n.getRowsCount(); ++r) {
//            for (int c = 0; c <= r; ++c) {
//                Parameter p = n.get(r, c);
//                if (Parameter.isDefined(p)) {
//                    tdesc.covar.set(r, c, p.getValue());
//                }
//            }
//        }
//        SymmetricMatrix.fromLower(tdesc.covar);
//        dfm.setTransition(tdesc);
//        // measurements
//        for (MeasurementSpec m : mspecs) {
//            IMeasurement type = DynamicFactorModel.measurement(m.getFactorsTransformation());
//            double[] coeff = new double[nb];
//            Parameter p[] = m.getCoefficients();
//            for (int i = 0; i < nb; ++i) {
//                if (Parameter.isDefined(p[i])) {
//                    if (p[i].isFixed() && p[i].getValue() == 0) {
//                        coeff[i] = Double.NaN;
//                    } else if (p[i].getType() != ParameterType.Undefined) {
//                        coeff[i] = p[i].getValue();
//                    } else {
//                        coeff[i] = DynamicFactorModel.C_DEF;
//
//                    }
//                }
//            }
//            double var = 1;
//            if (!Parameter.isDefault(m.getVariance())) {
//                var = m.getVariance().getValue();
//            }
//            dfm.addMeasurement(new MeasurementDescriptor(type, coeff, var));
//        }
//        dfm.setInitialization(vspec.getInitialization());
//        return dfm;
//    }
//
//    public void clear() {
//        vspec.clear();
//        for (MeasurementSpec m : mspecs) {
//            m.clear();
//        }
//    }
//
//    public static void fillDictionary(String prefix, Map<String, Class> dic) {
//        VarSpec.fillDictionary(InformationSet.item(prefix, VSPEC), dic);
//        MeasurementSpec.fillDictionary(InformationSet.item(prefix, MSPECS), dic);
//        dic.put(InformationSet.item(prefix, FHORIZON), Integer.class);
//    }
//
}
