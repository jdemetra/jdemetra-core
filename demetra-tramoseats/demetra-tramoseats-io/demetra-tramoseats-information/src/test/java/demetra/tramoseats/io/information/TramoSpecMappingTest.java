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
import demetra.toolkit.io.xml.legacy.core.XmlInformationSet;
import demetra.tramo.TramoSpec;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.tramo.TramoFactory;
import jdplus.tramo.TramoKernel;
import nbbrd.io.xml.bind.Jaxb;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author PALATEJ
 */
public class TramoSpecMappingTest {

    public TramoSpecMappingTest() {
    }

    @Test
    public void testAll() {
        test(TramoSpec.TR0);
        test(TramoSpec.TR1);
        test(TramoSpec.TR2);
        test(TramoSpec.TR3);
        test(TramoSpec.TR4);
        test(TramoSpec.TR5);
        test(TramoSpec.TRfull);
    }
    
    @Test
    public void testSpecific() {
        TramoKernel kernel = TramoKernel.of(TramoSpec.TRfull, null);
        RegSarimaModel rslt = kernel.process(Data.TS_PROD, null);
        TramoSpec pspec = TramoFactory.INSTANCE.generateSpec(TramoSpec.TRfull, rslt.getDescription());
        test(pspec);        
        testLegacy(pspec);        
   }

    private void test(TramoSpec spec) {
        InformationSet info = TramoSpecMapping.write(spec, true);
        TramoSpec nspec = TramoSpecMapping.readV3(info);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
        info = TramoSpecMapping.write(spec, false);
        nspec = TramoSpecMapping.readV3(info);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
    }

    @Test
    public void testAllLegacy() {
        testLegacy(TramoSpec.TR0);
        testLegacy(TramoSpec.TR1);
        testLegacy(TramoSpec.TR2);
        testLegacy(TramoSpec.TR3);
        testLegacy(TramoSpec.TR4);
        testLegacy(TramoSpec.TR5);
        testLegacy(TramoSpec.TRfull);
    }

    private void testLegacy(TramoSpec spec) {
        InformationSet info = TramoSpecMapping.writeLegacy(spec, true);
        TramoSpec nspec = TramoSpecMapping.readLegacy(info);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
        info = TramoSpecMapping.writeLegacy(spec, false);
        nspec = TramoSpecMapping.readLegacy(info);
//        System.out.println(spec);
//        System.out.println(nspec);
        assertEquals(nspec, spec);
    }
    
    public static void testXmlSerialization() throws JAXBException, FileNotFoundException, IOException {
        InformationSet info = TramoSpecMapping.writeLegacy(TramoSpec.TRfull, true);
 
        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        String tmp = Files.temporaryFolderPath();
        Jaxb.Formatter
                .of(XmlInformationSet.class)
                .withFormatted(true)
                .formatFile(xmlinfo, new File(tmp + "tramo.xml"));
    }

    public static void testXmlDeserialization() throws JAXBException, FileNotFoundException, IOException {
        String tmp = Files.temporaryFolderPath();
        XmlInformationSet rslt = Jaxb.Parser
                .of(XmlInformationSet.class)
                .parseFile(new File(tmp + "tramo.xml"));

        InformationSet info = rslt.create();
        TramoSpec nspec = TramoSpecMapping.readLegacy(info);
        System.out.println(nspec.equals(TramoSpec.TRfull));
    }
    
    public static void main(String[] arg) throws JAXBException, IOException{
        testXmlSerialization();
        testXmlDeserialization();
    }

}
