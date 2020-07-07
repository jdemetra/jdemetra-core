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

import ec.tss.xml.information.XmlInformationSet;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;
import nbbrd.io.xml.Xml;
import nbbrd.io.xml.bind.Jaxb;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public final class InformationSetSupport implements FileSupport {

    @NonNull
    public static FileSupport of(@NonNull Supplier<? extends InformationSetSerializable> factory, @NonNull String repository) {
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
        return readItem(resolveFile(root, fileName), factory);
    }

    @Override
    public void write(Path root, String fileName, Object value) throws IOException {
        writeItem(resolveFile(root, fileName), type, value);
    }

    static Object readItem(Path file, Supplier<? extends InformationSetSerializable> factory) throws IOException {
        return xmlToItem(factory, unmarshalItem(file));
    }

    static void writeItem(Path file, Class<? extends InformationSetSerializable> type, Object value) throws IOException {
        marshalItem(file, itemToXml(type.cast(value)));
    }

    private static String xmlFileName(String fileName) {
        return ec.tstoolkit.utilities.Paths.changeExtension(fileName, "xml");
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

    private static final Xml.Parser<XmlInformationSet> PARSER;
    private static final Xml.Formatter<XmlInformationSet> FORMATTER;

    static {
        try {
            PARSER = Jaxb.Parser.of(XmlInformationSet.class);
            FORMATTER = Jaxb.Formatter.of(XmlInformationSet.class).withFormatted(true);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static XmlInformationSet unmarshalItem(Path file) throws IOException {
        return PARSER.parsePath(file);
    }

    private static void marshalItem(Path file, XmlInformationSet jaxbElement) throws IOException {
        Files.createDirectories(file.getParent());
        FORMATTER.formatPath(jaxbElement, file);
    }
}
