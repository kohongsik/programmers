package kakao2020;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.tools.javac.util.StringUtils;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class apiTest {
    static final String xAuthToken = "7bb231b340a24370113de9113fca8ba1";
    static final String baseUrl = " https://kox947ka1a.execute-api.ap-northeast-2.amazonaws.com/prod/users";
    static int[][] move = new int [][] {
            {0,0} // 0 : 아무것도 안함
            ,{1,0} // 1 : 위쪽으로 -> col++
            ,{0,1} // 2 : 오른쪽으로 -> row++
            ,{-1,0} // 3 : 아래로 -> col--
            ,{0,-1} // 4 : 왼쪽으로 -> row--
    };
    static String score1;
    static String score2;
    public static void main(String[] args) throws Exception {
        // start
        JSONObject startParam = new JSONObject();
        startParam.put("problem", "1");
        JSONObject startResponse = httpPostBodyConnection("/start", startParam, xAuthToken, null, "POST");
        final String authKey1 = String.valueOf(startResponse.get("auth_key"));
        goScenario1(authKey1);
        startParam.put("problem", "2");
        JSONObject startResponse2 = httpPostBodyConnection("/start", startParam, xAuthToken, null, "POST");
        final String authKey2 = String.valueOf(startResponse2.get("auth_key"));
        goScenario2(authKey2);
        System.out.println("score1 : " + score1 + ", score2 : " + score2);
    }
    static void goScenario1 (String authKey) {
        // 시나리오 1'
        int cnt = 0;
        while(cnt < 720) {
            int[][] arr = new int[5][5];
            JSONObject locations = getLocations(authKey);
            JSONArray locationList = (JSONArray) locations.get("locations");
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    int cur = j + (i * 5);
                    JSONObject curLocation = (JSONObject) locationList.get(cur);
                    arr[j][i] = Integer.parseInt(curLocation.get("located_bikes_count").toString());
                }
            }
            List<int[]> trucks = new ArrayList<>();
            // api 호출
            JSONObject truckInfo = getTrucks(authKey);
            JSONArray truckInfoList = (JSONArray) truckInfo.get("trucks");
            // 시나리오 1번에 트럭 5개
            for (int i = 0; i < 5; i++) {
                JSONObject cur = (JSONObject) truckInfoList.get(i);
                int id = Integer.parseInt(cur.get("id").toString());
                int locationId = Integer.parseInt(cur.get("location_id").toString());
                int loadedBikesCount = Integer.parseInt(cur.get("loaded_bikes_count").toString());
                int col = locationId % 5;
                int row = locationId / 5;
                trucks.add(new int[] {id, col, row, loadedBikesCount});
            }
            JSONObject param = new JSONObject();
            JSONArray array = new JSONArray();
            int dir = 0;
            for (int i = 0; i < 5; i++) {
                JSONObject subParam = new JSONObject();
                subParam.put("truck_id", i);
                ArrayList<Integer> command = new ArrayList<>();
                int[] cur = trucks.get(i);
                int y = cur[1];
                int x = cur[2];
                int loaded = cur[3];
                int commandCnt = 0;
                for (int k = 1; k <= 4; k++) {
                    if (commandCnt >= 10) {
                        break;
                    }
                    int ny = y + move[k][0];
                    int nx = x + move[k][1];
                    if (ny >= 0 && ny < 5 && nx >= 0 && nx < 5) {
                        command.add(k);
                        commandCnt++;
                        if (arr[ny][nx] > cur[3] && commandCnt < 10 && loaded < 20) {
                            command.add(5); // 자전거 승차
                            loaded++;
                            commandCnt++;
                        }
                        if (arr[ny][nx] == 0 && loaded > 0 && commandCnt < 10) {
                            command.add(6); // 자전거 하차.
                            loaded--;
                            commandCnt++;
                            if (loaded > 15) {
                                int tmp = 0;
                                while(tmp < 5 && commandCnt < 10) {
                                    command.add(6);
                                    tmp++;
                                    commandCnt++;
                                    loaded--;
                                }
                            }
                        }
                    }else {
                        command.add(0);
                        commandCnt++;
                    }
                    y = ny;
                    x = nx;
                }
                for (int k = 4; k >= 1; k--) {
                    if (commandCnt >= 10) {
                        break;
                    }
                    int ny = y + move[k][0];
                    int nx = x + move[k][1];
                    if (ny >= 0 && ny < 5 && nx >= 0 && nx < 5 && commandCnt < 10) {
                        command.add(k);
                        commandCnt++;
                        if (arr[ny][nx] > cur[3] && loaded < 20) {
                            command.add(5); // 자전거 승차
                            loaded++;
                            commandCnt++;
                        }
                        if (arr[ny][nx] == 0 && loaded > 0 && commandCnt < 10) {
                            command.add(6); // 자전거 하차.
                            loaded--;
                            commandCnt++;
                            if (loaded > 15) {
                                int tmp = 0;
                                while(tmp < 5 && commandCnt < 10) {
                                    command.add(6);
                                    tmp++;
                                    commandCnt++;
                                    loaded--;
                                }
                            }
                        }
                    }else {
                        command.add(0);
                        commandCnt++;
                    }
                    y = ny;
                    x = nx;
                }
                if (commandCnt < 10) {
                    while(commandCnt != 10) {
                        commandCnt++;
                        command.add(0);
                    }
                }
                subParam.put("command", command);
                array.add(subParam);
            }
            param.put("commands", array);
            System.out.println(param.toString());

            httpPostBodyConnection("/simulate", param, null, authKey, "PUT");
            cnt++;
        }
        JSONObject finish = httpPostBodyConnection("/score", null, null, authKey, "GET");
        // System.out.println(finish.toString());
        score1 = finish.toString();
    }
    static void goScenario2 (String authKey) {
        // 시나리오 1'
        int cnt = 0;
        while(cnt < 720) {
            int[][] arr = new int[60][60];
            JSONObject locations = getLocations(authKey);
            JSONArray locationList = (JSONArray) locations.get("locations");
            for (int i = 0; i < 60; i++) {
                for (int j = 0; j < 60; j++) {
                    int cur = j + (i * 60);
                    JSONObject curLocation = (JSONObject) locationList.get(cur);
                    arr[j][i] = Integer.parseInt(curLocation.get("located_bikes_count").toString());
                }
            }
            List<int[]> trucks = new ArrayList<>();
            // api 호출
            JSONObject truckInfo = getTrucks(authKey);
            JSONArray truckInfoList = (JSONArray) truckInfo.get("trucks");
            // 시나리오 2번에 트럭 10개
            for (int i = 0; i < 10; i++) {
                JSONObject cur = (JSONObject) truckInfoList.get(i);
                int id = Integer.parseInt(cur.get("id").toString());
                int locationId = Integer.parseInt(cur.get("location_id").toString());
                int loadedBikesCount = Integer.parseInt(cur.get("loaded_bikes_count").toString());
                int col = locationId % 60;
                int row = locationId / 60;
                trucks.add(new int[] {id, col, row, loadedBikesCount});
            }
            JSONObject param = new JSONObject();
            JSONArray array = new JSONArray();
            int dir = 0;
            for (int i = 0; i < 10; i++) {
                JSONObject subParam = new JSONObject();
                subParam.put("truck_id", i);
                ArrayList<Integer> command = new ArrayList<>();
                int[] cur = trucks.get(i);
                int y = cur[1];
                int x = cur[2];
                int loaded = cur[3];
                int commandCnt = 0;
                for (int k = 4; k >= 1; k--) {
                    if (commandCnt >= 10) {
                        break;
                    }
                    int ny = y + move[k][0];
                    int nx = x + move[k][1];
                    if (ny >= 0 && ny < 5 && nx >= 0 && nx < 5 && commandCnt < 10) {
                        command.add(k);
                        commandCnt++;
                        if (arr[ny][nx] > cur[3] && loaded < 20) {
                            command.add(5); // 자전거 승차
                            loaded++;
                            commandCnt++;
                        }
                        if (arr[ny][nx] == 0 && loaded > 0 && commandCnt < 10) {
                            command.add(6); // 자전거 하차.
                            loaded--;
                            commandCnt++;
                            if (loaded > 15) {
                                int tmp = 0;
                                while(tmp < 7 && commandCnt < 10) {
                                    command.add(6);
                                    tmp++;
                                    commandCnt++;
                                    loaded--;
                                }
                            }
                        }
                    }else {
                        command.add(0);
                        commandCnt++;
                    }
                    y = ny;
                    x = nx;
                }
                for (int k = 1; k <= 4; k++) {
                    if (commandCnt >= 10) {
                        break;
                    }
                    int ny = y + move[k][0];
                    int nx = x + move[k][1];
                    if (ny >= 0 && ny < 5 && nx >= 0 && nx < 5) {
                        command.add(k);
                        commandCnt++;
                        if (arr[ny][nx] > cur[3] && commandCnt < 10 && loaded < 20) {
                            command.add(5); // 자전거 승차
                            loaded++;
                            commandCnt++;
                        }
                        if (arr[ny][nx] == 0 && loaded > 0 && commandCnt < 10) {
                            command.add(6); // 자전거 하차.
                            loaded--;
                            commandCnt++;
                            if (loaded > 15) {
                                int tmp = 0;
                                while(tmp < 7 && commandCnt < 10) {
                                    command.add(6);
                                    tmp++;
                                    commandCnt++;
                                    loaded--;
                                }
                            }
                        }

                    } else {
                        command.add(0);
                        commandCnt++;
                    }
                    y = ny;
                    x = nx;
                }
                if (commandCnt < 10) {
                    while(commandCnt != 10) {
                        commandCnt++;
                        command.add(0);
                    }
                }
                subParam.put("command", command);
                array.add(subParam);
            }
            param.put("commands", array);
            System.out.println(param.toString());

            httpPostBodyConnection("/simulate", param, null, authKey, "PUT");
            cnt++;
        }
        JSONObject finish = httpPostBodyConnection("/score", null, null, authKey, "GET");
//        System.out.println(finish.toString());
        score2 = finish.toString();
    }
    static public JSONObject getLocations (String authKey) {
        return httpPostBodyConnection("/locations", null, null, authKey, "GET");
    }
    static public JSONObject getTrucks (String authKey) {
        return httpPostBodyConnection("/trucks", null, null, authKey, "GET");
    }
    public static JSONObject httpPostBodyConnection (String urlSub, JSONObject parameterData, String xAuthToken, String authKey, String method) {
        URL url = null;
        HttpURLConnection conn = null;

        String responseData = "";
        BufferedReader bf = null;
        StringBuilder sb = new StringBuilder();

        String returnData = "";
        try {
            url = new URL (baseUrl + urlSub);
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
            if (parameterData != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                     byte requestData[] = parameterData.toString().getBytes(StandardCharsets.UTF_8);
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
            System.out.println("========URL CALL : " + baseUrl + urlSub + " : " + responseCode + " =========");
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
