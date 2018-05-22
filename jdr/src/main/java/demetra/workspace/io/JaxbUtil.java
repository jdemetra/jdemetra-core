/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package demetra.workspace.io;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class JaxbUtil {

    @Nonnull
    public Object unmarshal(@Nonnull Path file, @Nonnull JAXBContext context) throws JAXBException, IOException {
        try {
            return Jaxb.Parser.of(context).parsePath(file);
        } catch (Xml.WrappedException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof JAXBException) {
                throw (JAXBException) cause;
            }
            throw ex;
        }
    }

    @Nonnull
    public Object unmarshal(@Nonnull Path file, @Nonnull Unmarshaller unmarshaller) throws JAXBException, IOException {
        try {
            return Jaxb.Parser.builder().factory(() -> unmarshaller).build().parsePath(file);
        } catch (Xml.WrappedException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof JAXBException) {
                throw (JAXBException) cause;
            }
            throw ex;
        }
    }

    public void marshal(@Nonnull Path file, @Nonnull JAXBContext context, @Nonnull Object jaxbElement, boolean formatted) throws JAXBException, IOException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
        marshal(file, marshaller, jaxbElement);
    }

    public void marshal(@Nonnull Path file, @Nonnull Marshaller marshaller, @Nonnull Object jaxbElement) throws JAXBException, IOException {
        Optional<File> localFile = IO.getFile(file);
        if (localFile.isPresent()) {
            marshaller.marshal(jaxbElement, localFile.get());
        } else {
            try (Writer writer = Files.newBufferedWriter(file)) {
                marshaller.marshal(jaxbElement, writer);
            }
        }
    }

    @Nonnull
    public JAXBContext createContext(@Nonnull Class<?> type) {
        try {
            return JAXBContext.newInstance(type);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <X> void forSingle(@Nullable X item, @Nonnull Consumer<? super X> action) {
        Objects.requireNonNull(action, "action");
        if (item != null) {
            action.accept(item);
        }
    }

    public <X> void forEach(@Nullable X[] array, @Nonnull Consumer<? super X> action) {
        Objects.requireNonNull(action, "action");
        if (array != null) {
            for (X o : array) {
                action.accept(o);
            }
        }
    }
}
