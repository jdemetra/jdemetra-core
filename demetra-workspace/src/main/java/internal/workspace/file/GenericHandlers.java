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
import demetra.modelling.implementations.SarimaSpec;
import demetra.modelling.io.information.TsDocumentMapping;
import demetra.processing.ProcSpecification;
import demetra.processing.TsDataProcessor;
import demetra.processing.TsDataProcessorFactory;
import demetra.regarima.RegArima;
import demetra.regarima.RegArimaSpec;
import demetra.sa.io.information.SaItemsMapping;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import demetra.tramo.Tramo;
import demetra.tramo.TramoSpec;
import demetra.tramoseats.TramoSeats;
import demetra.tramoseats.TramoSeatsResults;
import demetra.tramoseats.TramoSeatsSpec;
import demetra.tramoseats.io.information.TramoSeatsSpecMapping;
import demetra.tramoseats.io.information.TramoSpecMapping;
import static demetra.workspace.WorkspaceFamily.SA_DOC_X13;
import static demetra.workspace.WorkspaceFamily.SA_DOC_TRAMOSEATS;
import static demetra.workspace.WorkspaceFamily.SA_MULTI;
import static demetra.workspace.WorkspaceFamily.UTIL_CAL;
import static demetra.workspace.WorkspaceFamily.UTIL_VAR;
import demetra.workspace.file.util.XmlConverterSupport;
import demetra.x13.X13;
import demetra.x13.X13Results;
import demetra.x13.X13Spec;
import demetra.x13.io.information.RegArimaSpecMapping;
import demetra.x13.io.information.X13SpecMapping;
import internal.workspace.file.xml.util.XmlCalendars;
import internal.workspace.file.xml.util.XmlTsVariables;
import java.util.Collections;
import java.util.function.Supplier;
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

    private static FamilyHandler xmlConverter(WorkspaceFamily family, Supplier<? extends IXmlConverter> factory, String repository) {
        return XmlConverterSupport.of(factory, repository).asHandler(family, FileFormat.GENERIC);
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaMulti implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_MULTI, SaItemsMapping.SERIALIZER, "SAProcessing");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaDocX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_X13,
                TsDocumentMapping.serializer(X13SpecMapping.SERIALIZER_V3,
                        new TsDataProcessorFactory<X13Spec, X13Results>() {
                    @Override
                    public boolean canHandle(ProcSpecification spec) {
                        return spec instanceof X13Spec; //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public TsDataProcessor<X13Results> generateProcessor(X13Spec specification) {

                        return series -> X13.process(series, specification, ModellingContext.getActiveContext(), Collections.emptyList());
                    }
                }), "X13Doc");
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
    public static final class SaSpecX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_X13, X13SpecMapping.SERIALIZER_V3, "X13Spec");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class SaSpecTramoseats implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_TRAMOSEATS, TramoSeatsSpecMapping.SERIALIZER_V3, "TramoSeatsSpec");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModDocRegarima implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_REGARIMA,
                TsDocumentMapping.serializer(RegArimaSpecMapping.SERIALIZER_V3,
                        new TsDataProcessorFactory<RegArimaSpec, GeneralLinearModel<SarimaSpec> >() {
                    @Override
                    public boolean canHandle(ProcSpecification spec) {
                        return spec instanceof TramoSeatsSpec;
                    }

                    @Override
                    public TsDataProcessor<GeneralLinearModel<SarimaSpec>> generateProcessor(RegArimaSpec specification) {

                        return series -> RegArima.process(series, specification, ModellingContext.getActiveContext(), Collections.emptyList());
                    }
                }), "RegArimaDoc");
    }
    @ServiceProvider(FamilyHandler.class)
    public static final class ModDocTramo implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_TRAMO,
                TsDocumentMapping.serializer(TramoSpecMapping.SERIALIZER_V3,
                        new TsDataProcessorFactory<TramoSpec, GeneralLinearModel<SarimaSpec> >() {
                    @Override
                    public boolean canHandle(ProcSpecification spec) {
                        return spec instanceof TramoSeatsSpec;
                    }

                    @Override
                    public TsDataProcessor<GeneralLinearModel<SarimaSpec>> generateProcessor(TramoSpec specification) {

                        return series ->Tramo.process(series, specification, ModellingContext.getActiveContext(), Collections.emptyList());
                    }
                }), "TramoDoc");
    }
 
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

    @ServiceProvider(FamilyHandler.class)
    public static final class UtilCal implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(UTIL_CAL, XmlCalendars::new, "Calendars");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class UtilVar implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(UTIL_VAR, XmlTsVariables::new, "Variables");
    }
}
