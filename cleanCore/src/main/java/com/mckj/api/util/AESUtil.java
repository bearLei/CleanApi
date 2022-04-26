package com.mckj.api.util;

import android.util.Base64;


import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    public static String KEY = "dwnxthekey902902";


    public static byte[] decrypt(byte[] content, byte[] keyByte, byte[] ivByte) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        Key sKeySpec = new SecretKeySpec(keyByte, "AES");
        //生成iv
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(ivByte));
        cipher.init(Cipher.DECRYPT_MODE, sKeySpec, params);
        // 初始化
        return cipher.doFinal(content);
    }

    public static String d(String input) throws Exception {
        byte[] b64 = Base64.decode(input, Base64.DEFAULT);
        byte[] k = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] dBytes = decrypt(b64, k, k);
        return new String(dBytes, StandardCharsets.UTF_8);
    }
}