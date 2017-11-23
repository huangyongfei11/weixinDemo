package com.weixinDemo.utils;


import org.json.JSONObject;
import sun.applet.Main;

import java.util.HashMap;
import java.util.Map;

/**
 * 上下班打卡的工具类
 */
public class BaiduAddress {

    public static final String SIGN_IN = "http://www.gongyoumeng.com/TerminalsApi/Worker_AddWorkerPosition";
    //32.047310//118.801281
    public static String addressFormLocation(String latitude, String longitude) {
        String address = "";
        try {
            String urlFormat = "http://api.map.baidu.com/geocoder/v2/?coordtype=gcj02ll&callback=&location=%s,%s&output=json&pois=0&ak=XxpLhKlQZORdAXAMDntbRPolvtiX40Ge";
            String url = String.format(urlFormat, latitude, longitude);
            String response = HttpUtils_Two.doGet(url);
            JSONObject resp = new JSONObject(response);
            address = resp.getJSONObject("result").getString("formatted_address");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }
    public static void  main(String[] args){

        System.out.println(addressFormLocation("32.047310","118.801281"));

        String jsonStr = "{\"status\":\"0\",\"data\":{\"name\":\"tom\",\"age\":18}}";
        JSONObject json = new JSONObject(jsonStr);

        System.out.println(json+"  111111111111");
        Map<String ,Object> map= json.toMap();
        for(Map.Entry<String ,Object> entry :map.entrySet()){
            System.out.println(entry.getKey()+"  "+entry.getValue());
        }
        System.out.println("1111111111111111111111");
        JSONObject json2 =json.getJSONObject("data");
        Map<String ,Object> map2= json2.toMap();
        for(Map.Entry<String ,Object> entry :map.entrySet()){
            System.out.println(entry.getKey()+"  "+entry.getValue());
        }
    }


}
