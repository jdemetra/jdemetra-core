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
package demetra.util;

/**
 *
 * @author Jean Palate
 */
public class DefaultNameValidator implements INameValidator {

    private final CharSequence ex;
    private String msg;
    private static final String EMPTY_ERROR = "The name can't be empty",
            WS_ERROR = "The name can't contain leading or trailing ws";

    public DefaultNameValidator(CharSequence ex) {
        this.ex = ex;
    }

    private String error(char c) {
        StringBuilder builder = new StringBuilder();
        builder.append("The name can't contain '").append(c).append('\'');
        return builder.toString();
    }

    @Override
    public boolean accept(String name) {
        msg = null;
        if (name == null || name.isEmpty()) {
            msg = EMPTY_ERROR;
            return false;
        }

        if (Character.isWhitespace(name.charAt(0)) || Character.isWhitespace(name.charAt(name.length() - 1))) {
            msg = WS_ERROR;
            return false;
        }

        for (int i = 0; i < ex.length(); ++i) {
            char c = ex.charAt(i);
            if (name.indexOf(c) >= 0) {
                msg = error(c);
                return false;
            }
        }
        return true;
    }

    @Override
    public String getLastError() {
        return msg;
    }

}
