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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines an abstract way to load/store data from/to a workspace.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface Workspace extends Closeable {

    /**
     * Gets the workspace name.
     *
     * @return a non-null name
     * @throws IOException if the operation failed
     */
    @NonNull
    String getName() throws IOException;

    /**
     * Sets the workspace name.
     *
     * @param name a non-null name
     * @throws IOException if the operation failed
     */
    void setName(@NonNull String name) throws IOException;

    /**
     * Lists all supported data families.
     *
     * @return a non-null collection
     * @throws IOException if the operation failed
     */
    @NonNull
    Collection<WorkspaceFamily> getSupportedFamilies() throws IOException;

    /**
     * Lists all items of this workspace.
     *
     * @return a non-null collection
     * @throws IOException if the operation failed
     */
    @NonNull
    Collection<WorkspaceItem> getItems() throws IOException;

    /**
     * Loads the data of a workspace item. Note that the type of the returned
     * object is linked to the item family.
     *
     * @param item a non-null item
     * @return a non-null data
     * @throws IOException if the operation failed
     */
    @NonNull
    Object load(@NonNull WorkspaceItem item) throws IOException;

    /**
     * Stores the metadata and data of a workspace item. The item is replaced if
     * it already exist in the workspace. Note that the type of the provided
     * object is linked to the item family.
     *
     * @param item a non-null item
     * @param value a non-null data
     * @throws IOException if the operation failed
     */
    void store(@NonNull WorkspaceItem item, @NonNull Object value) throws IOException;

    /**
     * Removes a workspace item if it exists, do nothing otherwise.
     *
     * @param item a non-null item
     * @throws IOException if the operation failed
     */
    void delete(@NonNull WorkspaceItem item) throws IOException;

    /**
     * Copies the content of this workspace to another one.
     *
     * @param target a non-null workspace
     * @throws IOException if the operation failed
     */
    default void copyTo(@NonNull Workspace target) throws IOException {
        target.setName(getName());
        Collection<WorkspaceFamily> families = target.getSupportedFamilies();
        for (WorkspaceItem o : getItems()) {
            if (families.contains(o.getFamily())) {
                target.store(o, load(o));
            }
        }
    }
}
