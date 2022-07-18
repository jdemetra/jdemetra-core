/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.ssf.sts;

import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;
import jdplus.ssf.utility.DynamicsCoherence;
import jdplus.ssf.utility.LoadingCoherence;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class CyclicalComponentTest {

    public CyclicalComponentTest() {
    }

    @Test
    public void testDynamics() {
        StateComponent lt = CyclicalComponent.stateComponent(0.8, 60, 1.5);
        DynamicsCoherence.check(lt.dynamics(), lt.dim());
    }

    @Test
    public void testMeasurement() {
        ISsfLoading l = CyclicalComponent.defaultLoading();
        LoadingCoherence.check(l, LocalLinearTrend.dim());
    }
}
