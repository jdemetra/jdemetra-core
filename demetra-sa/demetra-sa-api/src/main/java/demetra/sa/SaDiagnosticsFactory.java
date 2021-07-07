/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sa;

import demetra.processing.Diagnostics;
import demetra.processing.DiagnosticsFactory;
import demetra.processing.ProcDiagnostic;
import java.util.Comparator;
import java.util.List;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author PALATEJ
 * @param <R> Output
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE, mutability = Mutability.NONE, singleton = true)
public interface SaDiagnosticsFactory<R> extends DiagnosticsFactory<R> {

    public static enum Scope {

        General(0),
        Preliminary(1),
        Modelling(2),
        Decomposition(3),
        Final(4);
        private final int value;

        Scope(final int value) {
            this.value = value;
        }

        public int intValue() {
            return value;
        }
    }

    Scope getScope();

    int getOrder();

    public static class Ordering implements Comparator<SaDiagnosticsFactory> {

        @Override
        public int compare(SaDiagnosticsFactory diag1, SaDiagnosticsFactory diag2) {
            int s1 = diag1.getScope().value, s2 = diag2.getScope().value;
            if (s1 < s2) {
                return -1;
            }
            if (s1 > s2) {
                return 1;
            }
            // s1 == s2
            int o1 = diag1.getOrder(), o2 = diag2.getOrder();
            if (o1 < o2) {
                return -1;
            }
            if (o1 > o2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    default void fill(List<ProcDiagnostic> tests, R sa, String category) {
        if (sa == null) {
            return;
        }
        Diagnostics diags = of(sa);
        if (diags == null) {
            return;
        }
        for (String test : diags.getTests()) {
            ProcDiagnostic item = ProcDiagnostic.builder()
                    .diagnostic(test)
                    .category(category)
                    .quality(diags.getDiagnostic(test))
                    .value(diags.getValue(test))
                    .warnings(diags.getWarnings())
                    .build();
            tests.add(item);
        }
     }

}
