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
package demetra.workspace.file;

import demetra.workspace.io.JaxbUtil;
import demetra.workspace.util.Paths;
import demetra.xml.IXmlConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class XmlConverterSupport implements FileSupport {

    @Nonnull
    public static FileSupport of(@Nonnull Supplier<? extends IXmlConverter> factory, @Nonnull String repository) {
        return new XmlConverterSupport(factory, repository);
    }

    private final String repository;
    private final Supplier<? extends IXmlConverter> factory;
    private final Class<? extends IXmlConverter> type;

    private XmlConverterSupport(Supplier<? extends IXmlConverter> factory, String repository) {
        this.repository = Objects.requireNonNull(repository);
        this.factory = Objects.requireNonNull(factory);
        this.type = factory.get().getClass();
    }

    @Override
    public Path resolveFile(Path root, String fileName) {
        return root.resolve(repository).resolve(xmlFileName(fileName));
    }

    @Override
    public Object read(Path root, String fileName) throws IOException {
        try {
            return readItem(resolveFile(root, fileName), type);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void write(Path root, String fileName, Object value) throws IOException {
        try {
            writeItem(resolveFile(root, fileName), factory, value);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    static Object readItem(Path file, Class<? extends IXmlConverter> type) throws IOException, JAXBException {
        return xmlToItem(unmarshalItem(file, type));
    }

    static void writeItem(Path file, Supplier<? extends IXmlConverter> factory, Object value) throws IOException, JAXBException {
        marshalItem(file, itemToXml(factory, value));
    }

    private static String xmlFileName(String fileName) {
        return Paths.changeExtension(fileName, "xml");
    }

    private static Object xmlToItem(IXmlConverter xml) throws IOException {
        Object result = xml.create();
        if (result == null) {
            throw new IOException("Cannot create item using " + xml.getClass());
        }
        return result;
    }

    private static IXmlConverter itemToXml(Supplier<? extends IXmlConverter> factory, Object value) throws IOException {
        IXmlConverter result = factory.get();
        result.copy(value);
        return result;
    }

    private static <X extends IXmlConverter<?>> X unmarshalItem(Path file, Class<X> type) throws JAXBException, IOException {
        return (X) JaxbUtil.unmarshal(file, JAXBContext.newInstance(type));
    }

    private static void marshalItem(Path file, IXmlConverter<?> jaxbElement) throws JAXBException, IOException {
        Files.createDirectories(file.getParent());
        JaxbUtil.marshal(file, JAXBContext.newInstance(jaxbElement.getClass()), jaxbElement, true);
    }
}
