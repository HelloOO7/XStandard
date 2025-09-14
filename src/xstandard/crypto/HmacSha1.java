package xstandard.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * SHA-1 HMAC implementation.
 */
public class HmacSha1 {
    private final SecretKeySpec key;
    private Mac mac;
    
    public HmacSha1(byte[] key) {
        try {
            this.key = new SecretKeySpec(key, 0, key.length, "HmacSHA1");
            mac = Mac.getInstance("HmacSHA1");
            mac.init(this.key);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void update(byte[] data) {
        mac.update(data);
    }
    
    public byte[] digest() {
        return mac.doFinal();
    }
    
    public void reset() {
        mac.reset();
    }
}
