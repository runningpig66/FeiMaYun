package cn.aura.app.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

public class Md5 {
    private final static String token_key = "UH9IE73TXZK0H2A16A1M8N6B1AH2A1P2";

    private static String stringToMD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public String getDateToken() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH");//2018-10-12 16
        String date = sDateFormat.format(new java.util.Date());
        String dateMd5 = stringToMD5(date);
        String all = dateMd5 + token_key;
        return stringToMD5(all);
    }

    public String getUidToken(String Uid) {
        String UIDMd5 = stringToMD5(Uid);
        String all = UIDMd5 + token_key;
        return stringToMD5(all);

//        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
//        String date = sDateFormat.format(new java.util.Date());
//        String dateMd5 = stringToMD5(date);
//        String all = dateMd5 + token_key;
//        return stringToMD5(all);
    }
}
