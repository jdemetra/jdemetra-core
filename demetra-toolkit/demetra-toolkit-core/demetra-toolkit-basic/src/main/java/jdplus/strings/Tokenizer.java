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

    private final String input;
    private int curpos;
    private final char delimiter;
    private final boolean ws;

    /**
     * Default tokenizer, with items separated by white spaces
     * @param s The string being split
     */
    public Tokenizer(String s) {
        curpos = 0;
        input = s;
        ws = true;
        delimiter=0;
    }

    /**
     * Tokenizer with a specified delimiter
     * @param s The string being split
     * @param delimiter The delimiter
     */
    public Tokenizer(String s, char delimiter) {
        curpos = 0;
        input = s;
        ws = false;
        this.delimiter = delimiter;
    }

    /**
     * Checks if there is another token. This method should always be called before
     * a call to "nextToken"
     * @return True if there is another token.
     * 
     */
    public boolean hasNextToken() {
        if (ws) {
            while (curpos < input.length()
                    && Character.isWhitespace(input.charAt(curpos))) {
                ++curpos;
            }
            return curpos < input.length();
        } else {
            return curpos < input.length();
        }

    }

    /**
     * Retrieves the next token
     * @return The next token. Could be empty.
     */
    public String nextToken() {
        int cur = curpos;
        if (ws) {
            while (curpos < input.length()
                    && !Character.isWhitespace(input.charAt(curpos))) {
                ++curpos;
            }
            String sub = input.substring(cur, curpos);
            return sub;
        } else {
            while (curpos < input.length() && input.charAt(curpos) != delimiter) {
                ++curpos;
            }
            String sub = input.substring(cur, curpos++);
            return sub;
        }
    }
}
