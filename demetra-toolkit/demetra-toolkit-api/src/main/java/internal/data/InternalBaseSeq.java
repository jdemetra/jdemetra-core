/*
 * Copyright 2019 National Bank of Belgium
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
package internal.data;

import demetra.data.BaseSeq;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalBaseSeq {

    public static abstract class EmptyBaseSeq implements BaseSeq {

        @Override
        public int length() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    public static abstract class SingleBaseSeq implements BaseSeq {

        @Override
        public int length() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    public static abstract class MultiBaseSeq implements BaseSeq {

    }

    public static abstract class SubBaseSeq implements BaseSeq {

        protected final int begin;
        protected final int length;

        public SubBaseSeq(int beg, int len) {
            this.begin = beg;
            this.length = len;
        }

        @Override
        public int length() {
            return length;
        }
    }

    public static abstract class MappingBaseSeq implements BaseSeq {

        protected final int length;

        public MappingBaseSeq(int length) {
            this.length = length;
        }

        @Override
        public int length() {
            return length;
        }
    }
}
