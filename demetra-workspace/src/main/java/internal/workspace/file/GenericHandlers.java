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
import static demetra.workspace.WorkspaceFamily.xmlConverter;
import demetra.workspace.file.spi.FamilyHandler;
import internal.workspace.file.xml.util.XmlCalendars;
import internal.workspace.file.xml.util.XmlTsVariables;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class GenericHandlers {

    
 
    @ServiceProvider(FamilyHandler.class)
    public static final class UtilCal implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(demetra.workspace.WorkspaceFamily.UTIL_CAL, XmlCalendars::new, "Calendars");
    }

    @ServiceProvider(FamilyHandler.class)
    public static final class UtilVar implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = xmlConverter(demetra.workspace.WorkspaceFamily.UTIL_VAR, XmlTsVariables::new, "Variables");
    }
}
