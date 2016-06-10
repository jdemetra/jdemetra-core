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
package ec.tss.tsproviders.spreadsheet.facade.poi;

import ec.tss.tsproviders.spreadsheet.facade.Book;
import ec.tss.tsproviders.spreadsheet.facade.utils.BookFactoryAdapter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@Deprecated
@ServiceProvider(service = Book.Factory.class)
public class ExcelBookFactory extends BookFactoryAdapter {

    public ExcelBookFactory() {
        super(new ec.util.spreadsheet.poi.ExcelBookFactory());
    }

    //<editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public void setFast(boolean fast) {
        ((ec.util.spreadsheet.poi.ExcelBookFactory) adaptee).setFast(fast);
    }

    public boolean isFast() {
        return ((ec.util.spreadsheet.poi.ExcelBookFactory) adaptee).isFast();
    }
    //</editor-fold>

}
