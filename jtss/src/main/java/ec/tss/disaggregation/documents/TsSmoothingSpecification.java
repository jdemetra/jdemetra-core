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

import ec.benchmarking.simplets.TsExpander;
import ec.tss.disaggregation.processors.TsSmoothingProcessor;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class TsSmoothingSpecification implements IProcSpecification {

    public static final String PARAM = "param", CNT = "constant", TREND = "trend",
            TYPE = "aggregationtype", MODEL = "model", NFREQ = "newfrequency", NB = "nb", NF = "nf";

    private Parameter param_ = new Parameter();
    private boolean cnt_ = false, trend_ = false;
    private TsAggregationType type_ = TsAggregationType.Sum;
    private TsExpander.Model model_ = TsExpander.Model.I1;
    private TsFrequency nfreq_ = TsFrequency.Monthly;
    private int nb_, nf_;

    public Parameter getParameter() {
        return param_;
    }

    public void setParameter(Parameter p) {
        param_ = p;
    }

    public TsExpander.Model getModel() {
        return model_;
    }

    public void setModel(TsExpander.Model model) {
        model_ = model;
    }

    public TsAggregationType getAggregationType() {
        return type_;
    }

    public void setAggregationType(TsAggregationType type) {
        type_ = type;
    }

    public boolean isConstant() {
        return cnt_;
    }

    public void setConstant(boolean c) {
        cnt_ = c;
    }

    public boolean isTrend() {
        return trend_;
    }

    public void setTrend(boolean t) {
        trend_ = t;
    }

    public TsFrequency getNewFrequency() {
        return nfreq_;
    }

    public void setNewFrequency(TsFrequency nfreq) {
        nfreq_ = nfreq;
    }

    public int getBackcastsCount() {
        return nb_;
    }

    public void setBackcastsCount(int nb) {
        nb_ = nb;
    }

    public int getForecastsCount() {
        return nf_;
    }

    public void setForecastsCount(int nf) {
        nf_ = nf;
    }

    @Override
    public TsSmoothingSpecification clone() {
        try {
            TsSmoothingSpecification spec = (TsSmoothingSpecification) super.clone();
            if (param_ != null) {
                spec.param_ = param_.clone();
            }
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(ALGORITHM, TsSmoothingProcessor.DESCRIPTOR);
        if (cnt_ || verbose) {
            info.add(CNT, cnt_);
        }
        if (trend_ || verbose) {
            info.add(TREND, trend_);
        }
        if (!Parameter.isDefault(param_)) {
            info.add(PARAM, param_);
        }
        info.add(TYPE, type_.name());
        info.add(MODEL, model_.name());
        info.add(NFREQ, nfreq_.intValue());
        if (nb_ != 0 || verbose) {
            info.add(NB, nb_);
        }
        if (nf_ != 0 || verbose) {
            info.add(NF, nf_);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        Boolean b = info.get(CNT, Boolean.class);
        if (b != null) {
            cnt_ = b;
        }
        b = info.get(TREND, Boolean.class);
        if (b != null) {
            trend_ = b;
        }
        Parameter p = info.get(PARAM, Parameter.class);
        if (p != null) {
            param_ = p;
        }
        String s = info.get(TYPE, String.class);
        if (s != null) {
            type_ = TsAggregationType.valueOf(s);
        }
        s = info.get(MODEL, String.class);
        if (s != null) {
            model_ = TsExpander.Model.valueOf(s);
        }
        Integer f = info.get(NFREQ, Integer.class);
        if (f != null) {
            nfreq_ = TsFrequency.valueOf(f);
        } else {
            return false;
        }
        Integer n = info.get(NB, Integer.class);
        if (n != null) {
            nb_ = n;
        }
        n = info.get(NF, Integer.class);
        if (n != null) {
            nf_ = n;
        }
        return true;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, CNT), Boolean.class);
        dic.put(InformationSet.item(prefix, TREND), Boolean.class);
        dic.put(InformationSet.item(prefix, PARAM), Parameter.class);
        dic.put(InformationSet.item(prefix, MODEL), String.class);
        dic.put(InformationSet.item(prefix, TYPE), String.class);
        dic.put(InformationSet.item(prefix, NFREQ), Integer.class);
        dic.put(InformationSet.item(prefix, NB), Integer.class);
        dic.put(InformationSet.item(prefix, NF), Integer.class);
    }

}
