/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.workspace;

/**
 * Defines the meta of an item in a workspace.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.Value
public class WorkspaceItemDescriptor {

    @lombok.Value
    public static class Key {
        @lombok.NonNull
        WorkspaceFamily family;

        @lombok.NonNull
        String id;
    }

    @lombok.Value
    public static class Attributes {

        String label;
        boolean readOnly;
        String comments;
       
    }

    @lombok.NonNull
    Key key;

    Attributes attributes;
    
    
}
