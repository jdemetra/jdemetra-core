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
package ec.demetra.workspace;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface Workspace extends Closeable {

    @Nonnull
    String getName() throws IOException;

    void setName(@Nonnull String name) throws IOException;

    @Nonnull
    Collection<WorkspaceFamily> getSupportedFamilies() throws IOException;

    @Nonnull
    Collection<WorkspaceItem> getItems() throws IOException;

    @Nonnull
    Object load(@Nonnull WorkspaceItem item) throws IOException;

    void store(@Nonnull WorkspaceItem item, @Nonnull Object value) throws IOException;

    void delete(@Nonnull WorkspaceItem item) throws IOException;

    default void copyTo(@Nonnull Workspace target) throws IOException {
        target.setName(getName());
        Collection<WorkspaceFamily> families = target.getSupportedFamilies();
        for (WorkspaceItem o : getItems()) {
            if (families.contains(o.getFamily())) {
                target.store(o, load(o));
            }
        }
    }
}
