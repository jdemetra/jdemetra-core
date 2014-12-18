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
 * @author Kristof Bayens
 * @author Mats Maggi
 */
public class HtmlTableHeader extends HtmlTableCell {
    /**
     * 
     * @param text
     */
    public HtmlTableHeader(String text)
    {
	super(text);
    }
    
    /**
     * Constructs a HtmlTableHeader from a text and styles
     * @author Mats Maggi
     * @param text
     * @param styles 
     */
    public HtmlTableHeader(String text, HtmlStyle ... styles)
    {
	super(text, styles);
    }
    
    public HtmlTableHeader(String text, int colspan, int rowspan, HtmlStyle ... styles)
    {
	super(text, styles);
        this.colspan = colspan;
        this.rowspan = rowspan;
    }

    /**
     * 
     * @param text
     * @param width
     */
    public HtmlTableHeader(String text, int width)
    {
	super(text, width);
    }
}
