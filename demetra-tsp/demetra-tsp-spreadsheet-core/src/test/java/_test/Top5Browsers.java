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
package _test;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.*;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Philippe Charles
 */
@ServiceProvider(Book.Factory.class)
public final class Top5Browsers extends Book.Factory {

    public static File getRefFile() {
        try {
            File result = File.createTempFile("Top5Browsers", ".top5");
            result.deleteOnExit();
            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public @NonNull String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean canLoad() {
        return true;
    }

    @Override
    public @NonNull Book load(@NonNull InputStream stream) throws IOException {
        return ArrayBook
                .builder()
                .sheet(ArraySheet.copyOf("Top 5 Browsers - Monthly", DATA_1))
                .sheet(ArraySheet.copyOf("Top 5 Browsers - Quarterly", DATA_2))
                .sheet(ArraySheet.copyOf("DataTest", DATA_3))
                .build();
    }

    @Override
    public boolean canStore() {
        return false;
    }

    @Override
    public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.toString().endsWith(".top5");
    }

    private static final Object[][] DATA_1 = {
            {null, "IE", "Firefox", "Chrome", "Safari", "Opera", "Other"},
            {d("2008-07"), 68.57, 26.14, 0, 3.3, 1.78, 0.21},
            {d("2008-08"), 68.91, 26.08, 0, 2.99, 1.83, 0.2},
            {d("2008-09"), 67.16, 25.77, 1.03, 3, 2.86, 0.18},
            {d("2008-10"), 67.68, 25.54, 1.02, 2.91, 2.69, 0.17},
            {d("2008-11"), 68.14, 25.27, 0.93, 2.49, 3.01, 0.16},
            {d("2008-12"), 67.84, 25.23, 1.21, 2.41, 2.83, 0.48},
            {d("2009-01"), 65.41, 27.03, 1.38, 2.57, 2.92, 0.7},
            {d("2009-02"), 64.43, 27.85, 1.52, 2.59, 2.95, 0.67},
            {d("2009-03"), 62.52, 29.4, 1.73, 2.73, 2.94, 0.68},
            {d("2009-04"), 61.88, 29.67, 2.07, 2.75, 2.96, 0.68},
            {d("2009-05"), 62.09, 28.75, 2.42, 2.65, 3.23, 0.87},
            {d("2009-06"), 59.49, 30.33, 2.82, 2.93, 3.36, 1.07},
            {d("2009-07"), 60.11, 30.5, 3.01, 3.02, 2.64, 0.72},
            {d("2009-08"), 58.69, 31.28, 3.38, 3.25, 2.67, 0.73},
            {d("2009-09"), 58.37, 31.34, 3.69, 3.28, 2.62, 0.7},
            {d("2009-10"), 57.96, 31.82, 4.17, 3.47, 1.88, 0.7},
            {d("2009-11"), 56.57, 32.21, 4.66, 3.67, 2.02, 0.86},
            {d("2009-12"), 55.72, 31.97, 5.45, 3.48, 2.06, 1.31},
            {d("2010-01"), 55.25, 31.64, 6.04, 3.76, 2, 1.31},
            {d("2010-02"), 54.5, 31.82, 6.72, 4.08, 1.97, 0.91},
            {d("2010-03"), 54.44, 31.27, 7.29, 4.16, 1.97, 0.87},
            {d("2010-04"), 53.26, 31.74, 8.06, 4.23, 1.82, 0.89},
            {d("2010-05"), 52.77, 31.64, 8.61, 4.14, 1.96, 0.88},
            {d("2010-06"), 52.86, 31.15, 9.24, 4.07, 1.91, 0.77},
            {d("2010-07"), 52.68, 30.69, 9.88, 4.09, 1.91, 0.74},
            {d("2010-08"), 51.34, 31.09, 10.76, 4.23, 1.88, 0.7},
            {d("2010-09"), 49.87, 31.5, 11.54, 4.42, 2.03, 0.63},
            {d("2010-10"), 49.21, 31.24, 12.39, 4.56, 2, 0.6},
            {d("2010-11"), 48.16, 31.17, 13.35, 4.7, 2.01, 0.6},
            {d("2010-12"), 46.94, 30.76, 14.85, 4.79, 2.07, 0.58},
            {d("2011-01"), 46, 30.68, 15.68, 5.09, 2, 0.55},
            {d("2011-02"), 45.44, 30.37, 16.54, 5.08, 2, 0.55},
            {d("2011-03"), 45.11, 29.98, 17.37, 5.02, 1.97, 0.54},
            {d("2011-04"), 44.52, 29.67, 18.29, 5.04, 1.91, 0.57},
            {d("2011-05"), 43.87, 29.29, 19.36, 5.01, 1.84, 0.63},
            {d("2011-06"), 43.58, 28.34, 20.65, 5.07, 1.74, 0.61},
            {d("2011-07"), 42.45, 27.95, 22.14, 5.17, 1.66, 0.63},
            {d("2011-08"), 41.89, 27.49, 23.16, 5.19, 1.67, 0.61},
            {d("2011-09"), 41.66, 26.79, 23.61, 5.6, 1.72, 0.62},
            {d("2011-10"), 40.18, 26.39, 25, 5.93, 1.81, 0.69},
            {d("2011-11"), 40.48, 25.21, 25.65, 5.9, 1.83, 0.93}
    };

    private static final Object[][] DATA_2 = {
            {null, d("2008-07"), d("2008-10"), d("2009-01"), d("2009-04"), d("2009-07"), d("2009-10"), d("2010-01"), d("2010-04"), d("2010-07"), d("2010-10"), d("2011-01"), d("2011-04"), d("2011-07"), d("2011-10")},
            {"IE", 67.99, 67.9, 64.04, 61.12, 59.03, 56.73, 54.67, 52.96, 51.35, 48.1, 45.51, 44, 42, 40.32},
            {"Firefox", 25.94, 25.32, 28.16, 29.57, 31.05, 32, 31.53, 31.51, 31.08, 31.06, 30.34, 29.11, 27.42, 25.84},
            {"Chrome", 0.51, 1.07, 1.55, 2.45, 3.37, 4.78, 6.79, 8.64, 10.7, 13.54, 16.55, 19.42, 22.96, 25.3},
            {"Safari", 3.05, 2.56, 2.63, 2.78, 3.19, 3.54, 4.03, 4.15, 4.24, 4.69, 5.06, 5.04, 5.31, 5.92},
            {"Opera", 2.33, 2.85, 2.94, 3.19, 2.65, 1.99, 1.98, 1.89, 1.94, 2.03, 1.99, 1.83, 1.68, 1.82},
            {"Other", 0.19, 0.3, 0.68, 0.88, 0.71, 0.97, 1, 0.85, 0.69, 0.59, 0.55, 0.6, 0.62, 0.8}
    };

    private static final Object[][] DATA_3 = {
            {null, "IE", "Firefox", "Chrome", "Safari", "Opera"},
            {d("2008-07"), null, 26.14, 0, "hello", 1.78},
            {d("2008-08"), null, 26.08, 0, 2.99, 1.83},
            {d("2008-09"), 67.16, 25.77, 1.03, 3, 2.86},
            {d("2008-10"), 67.68, 25.54, null, 2.91, 2.69},
            {d("2008-11"), 68.14, 25.27, "helloworld", 2.49, 3.01},
            {d("2008-12"), 67.84, 25.23, null, 2.41, 2.83},
            {d("2009-01"), 65.41, 27.03, null, 2.57, 2.92},
            {d("2009-02"), 64.43, 27.85, 1.52, 2.59, 2.95},
            {d("2009-03"), 62.52, null, 1.73, 2.73, 2.94},
            {d("2009-04"), 61.88, null, 2.07, 2.75, "world"}
    };

    private static Date d(String text) {
        LocalDateTime result = YearMonth.parse(text).atDay(1).atStartOfDay();
        return Date.from(result.atZone(ZoneId.systemDefault()).toInstant());
    }
}
