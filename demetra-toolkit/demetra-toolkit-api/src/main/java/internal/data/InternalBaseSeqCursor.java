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
import demetra.data.BaseSeqCursor;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalBaseSeqCursor {

    public static abstract class DefaultBaseSeqCursor<T extends BaseSeq> implements BaseSeqCursor {

        protected final T data;
        protected int cursor = 0;

        public DefaultBaseSeqCursor(T data) {
            this.data = data;
        }

        @Override
        public void moveTo(int index) {
            cursor = index;
        }

        @Override
        public void skip(int n) {
            cursor += n;
        }
    }

    public static abstract class EmptyBaseSeqCursor implements BaseSeqCursor {

        @Override
        public void moveTo(int index) {
        }

        @Override
        public void skip(int n) {
        }
    }

    public static abstract class SingleBaseSeqCursor implements BaseSeqCursor {

        protected int cursor = 0;

        @Override
        public void moveTo(int index) {
            cursor = index;
        }

        @Override
        public void skip(int n) {
            cursor += n;
        }
    }

    public static abstract class MultiBaseSeqCursor implements BaseSeqCursor {

        protected int cursor = 0;

        @Override
        public void moveTo(int index) {
            cursor = index;
        }

        @Override
        public void skip(int n) {
            cursor += n;
        }
    }

    public static abstract class SubBaseSeqCursor implements BaseSeqCursor {

        protected int begin;
        protected int cursor;

        public SubBaseSeqCursor(int begin) {
            this.begin = begin;
            this.cursor = begin;
        }

        @Override
        public void moveTo(int index) {
            cursor = begin + index;
        }

        @Override
        public void skip(int n) {
            cursor += n;
        }
    }
}
