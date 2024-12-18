package com.example.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
 

@RestController
@RequestMapping("/api")
public class ImageOcrController { 
    private static final String REGION = "cn-north-1";  // 根据实际情况选择地区
    private static final String SecretAccessKey = "**==";
    private static final String AccessKeyID = "**";

    //生成一个能够接收上传图片的接口函数
    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        // todo 将file转换为base64编码的字符串 
        try {
            String base64String = Base64.getEncoder().encodeToString(file.getBytes()); 
            return callApi(base64String);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public String callApi(String base64Image) throws IOException {
        String endpoint = "visual.volcengineapi.com";
        String path = "/"; // 路径，不包含 Query// 请求接口信息
        String service = "cv"; 
        String schema = "https";
        Sign sign = new Sign(REGION, service, schema, endpoint, path, AccessKeyID, SecretAccessKey);

        String action = "OCRNormal";
        String version = "2020-08-26"; 
        Date date = new Date();
        HashMap<String, String> queryMap = new HashMap<>() {{
            put("Limit", "1");
        }};

        //todo 
        JsonObject bodyJson = new JsonObject();
        bodyJson.addProperty("image_base64", base64Image);
        byte[] body = bodyJson.toString().getBytes(StandardCharsets.UTF_8);

        try {
           String responseJson =  sign.doRequest("POST", queryMap, body, date, action, version);
            Gson gson = new Gson();
            OCRApiResponse apiResponse = gson.fromJson(responseJson, OCRApiResponse.class); 
            return apiResponse.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
 
    } 

    // API 响应模型
    static class OCRApiResponse {
        private String status;
        private String message;
        private Data data;

        // getter 和 setter 方法

        @Override
        public String toString() {
            return "Status: " + status + ", Message: " + message + ", Data: " + data;
        }
    }

    // 业务输出数据字段模型
    static class Data {
        private String[] line_texts;
        private RectInfo[] line_rects;
        private Float[] line_probs;
        private CharInfo[][] chars;
        private int[][][] polygons;

        // getter 和 setter 方法

        @Override
        public String toString() {
            return "Line texts: " + String.join(", ", line_texts);
        }
    }

    // RectInfo 字段模型
    static class RectInfo {
        private float x;
        private float y;
        private float width;
        private float height;

        // getter 和 setter 方法
    }

    // CharInfo 字段模型
    static class CharInfo {
        private float x;
        private float y;
        private float width;
        private float height;
        private float score;
        private String charStr;

        // getter 和 setter 方法
        
    }
}
