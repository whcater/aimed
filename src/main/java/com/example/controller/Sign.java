package com.example.controller;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copyright (year) Beijing Volcano Engine Technology Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Sign {

    private static final BitSet URLENCODER = new BitSet(256);

    private static final String CONST_ENCODE = "0123456789ABCDEF";
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    private final String region;
    private final String service;
    private final String schema;
    private final String host;
    private final String path;
    private final String ak;
    private final String sk;

    static {
        int i;
        for (i = 97; i <= 122; ++i) {
            URLENCODER.set(i);
        }

        for (i = 65; i <= 90; ++i) {
            URLENCODER.set(i);
        }

        for (i = 48; i <= 57; ++i) {
            URLENCODER.set(i);
        }
        URLENCODER.set('-');
        URLENCODER.set('_');
        URLENCODER.set('.');
        URLENCODER.set('~');
    }

    public Sign(String region, String service, String schema, String host, String path, String ak, String sk) {
        this.region = region;
        this.service = service;
        this.host = host;
        this.schema = schema;
        this.path = path;
        this.ak = ak;
        this.sk = sk;
    }

    public static void demoCall() throws Exception {
        String SecretAccessKey = "****";
        String AccessKeyID = "AK****";
        // 请求地址
        String endpoint = "iam.volcengineapi.com";
        String path = "/"; // 路径，不包含 Query// 请求接口信息
        String service = "iam";
        String region = "cn-beijing";
        String schema = "https";
        Sign sign = new Sign(region, service, schema, endpoint, path, AccessKeyID, SecretAccessKey);

        String action = "ListPolicies";
        String version = "2018-01-01";
        Date date = new Date();
        HashMap<String, String> queryMap = new HashMap<>() {
            {
                // put("Limit", "1");
            }
        };

        sign.doRequest("POST", queryMap, null, date, action, version);
    }

    public String doRequest(String method, Map<String, String> queryList, byte[] body,
            Date date, String action, String version) throws Exception {
        if (body == null) {
            body = new byte[0];
        }
        String xContentSha256 = hashSHA256(body);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String xDate = sdf.format(date);
        String shortXDate = xDate.substring(0, 8);
        String contentType = "application/x-www-form-urlencoded";

        String signHeader = "host;x-date;x-content-sha256;content-type";

        SortedMap<String, String> realQueryList = new TreeMap<>(queryList);
        realQueryList.put("Action", action);
        realQueryList.put("Version", version);
        StringBuilder querySB = new StringBuilder();
        for (String key : realQueryList.keySet()) {
            querySB.append(signStringEncoder(key)).append("=").append(signStringEncoder(realQueryList.get(key)))
                    .append("&");
        }
        querySB.deleteCharAt(querySB.length() - 1);

        String canonicalStringBuilder = method + "\n" + path + "\n" + querySB + "\n" +
                "host:" + host + "\n" +
                "x-date:" + xDate + "\n" +
                "x-content-sha256:" + xContentSha256 + "\n" +
                "content-type:" + contentType + "\n" +
                "\n" +
                signHeader + "\n" +
                xContentSha256;

        System.out.println(canonicalStringBuilder);

        String hashcanonicalString = hashSHA256(canonicalStringBuilder.getBytes());
        String credentialScope = shortXDate + "/" + region + "/" + service + "/request";
        String signString = "HMAC-SHA256" + "\n" + xDate + "\n" + credentialScope + "\n" + hashcanonicalString;

        byte[] signKey = genSigningSecretKeyV4(sk, shortXDate, region, service);
        String signature = HexFormat.of().formatHex(hmacSHA256(signKey, signString));

        URI uri = new URI(schema + "://" + host + path + "?" + querySB);

        // Create HttpClient and HttpRequestBase (GET or POST)
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpUriRequest request;
        if (method.equals("GET")) {
            request = new HttpGet(uri);
        } else if (method.equals("POST")) {
            HttpPost postRequest = new HttpPost(uri);
            postRequest.setEntity(new ByteArrayEntity(body)); // Set the body for POST requests
            request = postRequest;
        } else {
            throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        }

        // Set headers
        request.setHeader("Host", host);
        request.setHeader("X-Date", xDate);
        request.setHeader("X-Content-Sha256", xContentSha256);
        request.setHeader("Content-Type", contentType);
        request.setHeader("Authorization", "HMAC-SHA256" +
                " Credential=" + ak + "/" + credentialScope +
                ", SignedHeaders=" + signHeader +
                ", Signature=" + signature);

        // Execute the request
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int responseCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            System.out.println(responseCode);
            System.out.println(responseBody);
            return responseBody;
        } finally {
            httpClient.close(); // Ensure to close the client
        }
    }

    public String doRequest2(String method, Map<String, String> queryList, byte[] body,
            Date date, String action, String version) throws Exception {
        if (body == null) {
            body = new byte[0];
        }
        String xContentSha256 = hashSHA256(body);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String xDate = sdf.format(date);
        String shortXDate = xDate.substring(0, 8);
        String contentType = "application/json";

        String signHeader = "host;x-date;x-content-sha256;content-type";

        SortedMap<String, String> realQueryList = new TreeMap<>(queryList);
        realQueryList.put("Action", action);
        realQueryList.put("Version", version);
        StringBuilder querySB = new StringBuilder();
        for (String key : realQueryList.keySet()) {
            querySB.append(signStringEncoder(key)).append("=").append(signStringEncoder(realQueryList.get(key)))
                    .append("&");
        }
        querySB.deleteCharAt(querySB.length() - 1);

        String canonicalStringBuilder = method + "\n" + path + "\n" + querySB + "\n" +
                "host:" + host + "\n" +
                "x-date:" + xDate + "\n" +
                "x-content-sha256:" + xContentSha256 + "\n" +
                "content-type:" + contentType + "\n" +
                "\n" +
                signHeader + "\n" +
                xContentSha256;

        System.out.println(canonicalStringBuilder);

        String hashcanonicalString = hashSHA256(canonicalStringBuilder.getBytes());
        String credentialScope = shortXDate + "/" + region + "/" + service + "/request";
        String signString = "HMAC-SHA256" + "\n" + xDate + "\n" + credentialScope + "\n" + hashcanonicalString;

        byte[] signKey = genSigningSecretKeyV4(sk, shortXDate, region, service);
        String signature = HexFormat.of().formatHex(hmacSHA256(signKey, signString));

        URL url = new URL(schema + "://" + host + path + "?" + querySB);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Host", host);
        conn.setRequestProperty("X-Date", xDate);
        conn.setRequestProperty("X-Content-Sha256", xContentSha256);
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Authorization", "HMAC-SHA256" +
                " Credential=" + ak + "/" + credentialScope +
                ", SignedHeaders=" + signHeader +
                ", Signature=" + signature);

        if (!Objects.equals(conn.getRequestMethod(), "GET")) {
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(body);
            os.flush();
            os.close();
        }
        conn.connect();

        int responseCode = conn.getResponseCode();

        InputStream is;
        if (responseCode == 200) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }
        String responseBody = new String(is.readAllBytes());
        is.close();

        System.out.println(responseCode);
        System.out.println(responseBody);
        return responseBody;
    }

    private String signStringEncoder(String source) {
        if (source == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(source.length());
        ByteBuffer bb = UTF_8.encode(source);
        while (bb.hasRemaining()) {
            int b = bb.get() & 255;
            if (URLENCODER.get(b)) {
                buf.append((char) b);
            } else if (b == 32) {
                buf.append("%20");
            } else {
                buf.append("%");
                char hex1 = CONST_ENCODE.charAt(b >> 4);
                char hex2 = CONST_ENCODE.charAt(b & 15);
                buf.append(hex1);
                buf.append(hex2);
            }
        }

        return buf.toString();
    }

    public static String hashSHA256(byte[] content) throws Exception {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(md.digest(content));
        } catch (Exception e) {
            throw new Exception(
                    "Unable to compute hash while signing request: "
                            + e.getMessage(),
                    e);
        }
    }

    public static byte[] hmacSHA256(byte[] key, String content) throws Exception {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(content.getBytes());
        } catch (Exception e) {
            throw new Exception(
                    "Unable to calculate a request signature: "
                            + e.getMessage(),
                    e);
        }
    }

    private byte[] genSigningSecretKeyV4(String secretKey, String date, String region, String service)
            throws Exception {
        byte[] kDate = hmacSHA256((secretKey).getBytes(), date);
        byte[] kRegion = hmacSHA256(kDate, region);
        byte[] kService = hmacSHA256(kRegion, service);
        return hmacSHA256(kService, "request");
    }
}
