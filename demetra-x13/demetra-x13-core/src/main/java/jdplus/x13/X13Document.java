/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.x13;

import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcQuality;
import demetra.processing.ProcessingLog;
import demetra.processing.ProcessingStatus;
import demetra.sa.HasSaEstimation;
import demetra.sa.SaEstimation;
import demetra.sa.SaSpecification;
import demetra.timeseries.AbstractTsDocument;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.x13.X13Spec;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
public class X13Document extends AbstractTsDocument<X13Spec, X13Results> implements HasSaEstimation {

    private final ModellingContext context;

    public X13Document() {
        super(X13Spec.RSA4);
        context = ModellingContext.getActiveContext();
    }

    public X13Document(ModellingContext context) {
        super(X13Spec.RSA4);
        this.context = context;
    }

    public ModellingContext getContext(){
        return context;
    }

    @Override
    protected X13Results internalProcess(X13Spec spec, TsData data) {
        return X13Kernel.of(spec, context).process(data, ProcessingLog.dummy());
    }

    @Override
    public SaEstimation getEstimation() {
        if (getStatus() != ProcessingStatus.Valid) {
            return null;
        }
        List<ProcDiagnostic> tests = new ArrayList<>();
        X13Results result = getResult();
        X13Factory.getInstance().fillDiagnostics(tests, result);
        SaSpecification pspec = X13Factory.getInstance().generateSpec(getSpecification(), result);
        ProcQuality quality = ProcDiagnostic.summary(tests);
        return SaEstimation.builder()
                .results(result)
                .log(result.getLog())
                .diagnostics(tests)
                .quality(quality)
                .pointSpec(pspec)
                .build();
    }
}
