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
package demetra.workspace.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Stax {

    /**
     * Prevents XXE vulnerability by disabling features.
     *
     * @param factory non-null factory
     * @see
     * https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#XMLInputFactory_.28a_StAX_parser.29
     */
    public void preventXXE(@Nonnull XMLInputFactory factory) {
        setFeature(factory, XMLInputFactory.SUPPORT_DTD, false);
        setFeature(factory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    @FunctionalInterface
    public interface FlowHandler<I, T> {

        @Nonnull
        T parse(@Nonnull I input, @Nonnull Closeable onClose) throws IOException, XMLStreamException;

        @Nonnull
        static <I, T> FlowHandler<I, T> of(@Nonnull ValueHandler<I, T> handler) {
            return handler.asFlow();
        }
    }

    @FunctionalInterface
    public interface ValueHandler<I, T> {

        @Nonnull
        T parse(@Nonnull I input) throws XMLStreamException;

        @Nonnull
        default FlowHandler<I, T> asFlow() {
            return (input, onClose) -> {
                try (Closeable c = onClose) {
                    return parse(input);
                }
            };
        }
    }

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    public static final class StreamParser<T> implements Xml.Parser<T> {

        @Nonnull
        public static <T> StreamParser<T> flowOf(@Nonnull FlowHandler<XMLStreamReader, T> handler) {
            return StreamParser.<T>builder().handler(handler).build();
        }

        @Nonnull
        public static <T> StreamParser<T> valueOf(@Nonnull ValueHandler<XMLStreamReader, T> handler) {
            return StreamParser.<T>builder().handler(handler.asFlow()).build();
        }

        @lombok.NonNull
        private final FlowHandler<XMLStreamReader, T> handler;

        @lombok.NonNull
        @lombok.Builder.Default
        private final IO.Supplier<? extends XMLInputFactory> factory = XMLInputFactory::newFactory;

        @lombok.Builder.Default
        private boolean preventXXE = true;

        @Override
        public T parseReader(IO.Supplier<? extends Reader> source) throws IOException {
            Reader resource = source.getWithIO();
            return parse(o -> o.createXMLStreamReader(resource), resource);
        }

        @Override
        public T parseStream(IO.Supplier<? extends InputStream> source) throws IOException {
            InputStream resource = source.getWithIO();
            return parse(o -> o.createXMLStreamReader(resource), resource);
        }

        @Override
        public T parseReader(Reader resource) throws IOException {
            Objects.requireNonNull(resource);
            return parse(o -> o.createXMLStreamReader(resource), IO.Runnable.noOp().asCloseable());
        }

        @Override
        public T parseStream(InputStream resource) throws IOException {
            Objects.requireNonNull(resource);
            return parse(o -> o.createXMLStreamReader(resource), IO.Runnable.noOp().asCloseable());
        }

        @Nonnull
        public T parse(@Nonnull XMLStreamReader input, @Nonnull Closeable onClose) throws IOException {
            try {
                return handler.parse(input, onClose);
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException | IOException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
        }

        private T parse(XSupplier<XMLStreamReader> supplier, Closeable onClose) throws IOException {
            try {
                XMLStreamReader input = supplier.create(getEngine());
                return parse(input, () -> closeBoth(input, onClose));
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException | IOException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
        }

        private XMLInputFactory getEngine() throws IOException {
            XMLInputFactory result = factory.getWithIO();
            if (preventXXE) {
                preventXXE(result);
            }
            return result;
        }

        private static void closeBoth(XMLStreamReader input, Closeable onClose) throws IOException {
            try {
                input.close();
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
            onClose.close();
        }
    }

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    public static final class EventParser<T> implements Xml.Parser<T> {

        @Nonnull
        public static <T> EventParser<T> flowOf(@Nonnull FlowHandler<XMLEventReader, T> handler) {
            return EventParser.<T>builder().handler(handler).build();
        }

        @Nonnull
        public static <T> EventParser<T> valueOf(@Nonnull ValueHandler<XMLEventReader, T> handler) {
            return EventParser.<T>builder().handler(handler.asFlow()).build();
        }

        @lombok.NonNull
        private final FlowHandler<XMLEventReader, T> handler;

        @lombok.NonNull
        @lombok.Builder.Default
        private final IO.Supplier<? extends XMLInputFactory> factory = XMLInputFactory::newFactory;

        @lombok.Builder.Default
        private boolean preventXXE = true;

        @Override
        public T parseReader(IO.Supplier<? extends Reader> source) throws IOException {
            Reader resource = source.getWithIO();
            return parse(o -> o.createXMLEventReader(resource), resource);
        }

        @Override
        public T parseStream(IO.Supplier<? extends InputStream> source) throws IOException {
            InputStream resource = source.getWithIO();
            return parse(o -> o.createXMLEventReader(resource), resource);
        }

        @Override
        public T parseReader(Reader resource) throws IOException {
            Objects.requireNonNull(resource);
            return parse(o -> o.createXMLEventReader(resource), IO.Runnable.noOp().asCloseable());
        }

        @Override
        public T parseStream(InputStream resource) throws IOException {
            Objects.requireNonNull(resource);
            return parse(o -> o.createXMLEventReader(resource), IO.Runnable.noOp().asCloseable());
        }

        private T parse(XSupplier<XMLEventReader> supplier, Closeable onClose) throws IOException {
            try {
                XMLEventReader input = supplier.create(getEngine());
                return parse(input, () -> closeBoth(input, onClose));
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException | IOException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
        }

        private T parse(XMLEventReader input, Closeable onClose) throws IOException {
            try {
                return handler.parse(input, onClose);
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException | IOException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
        }

        private XMLInputFactory getEngine() throws IOException {
            XMLInputFactory result = factory.getWithIO();
            if (preventXXE) {
                preventXXE(result);
            }
            return result;
        }

        private static void closeBoth(XMLEventReader input, Closeable onClose) throws IOException {
            try {
                input.close();
            } catch (XMLStreamException ex) {
                IO.ensureClosed(ex, onClose);
                throw new Xml.WrappedException(ex);
            } catch (Error | RuntimeException ex) {
                IO.ensureClosed(ex, onClose);
                throw ex;
            }
            onClose.close();
        }
    }

    @FunctionalInterface
    private interface XSupplier<T> {

        T create(XMLInputFactory input) throws XMLStreamException;
    }

    private void setFeature(XMLInputFactory factory, String feature, boolean value) {
        if (factory.isPropertySupported(feature)
                && ((Boolean) factory.getProperty(feature)) != value) {
            factory.setProperty(feature, value);
        }
    }
}
