/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.workspace.file;

import ec.demetra.workspace.WorkspaceFamily;
import static ec.demetra.workspace.WorkspaceFamily.SA_DOC_TRAMOSEATS;
import static ec.demetra.workspace.WorkspaceFamily.SA_DOC_X13;
import static ec.demetra.workspace.WorkspaceFamily.SA_MULTI;
import static ec.demetra.workspace.WorkspaceFamily.SA_SPEC_TRAMOSEATS;
import static ec.demetra.workspace.WorkspaceFamily.SA_SPEC_X13;
import static ec.demetra.workspace.WorkspaceFamily.UTIL_CAL;
import static ec.demetra.workspace.WorkspaceFamily.UTIL_VAR;
import nbbrd.service.ServiceProvider;
import ec.demetra.workspace.file.FileFormat;
import ec.demetra.workspace.file.spi.FamilyHandler;
import ec.demetra.workspace.file.util.XmlConverterSupport;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.sa.XmlSaProcessing;
import ec.tss.xml.tramoseats.XmlTramoSeatsDocument;
import ec.tss.xml.tramoseats.XmlTramoSeatsSpecification;
import ec.tss.xml.x12.XmlX12Document;
import ec.tss.xml.x12.XmlX12Specification;
import java.util.function.Supplier;

/**
 *
 * @author Philippe Charles
 */
public final class LegacyHandlers {

    private LegacyHandlers() {
        // static class
    }

    private static FamilyHandler xmlConverter(WorkspaceFamily family, Supplier<? extends IXmlConverter> factory, String repository) {
        return XmlConverterSupport.of(factory, repository).asHandler(family, FileFormat.LEGACY);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaMulti implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(SA_MULTI, XmlSaProcessing::new, "SAProcessing");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaDocX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(SA_DOC_X13, XmlX12Document::new, "X12Doc");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaDocTramoSeats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(SA_DOC_TRAMOSEATS, XmlTramoSeatsDocument::new, "TramoSeatsDoc");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(SA_SPEC_X13, XmlX12Specification::new, "X12Spec");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecTramoseats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(SA_SPEC_TRAMOSEATS, XmlTramoSeatsSpecification::new, "TramoSeatsSpec");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class UtilCal implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(UTIL_CAL, ec.tss.xml.legacy.XmlCalendars::new, "Calendars");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class UtilVar implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(UTIL_VAR, ec.tss.xml.legacy.XmlTsVariables::new, "Variables");
    }
}
