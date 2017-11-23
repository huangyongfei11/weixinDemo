package com.weixinDemo.utils;
/***
 *  获取签名的工具类
 *
 */

import org.json.JSONObject;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Formatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sign {

    public static String RootPath = "";

    public static Map<String, String> sign(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
        System.out.println(string1);

        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    public static void main(String[] args) {
        String jsapi_ticket = JsapiTicketUtil.getJSApiTicket();
        // 注意 URL 一定要动态获取，不能 hardcode
        String url = "http://sign.gongyoumeng.com/sign";//url是你请求的一个action或者controller地址，并且方法直接跳转到使用jsapi的jsp界面
        Map<String, String> ret = sign(jsapi_ticket, url);
        for (Map.Entry entry : ret.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
    }

    //zc add;2017-10-18 15:21:52;定时更新微信的jsapiTicket
        public synchronized static String jsapiTicket() {
        String weixinCon = readWeixinCookieFile();
        JSONObject con = null;
        if (StringUtils.isEmpty(weixinCon)) {
            String jsapi_ticket = JsapiTicketUtil.getJSApiTicket();
            con = new JSONObject();
            con.put("jsapi_ticket", jsapi_ticket);
            con.put("update_at", DateUtil.getTime());
            writeCookieFile(weixinCookieFile(), con.toString());
        } else {
            con = new JSONObject(weixinCon);
        }
        String update_at = con.getString("update_at");
        String currentTime = DateUtil.getTime();
        long delta = DateUtil.deltaSecond(update_at, currentTime);
        //正常情况下，jsapi_ticket的有效期为7200秒
        if (delta > 7000) {
            String jsapi_ticket = JsapiTicketUtil.getJSApiTicket();
            con.put("jsapi_ticket", jsapi_ticket);
            writeCookieFile(weixinCookieFile(), con.toString());
        }
        return con.getString("jsapi_ticket");
    }

    private static void writeCookieFile(File file, String con) {
        PrintWriter writer = null;
        try {
            FileWriter fileWrite = new FileWriter(file, true);
            writer = new PrintWriter(fileWrite);
            writer.append(con);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File weixinCookieFile() {
        //String projectPath = System.getProperty("user.dir");
        String projectPath = Sign.RootPath;
        System.out.println("weixinCookieFile:" + projectPath);
        File fileName = new File(projectPath + File.separator + "weixinCookie.txt");
        if (!fileName.exists()) {
            try {
                fileName.createNewFile();// 不存在则创建
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    private static String readWeixinCookieFile() {
        File fileName = weixinCookieFile();
        String result = "";
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader=new FileReader(fileName);
            bufferedReader=new BufferedReader(fileReader);
            try {
                String read = "";
                while ((read=bufferedReader.readLine()) != null) {
                    result += read;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                if(fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
