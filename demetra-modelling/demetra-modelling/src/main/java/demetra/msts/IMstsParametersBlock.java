/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametersDomain;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author palatej
 */
public interface IMstsParametersBlock {
    
    public static int fullDim(Stream<IMstsParametersBlock> blocks) {
        return blocks.map(block -> block.getDomain().getDim())
                .reduce(0, (a, b) -> a + b);
    }
    
    public static int dim(Stream<IMstsParametersBlock> blocks) {
        return blocks.filter(block -> !block.isFixed())
                .map(block -> block.getDomain().getDim())
                .reduce(0, (a, b) -> a + b);
    }

    /**
     * From function parameters to model parameters.
     *
     * @param blocks
     * @param inparams
     * @return Contains fixed model parameters, initialized with default values
     */
    public static double[] decode(List<IMstsParametersBlock> blocks, DoubleSequence inparams) {
        double[] buffer = new double[fullDim(blocks.stream())];
        int pos = 0;
        DoubleReader reader = inparams.reader();
        for (IMstsParametersBlock p : blocks) {
            pos = p.decode(reader, buffer, pos);
        }
        return buffer;
    }

    /**
     * Fix some parameters, using given (full) model parameters and a selection
     * criterion. This function will change existing default values
     *
     * @param blocks
     * @param test
     * @param inparams
     */
    public static void fixModelParameters(List<IMstsParametersBlock> blocks, Predicate<IMstsParametersBlock> test, DoubleSequence inparams) {
        DoubleReader reader = inparams.reader();
        for (IMstsParametersBlock p : blocks) {
            if (test.test(p)) {
                p.fixModelParameter(reader);
            } else {
                reader.skip(p.getDomain().getDim());
            }
        }
    }

    /**
     * From model parameters to function (transformed) parameters Fixed
     * parameters are not included in the output
     *
     * @param blocks
     * @param inparams
     * @return
     */
    public static double[] encode(List<IMstsParametersBlock> blocks, DoubleSequence inparams) {
        double[] buffer = new double[dim(blocks.stream())];
        int pos = 0;
        DoubleReader reader = inparams.reader();
        for (IMstsParametersBlock p : blocks) {
            pos = p.encode(reader, buffer, pos);
        }
        return buffer;
    }
    
    public static double[] defaultFunctionParameters(List<IMstsParametersBlock> blocks) {
        double[] buffer = new double[dim(blocks.stream())];
        int pos = 0;
        for (IMstsParametersBlock p : blocks) {
            pos = p.fillDefault(buffer, pos);
        }
        return buffer;
    }
    
    IMstsParametersBlock duplicate();
    
    String getName();
    
    boolean isFixed();
    
    default boolean isPotentialInstability() {
        return false;
    }

    /**
     * Reads the parameters and fix them.
     *
     * @param reader The current parameters
     */
    void fixModelParameter(DoubleReader reader);

    void free();    
    
    /**
     * Reads the parameters and transforms them into a suitable input for the
     * builders.
     *
     * @param reader The current parameters
     * @param buffer The buffer with the transformed + fixed parameters
     * @param pos The current position in the buffer
     * @return The new position in the buffer
     */
    int decode(DoubleReader reader, double[] buffer, int pos);

    /**
     * Transforms true parameters into function parameters (skipping fixed
     * parameters)
     *
     * @param reader
     * @param buffer
     * @param pos
     * @return
     */
    int encode(DoubleReader reader, double[] buffer, int pos);
    
    IParametersDomain getDomain();

    /**
     * Fill the default parameters (without fixed parameters)
     *
     * @param buffer
     * @param pos
     * @return
     */
    int fillDefault(double[] buffer, int pos);
    
}
