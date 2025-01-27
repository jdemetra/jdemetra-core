/*
 * Copyright 2016 National Bank of Belgium
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
package data;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.utilities.DoubleList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Jean Palate
 */
public class MatrixReader {

    public static Matrix read(File file) throws FileNotFoundException, IOException {
        return read(file, Locale.ROOT);
    }
    
    public static Matrix read(File file, Locale locale) throws FileNotFoundException, IOException {

        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            List<double[]> data = new ArrayList<>();
            String curline;
            int nc = 0;
            while ((curline = reader.readLine()) != null) {
                double[] c = split(curline, locale);
                if (c == null) {
                    return null;
                }
                if (nc == 0) {
                    nc = c.length;
                } else if (nc != c.length) {
                    return null;
                }
                data.add(c);
            }
            if (data.isEmpty()) {
                return null;
            }
            Matrix M = new Matrix(data.size(), data.get(0).length);
            for (int i = 0; i < data.size(); ++i) {
                M.row(i).copyFrom(data.get(i), 0);
            }
            return M;
        }
    }

    private static double[] split(String line, Locale locale) {
        String[] items = line.split("\\s+|,");
        double[] data = new double[items.length];
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        try {
            for (int i = 0; i < data.length; ++i) {
                Number n = fmt.parse(items[i]);
                data[i] = n.doubleValue();
            }
            return data;
        } catch (ParseException ex) {
            return null;
        }
    }
}
