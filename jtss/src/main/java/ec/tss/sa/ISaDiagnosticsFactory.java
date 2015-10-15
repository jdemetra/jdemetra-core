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

package ec.tss.sa;

import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnosticsFactory;
import ec.tstoolkit.design.IntValue;
import ec.tstoolkit.design.ServiceDefinition;
import java.util.Comparator;

/**
 *
 * @author Jean Palate
 */
@ServiceDefinition
public interface ISaDiagnosticsFactory
        extends IDiagnosticsFactory<CompositeResults> {

    public static enum Scope implements IntValue {

        General(0),
        Preliminary(1),
        Modelling(2),
        Decomposition(3),
        Final(4);
        private final int value;

        Scope(final int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }
    }

    Scope getScope();

    int getOrder();
    
    public static class DiagnosticOrdering implements Comparator<ISaDiagnosticsFactory>{

        @Override
        public int compare(ISaDiagnosticsFactory diag1, ISaDiagnosticsFactory diag2) {
            int s1=diag1.getScope().value, s2=diag2.getScope().value;
            if (s1 < s2)
                return -1;
            if (s1 > s2)
                return 1;
            // s1 == s2
            int o1=diag1.getOrder(), o2=diag2.getOrder();
            if (o1 < o2)
                return -1;
            if (o1 > o2)
                return 1;
            else
                return 0;
        }
        
    }
}
