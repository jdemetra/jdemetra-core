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
package jdplus.stats;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import java.util.function.IntToDoubleFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class InverseAutoCorrelationsTest {
    
    public InverseAutoCorrelationsTest() {
    }

    public static void main(String[] args) {
        DoubleSeq y=DoubleSeq.of(Data.PROD);
        IntToDoubleFunction fn = InverseAutoCorrelations.sampleInverseAutoCorrelationsFunction(y.log().delta(1).delta(12), 50);
        for (int i=1; i<=50; ++i){
            System.out.println(fn.applyAsDouble(i));
        }
    }
    
}
