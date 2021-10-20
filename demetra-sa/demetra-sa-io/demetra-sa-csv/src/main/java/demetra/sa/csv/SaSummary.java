/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

package demetra.sa.csv;

import java.io.StringWriter;

/**
 *
 * @author Kristof Bayens
 */
public class SaSummary {

//    public final TsData Orig, T, S, I, SA, CAL;
//    public final TsIdentifier Identifier;
//    public final String Spec;
//    public final DecompositionMode Mode;
//
//    public SaSummary(SaDocument<ISaSpecification> doc) {
//        Identifier = new TsIdentifier(doc.getTs());
//        Orig = doc.getSeries();
//        CompositeResults sadj = doc.getResults();
//        if (sadj != null) {
//            T = sadj.getData(ModellingDictionary.T, TsData.class);
//            S = sadj.getData(ModellingDictionary.S, TsData.class);
//            SA = sadj.getData(ModellingDictionary.SA, TsData.class);
//            I = sadj.getData(ModellingDictionary.I, TsData.class);
//            CAL = sadj.getData(ModellingDictionary.CAL, TsData.class);
//            Mode = doc.getFinalDecomposition().getMode();
//        } else {
//            T = null;
//            S = null;
//            SA = null;
//            I = null;
//            CAL = null;
//            Mode = DecompositionMode.Undefined;
//        }
//        String xml = null;
//        AbstractXmlSaSpecification xspec = AbstractXmlSaSpecification.create(doc.getSpecification());
//        if (xspec != null) {
//            try {
//                StringWriter writer = new StringWriter();
//                xspec.serialize(writer);
//                xml = writer.toString();
//            } catch (Exception ex) {
//            }
//        }
//        Spec = xml == null ? "" : xml;
//    }
}
