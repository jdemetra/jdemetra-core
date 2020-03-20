/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.regarima;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Mats Maggi
 */
public class AutoModelSpecTest {

    @Test
    public void testDefault() {
        assertTrue(AutoModelSpec.builder().build().isDefault());
    }
    
    @Test
    public void testNotDefault() {
        assertFalse(AutoModelSpec.builder()
                .acceptDefault(true)
                .cancel(0.145)
                .build().isDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidArmaSignificance() {
        AutoModelSpec.builder()
                .armaSignificance(0.3)
                .build();
    }

    @Test
    public void validArmaSignificance1() {
        AutoModelSpec.builder()
                .armaSignificance(1.2)
                .build();
    }

    @Test
    public void validPercentRSE() {
        AutoModelSpec.builder()
                .percentRSE(1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPercentRSE() {
        AutoModelSpec.builder()
                .percentRSE(0)
                .build();
    }

    @Test
    public void validPredCV() {
        AutoModelSpec.builder()
                .predcv(.254)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPredCV1() {
        AutoModelSpec.builder()
                .predcv(0.02)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPredCV2() {
        AutoModelSpec.builder()
                .predcv(3.4)
                .build();
    }

    @Test
    public void validUb1() {
        AutoModelSpec.builder()
                .ub1(1.4)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUb1() {
        AutoModelSpec.builder()
                .ub1(1)
                .build();
    }

    @Test
    public void validUb2() {
        AutoModelSpec.builder()
                .ub2(1/.8)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUb2() {
        AutoModelSpec.builder()
                .ub2(1)
                .build();
    }

    @Test
    public void validUbFinal() {
        AutoModelSpec.builder()
                .ubfinal(1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUbFinal() {
        AutoModelSpec.builder()
                .ubfinal(-1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCancel1() {
        AutoModelSpec.builder()
                .cancel(-1.4)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCancel2() {
        AutoModelSpec.builder()
                .cancel(.3)
                .build();
    }

    @Test
    public void validCancel() {
        AutoModelSpec.builder()
                .cancel(.136)
                .build();
    }
}
