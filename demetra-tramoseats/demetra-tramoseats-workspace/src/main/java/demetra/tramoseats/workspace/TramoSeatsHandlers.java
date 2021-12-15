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

import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.modelling.io.information.TsDocumentMapping;
import demetra.processing.ProcSpecification;
import demetra.tramo.TramoDocument;
import demetra.tramoseats.TramoSeatsDocument;
import demetra.tramoseats.TramoSeatsResults;
import demetra.tramoseats.TramoSeatsSpec;
import demetra.tramoseats.io.information.TramoSeatsSpecMapping;
import demetra.tramoseats.io.information.TramoSpecMapping;
import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.informationSet;
import static demetra.workspace.WorkspaceFamily.parse;
import demetra.workspace.file.spi.FamilyHandler;
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
                new InformationSetSerializer<TramoDocument>() {
            @Override
            public InformationSet write(TramoDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, TramoSpecMapping.SERIALIZER_V3, verbose, true);
            }

            @Override
            public TramoDocument read(InformationSet info) {

                TramoDocument doc = new TramoDocument();
                TsDocumentMapping.read(info, TramoSpecMapping.SERIALIZER_V3, doc);
                return doc;
            }
        }, "TramoDoc");

    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaDocTramoSeats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_TRAMOSEATS,
                new InformationSetSerializer<TramoSeatsDocument>() {
            @Override
            public InformationSet write(TramoSeatsDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, TramoSeatsSpecMapping.SERIALIZER_V3, verbose, true);
            }

            @Override
            public TramoSeatsDocument read(InformationSet info) {

                TramoSeatsDocument doc = new TramoSeatsDocument();
                TsDocumentMapping.read(info, TramoSeatsSpecMapping.SERIALIZER_V3, doc);
                return doc;
            }
        },
                "TramoSeatsDoc");
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
