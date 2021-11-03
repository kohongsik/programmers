package kakao2020;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class ApiCall {
    // 바로 api 호출 할 수 있게 만듬..
    String xAuthToken = "";
    String baseUrl = "";
    public ApiCall () {
        this.xAuthToken = "";
        this.baseUrl = "";
    }
    public String getxAuthToken() {
        return this.xAuthToken;
    }
    public void setxAuthToken(String xAuthToken) {
        this.xAuthToken = xAuthToken;
    }
    public String getBaseUrl() {
        return this.baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public JSONObject httpConnection (String additionalUrl, JSONObject requestBodyData, String xAuthToken, String authKey, String method) {
        URL url = null;
        HttpURLConnection conn = null;

        String responseData = "";
        BufferedReader bf = null;
        StringBuilder sb = new StringBuilder();

        String returnData = "";
        try {
            url = new URL (baseUrl + additionalUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            if (xAuthToken != null) {
                conn.setRequestProperty("X-Auth-Token", xAuthToken);
            }
            if (authKey != null) {
                conn.setRequestProperty("Authorization", authKey);
            }
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            if (requestBodyData != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte requestData[] = requestBodyData.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(requestData);
                    os.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            conn.connect();
            bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while((responseData = bf.readLine()) != null) {
                sb.append(responseData);
            }
            returnData = sb.toString();
            String responseCode = String.valueOf(conn.getResponseCode());
            System.out.println("========URL CALL : " + baseUrl + additionalUrl + " : " + responseCode + " =========");
            System.out.println("========RESPONSE=========");
            System.out.println(returnData);
            System.out.println("========//RESPONSE=========");
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(returnData);

            return jsonObject;
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if (bf != null) {
                    bf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
