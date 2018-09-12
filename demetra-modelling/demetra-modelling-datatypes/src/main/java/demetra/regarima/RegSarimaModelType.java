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
package demetra.regarima;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.linearmodel.LinearModelType;
import demetra.sarima.SarimaType;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder(toBuilder=true)
public class RegSarimaModelType {
    
    @lombok.NonNull
    @lombok.experimental.Delegate
    private LinearModelType model;
    
    @lombok.NonNull
    private SarimaType sarima;
    
    private static final int[] NOMISSING = new int[0];
    private static final DoubleSequence[] NOX=new DoubleSequence[0];
}
