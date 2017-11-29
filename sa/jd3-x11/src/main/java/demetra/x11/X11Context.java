/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.sa.DecompositionMode;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
public class X11Context {

    @lombok.NonNull DecompositionMode mode;
    @lombok.NonNull Number period;
    int hendersonFilterLength;

    DoubleSequence remove(DoubleSequence l, DoubleSequence r) {
        if (mode.isMultiplicative()) {
            return DoubleSequence.of(l.length(), i -> l.get(i) / r.get(i));
        } else {
            return DoubleSequence.of(l.length(), i -> l.get(i) - r.get(i));
        }
    }

    DoubleSequence add(DoubleSequence l, DoubleSequence r) {
        if (mode.isMultiplicative()) {
            return DoubleSequence.of(l.length(), i -> l.get(i) * r.get(i));
        } else {
            return DoubleSequence.of(l.length(), i -> l.get(i) + r.get(i));
        }
    }

    void remove(DoubleSequence l, DoubleSequence r, DataBlock q) {
        if (mode.isMultiplicative()) {
            q.set(l, r, (x, y) -> x / y);
        } else {
            q.set(l, r, (x, y) -> x - y);
        }
    }

    void add(DoubleSequence l, DoubleSequence r, DataBlock q) {
        if (mode.isMultiplicative()) {
            q.set(l, r, (x, y) -> x * y);
        } else {
            q.set(l, r, (x, y) -> x + y);
        }
    }

}
