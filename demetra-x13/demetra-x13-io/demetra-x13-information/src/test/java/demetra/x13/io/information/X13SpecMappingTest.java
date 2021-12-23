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
package demetra.x13.io.information;

import demetra.data.Data;
import demetra.information.InformationSet;
import demetra.sa.SaDefinition;
import demetra.sa.SaEstimation;
import demetra.sa.SaItem;
import demetra.sa.SaItems;
import demetra.sa.SaSpecification;
import demetra.sa.io.information.SaItemMapping;
import demetra.sa.io.information.SaItemsMapping;
import demetra.timeseries.Ts;
import demetra.timeseries.TsMoniker;
import demetra.toolkit.io.xml.information.XmlInformationSet;
import demetra.util.NameManager;
import demetra.x13.X13Spec;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import jdplus.x13.X13Results;
import org.assertj.core.util.Files;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.information.Explorable;

/**
 *
 * @author PALATEJ
 */
public class X13SpecMappingTest {

    public X13SpecMappingTest() {
    }

    @Test
    public void testSaItem() {
        Ts ts = Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();

        SaDefinition sadef = SaDefinition.builder()
                .domainSpec(X13Spec.RSA5)
                .ts(ts)
                .build();

        SaItem item = SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(null, false);
        NameManager<SaSpecification> mgr = SaItemsMapping.defaultNameManager();
        InformationSet info = SaItemMapping.write(item, mgr, true);
        
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
                .domainSpec(X13Spec.RSA5)
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
        
        InformationSet info = SaItemsMapping.write(items, true);
        
        SaItems nitems = SaItemsMapping.read(info);
        nitems.getItems().forEach(v->v.process(null, true));
    }


    public static void testXmlDeserializationLegacy() throws FileNotFoundException {
        String tmp = Files.temporaryFolderPath();

        FileInputStream istream = new FileInputStream(tmp + "saprocessing-2.xml");
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            XmlInformationSet rslt = (XmlInformationSet) unmarshaller.unmarshal(reader);
            InformationSet info = rslt.create();
            SaItems nspec = SaItemsMapping.read(info);
            nspec.getItems().forEach(v->v.process(null, false));
            System.out.println(nspec.getItems().size());
//            nspec.getItems().forEach(v -> System.out.println(((TramoSeatsResults) v.getEstimation().getResults()).getPreprocessing().getEstimation().getStatistics().getLogLikelihood()));
            long t0=System.currentTimeMillis();
            nspec.getItems().forEach(v->
            {
                v.process(null, false);
                SaEstimation estimation = v.getEstimation();
                X13Results results = (X13Results) estimation.getResults();
                System.out.println(results.getPreprocessing().getEstimation().getStatistics().getLogLikelihood());
            }
            );
//            System.out.println(nspec.getItems().get(0).getDefinition().getDomainSpec().equals(TramoSeatsSpec.RSA5));
            long t1=System.currentTimeMillis();
            System.out.println(t1-t0);
        } catch (IOException ex) {
        } catch (JAXBException ex) {
        }
    }

    public static void main(String[] arg) throws JAXBException, IOException {
        testXmlDeserializationLegacy();
    }
}
