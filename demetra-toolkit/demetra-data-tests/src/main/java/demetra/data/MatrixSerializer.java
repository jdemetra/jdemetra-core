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
package demetra.data;

import demetra.maths.MatrixType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public class MatrixSerializer {
    
    

    public static MatrixType read(File file, String separators) throws FileNotFoundException, IOException {
        return read(file, Locale.ROOT, separators);
    }

    public static MatrixType read(File file) throws FileNotFoundException, IOException {
        return read(file, Locale.ROOT, "\\s+|,");
    }

    public static void write(MatrixType m, File file) throws FileNotFoundException, IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(m.toString());
        }
    }

    public static MatrixType read(File file, Locale locale, String separators) throws FileNotFoundException, IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<double[]> data = new ArrayList<>();
            String curline;
            int nc = 0;
            while ((curline = reader.readLine()) != null) {
                double[] c = split(curline, locale, separators);
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
            int nrows = data.size(), ncols = data.get(0).length;
            double[] all = new double[nrows * ncols];
            for (int i = 0; i < nrows; ++i) {
                double[] cur = data.get(i);
                for (int j = 0; j < ncols; ++j) {
                    all[i + j * nrows] = cur[j];
                }
            }
            return MatrixType.ofInternal(all, nrows, ncols);
        }
    }

    private static double[] split(String line, Locale locale, String separators) {
        String[] items = line.split(separators);
        double[] data = new double[items.length];
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        for (int i = 0; i < data.length; ++i) {
            try {
                Number n = fmt.parse(items[i]);
                data[i] = n.doubleValue();
            } catch (ParseException ex) {
                data[i] = Double.NaN;
            }
        }
        return data;
    }

}
