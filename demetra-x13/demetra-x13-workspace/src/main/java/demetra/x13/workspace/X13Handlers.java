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
package demetra.x13.workspace;

import demetra.workspace.file.spi.FamilyHandler;
import demetra.modelling.implementations.SarimaSpec;
import demetra.modelling.io.information.TsDocumentMapping;
import demetra.processing.ProcSpecification;
import demetra.processing.TsDataProcessor;
import demetra.processing.TsDataProcessorFactory;
import demetra.regarima.RegArima;
import demetra.regarima.RegArimaSpec;
import demetra.timeseries.regression.ModellingContext;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.informationSet;
import static demetra.workspace.WorkspaceFamily.parse;
import demetra.x13.X13;
import demetra.x13.X13Results;
import demetra.x13.X13Spec;
import demetra.x13.io.information.RegArimaSpecMapping;
import demetra.x13.io.information.X13SpecMapping;
import java.util.Collections;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class X13Handlers {

    public final WorkspaceFamily SA_DOC_X13 = parse("Seasonal adjustment@documents@x13");
    public final WorkspaceFamily SA_SPEC_X13 = parse("Seasonal adjustment@specifications@x13");

    public final WorkspaceFamily MOD_DOC_REGARIMA = parse("Modelling@documents@regarima");
    public final WorkspaceFamily MOD_SPEC_REGARIMA = parse("Modelling@specifications@regarima");


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
    public static final class SaSpecX13 implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_SPEC_X13, X13SpecMapping.SERIALIZER_V3, "X13Spec");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class ModDocRegarima implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_REGARIMA,
                TsDocumentMapping.serializer(RegArimaSpecMapping.SERIALIZER_V3,
                        new TsDataProcessorFactory<RegArimaSpec, GeneralLinearModel<SarimaSpec> >() {
                    @Override
                    public boolean canHandle(ProcSpecification spec) {
                        return spec instanceof RegArimaSpec;
                    }

                    @Override
                    public TsDataProcessor<GeneralLinearModel<SarimaSpec>> generateProcessor(RegArimaSpec specification) {

                        return series -> RegArima.process(series, specification, ModellingContext.getActiveContext(), Collections.emptyList());
                    }
                }), "RegArimaDoc");
    }
 
    @ServiceProvider(FamilyHandler.class)
    public static final class ModSpecRegarima implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_SPEC_REGARIMA, RegArimaSpecMapping.SERIALIZER_V3, "RegArimaSpec");
    }

 
 }
