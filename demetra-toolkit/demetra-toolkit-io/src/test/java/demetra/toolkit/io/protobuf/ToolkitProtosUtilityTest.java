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
package demetra.toolkit.io.protobuf;

import demetra.data.Data;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jdplus.math.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class ToolkitProtosUtilityTest {

    public ToolkitProtosUtilityTest() {
    }

    @Test
    public void testSpan() {
        TimeSelector first = TimeSelector.first(10);
        TimeSelector convert = ToolkitProtosUtility.convert(ToolkitProtosUtility.convert(first));
        assertTrue(first.equals(convert));
        TimeSelector last = TimeSelector.last(10);
        convert = ToolkitProtosUtility.convert(ToolkitProtosUtility.convert(last));
        assertTrue(last.equals(convert));
        TimeSelector from = TimeSelector.from(LocalDate.now().atStartOfDay());
        convert = ToolkitProtosUtility.convert(ToolkitProtosUtility.convert(from));
        assertTrue(from.equals(convert));
        TimeSelector to=TimeSelector.to(LocalDate.now().atStartOfDay());
        convert = ToolkitProtosUtility.convert(ToolkitProtosUtility.convert(to));
        assertTrue(to.equals(convert));
        TimeSelector between=TimeSelector.between(LocalDate.now().atStartOfDay(), LocalDate.MAX.atStartOfDay());
        convert = ToolkitProtosUtility.convert(ToolkitProtosUtility.convert(between));
        assertTrue(between.equals(convert));
        TimeSelector excluding = TimeSelector.excluding(5, 10);
        convert = ToolkitProtosUtility.convert(ToolkitProtosUtility.convert(excluding));
        assertTrue(excluding.equals(convert));
    }
    
    @Test
    public void testMatrix(){
        Matrix M=Matrix.make(10, 20);
        M.set((r, c)->r+c);
        ToolkitProtos.Matrix m = ToolkitProtosUtility.convert(M);
        int n = m.getValueCount();
        assertTrue(n == M.getRowsCount()*M.getColumnsCount());
        assertTrue(m.getValue(n-1) == M.get(M.getRowsCount()-1, M.getColumnsCount()-1));
    }
    
    @Test
    public void testTsData(){
        TsData s=Data.TS_ABS_RETAIL;
        ToolkitProtos.TsData data = ToolkitProtosUtility.convert(s);
        int n=s.length();
        assertTrue(data.getValueCount() == n);
        assertTrue(data.getValue(n-1) == s.getValue(n-1));
        
    }

}
