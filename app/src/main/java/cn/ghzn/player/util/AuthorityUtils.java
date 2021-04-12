package cn.ghzn.player.util;

import android.util.Base64;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AuthorityUtils {
    private static final String AUTH_PASSWORD = "SYIFPSNYCGSPSNDI";
    private static final String IV = "0000000000000000";

    /**
     * MD5加密方法
     * @param password
     * @return
     */
    public static String digest(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                int c = b & 0xff; //负数转换成正数
                String result = Integer.toHexString(c); //把十进制的数转换成十六进制的书
                if(result.length()<2){
                    sb.append(0); //让十六进制全部都是两位数
                }
                sb.append(result);
            }
            return sb.toString(); //返回加密后的密文
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static Map<String, Object> getAuthInfo(String content){//从txt文本中读取加密信息，分别存储于map集合中，再进行解密
        try {
            Map<String, Object> resMap = new HashMap<String, Object>();
            String decContent = decrypt(content, AUTH_PASSWORD, IV);
            int flag = Integer.parseInt(decContent.substring(0,1));
            if(flag==1){
                long startTime = Long.parseLong(decContent.substring(1,11))*1000;
                long endTime = Long.parseLong(decContent.substring(11,21))*1000;
                String info = decContent.substring(21, decContent.length()-10);
                long sumTime = Long.parseLong(decContent.substring(decContent.length()-10));
                resMap.put("startTime", startTime);
                resMap.put("endTime", endTime);
                resMap.put("info", info);
                resMap.put("sumTime", sumTime);
            }else if(flag==2){
                long endTime = Long.parseLong(decContent.substring(1,11))*1000;
                long startTime = Long.parseLong(decContent.substring(11,21))*1000;
                String info = decContent.substring(21, decContent.length()-10);
                long sumTime = Long.parseLong(decContent.substring(decContent.length()-10));
                resMap.put("startTime", startTime);
                resMap.put("endTime", endTime);
                resMap.put("info", info);
                resMap.put("sumTime", sumTime);
            }
            return resMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * content: 解密内容(base64编码格式)
     * slatKey: 加密时使用的盐，16位字符串
     * vectorKey: 加密时使用的向量，16位字符串
     */
    public static String decrypt(String base64Content, String slatKey, String vectorKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey secretKey = new SecretKeySpec(slatKey.getBytes(), "AES");
        IvParameterSpec iv = new IvParameterSpec(vectorKey.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] content = Base64.decode(base64Content,1);
        byte[] encrypted = cipher.doFinal(content);
        return new String(encrypted);
    }

}