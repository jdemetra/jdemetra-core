/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.tramoseats.workspace;

import demetra.modelling.implementations.SarimaSpec;
import demetra.modelling.io.information.TsDocumentMapping;
import demetra.processing.ProcSpecification;
import demetra.processing.TsDataProcessor;
import demetra.processing.TsDataProcessorFactory;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.tramo.Tramo;
import demetra.tramo.TramoSpec;
import demetra.tramoseats.TramoSeats;
import demetra.tramoseats.TramoSeatsResults;
import demetra.tramoseats.TramoSeatsSpec;
import demetra.tramoseats.io.information.TramoSeatsSpecMapping;
import demetra.tramoseats.io.information.TramoSpecMapping;
import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.informationSet;
import static demetra.workspace.WorkspaceFamily.parse;
import demetra.workspace.file.spi.FamilyHandler;
import java.util.Collections;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoSeatsHandlers {

    public final WorkspaceFamily SA_DOC_TRAMOSEATS = parse("Seasonal adjustment@documents@tramoseats");
    public final WorkspaceFamily SA_SPEC_TRAMOSEATS = parse("Seasonal adjustment@specifications@tramoseats");

    public final WorkspaceFamily MOD_DOC_TRAMO = parse("Modelling@documents@tramo");
    public final WorkspaceFamily MOD_SPEC_TRAMO = parse("Modelling@specifications@tramo");

    @ServiceProvider(FamilyHandler.class)
    public static final class ModDocTramo implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_TRAMO,
                TsDocumentMapping.serializer(TramoSpecMapping.SERIALIZER_V3,
                        new TsDataProcessorFactory<TramoSpec, GeneralLinearModel<SarimaSpec>>() {
                    @Override
                    public boolean canHandle(ProcSpecification spec) {
                        return spec instanceof TramoSeatsSpec;
                    }

                    @Override
                    public TsDataProcessor<GeneralLinearModel<SarimaSpec>> generateProcessor(TramoSpec specification) {

                        return series -> Tramo.process(series, specification, ModellingContext.getActiveContext(), Collections.emptyList());
                    }
                }), "TramoDoc");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaDocTramoSeats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_TRAMOSEATS,
                TsDocumentMapping.serializer(TramoSeatsSpecMapping.SERIALIZER_V3,
                        new TsDataProcessorFactory<TramoSeatsSpec, TramoSeatsResults>() {
                    @Override
                    public boolean canHandle(ProcSpecification spec) {
                        return spec instanceof TramoSeatsSpec;
                    }

                    @Override
                    public TsDataProcessor<TramoSeatsResults> generateProcessor(TramoSeatsSpec specification) {

                        return series -> TramoSeats.process(series, specification, ModellingContext.getActiveContext(), Collections.emptyList());
                    }
                }), "TramoSeatsDoc");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecTramoseats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(TramoSeatsHandlers.SA_SPEC_TRAMOSEATS, TramoSeatsSpecMapping.SERIALIZER_V3, "TramoSeatsSpec");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModSpecTramo implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(TramoSeatsHandlers.MOD_SPEC_TRAMO, TramoSpecMapping.SERIALIZER_V3, "TramoSpec");
    }

}
