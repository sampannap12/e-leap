package com.nfclab.e_leap_project;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;



public class FlexiblePolylineEncoderDecoder {


    public static final byte FORMAT_VERSION = 1;

    //Base64 URL-safe characters
    public static final char[] ENCODING_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();

    public static final  int[] DECODING_TABLE = {
            62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
            22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };


    public final List<LatLngZ> decode(String encoded) {

        if (encoded == null || encoded.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid argument!");
        }
        List<LatLngZ> result = new ArrayList<>();
        Decoder dec = new Decoder(encoded);
        AtomicReference<Double> lat = new AtomicReference<>(0d);
        AtomicReference<Double> lng = new AtomicReference<>(0d);
        AtomicReference<Double> z   = new AtomicReference<>(0d);

        while (dec.decodeOne(lat, lng, z)) {
            result.add(new LatLngZ(lat.get(), lng.get(), z.get()));
            lat = new AtomicReference<>(0d);
            lng = new AtomicReference<>(0d);
            z   = new AtomicReference<>(0d);
        }
        return result;
    }



    public byte getVersion() {
        return FORMAT_VERSION;
    }



    /*
     * Single instance for decoding an input request.
     */
    private static class Decoder {

        private final char[] encoded;
        private final AtomicInteger index;
        private final Converter latConverter;
        private final Converter lngConverter;
        private final Converter zConverter;

        private int precision;
        private int thirdDimPrecision;
        private ThirdDimension thirdDimension;


        public Decoder(String encoded) {
            this.encoded = encoded.toCharArray();
            this.index = new AtomicInteger(0);
            decodeHeader();
            this.latConverter = new Converter(precision);
            this.lngConverter = new Converter(precision);
            this.zConverter = new Converter(thirdDimPrecision);
        }

        private boolean hasThirdDimension() {
            return thirdDimension != ThirdDimension.ABSENT;
        }

        private void decodeHeader() {
            AtomicLong header = new AtomicLong(0);
            decodeHeaderFromString(encoded, index, header);
            precision = (int) (header.get() & 15); // we pick the first 4 bits only
            header.set(header.get() >> 4);
            thirdDimension = ThirdDimension.fromNum(header.get() & 7); // we pick the first 3 bits only
            thirdDimPrecision = (int) ((header.get() >> 3) & 15);
        }

        private static void decodeHeaderFromString(char[] encoded, AtomicInteger index, AtomicLong header) {
            AtomicLong value = new AtomicLong(0);

            // Decode the header version
            if(!Converter.decodeUnsignedVarint(encoded, index, value)) {
                throw new IllegalArgumentException("Invalid encoding");
            }
            if (value.get() != FORMAT_VERSION) {
                throw new IllegalArgumentException("Invalid format version");
            }
            // Decode the polyline header
            if(!Converter.decodeUnsignedVarint(encoded, index, value)) {
                throw new IllegalArgumentException("Invalid encoding");
            }
            header.set(value.get());
        }


        private boolean decodeOne(AtomicReference<Double> lat,
                                  AtomicReference<Double> lng,
                                  AtomicReference<Double> z) {
            if (index.get() == encoded.length) {
                return false;
            }
            if (!latConverter.decodeValue(encoded, index, lat)) {
                throw new IllegalArgumentException("Invalid encoding");
            }
            if (!lngConverter.decodeValue(encoded, index, lng)) {
                throw new IllegalArgumentException("Invalid encoding");
            }
            if (hasThirdDimension()) {
                if (!zConverter.decodeValue(encoded, index, z)) {
                    throw new IllegalArgumentException("Invalid encoding");
                }
            }
            return true;
        }
    }

    //Decode a single char to the corresponding value
    private static int decodeChar(char charValue) {
        int pos = charValue - 45;
        if (pos < 0 || pos > 77) {
            return -1;
        }
        return DECODING_TABLE[pos];
    }

    /*
     * Stateful instance for encoding and decoding on a sequence of Coordinates part of an request.
     * Instance should be specific to type of coordinates (e.g. Lat, Lng)
     * so that specific type delta is computed for encoding.
     * Lat0 Lng0 3rd0 (Lat1-Lat0) (Lng1-Lng0) (3rdDim1-3rdDim0)
     */
    public static class Converter {

        private long multiplier = 0;
        private long lastValue = 0;

        public Converter(int precision) {
            setPrecision(precision);
        }

        private void setPrecision(int precision) {
            multiplier = (long) Math.pow(10, Double.valueOf(precision));
        }

        private static void encodeUnsignedVarint(long value, StringBuilder result) {
            while (value > 0x1F) {
                byte pos =  (byte) ((value & 0x1F) | 0x20);
                result.append(ENCODING_TABLE[pos]);
                value >>= 5;
            }
            result.append(ENCODING_TABLE[(byte) value]);
        }

        void encodeValue(double value, StringBuilder result) {
            /*
             * Round-half-up
             * round(-1.4) --> -1
             * round(-1.5) --> -2
             * round(-2.5) --> -3
             */
            long scaledValue = (long) Math.round(Math.abs(value * multiplier)) * Math.round(Math.signum(value));
            long delta = scaledValue - lastValue;
            boolean negative = delta < 0;

            lastValue = scaledValue;

            // make room on lowest bit
            delta <<= 1;

            // invert bits if the value is negative
            if (negative) {
                delta = ~delta;
            }
            encodeUnsignedVarint(delta, result);
        }

        private static boolean decodeUnsignedVarint(char[] encoded,
                                                    AtomicInteger index,
                                                    AtomicLong result) {
            short shift = 0;
            long delta = 0;
            long value;

            while (index.get() < encoded.length) {
                value = decodeChar(encoded[index.get()]);
                if (value < 0) {
                    return false;
                }
                index.incrementAndGet();
                delta |= (value & 0x1F) << shift;
                if ((value & 0x20) == 0) {
                    result.set(delta);
                    return true;
                } else {
                    shift += 5;
                }
            }

            if (shift > 0) {
                return false;
            }
            return true;
        }

        //Decode single coordinate (say lat|lng|z) starting at index
        boolean decodeValue(char[] encoded,
                            AtomicInteger index,
                            AtomicReference<Double> coordinate) {
            AtomicLong delta = new AtomicLong();
            if (!decodeUnsignedVarint(encoded, index, delta)) {
                return false;
            }
            if ((delta.get() & 1) != 0) {
                delta.set(~delta.get());
            }
            delta.set(delta.get()>>1);
            lastValue += delta.get();
            coordinate.set(((double)lastValue / multiplier));
            return true;
        }

        // Overloaded version for backwards compatibility
        boolean decodeValue(String encoded,
                            AtomicInteger index,
                            AtomicReference<Double> coordinate) {
            return decodeValue(encoded.toCharArray(), index, coordinate);
        }
    }

    /**
     * 	3rd dimension specification.
     *  Example a level, altitude, elevation or some other custom value.
     *  ABSENT is default when there is no third dimension en/decoding required.
     */
    public static enum ThirdDimension {
        ABSENT(0),
        LEVEL(1),
        ALTITUDE(2),
        ELEVATION(3),
        RESERVED1(4),
        RESERVED2(5),
        CUSTOM1(6),
        CUSTOM2(7);

        private int num;

        ThirdDimension(int num) {
            this.num = num;
        }

        public int getNum() {
            return num;
        }

        public static ThirdDimension fromNum(long value) {
            for (ThirdDimension dim : ThirdDimension.values()) {
                if (dim.getNum() == value) {
                    return dim;
                }
            }
            return null;
        }
    }

    /**
     * Coordinate triple
     */
    public static class LatLngZ {
        public final double lat;
        public final double lng;
        public final double z;

        public LatLngZ (double latitude, double longitude) {
            this(latitude, longitude, 0);
        }

        public LatLngZ (double latitude, double longitude, double thirdDimension) {
            this.lat = latitude;
            this.lng = longitude;
            this.z   = thirdDimension;
        }

        @NonNull
        @Override
        public String toString() {
            return "LatLngZ [lat=" + lat + ", lng=" + lng + ", z=" + z + "]";
        }

        @Override
        public boolean equals(Object anObject) {
            if (this == anObject) {
                return true;
            }
            if (anObject instanceof LatLngZ) {
                LatLngZ passed = (LatLngZ)anObject;
                return passed.lat == this.lat && passed.lng == this.lng && passed.z == this.z;
            }
            return false;
        }
    }
}