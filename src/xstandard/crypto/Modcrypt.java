package xstandard.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import xstandard.fs.FSUtil;

/**
 * Modcrypt AES-CTR encryption/decryption.
 * Used by DSi-exclusive binaries.
 */

public class Modcrypt {
    
    private final SecretKey key;
    private BigInteger counter;

    private static final int BLOCK_SIZE = 16;
    private static final BigInteger ROLMASK = new BigInteger("3FFFFFFFFFF", 16);
    private static final BigInteger MODULUS = BigInteger.valueOf(2).pow(128);
    private static final BigInteger SCRAMBLER = new BigInteger("FFFEFB4E295902582A680F5F1A4F3E79", 16);

    public Modcrypt(byte[] key, byte[] iv) {
        this.key = new SecretKeySpec(key, 0, iv.length, "AES");
        this.counter = new BigInteger(1, iv);
    }

    /**
     * Initializes a Modcrypt object. Automatically derives the key from the provided header data.
     * 
     * @param gamecode The game code as an ASCII string.
     * @param arm9iHmac The SHA-1 HMAC of the arm9i binary.
     * @param arm9HmacWithSecureArea The SHA-1 HMAC of the arm9 binary (including secure area).
     */
    public Modcrypt(String gamecode, byte[] arm9iHmac, byte[] arm9HmacWithSecureArea) {
        byte[] keyBytes = deriveRetailKey(gamecode, arm9iHmac);
        key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
        
        byte[] counterBytes = new byte[16];
        System.arraycopy(arm9HmacWithSecureArea, 0, counterBytes, 0, 16);
        counter = new BigInteger(1, reverse(counterBytes));
    }

    /**
     * Generates the retail Modcrypt key from the gamecode and the SHA-1 HMAC of the arm9i binary.
     * 
     * @param gamecode The game code as an ASCII string.
     * @param arm9iHmac The SHA-1 HMAC of the arm9i binary from the header.
     * @return The key as a byte array.
     */
    public static byte[] deriveRetailKey(String gamecode, byte[] arm9iHmac) {
        String emagcode = new StringBuilder(gamecode).reverse().toString();
        
        byte[] retailKeyX = ("Nintendo" + gamecode + emagcode).getBytes(StandardCharsets.US_ASCII);
        byte[] retailKeyY = new byte[16];
        System.arraycopy(arm9iHmac, 0, retailKeyY, 0, 16);

        // KeyDSi = (((KeyX) XOR KeyY) + FFFEFB4E295902582A680F5F1A4F3E79h) ROL 42
        BigInteger nX = new BigInteger(1, reverse(retailKeyX));
        BigInteger nY = new BigInteger(1, reverse(retailKeyY));
        BigInteger keyN = (nX.xor(nY)).add(SCRAMBLER);
        BigInteger shiftedKeyN = keyN.shiftLeft(42);
        BigInteger rotatedKeyN = shiftedKeyN.or(shiftedKeyN.shiftRight(128).and(ROLMASK)).and(MODULUS.subtract(BigInteger.ONE));

        return toFixedLengthArray(rotatedKeyN, 16);
    }
    
    /**
     * Transforms an array of bytes using the current key and IV.
     * 
     * Modcrypt is a stream cipher, so this method performs both encryption and decryption.
     * 
     * @param input The array of bytes to transform.
     * @return The transformed array.
     */
    public byte[] transform(byte[] input) throws IOException, GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] output = new byte[input.length];
        
        for (int i = 0; i < input.length; i += BLOCK_SIZE) {
            int bytesToRead = Math.min(BLOCK_SIZE, input.length - i);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(input, i, block, 0, bytesToRead);
            
            byte[] ctr = toFixedLengthArray(counter, 16);
            byte[] pad = cipher.update(ctr);
            counter = counter.add(BigInteger.ONE).mod(MODULUS);
            
            byte[] transformed = new byte[block.length];
            for (int j = 0; j < block.length; j++) {
                transformed[j] = (byte) (block[j] ^ pad[BLOCK_SIZE - 1 - j]);
            }
            
            System.arraycopy(transformed, 0, output, i, bytesToRead);
        }
        
        return output;        
    }

    /** 
     * Reads all bytes from the input stream and transforms them,
     * then writes the result to the output stream.
     * 
     * @param input The input stream.
     * @param output The output stream.
     */
    public void transform(InputStream input, OutputStream output) throws IOException, GeneralSecurityException {
        byte[] inputBytes = FSUtil.readStreamToBytes(input);
        byte[] outputBytes = transform(inputBytes);
        output.write(outputBytes);
    }
    
    /** 
     * Reads the specified number of bytes from the input stream and transforms them,
     * then writes the result to the output stream.
     * 
     * @param input The input stream.
     * @param output The output stream.
     * @param length The number of bytes to process. If -1, transform all bytes.
     * @throws IllegalArgumentException If the length argument is neither positive nor -1.
     */
    public void transform(InputStream input, OutputStream output, int length) throws IOException, GeneralSecurityException {
        if (length == -1) {
            transform(input, output);
        } else if (length > 0) {
            byte[] inputBytes = new byte[length];
            input.read(inputBytes, 0, length);
            byte[] outputBytes = transform(inputBytes);
            output.write(outputBytes);
        } else {
            throw new IllegalArgumentException("The length argument must be positive or -1.");
        }
    }
    
    private static byte[] toFixedLengthArray(BigInteger i, int length) {
        byte[] array = i.toByteArray();
        if (array.length > length) {
            byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        } else if (array.length < length) {
            byte[] tmp = new byte[length];
            Arrays.fill(tmp, (byte)0);
            System.arraycopy(array, 0, tmp, length - array.length, array.length);
            array = tmp;
        }
        return array;
    }
    
    private static byte[] reverse(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
        return array;
    }
}
