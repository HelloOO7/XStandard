package xstandard.crypto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
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
    private static final BigInteger MODULUS = new BigInteger("2").pow(128);
    private static final BigInteger SCRAMBLER = new BigInteger("FFFEFB4E295902582A680F5F1A4F3E79", 16);

    public Modcrypt(byte[] key, byte[] iv) {
        this.key = new SecretKeySpec(key, 0, iv.length, "AES");
        this.counter = new BigInteger(iv);
    }

    public Modcrypt(String gamecode, byte[] arm9iHmac, byte[] arm9HmacWithSecureArea) {
        byte[] keyBytes = deriveRetailKey(gamecode, arm9iHmac);
        this.key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
        this.counter = new BigInteger(arm9HmacWithSecureArea);
    }

    public static byte[] deriveRetailKey(String gamecode, byte[] arm9iHmac) {
        String emagcode = new StringBuilder(gamecode).reverse().toString();
        
        byte[] retailKeyX = ("Nintendo" + gamecode + emagcode).getBytes();
        byte[] retailKeyY = new byte[16];
        System.arraycopy(arm9iHmac, 0, retailKeyY, 0, 16);

        // KeyDSi = (((KeyX) XOR KeyY) + FFFEFB4E295902582A680F5F1A4F3E79h) ROL 42
        BigInteger nX = new BigInteger(retailKeyX);
        BigInteger nY = new BigInteger(retailKeyY);
        BigInteger keyN = (nX.xor(nY)).add(SCRAMBLER);
        BigInteger shiftedKeyN = keyN.shiftLeft(42);
        BigInteger rotatedKeyN = shiftedKeyN.or(shiftedKeyN.shiftRight(128).and(ROLMASK)).and(MODULUS.subtract(new BigInteger("1")));

        return rotatedKeyN.toByteArray();
    }

    public void transform(DataInputStream input, DataOutputStream output) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, this.key);

            while (true) { 
                byte[] block = input.readNBytes(BLOCK_SIZE);
                if(block.length == 0)
                    break;
                
                BigInteger pad = new BigInteger(cipher.update(this.counter.toByteArray()));
                this.counter = this.counter.add(new BigInteger("1")).mod(MODULUS);
                BigInteger transformed = new BigInteger(block).xor(pad);
                output.write(transformed.toByteArray());
            }
        } catch (java.security.NoSuchAlgorithmException | javax.crypto.NoSuchPaddingException | java.security.InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
