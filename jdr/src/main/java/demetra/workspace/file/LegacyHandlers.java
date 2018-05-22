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
package demetra.workspace.file;

import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.SA_DOC_TRAMOSEATS;
import static demetra.workspace.WorkspaceFamily.SA_DOC_X13;
import static demetra.workspace.WorkspaceFamily.SA_MULTI;
import static demetra.workspace.WorkspaceFamily.SA_SPEC_TRAMOSEATS;
import static demetra.workspace.WorkspaceFamily.SA_SPEC_X13;
import static demetra.workspace.WorkspaceFamily.UTIL_CAL;
import static demetra.workspace.WorkspaceFamily.UTIL_VAR;
import org.openide.util.lookup.ServiceProvider;
import demetra.xml.IXmlConverter;
import demetra.xml.calendar.XmlCalendars;
import demetra.xml.regression.XmlTsVariables;
import demetra.xml.sa.XmlSaProcessing;
import demetra.xml.sa.tramoseats.XmlTramoSeatsSpecification;
import demetra.xml.sa.x13.XmlX13Specification;
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

    @ServiceProvider(service = FamilyHandler.class)
    public static final class SaMulti implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(SA_MULTI, XmlSaProcessing::new, "SAProcessing");
    }

//    @ServiceProvider(service = FamilyHandler.class)
//    public static final class SaDocX13 implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = xmlConverter(SA_DOC_X13, XmlX12Document::new, "X12Doc");
//    }
//
//    @ServiceProvider(service = FamilyHandler.class)
//    public static final class SaDocTramoSeats implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = xmlConverter(SA_DOC_TRAMOSEATS, XmlTramoSeatsDocument::new, "TramoSeatsDoc");
//    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class SaSpecX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(SA_SPEC_X13, XmlX13Specification::new, "X13Spec");
    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class SaSpecTramoseats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(SA_SPEC_TRAMOSEATS, XmlTramoSeatsSpecification::new, "TramoSeatsSpec");
    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class UtilCal implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(UTIL_CAL, XmlCalendars::new, "Calendars");
    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class UtilVar implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(UTIL_VAR, XmlTsVariables::new, "Variables");
    }
}
