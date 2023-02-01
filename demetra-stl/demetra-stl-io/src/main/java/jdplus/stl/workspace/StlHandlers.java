package jdplus.stl.workspace;

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


import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.modelling.io.information.TsDocumentMapping;
import demetra.workspace.WorkspaceFamily;
import static demetra.workspace.WorkspaceFamily.informationSet;
import static demetra.workspace.WorkspaceFamily.parse;
import demetra.workspace.file.spi.FamilyHandler;
import jdplus.stl.io.information.StlPlusSpecMapping;
import jdplus.stlplus.StlPlusDocument;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class StlHandlers {

    public final WorkspaceFamily SA_DOC_STLPLUS = parse("Seasonal adjustment@documents@stlplus");

    @ServiceProvider(FamilyHandler.class)
    public static final class DocStlPlus implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(SA_DOC_STLPLUS,
                new InformationSetSerializer<StlPlusDocument>() {
            @Override
            public InformationSet write(StlPlusDocument object, boolean verbose) {
                return TsDocumentMapping.write(object, StlPlusSpecMapping.SERIALIZER, verbose, true);
            }

            @Override
            public StlPlusDocument read(InformationSet info) {

                StlPlusDocument doc = new StlPlusDocument();
                TsDocumentMapping.read(info, StlPlusSpecMapping.SERIALIZER, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }

        }, "StlPlusDoc");

    }

}
