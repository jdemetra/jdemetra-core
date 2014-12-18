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
 * 
 */
package ec.tstoolkit.algorithm;

import ec.tstoolkit.algorithm.IProcessing.Status;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class DefaultProcessingFactory {

    public static IProcessingNode<TsData> createInitialStep(final TsPeriodSelector selector, final SingleTsDataProcessing.Validation validation) {
        return new IProcessingNode<TsData>() {
            @Override
            public String getName() {
                return IProcDocument.INPUT;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public Status process(TsData input, Map<String, IProcResults> results) {
                SingleTsDataProcessing processing = new SingleTsDataProcessing(selector);
                processing.setValidation(validation);
                SingleTsData rslt = processing.process(input);
                if (rslt != null) {
                    results.put(IProcDocument.INPUT, rslt);
                    return Status.Valid;
                } else {
                    return Status.Invalid;
                }
            }
        };
    }

    public static IProcessingNode<TsData[]> createInitialStep(final TsPeriodSelector selector, final MultiTsDataProcessing.Validation validation) {
        return new IProcessingNode<TsData[]>() {
            @Override
            public String getName() {
                return IProcDocument.INPUT;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public Status process(TsData[] input, Map<String, IProcResults> results) {
                MultiTsDataProcessing processing = new MultiTsDataProcessing(selector);
                processing.setValidation(validation);
                MultiTsData rslt = processing.process(input);
                if (rslt != null) {
                    results.put(IProcDocument.INPUT, rslt);
                    return Status.Valid;
                } else {
                    return Status.Invalid;
                }
            }
        };
    }
}
