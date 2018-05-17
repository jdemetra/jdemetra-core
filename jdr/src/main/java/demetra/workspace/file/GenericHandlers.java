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

import demetra.datatypes.sa.SaProcessingType;
import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.MOD_DOC_REGARIMA;
import static demetra.workspace.WorkspaceFamily.MOD_DOC_TRAMO;
import static demetra.workspace.WorkspaceFamily.MOD_SPEC_REGARIMA;
import static demetra.workspace.WorkspaceFamily.MOD_SPEC_TRAMO;
import static demetra.workspace.WorkspaceFamily.SA_DOC_TRAMOSEATS;
import static demetra.workspace.WorkspaceFamily.SA_DOC_X13;
import static demetra.workspace.WorkspaceFamily.SA_MULTI;
import static demetra.workspace.WorkspaceFamily.SA_SPEC_TRAMOSEATS;
import static demetra.workspace.WorkspaceFamily.SA_SPEC_X13;
import static demetra.workspace.WorkspaceFamily.UTIL_CAL;
import static demetra.workspace.WorkspaceFamily.UTIL_VAR;
import demetra.xml.IXmlConverter;
import demetra.xml.calendar.XmlCalendars;
import demetra.xml.regression.XmlTsVariables;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import org.openide.util.lookup.ServiceProvider;
import ec.tstoolkit.information.InformationSetSerializable;
import java.util.function.Supplier;

/**
 *
 * @author Philippe Charles
 */
public final class GenericHandlers {

    private GenericHandlers() {
        // static class
    }

    private static FamilyHandler informationSet(WorkspaceFamily family, Supplier<? extends InformationSetSerializable> factory, String repository) {
        return InformationSetSupport.of(factory, repository).asHandler(family, FileFormat.GENERIC);
    }

    private static FamilyHandler xmlConverter(WorkspaceFamily family, Supplier<? extends IXmlConverter> factory, String repository) {
        return XmlConverterSupport.of(factory, repository).asHandler(family, FileFormat.GENERIC);
    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class SaMulti implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_MULTI, SaProcessingType::new, "SAProcessing");
    }

//    @ServiceProvider(service = FamilyHandler.class)
//    public static final class SaDocX13 implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(SA_DOC_X13, X13Document::new, "X13Doc");
//    }

//    @ServiceProvider(service = FamilyHandler.class)
//    public static final class SaDocTramoSeats implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(SA_DOC_TRAMOSEATS, TramoSeatsDocument::new, "TramoSeatsDoc");
//    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class SaSpecX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_X13, X13Specification::new, "X13Spec");
    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class SaSpecTramoseats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_TRAMOSEATS, TramoSeatsSpecification::new, "TramoSeatsSpec");
    }

//    @ServiceProvider(service = FamilyHandler.class)
//    public static final class ModDocRegarima implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(MOD_DOC_REGARIMA, RegArimaDocument::new, "RegArimaDoc");
//    }
//
//    @ServiceProvider(service = FamilyHandler.class)
//    public static final class ModDocTramo implements FamilyHandler {
//
//        @lombok.experimental.Delegate
//        private final FamilyHandler delegate = informationSet(MOD_DOC_TRAMO, TramoDocument::new, "TramoDoc");
//    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class ModSpecRegarima implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_SPEC_REGARIMA, RegArimaSpecification::new, "RegArimaSpec");
    }

    @ServiceProvider(service = FamilyHandler.class)
    public static final class ModSpecTramo implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_SPEC_TRAMO, TramoSpecification::new, "TramoSpec");
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
