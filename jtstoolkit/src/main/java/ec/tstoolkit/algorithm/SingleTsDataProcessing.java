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
package ec.tstoolkit.algorithm;

import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class SingleTsDataProcessing implements IProcessing<TsData, SingleTsData> {

    public static final String SERIES = "series";

    @Override
    public SingleTsData process(final TsData input) {
        TsData sinput = input; //.cleanExtremities();
        if (selector_ != null && selector_.getType() != PeriodSelectorType.All) {
            sinput = sinput.select(selector_);
        }
        if (validation != null && !validation.validate(sinput)) {
            return null;
        } else {
            return new SingleTsData(SERIES, sinput);
        }
    }

    public static interface Validation {

        boolean validate(TsData s);
    }
    private final TsPeriodSelector selector_;
    private Validation validation;

    public SingleTsDataProcessing(TsPeriodSelector sel) {
        this.selector_ = sel;
    }

    public void setValidation(Validation val) {
        validation = val;
    }

    public Validation getValidation() {
        return validation;
    }

}
