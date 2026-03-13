package CRUD.util;

public class Crypto {

    private static final String KEY = "AEDS3";

    public static String xor(String texto) {

        char[] key = KEY.toCharArray();
        char[] input = texto.toCharArray();
        char[] output = new char[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = (char) (input[i] ^ key[i % key.length]);
        }

        return new String(output);
    }
}