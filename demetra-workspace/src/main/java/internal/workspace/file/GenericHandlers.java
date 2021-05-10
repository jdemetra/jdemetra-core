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

import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.MOD_DOC_REGARIMA;
import static demetra.workspace.WorkspaceFamily.MOD_DOC_TRAMO;
import static demetra.workspace.WorkspaceFamily.MOD_SPEC_REGARIMA;
import static demetra.workspace.WorkspaceFamily.MOD_SPEC_TRAMO;
import static demetra.workspace.WorkspaceFamily.SA_SPEC_TRAMOSEATS;
import static demetra.workspace.WorkspaceFamily.SA_SPEC_X13;
import demetra.workspace.file.FileFormat;
import demetra.workspace.file.spi.FamilyHandler;
import demetra.workspace.file.util.InformationSetSupport;
import demetra.information.InformationSetSerializer;
import demetra.tramoseats.io.information.TramoSeatsSpecMapping;
import demetra.tramoseats.io.information.TramoSpecMapping;
import demetra.x13.io.information.RegArimaSpecMapping;
import demetra.x13.io.information.X13SpecMapping;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
public final class GenericHandlers {

    private GenericHandlers() {
        // static class
    }

    private static FamilyHandler informationSet(WorkspaceFamily family, InformationSetSerializer factory, String repository) {
        return InformationSetSupport.of(factory, repository).asHandler(family, FileFormat.GENERIC);
    }

//    private static FamilyHandler xmlConverter(WorkspaceFamily family, Supplier<? extends IXmlConverter> factory, String repository) {
//        return XmlConverterSupport.of(factory, repository).asHandler(family, FileFormat.GENERIC);
//    }
//
    
//    @ServiceProvider(FamilyHandler.class)
//    public static final class SaMulti implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(SA_MULTI, SaProcessing::new, "SAProcessing");
//    }
//
//    @ServiceProvider(FamilyHandler.class)
//    public static final class SaDocX13 implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(SA_DOC_X13, X13Document::new, "X13Doc");
//    }
//
//    @ServiceProvider(FamilyHandler.class)
//    public static final class SaDocTramoSeats implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(SA_DOC_TRAMOSEATS, TramoSeatsDocument::new, "TramoSeatsDoc");
//    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_X13, X13SpecMapping.SERIALIZER_V3, "X13Spec");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecTramoseats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_TRAMOSEATS, TramoSeatsSpecMapping.SERIALIZER_V3, "TramoSeatsSpec");
    }

//    @ServiceProvider(FamilyHandler.class)
//    public static final class ModDocRegarima implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(MOD_DOC_REGARIMA, RegArimaDocument::new, "RegArimaDoc");
//    }

//    @ServiceProvider(FamilyHandler.class)
//    public static final class ModDocTramo implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(MOD_DOC_TRAMO, TramoDocument::new, "TramoDoc");
//    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModSpecRegarima implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_SPEC_REGARIMA, RegArimaSpecMapping.SERIALIZER_V3, "RegArimaSpec");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModSpecTramo implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_SPEC_TRAMO, TramoSpecMapping.SERIALIZER_V3, "TramoSpec");
    }

//    @ServiceProvider(FamilyHandler.class)
//    public static final class UtilCal implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = xmlConverter(UTIL_CAL, ec.tss.xml.calendar.XmlCalendars::new, "Calendars");
//    }
//
//    @ServiceProvider(FamilyHandler.class)
//    public static final class UtilVar implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = xmlConverter(UTIL_VAR, ec.tss.xml.regression.XmlTsVariables::new, "Variables");
//    }
}
