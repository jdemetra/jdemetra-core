package demetra.sa.workspace;

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


import demetra.workspace.file.spi.FamilyHandler;
import demetra.sa.io.information.SaItemsMapping;
import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.informationSet;
import static demetra.workspace.WorkspaceFamily.parse;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class SaHandlers {

    public final WorkspaceFamily SA_MULTI = parse("Seasonal adjustment@multi-documents");
    
    @ServiceProvider(FamilyHandler.class)
    public static final class SaMulti implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_MULTI, SaItemsMapping.SERIALIZER, "SAProcessing");
    }


}
