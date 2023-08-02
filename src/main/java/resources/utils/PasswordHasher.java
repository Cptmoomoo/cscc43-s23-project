package resources.utils;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Base64;

public class PasswordHasher
{
    private static MessageDigest md;

    public static void init()
    {
        try
        {
            md = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static String hashPassword(String password)
    {
        byte[] hash;

        hash = md.digest(password.getBytes(StandardCharsets.UTF_8));

        md.reset();

        return Base64.getEncoder().encodeToString(hash);
    }

    public static Boolean checkPassword(String password, String hashed)
    {
        return hashPassword(password).equals(hashed);
    }
}
