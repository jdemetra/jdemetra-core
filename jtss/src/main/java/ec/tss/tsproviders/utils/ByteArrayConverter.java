/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.tsproviders.utils;

import ec.tstoolkit.design.NewObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A thread-safe converter that converts objects from/to byte arrays. Note that
 * subclasses might provide compression.
 *
 * @author Philippe Charles
 */
@ThreadSafe
public abstract class ByteArrayConverter {

    /**
     * Gets a system-wide converter.
     * <p>
     * This converter is designed to store data in memory only and not to
     * persist it to disk and/or network. It should be retrieved only once by a
     * class that needs it in order to avoid incompatibilities during subsequent
     * conversions.
     *
     * @return a non-null converter
     */
    @Nonnull
    public static final ByteArrayConverter getInstance() {
        return INSTANCE.get();
    }

    /**
     * Sets a system-wide converter.
     *
     * @param s a non-null converter
     * @see #getInstance()
     */
    public static final void setInstance(@Nonnull ByteArrayConverter s) {
        INSTANCE.set(Objects.requireNonNull(s));
    }

    @Nonnull
    @NewObject
    public byte[] fromDoubleArray(@Nonnull double[] input) {
        int pos = 0;
        byte[] result = new byte[input.length * 8];
        for (int i = 0; i < input.length; i++) {
            long bits = Double.doubleToLongBits(input[i]);
            result[pos++] = (byte) (bits >>> 56);
            result[pos++] = (byte) (bits >>> 48);
            result[pos++] = (byte) (bits >>> 40);
            result[pos++] = (byte) (bits >>> 32);
            result[pos++] = (byte) (bits >>> 24);
            result[pos++] = (byte) (bits >>> 16);
            result[pos++] = (byte) (bits >>> 8);
            result[pos++] = (byte) (bits);
        }
        return result;
    }

    @Nonnull
    @NewObject
    public double[] toDoubleArray(@Nonnull byte[] input) {
        int pos = 0;
        double[] result = new double[input.length / 8];
        for (int i = 0; i < result.length; i++) {
            long bits = ((input[pos++] & 0xffL) << 56)
                    + ((input[pos++] & 0xffL) << 48)
                    + ((input[pos++] & 0xffL) << 40)
                    + ((input[pos++] & 0xffL) << 32)
                    + ((input[pos++] & 0xffL) << 24)
                    + ((input[pos++] & 0xffL) << 16)
                    + ((input[pos++] & 0xffL) << 8)
                    + ((input[pos++] & 0xffL));
            result[i] = Double.longBitsToDouble(bits);
        }
        return result;
    }

    /**
     * Gets a basic default converter.
     *
     * @return a non-null converter
     */
    @Nonnull
    public static final ByteArrayConverter getDefault() {
        return DefaultConverter.INSTANCE;
    }

    /**
     * Gets a converter that compress bytes using {@link Deflater}.
     *
     * @param level the compression level (0-9)
     * @param nowrap if true then use GZIP compatible compression
     * @return a non-null converter
     * @throws IllegalArgumentException if the compression level is invalid
     */
    @Nonnull
    public static final ByteArrayConverter getDeflate(int level, boolean nowrap) throws IllegalArgumentException {
        return new DeflateConverter(level, nowrap);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final AtomicReference<ByteArrayConverter> INSTANCE = new AtomicReference<>(getDefault());

    private static final class DefaultConverter extends ByteArrayConverter {

        private static final DefaultConverter INSTANCE = new DefaultConverter();
    }

    private static final class DeflateConverter extends ByteArrayConverter {

        private final int level;
        private final boolean nowrap;

        public DeflateConverter(int level, boolean nowrap) throws IllegalArgumentException {
            if ((level < 0 || level > 9) && level != Deflater.DEFAULT_COMPRESSION) {
                throw new IllegalArgumentException("invalid compression level");
            }
            this.level = level;
            this.nowrap = nowrap;
        }

        @Override
        public byte[] fromDoubleArray(double[] input) {
            try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                try (DeflaterOutputStream deflater = new DeflaterOutputStream(result, new Deflater(level, nowrap), 512)) {
                    deflater.write(super.fromDoubleArray(input));
                }
                return result.toByteArray();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public double[] toDoubleArray(byte[] input) {
            try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                try (ByteArrayInputStream stream = new ByteArrayInputStream(input)) {
                    try (InflaterInputStream gzip = new InflaterInputStream(stream, new Inflater(nowrap))) {
                        byte[] buffer = new byte[512];
                        int n;
                        while ((n = gzip.read(buffer)) >= 0) {
                            result.write(buffer, 0, n);
                        }
                        return super.toDoubleArray(result.toByteArray());
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    //</editor-fold>
}
