package com.example.neighborfriend.Class;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpUrlConnetionClass {
    // POST
    public static String postRequest(String targetUrl, Map<String, String> requestMap) {

        String response = "";

        try {

            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST"); // 전송 방식
            conn.setRequestProperty("Content-Type", "charset=utf-8");
            conn.setConnectTimeout(5000); // 연결 타임아웃 설정(5초)
            conn.setReadTimeout(5000); // 읽기 타임아웃 설정(5초)
            conn.setDoOutput(true);	// URL 연결을 출력용으로 사용(true)

            String requestBody = getJsonStringFromMap(requestMap);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            bw.write(requestBody);
            bw.flush();
            bw.close();

            Charset charset = Charset.forName("UTF-8");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));

            String inputLine;
            StringBuffer sb = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            br.close();

            response = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }


    /** Map을 jsonString으로 변환 */
    @SuppressWarnings("unchecked")
    public static String getJsonStringFromMap(Map<String, String> map) throws JSONException {

        JSONObject json = new JSONObject();

        for(Map.Entry<String, String> entry : map.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();

            json.put(key, value);
        }

        return json.toString();
    }
}
