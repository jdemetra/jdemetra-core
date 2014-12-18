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

import ec.tstoolkit.algorithm.IProcessing.Status;

/**
 *
 * @author Jean Palate
 */
public class StaticDocument <S extends IProcSpecification, I, R extends IProcResults>
extends AbstractDocument<S,I,R>
{
    private final I input_;
    private final S spec_;
    private final R results_;
    private final String desc_;
    
    public StaticDocument(String desc, S spec, I input, R results){
        desc_=desc;
        spec_=spec;
        input_=input;
        results_=results;
    }

    public Status getStatus() {
        return results_ == null ? Status.Invalid : Status.Valid;
    }

    @Override
    public S getSpecification() {
        return spec_;
    }

    @Override
    public R getResults() {
        return results_;
    }
 
    @Override
    public I getInput() {
        return input_;        
    }

    @Override
    public String getDescription() {
        return desc_;
    }

 }
