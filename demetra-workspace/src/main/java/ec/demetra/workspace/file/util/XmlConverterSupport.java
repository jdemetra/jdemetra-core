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
package ec.demetra.workspace.file.util;

import ec.tss.xml.IXmlConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import lombok.AccessLevel;
import nbbrd.io.xml.Xml;
import nbbrd.io.xml.bind.Jaxb;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class XmlConverterSupport implements FileSupport {

    @NonNull
    public static <VALUE, XML extends IXmlConverter<VALUE>> FileSupport of(@NonNull Supplier<XML> factory, @NonNull String repository) {
        ValueAdapter<VALUE, XML> adapter = new ValueAdapter<>(factory);
        Xml.Parser<VALUE> parser = Jaxb.Parser.of(adapter.getXmlType()).andThen(adapter::toValue);
        Xml.Formatter<VALUE> formatter = Jaxb.Formatter.of(adapter.getXmlType()).withFormatted(true).compose(adapter::fromValue);
        return new XmlConverterSupport(repository, parser, formatter);
    }

    @lombok.NonNull
    private final String repository;

    @lombok.NonNull
    private final Xml.Parser parser;

    @lombok.NonNull
    private final Xml.Formatter formatter;

    @Override
    public Path resolveFile(Path root, String fileName) {
        return root.resolve(repository).resolve(xmlFileName(fileName));
    }

    @Override
    public Object read(Path root, String fileName) throws IOException {
        return readItem(resolveFile(root, fileName));
    }

    @Override
    public void write(Path root, String fileName, Object value) throws IOException {
        writeItem(resolveFile(root, fileName), value);
    }

    private Object readItem(Path file) throws IOException {
        return parser.parsePath(file);
    }

    private void writeItem(Path file, Object value) throws IOException {
        Files.createDirectories(file.getParent());
        formatter.formatPath(value, file);
    }

    private static String xmlFileName(String fileName) {
        return ec.tstoolkit.utilities.Paths.changeExtension(fileName, "xml");
    }

    private static final class ValueAdapter<VALUE, XML extends IXmlConverter<VALUE>> {

        private final Supplier<XML> xmlFactory;
        private final Class<XML> xmlType;

        public ValueAdapter(Supplier<XML> xmlFactory) {
            this.xmlFactory = xmlFactory;
            this.xmlType = (Class<XML>) xmlFactory.get().getClass();
        }

        public Class<XML> getXmlType() {
            return xmlType;
        }

        public VALUE toValue(XML xml) {
            return xml.create();
        }

        public XML fromValue(VALUE value) {
            XML result = xmlFactory.get();
            result.copy(value);
            return result;
        }
    }
}
