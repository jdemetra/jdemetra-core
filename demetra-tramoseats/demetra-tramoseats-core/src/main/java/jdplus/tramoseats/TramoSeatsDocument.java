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
package jdplus.tramoseats;

import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcQuality;
import demetra.processing.ProcessingLog;
import demetra.processing.ProcessingStatus;
import demetra.sa.HasSaEstimation;
import demetra.sa.SaEstimation;
import demetra.sa.SaSpecification;
import demetra.timeseries.TsData;
import demetra.timeseries.AbstractTsDocument;
import demetra.timeseries.regression.ModellingContext;
import demetra.tramoseats.TramoSeatsSpec;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsDocument extends AbstractTsDocument<TramoSeatsSpec, TramoSeatsResults> implements HasSaEstimation {

    private final ModellingContext context;

    public TramoSeatsDocument() {
        super(TramoSeatsSpec.RSAfull);
        context = ModellingContext.getActiveContext();
    }

    public TramoSeatsDocument(ModellingContext context) {
        super(TramoSeatsSpec.RSAfull);
        this.context = context;
    }
    
    public ModellingContext getContext(){
        return context;
    }

    @Override
    protected TramoSeatsResults internalProcess(TramoSeatsSpec spec, TsData data) {
        return TramoSeatsKernel.of(spec, context).process(data, ProcessingLog.dummy());
    }

    @Override
    public SaEstimation getEstimation() {
        if (getStatus() != ProcessingStatus.Valid) {
            return null;
        }
        List<ProcDiagnostic> tests = new ArrayList<>();
        TramoSeatsResults result = getResult();
        TramoSeatsFactory.INSTANCE.fillDiagnostics(tests, result);
        SaSpecification pspec = TramoSeatsFactory.INSTANCE.generateSpec(getSpecification(), result);
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
