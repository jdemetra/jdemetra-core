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


package demetra.data;

import demetra.design.Development;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LogSign {

    public final double value;
    public final boolean pos;
    
    public static LogSign of(Doubles reader){
        double value = 0;
        boolean pos = true;
        CellReader cell = reader.reader();
        int n=reader.length();
        for (int i = 0; i <n; ++i) {
            double x = cell.next();
            if (x < 0) {
                pos = !pos;
                x = -x;
            }
            value += Math.log(x);
        }
        return new LogSign(value, pos);
       
    }
    
    public static LogSign of(Doubles reader, boolean chs){
        double value = 0;
        boolean pos = true;
        CellReader cell = reader.reader();
        int n=reader.length();
        for (int i = 0; i <n; ++i) {
            double x = cell.next();
            if (x < 0) {
                pos = !pos;
                x = -x;
            }
            value += Math.log(x);
        }
        return new LogSign(value, chs ? !pos : pos);
       
    }
    
    private LogSign(double value, boolean pos){
        this.value=value;
        this.pos=pos;
    }
}
