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
package ec.tss.tsproviders.legacy;

@Deprecated
public enum StringHandlers implements IStringHandler {

    PLAIN {
        static final String SEP = "<<>>";

        @Override
        public String aggregate(String[] o) {
            if (0 == o.length) {
                return "";
            } else {
                final StringBuilder sb = new StringBuilder(o[0]);
                for (int i = 1; i < o.length; i++) {
                    sb.append(SEP);
                    sb.append(o[i]);
                }
                return sb.toString();
            }
        }

        @Override
        public String[] split(String o) {
            return o.split(SEP);
        }
    },

    BASE64 {
        static final String SEP = "/";

        @Override
        public String aggregate(String[] o) {
            if (0 == o.length) {
                return "";
            } else {
                final StringBuilder sb = new StringBuilder(o[0]);
                for (int i = 1; i < o.length; i++) {
                    sb.append(SEP);
                    sb.append(javax.xml.bind.DatatypeConverter.printBase64Binary(o[i].getBytes()));
                }
                return sb.toString();
            }
        }

        @Override
        public String[] split(String o) {
            String[] splitResult = o.split(SEP);
            for (int i = 0; i < splitResult.length; i++) {
                try {
                    splitResult[i] = new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(splitResult[i]));
                }
                catch(Exception e){
                }
            }
            return splitResult;
        }
    }
}
