package com.example.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.util.Date;
import java.util.HashMap;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.EnvReader;
import com.google.gson.Gson;

@RestController
@RequestMapping("/api")
public class ImageOcrController {
    private static final String REGION = "cn-north-1"; // 根据实际情况选择地区
    private static final String SecretAccessKey = EnvReader.getEnvVariable("SECRET_ACCESS_KEY");
    private static final String AccessKeyID = EnvReader.getEnvVariable("ACCESS_KEY_ID");

    // 生成一个能够接收上传图片的接口函数
    @PostMapping("/uploadImage")
    public OCRApiResponse uploadImage(@RequestParam("file") MultipartFile file) {
        // todo 将file转换为base64编码的字符串
        try {
            long maxSize = 10 * 1024 * 1024; // 10 MB
            if (file.getSize() > maxSize) {
                throw new IOException("File size exceeds the limit of 10 MB");
            }

            String base64Image = null;
            // 获取输入流
            try (InputStream inputStream = file.getInputStream()) {
                byte[] bytes = IOUtils.toByteArray(inputStream);
                base64Image = Base64.encodeBase64String(bytes);
            }
            return callApi(base64Image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public OCRApiResponse callApi(String base64Image) throws IOException {
        String endpoint = "visual.volcengineapi.com";
        String path = "/"; // 路径，不包含 Query// 请求接口信息
        String service = "cv";
        String schema = "https";
        Sign sign = new Sign(REGION, service, schema, endpoint, path, AccessKeyID, SecretAccessKey);

        String action = "OCRNormal";
        String version = "2020-08-26";
        Date date = new Date();
        HashMap<String, String> queryMap = new HashMap<>() {
            {
                put("Limit", "1");
            }
        };

        // todo 使用contentType application/x-www-form-urlencoded 修改body的构建
        // JsonObject bodyJson = new JsonObject();
        // bodyJson.addProperty("image_base64", base64Image);
        // // byte[] body = bodyJson.toString().getBytes(StandardCharsets.UTF_8);
        // byte[] body = bodyJson.toString().getBytes();

        String imageBase64Param = "image_base64=" + URLEncoder.encode(base64Image, StandardCharsets.UTF_8.name());
        byte[] body = imageBase64Param.getBytes(StandardCharsets.UTF_8);

        try {
            String responseJson = sign.doRequest("POST", queryMap, body, date, action, version);
            Gson gson = new Gson();
            OCRApiResponse apiResponse = gson.fromJson(responseJson, OCRApiResponse.class);
            return apiResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    // API 响应模型
    class OCRApiResponse implements Serializable {
        private String status;
        private String message;
        private Data data;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "Status: " + status + ", Message: " + message + ", Data: " + data;
        }
    }

    // 业务输出数据字段模型
    class Data implements Serializable  {
        private String text;
        private String[] line_texts;
        // private RectInfo[] line_rects;
        // private Float[] line_probs;
        // private CharInfo[][] chars;
        // private int[][][] polygons;

        public String getText(){
            text = String.join("", line_texts);
            return text;
        }

        // getter 和 setter 方法
        public String[] getLine_texts() {
            return line_texts;
        }
        public void setLine_texts(String[] line_texts) {
            this.line_texts = line_texts;
        }
        // public RectInfo[] getLine_rects() {
        //     return line_rects;
        // }
        // public void setLine_rects(RectInfo[] line_rects) {
        //     this.line_rects = line_rects;
        // }
        // public Float[] getLine_probs() {
        //     return line_probs;
        // }
        // public void setLine_probs(Float[] line_probs) {
        //     this.line_probs = line_probs;
        // }
        // public CharInfo[][] getChars() {
        //     return chars;
        // }
        // public void setChars(CharInfo[][] chars) {
        //     this.chars = chars;
        // }
        // public int[][][] getPolygons() {
        //     return polygons;
        // }
        // public void setPolygons(int[][][] polygons) {
        //     this.polygons = polygons;
        // }
 
        @Override
        public String toString() {
            return "Line texts: " + String.join(", ", line_texts);
        }
    }

    // RectInfo 字段模型
    class RectInfo  implements Serializable {
        private float x;
        private float y;
        private float width;
        private float height;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }
         
    }

    // CharInfo 字段模型
    class CharInfo  implements Serializable {
        private float x;
        private float y;
        private float width;
        private float height;
        private float score;
        private String charStr;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public String getCharStr() {
            return charStr;
        }

        public void setCharStr(String charStr) {
            this.charStr = charStr;
        }
    }
}
