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

import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.timeseries.analysis.ITsProcessing;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate
 */
public class TsDocumentProcessing<R extends IProcResults> implements ITsProcessing<R> {

    private final IProcessing<TsData, R> processing_;
    private final TsData data_;

    public <S extends IProcSpecification> TsDocumentProcessing(TsDocument<S, R> doc) {
        processing_ = doc.getProcessor().generateProcessing(doc.getSpecification(), doc.getContext());
        data_ = doc.getInput().getTsData();
    }

    @Override
    public R process(TsDomain domain) {
        if (data_ == null) {
            return null;
        }
        return processing_.process(data_.fittoDomain(domain));
    }
}
