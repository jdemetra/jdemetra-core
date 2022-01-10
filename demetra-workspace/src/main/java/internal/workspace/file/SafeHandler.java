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

import demetra.DemetraVersion;
import demetra.workspace.WorkspaceFamily;
import demetra.workspace.file.spi.FamilyHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 */
final class SafeHandler {

    private final Map<WorkspaceFamily, FamilyHandler> handlerByFamily;

    SafeHandler(Map<WorkspaceFamily, FamilyHandler> handlerByFamily) {
        this.handlerByFamily = handlerByFamily;
    }

    private FamilyHandler get(WorkspaceFamily family) throws IOException {
        FamilyHandler result = handlerByFamily.get(family);
        if (result == null) {
            throw new IOException("Cannot handle family: " + family);
        }
        return result;
    }

    public Collection<WorkspaceFamily> getSupportedFamilies() throws IOException {
        return Collections.unmodifiableSet(handlerByFamily.keySet());
    }

    public Object loadValue(WorkspaceFamily family, Path rootFolder, String fileName) throws IOException {
        FamilyHandler handler = get(family);
        try {
            return handler.read(rootFolder, fileName);
        } catch (RuntimeException unexpected) {
            throw new IOException("Unexpected exception while loading " + fileName, unexpected);
        }
    }

    public void storeValue(WorkspaceFamily family, Path rootFolder, String fileName, Object value) throws IOException {
        FamilyHandler handler = get(family);
        try {
            handler.write(rootFolder, fileName, value);
        } catch (RuntimeException unexpected) {
            throw new IOException("Unexpected exception while storing " + fileName, unexpected);
        }
    }

    public void deleteValue(WorkspaceFamily family, Path rootFolder, String fileName) throws IOException {
        FamilyHandler handler = get(family);
        try {
            Files.delete(handler.resolveFile(rootFolder, fileName));
        } catch (RuntimeException unexpected) {
            throw new IOException("Unexpected exception while deleting " + fileName, unexpected);
        }
    }

    public Path resolveFile(WorkspaceFamily family, Path rootFolder, String fileName) throws IOException {
        FamilyHandler handler = get(family);
        try {
            return handler.resolveFile(rootFolder, fileName);
        } catch (RuntimeException unexpected) {
            throw new IOException("Unexpected exception while resolving file " + fileName, unexpected);
        }
    }

    public static SafeHandler create(Logger logger, Supplier<Iterable<FamilyHandler>> handlers, DemetraVersion version) throws IOException {
        try {
            Map<WorkspaceFamily, FamilyHandler> result = new HashMap<>();
            handlers.get().forEach(o -> pushHandler(logger, result, version, o));
            return new SafeHandler(result);
        } catch (RuntimeException unexpected) {
            throw new IOException("Unexpected exception while opening workspace", unexpected);
        }
    }

    @SuppressWarnings("null")
    private static void pushHandler(Logger logger, Map<WorkspaceFamily, FamilyHandler> result, DemetraVersion requested, FamilyHandler o) {
        if (o.match(requested)) {
            WorkspaceFamily family = o.getFamily();
            if (family != null) {
                result.put(family, o);
            } else {
                logger.info("Handler should not return null family: {}", o);
            }
        }
    }
}
