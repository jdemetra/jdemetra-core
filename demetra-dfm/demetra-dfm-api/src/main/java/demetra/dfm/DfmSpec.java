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

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
//public class DfmSpec implements Cloneable {
//
//    public static final String MSPEC = "model", ESPEC = "estimation", SASPEC = "sa", SIMULATION = "simulation";
//    private DfmModelSpec model_ = new DfmModelSpec();
//    private DfmEstimationSpec estimation_ = new DfmEstimationSpec();
//    private DfmSimulationSpec simulation_ = new DfmSimulationSpec();
//    private ISaSpecification sa_ = TramoSeatsSpecification.RSAfull;
//
//    public DfmModelSpec getModelSpec() {
//        return model_;
//    }
//
//    public void setModelSpec(DfmModelSpec spec) {
//        model_ = spec;
//    }
//
//    public DfmSimulationSpec getSimulationSpec() {
//        return simulation_;
//    }
//
//    public void setSimulationSpec(DfmSimulationSpec simulation) {
//        this.simulation_ = simulation;
//    }
//
//    public DfmEstimationSpec getEstimationSpec() {
//        return estimation_;
//    }
//
//    public void setEstimationSpec(DfmEstimationSpec spec) {
//        estimation_ = spec;
//    }
//
//    public ISaSpecification getSaSpec() {
//        return sa_;
//    }
//
//    public void setSaSpecification(ISaSpecification sa) {
//        sa_ = sa;
//    }
//
//    //@Override
//    public DfmSpec clone() {
//        try {
//            DfmSpec spec = (DfmSpec) super.clone();
//            spec.model_ = model_.clone();
//            spec.estimation_ = estimation_.clone();
//            spec.sa_ = sa_.clone();
//            spec.simulation_ = simulation_.clone();
//            return spec;
//        } catch (CloneNotSupportedException ex) {
//            throw new AssertionError();
//        }
//    }
//
//    public DfmSpec cloneDefinition() {
//        DfmSpec spec = clone();
//        spec.model_.clear();
//        return spec;
//    }
//
//    @Override
//    public InformationSet write(boolean verbose) {
//        InformationSet info = new InformationSet();
//        info.add(MSPEC, model_.write(verbose));
//        info.add(ESPEC, estimation_.write(verbose));
//        info.add(SIMULATION, simulation_.write(verbose));
//        info.add(SASPEC, sa_.write(verbose));
//        return info;
//    }
//
//    @Override
//    public boolean read(InformationSet info) {
//        if (info == null) {
//            return true;
//        }
//        if (!model_.read(info.getSubSet(MSPEC))) {
//            return false;
//        }
//        if (!estimation_.read(info.getSubSet(ESPEC))) {
//            return false;
//        }
//        if (!simulation_.read(info.getSubSet(SIMULATION))) {
//            return false;
//        }        
//
//        return sa_.read(info.getSubSet(SASPEC));
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return this == obj || (obj instanceof DfmSpec && equals((DfmSpec) obj));
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 29 * hash + Objects.hashCode(this.model_);
//        hash = 29 * hash + Objects.hashCode(this.estimation_);
//        hash = 29 * hash + Objects.hashCode(this.simulation_);
//        return hash;
//    }
//
//    public boolean equals(DfmSpec spec) {
//        return model_.equals(spec.model_) && estimation_.equals(spec.estimation_) 
//                && sa_.equals(spec.sa_) && simulation_.equals(spec.simulation_);
//    }
//
//    public static void fillDictionary(String prefix, Map<String, Class> dic) {
//        DfmModelSpec.fillDictionary(MSPEC, dic);
//        DfmEstimationSpec.fillDictionary(ESPEC, dic);
//        DfmSimulationSpec.fillDictionary(SIMULATION, dic);
//    }
//
//}
