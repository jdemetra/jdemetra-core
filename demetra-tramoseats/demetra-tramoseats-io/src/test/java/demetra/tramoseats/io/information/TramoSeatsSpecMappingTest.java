/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.tramoseats.io.information;

import demetra.data.Data;
import demetra.information.InformationSet;
import demetra.sa.SaDefinition;
import demetra.sa.SaItem;
import demetra.sa.SaItems;
import demetra.sa.SaSpecification;
import demetra.sa.io.information.SaItemMapping;
import demetra.sa.io.information.SaItemsMapping;
import demetra.timeseries.Ts;
import demetra.timeseries.TsMoniker;
import demetra.toolkit.io.xml.information.XmlInformationSet;
import demetra.tramoseats.TramoSeatsSpec;
import demetra.util.NameManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import jdplus.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.TramoSeatsResults;
import org.assertj.core.util.Files;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class TramoSeatsSpecMappingTest {

    public TramoSeatsSpecMappingTest() {
    }

    @Test
    public void testAll() {
        test(TramoSeatsSpec.RSA0);
        test(TramoSeatsSpec.RSA1);
        test(TramoSeatsSpec.RSA2);
        test(TramoSeatsSpec.RSA3);
        test(TramoSeatsSpec.RSA4);
        test(TramoSeatsSpec.RSA5);
        test(TramoSeatsSpec.RSAfull);
    }

    @Test
    public void testSpecific() {
        TramoSeatsKernel kernel = TramoSeatsKernel.of(TramoSeatsSpec.RSAfull, null);
        TramoSeatsResults rslt = kernel.process(Data.TS_PROD, null);
        TramoSeatsSpec pspec = TramoSeatsFactory.INSTANCE.generateSpec(TramoSeatsSpec.RSAfull, rslt);
        test(pspec);
        testLegacy(pspec);
    }

    private void test(TramoSeatsSpec spec) {
        InformationSet info = TramoSeatsSpecMapping.write(spec, true);
        TramoSeatsSpec nspec = TramoSeatsSpecMapping.readV3(info);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
        info = TramoSeatsSpecMapping.write(spec, false);
        nspec = TramoSeatsSpecMapping.readV3(info);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
    }

    @Test
    public void testAllLegacy() {
        testLegacy(TramoSeatsSpec.RSA0);
        testLegacy(TramoSeatsSpec.RSA1);
        testLegacy(TramoSeatsSpec.RSA2);
        testLegacy(TramoSeatsSpec.RSA3);
        testLegacy(TramoSeatsSpec.RSA4);
        testLegacy(TramoSeatsSpec.RSA5);
        testLegacy(TramoSeatsSpec.RSAfull);
    }

    private void testLegacy(TramoSeatsSpec spec) {
        InformationSet info = TramoSeatsSpecMapping.writeLegacy(spec, true);
        TramoSeatsSpec nspec = TramoSeatsSpecMapping.readLegacy(info);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
        info = TramoSeatsSpecMapping.writeLegacy(spec, false);
        nspec = TramoSeatsSpecMapping.readLegacy(info);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
    }

    public static void testXmlSerialization() throws JAXBException, FileNotFoundException, IOException {
        InformationSet info = TramoSeatsSpecMapping.writeLegacy(TramoSeatsSpec.RSAfull, true);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        String tmp = Files.temporaryFolderPath();
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);

        FileOutputStream stream = new FileOutputStream(tmp + "tramoseats.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

    public static void testXmlDeserialization() throws JAXBException, FileNotFoundException, IOException {
        String tmp = Files.temporaryFolderPath();
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);

        FileInputStream istream = new FileInputStream(tmp + "tramoseats.xml");
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            XmlInformationSet rslt = (XmlInformationSet) unmarshaller.unmarshal(reader);
            InformationSet info = rslt.create();
            TramoSeatsSpec nspec = TramoSeatsSpecMapping.readLegacy(info);
            System.out.println(nspec.equals(TramoSeatsSpec.RSAfull));
        }
    }

    public static void main(String[] arg) throws JAXBException, IOException {
//        testXmlSerialization();
//        testXmlDeserialization();
//        testXmlSerialization2();
//        testXmlDeserialization2();
        testXmlDeserializationLegacy();
    }

    @Test
    public void testSaItem() {
        Ts ts = Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();

        SaDefinition sadef = SaDefinition.builder()
                .domainSpec(TramoSeatsSpec.RSA5)
                .ts(ts)
                .build();

        SaItem item = SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(false);
        NameManager<SaSpecification> mgr = SaItemsMapping.defaultNameManager();
        InformationSet info = SaItemMapping.write(item, mgr, true);

        SaItem nitem = SaItemMapping.read(info, mgr, Collections.emptyMap());
        nitem.process(true);
    }

    @Test
    public void testSaItems() {
        Ts ts = Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();

        SaDefinition sadef = SaDefinition.builder()
                .domainSpec(TramoSeatsSpec.RSA5)
                .ts(ts)
                .build();

        SaItem item = SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(false);

        SaItems items = SaItems.builder()
                .item(item)
                .build();

        InformationSet info = SaItemsMapping.write(items, true);

        SaItems nitems = SaItemsMapping.read(info);
        nitems.getItems().forEach(v -> v.process(true));
    }

    public static void testXmlSerialization2() throws JAXBException, FileNotFoundException, IOException {
        Ts ts = Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();

        SaDefinition sadef = SaDefinition.builder()
                .domainSpec(TramoSeatsSpec.RSA5)
                .ts(ts)
                .build();

        SaItem item = SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(false);

        SaItems items = SaItems.builder()
                .item(item)
                .build();

        InformationSet info = SaItemsMapping.write(items, true);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        String tmp = Files.temporaryFolderPath();
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);

        FileOutputStream stream = new FileOutputStream(tmp + "processing.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

    public static void testXmlDeserialization2() throws JAXBException, FileNotFoundException, IOException {
        String tmp = Files.temporaryFolderPath();
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);

        FileInputStream istream = new FileInputStream(tmp + "processing.xml");
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            XmlInformationSet rslt = (XmlInformationSet) unmarshaller.unmarshal(reader);
            InformationSet info = rslt.create();
            SaItems nspec = SaItemsMapping.read(info);
            System.out.println(nspec.getItems().get(0).getDefinition().getDomainSpec().equals(TramoSeatsSpec.RSA5));
            System.out.println("");
        }
    }

    public static void testXmlDeserializationLegacy() throws FileNotFoundException {
        String tmp = Files.temporaryFolderPath();

        FileInputStream istream = new FileInputStream(tmp + "saprocessing-1.xml");
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            XmlInformationSet rslt = (XmlInformationSet) unmarshaller.unmarshal(reader);
            InformationSet info = rslt.create();
            SaItems nspec = SaItemsMapping.read(info);
            nspec.getItems().forEach(v->v.process(false));
            System.out.println(nspec.getItems().size());
//            nspec.getItems().forEach(v -> System.out.println(((TramoSeatsResults) v.getEstimation().getResults()).getPreprocessing().getEstimation().getStatistics().getLogLikelihood()));
            long t0=System.currentTimeMillis();
            nspec.getItems().forEach(v->v.process(false));
//            System.out.println(nspec.getItems().get(0).getDefinition().getDomainSpec().equals(TramoSeatsSpec.RSA5));
            long t1=System.currentTimeMillis();
            System.out.println(t1-t0);
        } catch (IOException ex) {
            Logger.getLogger(TramoSeatsSpecMappingTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(TramoSeatsSpecMappingTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
