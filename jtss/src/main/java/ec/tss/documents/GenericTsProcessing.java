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
package ec.tss.documents;

import ec.tss.Ts;
import ec.tss.TsInformationType;
import ec.tss.TsStatus;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * Generic processor for transforming processor needing TsData to
 * processor designed for Ts
 * @author Jean Palate
 */
@Development(status=Development.Status.Alpha)
public class GenericTsProcessing<R extends IProcResults> implements IProcessing<Ts, R> {

    private final IProcessing<TsData, R> processing_;

    public GenericTsProcessing(IProcessing<TsData, R> processing) {
        processing_ = processing;
    }

    @Override
    public R process(Ts input) {
        if (input.hasData() == TsStatus.Undefined) {
            input.load(TsInformationType.Data);
        }
        TsData s = input.getTsData();
        if (s == null) {
            throw new TsException("No data: "+input.getName());
        } else {
            return processing_.process(s);
        }
    }
}
