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
public class MultiTsDataProcessing implements IProcessing<TsData[], MultiTsData> {

    public static final String SERIES = "series";
    private final TsPeriodSelector selector_;
    private Validation validation;

    @Override
    public MultiTsData process(TsData[] input) {
        TsData[] minput;
        if (selector_ == null || selector_.getType() == PeriodSelectorType.All) {
            minput = input;
        } else {
            minput = new TsData[input.length];
            for (int i = 0; i < minput.length; ++i) {
                minput[i] = input[i].select(selector_);
            }
        }
        if (validation != null) {
            for (int i = 0; i < minput.length; ++i) {
                if (!validation.validate(minput)) {
                    return null;
                }
            }
        }
        return new MultiTsData(SERIES, minput);
    }

    public static interface Validation {

        boolean validate(TsData[] s);
    }

    public MultiTsDataProcessing(TsPeriodSelector sel) {
        this.selector_ = sel;
    }

    public void setValidation(Validation val) {
        validation = val;
    }

    public Validation getValidation() {
        return validation;
    }

}
