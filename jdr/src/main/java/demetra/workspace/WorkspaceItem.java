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
package demetra.workspace;

/**
 * Defines the meta of an item in a workspace.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class WorkspaceItem {

    /**
     * Defines the kind of data that this item represents.
     */
    @lombok.NonNull
    WorkspaceFamily family;

    /**
     * A non-null identifier that is unique per family.
     */
    @lombok.NonNull
    String id;

    /**
     * An optional label.
     */
    String label;

    boolean readOnly;

    /**
     * Some optional comments.
     */
    String comments;
}
