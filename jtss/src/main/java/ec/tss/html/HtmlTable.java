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
package ec.tss.html;

/**
 *
 * @author Kristof Bayens, Jean Palate
 */
public class HtmlTable {

    @Deprecated
    public final int border;

    @Deprecated
    public int width;

    public HtmlTable() {
        this.border = 0;
        this.width = 0;
    }

    @Deprecated
    public HtmlTable(int border, int width) {
        this.border = border;
        this.width = width;
    }

    @Deprecated
    public HtmlTable withWidth(int width) {
        this.width = width;
        return this;
    }
}
