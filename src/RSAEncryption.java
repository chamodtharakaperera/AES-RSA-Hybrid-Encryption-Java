import javax.crypto.Cipher;
import java.io.InputStream;
import java.security.*;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RSAEncryption {

    //Generate Public and Private Key pair
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        return pair;
    }

    public static KeyPair getKeyPairFromKeyStore() throws Exception {
        // Generated with:
        // keytool -genkeypair -alias mykey -storepass s3cr3t -keypass s3cr3t -keyalg
        // RSA -keystore keystore.jks

        InputStream ins = RSAEncryption.class.getResourceAsStream("/keystore.jks");

        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(ins, "s3cr3t".toCharArray()); // Keystore password
        KeyStore.PasswordProtection keyPassword = // Key password
                new KeyStore.PasswordProtection("s3cr3t".toCharArray());

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry("mykey", keyPassword);

        java.security.cert.Certificate cert = keyStore.getCertificate("mykey");
        PublicKey publicKey = cert.getPublicKey();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        return new KeyPair(publicKey, privateKey);
    }

    //Encrypting Using Public Key
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        return Base64.getEncoder().encodeToString(cipherText);
    }

    //Decrypting Using Private Key
    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }

    //Signing Using Private Key
    public static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }


    public static String main(String... argv) throws Exception {
        // First generate a public/private key pair
        KeyPair pair = generateKeyPair();
        // KeyPair pair = getKeyPairFromKeyStore();

        // Our secret message
        String message = "nibmnibmnibmnibm";
        System.out.println("The AES Large File Decrypting Secret Key Message: " + message);

        // Encrypt the message
        String cipherText = encrypt(message, pair.getPublic());
        System.out.println("After encrypting the message using RSA Algorithm: " + cipherText);

        // Now decrypt it
        String decipheredMessage = decrypt(cipherText, pair.getPrivate());

        //System.out.println(decipheredMessage);

        // Let's sign our message
        String signature = sign("foobar", pair.getPrivate());

        // Let's check the signature
        boolean isCorrect = verify("foobar", signature, pair.getPublic());
        System.out.println("Signature correct: " + isCorrect);

        System.out.println("\n\nWow!!! RSA returned deciphered Message Successfully for AES Encryption...\n\n");
        System.out.println("Now the AES encryption starts... Please wait...");
        return decipheredMessage;
    }

}
