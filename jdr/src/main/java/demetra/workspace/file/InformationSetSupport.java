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
import demetra.xml.information.XmlInformationSet;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
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
public final class InformationSetSupport implements FileSupport {

    @Nonnull
    public static FileSupport of(@Nonnull Supplier<? extends InformationSetSerializable> factory, @Nonnull String repository) {
        return new InformationSetSupport(factory, repository);
    }

    private final String repository;
    private final Supplier<? extends InformationSetSerializable> factory;
    private final Class<? extends InformationSetSerializable> type;

    private InformationSetSupport(Supplier<? extends InformationSetSerializable> factory, String repository) {
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
            return readItem(resolveFile(root, fileName), factory);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void write(Path root, String fileName, Object value) throws IOException {
        try {
            writeItem(resolveFile(root, fileName), type, value);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

    static Object readItem(Path file, Supplier<? extends InformationSetSerializable> factory) throws IOException, JAXBException {
        return xmlToItem(factory, unmarshalItem(file));
    }

    static void writeItem(Path file, Class<? extends InformationSetSerializable> type, Object value) throws IOException, JAXBException {
        marshalItem(file, itemToXml(type.cast(value)));
    }

    private static String xmlFileName(String fileName) {
        return Paths.changeExtension(fileName, "xml");
    }

    private static InformationSetSerializable xmlToItem(Supplier<? extends InformationSetSerializable> factory, XmlInformationSet xml) throws IOException {
        InformationSetSerializable result = factory.get();
        if (!result.read(xml.create())) {
            throw new IOException("Cannot read information set");
        }
        return result;
    }

    private static XmlInformationSet itemToXml(InformationSetSerializable value) throws IOException {
        InformationSet info = value.write(false);
        if (info == null) {
            throw new IOException("Cannot write information set");
        }
        XmlInformationSet result = new XmlInformationSet();
        result.copy(info);
        return result;
    }

    private static XmlInformationSet unmarshalItem(Path file) throws IOException, JAXBException {
        return (XmlInformationSet) JaxbUtil.unmarshal(file, XML_INFORMATION_SET_CONTEXT);
    }

    private static void marshalItem(Path file, XmlInformationSet jaxbElement) throws IOException, JAXBException {
        Files.createDirectories(file.getParent());
        JaxbUtil.marshal(file, XML_INFORMATION_SET_CONTEXT, jaxbElement, true);
    }

    private static final JAXBContext XML_INFORMATION_SET_CONTEXT = JaxbUtil.createContext(XmlInformationSet.class);
}
