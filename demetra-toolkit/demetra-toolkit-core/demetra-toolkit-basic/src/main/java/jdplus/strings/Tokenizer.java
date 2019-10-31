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
package jdplus.strings;

import demetra.design.Development;


/**
 * A tokenizer split a given string in sub-string, delimited by predefined
 * characters
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Tokenizer {

    private String m_str;
    private int m_cur;
    private char m_delimiter;
    private boolean m_ws;

    // / <summary>
    // / Default tokenizer, with items separated by white spaces
    // / </summary>
    // / <param name="s">The string being split</param>
    /**
     *
     * @param s
     */
    public Tokenizer(String s) {
        m_cur = 0;
        m_str = s;
        m_ws = true;
    }

    // / <summary>
    // / Tokenizer with a specified delimiter
    // / </summary>
    // / <param name="s">The string being split</param>
    // / <param name="delimiter">The delimiter character</param>
    /**
     *
     * @param s
     * @param delimiter
     */
    public Tokenizer(String s, char delimiter) {
        m_cur = 0;
        m_str = s;
        m_ws = false;
        m_delimiter = delimiter;
    }

    // / <summary>
    // / Checks if there is another token
    // / </summary>
    // / <returns>True if there is another token.</returns>
    // / <remarks>That method should always be called before
    // "NextToken"</remarks>
    /**
     *
     * @return
     */
    public boolean hasNextToken() {
        if (m_ws) {
            while (m_cur < m_str.length()
                    && Character.isWhitespace(m_str.charAt(m_cur))) {
                ++m_cur;
            }
            return m_cur < m_str.length();
        } else {
            return m_cur < m_str.length();
        }

    }

    // / <summary>
    // / Retrieves the next token
    // / </summary>
    // / <returns>The next token. Coul be empty</returns>
    /**
     *
     * @return
     */
    public String nextToken() {
        int cur = m_cur;
        if (m_ws) {
            while (m_cur < m_str.length()
                    && !Character.isWhitespace(m_str.charAt(m_cur))) {
                ++m_cur;
            }
            String sub = m_str.substring(cur, m_cur);
            return sub;
        } else {
            while (m_cur < m_str.length() && m_str.charAt(m_cur) != m_delimiter) {
                ++m_cur;
            }
            String sub = m_str.substring(cur, m_cur++);
            return sub;
        }
    }
}
