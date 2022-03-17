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
import demetra.math.matrices.Matrix;
import demetra.timeseries.TsData;
import nbbrd.io.xml.bind.Jaxb;
import org.assertj.core.util.Files;

import java.io.File;
import java.io.IOException;

/**
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

    public static void main(String[] s) throws IOException {
        testXmlConversion();
        testXmlSerialization();
        testXmlDeserialization();
    }

    public static void testXmlSerialization() throws IOException {
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
        Jaxb.Formatter
                .of(XmlInformationSet.class)
                .withFormatted(true)
                .formatFile(xmlinfo, new File(tmp + "test.xml"));
    }

    public static void testXmlDeserialization() throws IOException {
        String tmp = Files.temporaryFolderPath();
        XmlInformationSet rslt = Jaxb.Parser
                .of(XmlInformationSet.class)
                .parseFile(new File(tmp + "test.xml"));

        InformationSet info = rslt.create();
        Matrix M = info.get("m", Matrix.class);
        TsData ts = info.get("ts", TsData.class);
        System.out.println(M);
        System.out.println(ts);
    }

}
