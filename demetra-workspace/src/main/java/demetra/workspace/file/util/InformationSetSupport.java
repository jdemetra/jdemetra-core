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
package demetra.workspace.file.util;

import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.toolkit.io.xml.information.XmlInformationSet;
import demetra.util.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
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
    public static FileSupport of(@NonNull InformationSetSerializer factory, @NonNull String repository) {
        return new InformationSetSupport(factory, repository);
    }

    private final String repository;
    private final InformationSetSerializer factory;

    private InformationSetSupport(InformationSetSerializer factory, String repository) {
        this.repository = Objects.requireNonNull(repository);
        this.factory = Objects.requireNonNull(factory);
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
        writeItem(resolveFile(root, fileName), factory, value);
    }

    @Override
    public boolean match(DemetraVersion version) {
        return factory.match(version);
    }

    static <T> T readItem(Path file, InformationSetSerializer<T> factory) throws IOException {
        return xmlToItem(factory, unmarshalItem(file));
    }

    static void writeItem(Path file, InformationSetSerializer factory, Object value) throws IOException {
        marshalItem(file, itemToXml(factory, value));
    }

    private static String xmlFileName(String fileName) {
        return Paths.changeExtension(fileName, "xml");
    }

    private static <T> T xmlToItem(InformationSetSerializer<T> factory, XmlInformationSet xml) throws IOException {
         T obj = factory.read(xml.create());
        if (obj == null) {
            throw new IOException("Cannot read information set");
        }
        return obj;
    }

    private static<T> XmlInformationSet itemToXml(InformationSetSerializer<T> factory, T value) throws IOException {
        InformationSet info = factory.write(value, false);
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
        PARSER = Jaxb.Parser.of(XmlInformationSet.class);
        FORMATTER = Jaxb.Formatter.of(XmlInformationSet.class).withFormatted(true);
    }

    private static XmlInformationSet unmarshalItem(Path file) throws IOException {
        return PARSER.parsePath(file);
    }

    private static void marshalItem(Path file, XmlInformationSet jaxbElement) throws IOException {
        Files.createDirectories(file.getParent());
        FORMATTER.formatPath(jaxbElement, file);
    }

}
