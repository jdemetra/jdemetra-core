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

package ec.tstoolkit.algorithm;

import ec.tstoolkit.information.InformationSet;
import java.util.Collections;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class ProcessingManagerTest {

    public ProcessingManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testPower() {
        ProcessingManager.getInstance().register(new PowerProcessingFactory(), PowerSpecification.class);
        double power = 1.5, input = 2;

        PowerSpecification tmp = new PowerSpecification(power);
        // the information spec could have been created by another way: xml de-serialization...
        InformationSet spec = tmp.write(false);
        IProcessing<Double, IProcResults> processor = (IProcessing<Double, IProcResults>) ProcessingManager.getInstance().createProcessor(spec, null);
        IProcResults rslt = processor.process(input);
        double z = rslt.getData(SingleResult.VALUE, Double.class);

        assertTrue(Math.pow(input, power) == z);
    }
}

class PowerSpecification implements IProcSpecification {

    private double power;

    PowerSpecification() {
        power = 1;
    }

    PowerSpecification(double power) {
        this.power = power;
    }

    public double getPower() {
        return power;
    }

    @Override
    public IProcSpecification clone() {
        try {
            return (IProcSpecification) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(ALGORITHM, PowerProcessingFactory.DESCRIPTOR);
        info.set(PARAMETER, power);
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        AlgorithmDescriptor desc = info.get(ALGORITHM, AlgorithmDescriptor.class);
        if (desc != null && !desc.equals(PowerProcessingFactory.DESCRIPTOR)) {
            return false;
        }
        Double p = info.get(PARAMETER, Double.class);
        if (p == null) {
            return false;
        }
        power = p;
        return true;
    }
    static final String PARAMETER = "parameter";

    static Map<String, Class> dictionary() {
        return Collections.singletonMap(PARAMETER, (Class) Double.class);
    }
}

class PowerProcessingFactory implements IProcessingFactory<PowerSpecification, Double, IProcResults> {

    static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor("math", "power", "19092012");

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof PowerSpecification;
    }

    @Override
    public IProcessing<Double, IProcResults> generateProcessing(PowerSpecification specification, ProcessingContext context) {
        return new PowerProcessing(specification.getPower());
    }

    public IProcessing<Double, IProcResults> generateProcessing(PowerSpecification specification) {
        return new PowerProcessing(specification.getPower());
    }
    
    @Override
    public Map<String, Class> getSpecificationDictionary(Class<PowerSpecification> specClass) {
        return PowerSpecification.dictionary();
    }

    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class PowerProcessing implements IProcessing<Double, IProcResults> {

    private double power;

    PowerProcessing(double power) {
        this.power = power;
    }

    @Override
    public IProcResults process(Double input) {
        return new SingleResult<>(Math.pow(input, power), Double.class);
    }
    public static final String POWER = "power";
}
