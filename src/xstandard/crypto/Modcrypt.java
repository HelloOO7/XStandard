package xstandard.crypto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Modcrypt AES-CTR encryption/decryption.
 * Used by DSi-exclusive binaries.
 */

public class Modcrypt {
    
    private final SecretKey key;
    private BigInteger counter;

    private static final int BLOCK_SIZE = 16;
    private static final BigInteger ROLMASK = new BigInteger("3FFFFFFFFFF", 16);
    private static final BigInteger MODULUS = BigInteger.TWO.pow(128);
    private static final BigInteger SCRAMBLER = new BigInteger("FFFEFB4E295902582A680F5F1A4F3E79", 16);

    public Modcrypt(byte[] key, byte[] iv) {
        this.key = new SecretKeySpec(key, 0, iv.length, "AES");
        this.counter = new BigInteger(1, iv);
    }

    public Modcrypt(String gamecode, byte[] arm9iHmac, byte[] arm9HmacWithSecureArea) {
        byte[] keyBytes = deriveRetailKey(gamecode, arm9iHmac);
        key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
        
        byte[] counterBytes = new byte[16];
        System.arraycopy(arm9HmacWithSecureArea, 0, counterBytes, 0, 16);
        counter = new BigInteger(1, reverse(counterBytes));
    }

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

    public void transform(DataInputStream input, DataOutputStream output) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            while (true) { 
                byte[] block = input.readNBytes(BLOCK_SIZE);
                if(block.length == 0)
                    break;
                
                byte[] ctr = toFixedLengthArray(counter, 16);
                byte[] pad = cipher.update(ctr);
                counter = counter.add(BigInteger.ONE).mod(MODULUS);
                
                byte[] transformed = new byte[block.length];
                for (int i = 0; i < block.length; i++) {
                    transformed[i] = (byte) (block[i] ^ pad[BLOCK_SIZE - 1 - i]);
                }
                
                output.write(transformed);
            }
        } catch (java.security.NoSuchAlgorithmException | javax.crypto.NoSuchPaddingException | java.security.InvalidKeyException e) {
            throw new RuntimeException(e);
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
