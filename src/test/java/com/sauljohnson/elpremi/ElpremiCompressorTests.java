package com.sauljohnson.elpremi;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Random;

/**
 * Unit tests for the {@link ElpremiCompressor} class.
 *
 * @version 1.0 05 June 2016
 * @author  Saul Johnson
 */
public class ElpremiCompressorTests extends TestCase {

    /**
     * Creates an array of random bytes.
     * @param length    the length of the array to create
     * @return  an array of random bytes with the specified length
     */
    private static byte[] getRandomBytes(int length) {
        byte[] buffer = new byte[length];
        new Random().nextBytes(buffer);
        return buffer;
    }

    public static void testCompress() throws IOException {
        // Generate array of random bytes to compress.
        byte[] expected = getRandomBytes(1024);

        // Compress then decompress data.
        ElpremiCompressor subject = new ElpremiCompressor();
        byte[] compressed = subject.compress(expected);
        byte[] actual = subject.decompress(compressed);

        // Data should be the same length before and after compression/decompression.
        assertEquals(expected.length, actual.length);

        // Data should be identical before and after compression/decompression.
        for (int i = 0; i < expected.length; i++){
            assertEquals(expected[i], actual[i]);
        }
    }
}
