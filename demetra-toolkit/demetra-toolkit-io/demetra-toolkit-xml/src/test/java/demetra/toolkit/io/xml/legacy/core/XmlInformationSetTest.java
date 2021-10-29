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
package demetra.toolkit.io.xml.legacy.core;

import demetra.data.Data;
import demetra.information.InformationSet;
import demetra.timeseries.TsData;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.assertj.core.util.Files;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class XmlInformationSetTest {

    public XmlInformationSetTest() {
    }

    public static void testXmlConversion() {
        InformationSet info = new InformationSet();
        double[] data = new double[20];
        for (int i = 0; i < data.length; ++i) {
            data[i] = i;
        }
        Matrix M = Matrix.of(data, 5, 4);
        TsData ts = Data.TS_PROD;
        info.add("m", M);
        info.add("ts", ts);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        // back to information set
        InformationSet ninfo = xmlinfo.create();
        Matrix nM = ninfo.get("m", Matrix.class);
        TsData nts = ninfo.get("ts", TsData.class);
        System.out.println(M.equals(nM));
        System.out.println(ts.equals(nts));
    }

    public static void main(String[] s) throws JAXBException, IOException {
        testXmlConversion();
        testXmlSerialization();
        testXmlDeserialization();
    }

    public static void testXmlSerialization() throws JAXBException, FileNotFoundException, IOException {
        InformationSet info = new InformationSet();
        double[] data = new double[20];
        for (int i = 0; i < data.length; ++i) {
            data[i] = i;
        }
        Matrix M = Matrix.of(data, 5, 4);
        TsData ts = Data.TS_PROD;
        info.add("m", M);
        info.add("ts", ts);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        String tmp = Files.temporaryFolderPath();
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);

        FileOutputStream stream = new FileOutputStream(tmp + "test.xml");
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

        FileInputStream istream = new FileInputStream(tmp + "test.xml");
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            XmlInformationSet rslt = (XmlInformationSet) unmarshaller.unmarshal(reader);
            InformationSet info = rslt.create();
            Matrix M = info.get("m", Matrix.class);
            TsData ts = info.get("ts", TsData.class);
            System.out.println(M);
            System.out.println(ts);
        }
    }

}
