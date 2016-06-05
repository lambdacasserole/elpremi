package com.sauljohnson.elpremi;

import com.sauljohnson.backspin.BitInputStream;
import com.sauljohnson.backspin.BitSequence;
import com.sauljohnson.huff.HuffmanCompressionResult;
import com.sauljohnson.huff.HuffmanCompressor;
import com.sauljohnson.huff.PrefixCodeTable;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides compression and decompression in the Elpremi format.
 *
 * @version 1.0 05 June 2016
 * @author  Saul Johnson, Alex Mullen, Lee Oliver
 */

public class ElpremiCompressor {

    /** The underlying compressor. */
    private HuffmanCompressor compressor;

    /**
     * Initializes a new instance of a compression provider that uses plain Huffman coding to compress a set of bytes.
     */
    public ElpremiCompressor() {
        compressor = new HuffmanCompressor();
    }

    /**
     * Converts a boxed {@link Integer} array to an array of primitives.
     * @param boxedArray    the boxed array to convert
     * @return  an array of primitives corresponding to the values in the boxed array
     */
    private static int[] toPrimitiveIntArray(Integer[] boxedArray) {
        int[] primitive = new int[boxedArray.length];
        for (int i = 0; i < boxedArray.length; i++) {
            primitive[i] = boxedArray[i];
        }
        return primitive;
    }

    /**
     * Returns the number of bytes needed to hold the given number of bits.
     * @param bitLength the number of bits
     * @return  the number of bytes needed to hold the given number of bits
     */
    private static int toByteLength(int bitLength) {
        return (int) Math.ceil((double) bitLength / 8.00d);
    }

    /**
     * Converts a byte array to a bit sequence.
     * @param bytes the byte array to convert
     * @param len   the number of bits to convert
     * @return  the byte array converted to a bit sequence
     */
    private static BitSequence bytesToBitSequence(byte[] bytes, int len) {
        BitSequence code = new BitSequence();
        BitInputStream codeReader = new BitInputStream(bytes);
        for (int i = 0; i < len; i++) {
            code.append(codeReader.read());
        }
        return code;
    }

    /**
     * Serializes a {@link PrefixCodeTable} to an {@link OutputStream}.
     * @param table     the prefix code table to serialize
     * @param stream    the stream to serialize to
     * @throws IOException
     */
    private static void serializePrefixCodeTable(PrefixCodeTable table, OutputStream stream) throws IOException {
        // Get symbols and codes.
        int[] symbols = table.getSymbols();
        BitSequence[] codes = table.getCodes();

        // Serialise table to output.
        for (int i = 0; i < symbols.length; i++) {
            byte[] codeBytes = codes[i].toArray();
            stream.write((byte) symbols[i]);
            stream.write((byte) codes[i].getLength());
            stream.write(codeBytes);
            stream.write(i == symbols.length - 1 ? 0xFF : 0x00);
        }
    }

    /**
     * Deserializes a {@link PrefixCodeTable} from an {@link InputStream}.
     * @param stream    the stream to deserialize from
     * @return  the deserialized prefix code table
     * @throws IOException
     */
    private static PrefixCodeTable deserializePrefixCodeTable(InputStream stream) throws IOException {
        // Create lists of symbols and codes.
        List<Integer> symbols = new ArrayList<Integer>();
        List<BitSequence> codes = new ArrayList<BitSequence>();

        do {
            // Get symbol and code length in bits.
            int symbol = stream.read();
            int codeLength = stream.read();

            // Read code bytes.
            byte[] codeBytes = new byte[toByteLength(codeLength)];
            stream.read(codeBytes);

            // Read code into bit sequence.
            BitSequence code = bytesToBitSequence(codeBytes, codeLength);

            // Add code and symbol to lists.
            symbols.add(symbol);
            codes.add(code);
        } while (stream.read() != 0xFF); // Trailing byte is 0xFF only if this is the last entry.

        // Convert to arrays.
        int[] symbolArray = toPrimitiveIntArray(symbols.toArray(new Integer[] {}));
        BitSequence[] codeArray = codes.toArray(new BitSequence[] {});

        return new PrefixCodeTable(symbolArray, codeArray); // Pass back code table.
    }

    /**
     * Converts a length-4 byte array to a 32-bit integer.
     * @param array the array to convert
     * @return  the length-4 byte array represented as a 32-bit integer.
     */
    private static int byteArrayToInt(byte[] array) {
        return ByteBuffer.allocate(4).put(array).getInt(0);
    }

    /**
     * Converts an integer to a length-4 byte array.
     * @param num   the integer to convert
     * @return  the specified integer represented as a length-4 byte array
     */
    private static byte[] intToByteArray(int num) {
        return ByteBuffer.allocate(4).putInt(num).array();
    }

    /**
     * Compresses a byte array using the Elpremi compression format.
     * @param data  the byte array to compress
     * @return      the compressed byte array
     * @throws IOException
     */
    public byte[] compress(byte[] data) throws IOException {
        // Initialize stream to write to.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        HuffmanCompressionResult result = compressor.compress(data); // Compress data.
        stream.write(intToByteArray(result.getLength())); // Write length of compressed data in bits.
        serializePrefixCodeTable(result.getTable(), stream); // Serialize prefix code table to stream.
        stream.write(result.getData()); // Write compressed data to output.

        // Close stream.
        stream.close();

        // Result contains everything needed to decompress the data again.
        return stream.toByteArray();
    }

    /**
     * Decompresses a byte array using the Elpremi compression format.
     * @param data  the byte array to decompress
     * @return      the decompressed byte array
     * @throws IOException
     */
    public byte[] decompress(byte[] data) throws IOException {
        // Initialize stream to read from.
        ByteArrayInputStream stream = new ByteArrayInputStream(data);

        // Get length in bits of compressed file.
        byte[] lengthBytes = new byte[4];
        stream.read(lengthBytes);
        int lengthInBits = byteArrayToInt(lengthBytes);

        // Reconstruct prefix code table.
        PrefixCodeTable table = deserializePrefixCodeTable(stream);

        // Get compressed data payload.
        final byte[] compressedData = new byte[toByteLength(lengthInBits)];
        stream.read(compressedData);
        stream.close();

        // Read data in a bits, output as bytes.
        final BitInputStream bitIn = new BitInputStream(compressedData);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Decompress data.
        BitSequence buffer = new BitSequence();
        while (bitIn.getPosition() < lengthInBits) {
            buffer.append(bitIn.read());
            if (table.hasCode(buffer)) {
                out.write(table.translateCode(buffer));
                buffer = new BitSequence();
            }
        }

        return out.toByteArray();
    }
}
