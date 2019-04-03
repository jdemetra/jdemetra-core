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

import demetra.data.BaseSeqCursor;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleVectorCursor;
import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalBlockCursors {

    private static class BlockBaseSeqCursor implements BaseSeqCursor {

        protected final int inc;
        protected final int leftPos;
        protected int cursor;

        public BlockBaseSeqCursor(int inc, int leftPos) {
            this.inc = inc;
            this.leftPos = leftPos;
            this.cursor = leftPos;
        }

        @Override
        public void moveTo(int index) {
            cursor = leftPos + inc * index;
        }

        @Override
        public void skip(int n) {
            cursor += inc * n;
        }
    }

    public static class BlockDoubleSeqCursor extends BlockBaseSeqCursor implements DoubleSeqCursor {

        protected final double[] data;

        public BlockDoubleSeqCursor(double[] data, int inc, int leftPos) {
            super(inc, leftPos);
            this.data = data;
        }

        @Override
        public double getAndNext() {
            double result = data[cursor];
            cursor += inc;
            return result;
        }
    }

    public static class BlockDoubleVectorCursor extends BlockDoubleSeqCursor implements DoubleVectorCursor {

        public BlockDoubleVectorCursor(double[] data, int inc, int leftPos) {
            super(data, inc, leftPos);
        }

        @Override
        public void setAndNext(double newValue) {
            data[cursor] = newValue;
            cursor += inc;
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) {
            data[cursor] = fn.applyAsDouble(data[cursor]);
            cursor += inc;
        }
    }

    private static class BlockP1BaseSeqCursor implements BaseSeqCursor {

        protected final int leftPos;
        protected int cursor;

        public BlockP1BaseSeqCursor(int leftPos) {
            this.leftPos = leftPos;
            this.cursor = leftPos;
        }

        @Override
        public void moveTo(int index) {
            cursor = leftPos + index;
        }

        @Override
        public void skip(int n) {
            cursor += n;
        }
    }

    public static class BlockP1DoubleSeqCursor extends BlockP1BaseSeqCursor implements DoubleSeqCursor {

        protected final double[] data;

        public BlockP1DoubleSeqCursor(double[] data, int leftPos) {
            super(leftPos);
            this.data = data;
        }

        @Override
        public double getAndNext() {
            return data[cursor++];
        }
    }

    public static class BlockP1DoubleVectorCursor extends BlockP1DoubleSeqCursor implements DoubleVectorCursor {

        public BlockP1DoubleVectorCursor(double[] data, int leftPos) {
            super(data, leftPos);
        }

        @Override
        public void setAndNext(double newValue) {
            data[cursor++] = newValue;
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) {
            data[cursor] = fn.applyAsDouble(data[cursor]);
            cursor++;
        }
    }

    private static class BlockM1BaseSeqCursor implements BaseSeqCursor {

        protected final int leftPos;
        protected int cursor;

        public BlockM1BaseSeqCursor(int leftPos) {
            this.leftPos = leftPos;
            this.cursor = leftPos;
        }

        @Override
        public void moveTo(int index) {
            cursor = leftPos - index;
        }

        @Override
        public void skip(int n) {
            cursor -= n;
        }
    }

    public static class BlockM1DoubleSeqCursor extends BlockM1BaseSeqCursor implements DoubleSeqCursor {

        protected final double[] data;

        public BlockM1DoubleSeqCursor(double[] data, int leftPos) {
            super(leftPos);
            this.data = data;
        }

        @Override
        public double getAndNext() {
            return data[cursor--];
        }
    }

    public static class BlockM1DoubleVectorCursor extends BlockM1DoubleSeqCursor implements DoubleVectorCursor {

        public BlockM1DoubleVectorCursor(double[] data, int leftPos) {
            super(data, leftPos);
        }

        @Override
        public void setAndNext(double newValue) {
            data[cursor--] = newValue;
        }

        @Override
        public void applyAndNext(DoubleUnaryOperator fn) {
            data[cursor] = fn.applyAsDouble(data[cursor]);
            cursor--;
        }
    }
}
