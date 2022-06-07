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

import demetra.DemetraVersion;
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
import jdplus.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.TramoSeatsResults;
import nbbrd.io.xml.bind.Jaxb;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public static void testXmlSerialization() throws IOException {
        InformationSet info = TramoSeatsSpecMapping.writeLegacy(TramoSeatsSpec.RSAfull, true);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        String tmp = Files.temporaryFolderPath();
        Jaxb.Formatter
                .of(XmlInformationSet.class)
                .withFormatted(true)
                .formatFile(xmlinfo, new File(tmp + "tramoseats.xml"));
    }

    public static void testXmlDeserialization() throws IOException {
        String tmp = Files.temporaryFolderPath();
        XmlInformationSet rslt = Jaxb.Parser
                .of(XmlInformationSet.class)
                .parseFile(new File(tmp + "tramoseats.xml"));

        InformationSet info = rslt.create();
        TramoSeatsSpec nspec = TramoSeatsSpecMapping.readLegacy(info);
        System.out.println(nspec.equals(TramoSeatsSpec.RSAfull));
    }

    public static void main(String[] arg) throws JAXBException, IOException {
        testXmlSerialization();
        testXmlDeserialization();
        testXmlSerialization2();
        testXmlDeserialization2();
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
        item.process(null, false);
        NameManager<SaSpecification> mgr = SaItemsMapping.defaultNameManager();
        InformationSet info = SaItemMapping.write(item, mgr, true, DemetraVersion.JD3);

        SaItem nitem = SaItemMapping.read(info, mgr, Collections.emptyMap());
        nitem.process(null, true);
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
        item.process(null, false);

        SaItems items = SaItems.builder()
                .item(item)
                .build();

        InformationSet info = SaItemsMapping.write(items, true, DemetraVersion.JD3);

        SaItems nitems = SaItemsMapping.read(info);
        nitems.getItems().forEach(v -> v.process(null, true));
    }

    public static void testXmlSerialization2() throws IOException {
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
        item.process(null, false);

        SaItems items = SaItems.builder()
                .item(item)
                .build();

        InformationSet info = SaItemsMapping.write(items, true, DemetraVersion.JD3);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        String tmp = Files.temporaryFolderPath();
        Jaxb.Formatter
                .of(XmlInformationSet.class)
                .withFormatted(true)
                .formatFile(xmlinfo, new File(tmp + "processing.xml"));
    }

    public static void testXmlDeserialization2() throws IOException {
        String tmp = Files.temporaryFolderPath();
        XmlInformationSet rslt = Jaxb.Parser
                .of(XmlInformationSet.class)
                .parseFile(new File(tmp + "processing.xml"));

        InformationSet info = rslt.create();
        SaItems nspec = SaItemsMapping.read(info);
        System.out.println(nspec.getItems().get(0).getDefinition().getDomainSpec().equals(TramoSeatsSpec.RSA5));
        System.out.println("");
    }

    public static void testXmlDeserializationLegacy() {
        String tmp = Files.temporaryFolderPath();
        try {
            XmlInformationSet rslt = Jaxb.Parser
                .of(XmlInformationSet.class)
                .parseFile(new File(tmp + "saprocessing-1.xml"));

            InformationSet info = rslt.create();
            SaItems nspec = SaItemsMapping.read(info);
            nspec.getItems().forEach(v->v.process(null, false));
            System.out.println(nspec.getItems().size());
//            nspec.getItems().forEach(v -> System.out.println(((TramoSeatsResults) v.getEstimation().getResults()).getPreprocessing().getEstimation().getStatistics().getLogLikelihood()));
            long t0=System.currentTimeMillis();
            nspec.getItems().forEach(v->v.process(null, false));
//            System.out.println(nspec.getItems().get(0).getDefinition().getDomainSpec().equals(TramoSeatsSpec.RSA5));
            long t1=System.currentTimeMillis();
            System.out.println(t1-t0);
        } catch (IOException ex) {
            Logger.getLogger(TramoSeatsSpecMappingTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
