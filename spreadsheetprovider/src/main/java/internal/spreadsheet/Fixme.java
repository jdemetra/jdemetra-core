/*
 * Copyright 2017 National Bank of Belgium
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
package internal.spreadsheet;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Fixme {

    public interface Matrix {

        int getRowsCount();

        int getColumnsCount();

        double get(int i, int j);
    }

    public interface Table<T> {

        static <T> Table<T> of(int rowCount, int columnsCount) {
            return null;
        }

        int getRowsCount();

        int getColumnsCount();

        T get(int i, int j);

        void set(int i, int j, T value);
    }
}
