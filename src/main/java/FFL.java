import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFL {
    private static String value = "";
    static OkHttpClient client=new OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .build();
    static ObjectMapper mapper = new ObjectMapper();

    public static Map<String, String> userinst (Map<String, Object> UserInfo){
        Map<String, String> Info = new HashMap<>();
        String uname = (String) UserInfo.get("uname");
        String uid = String.valueOf(UserInfo.get("uid"));
        String billCoin = String.valueOf(UserInfo.get("billCoin"));
        int Silver = (int) UserInfo.get("silver");
        int gold = (int) UserInfo.get("gold")/100;
        Info.put("name",uname);
        Info.put("uid",uid);
        Info.put("Coin",billCoin);
        Info.put("Silver", String.valueOf(Silver));
        Info.put("gold", String.valueOf(gold));
        System.out.println("---------用户信息--------");
        System.out.println("uid:" + uid + "\n昵称:" + uname + "\n银瓜子:" + Silver + "\n电池余额:" + gold + "\n硬币:" + billCoin);
        return Info;
    }
    public static Map<String,String> cookieToMap(String value) {
        Map<String, String> map = new HashMap<>();
        value = value.replace(" ", "");
        if (value.contains(";")) {
            String values[] = value.split(";");
            for (String val : values) {
                String vals[] = val.split("=");
                map.put(vals[0], vals[1]);
            }
        } else {
            String values[] = value.split("=");
            map.put(values[0], values[1]);
        }
        return map;
    }
    public static String MapTocookie(Map<String,String> map) {
        map.forEach((k, v) -> {
            value = value + k + "=" + v + ";";
            System.out.println("Key: " + k + ", Value: " + v);
        });
        return value;
    }
    public static Map<String, Object> readJsonFile(String fileName) {
        Gson gson = new Gson();
        String json;
        try {
            File file = new File(fileName);
            Reader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
            int ch = 0;
            StringBuffer buffer = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                buffer.append((char) ch);
            }
            reader.close();
            json = buffer.toString();
            return gson.fromJson(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void writeFile(String file,String newfile,boolean append){
        try {
            FileWriter fw = new FileWriter(file,append); //创,覆盖写入
            fw.write(newfile); //写
            fw.close();  //关
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void timeing(int debug,int hours,int Minutes,int Seconds) throws InterruptedException {
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss");
        System.out.println("["+dateFormat1.format(new Date())+"] 脚本将在" + hours + ":" + Minutes + ":" + Seconds + "时开始执行...");
        while(debug==1) {
            Date curTime1 = new Date();
            if (curTime1.getHours() == hours) {
                while(debug==1) {
                    Date curTime3 = new Date();
                    if (curTime3.getMinutes() == Minutes) {
                        while(debug==1) {
                            Date curTime4 = new Date();
                            if (curTime4.getSeconds() == Seconds) {
                                debug=0;
                            } else {
                                System.out.println(dateFormat1.format(new Date()) + "秒不满足");
                                Thread.sleep(500);
                            }
                        }
                    } else {
                        System.out.println(dateFormat1.format(new Date()) + "分钟不满足");
                        TimeUnit.SECONDS.sleep(30);
                    }
                }
            } else{
                System.out.println(dateFormat1.format(new Date()) + "小时不满足或超过时间");
                TimeUnit.SECONDS.sleep(180);
            }
        }
    }
    public static List<String> getFileContext(String path) throws Exception {
        FileReader fileReader =new FileReader(path);
        BufferedReader bufferedReader =new BufferedReader(fileReader);
        List<String> list =new ArrayList<String>();
        String str=null;
        while((str=bufferedReader.readLine())!=null) {
            if(str.trim().length()>2) {
                list.add(str);
            }
        }
        return list;
    }
    public static boolean refrefrsh(String COOKIE) throws IOException {
        String refreshUrl=String.format("https://passport.bilibili.com/x/passport-login/web/cookie/info");
        Request getrefresh =new Request.Builder()
                .url(refreshUrl)
                .get()
                .addHeader("Cookie", COOKIE)
                .build();
        Response refreshRes = client.newCall(getrefresh).execute();
        Map<String, Object> refMap = mapper.readValue(refreshRes.body().string(), new TypeReference<>(){});
        boolean refresh = (boolean) ((Map<String, Object>) refMap.get("data")).get("refresh");
        return refresh;
    }
    public static String getCorrespondPath() throws IOException {
        String CPathapi = String.format("https://api.ikkun.cf/CorrespondPath?lx=json");
        Request getCorrespondPath = new Request.Builder()
                .url(CPathapi)
                .get()
                .build();
        Response CPResponse = client.newCall(getCorrespondPath).execute();
        Map<String, Object> CPMap = mapper.readValue(CPResponse.body().string(), new TypeReference<>() {
        });
        String CorrespondPath = (String) CPMap.get("CorrespondPath");
        return CorrespondPath;
    }
    public static String getrefresh_csrf(String COOKIE,String CorrespondPath) throws IOException {
        String csrfUrl = String.format("https://www.bilibili.com/correspond/1/%s", CorrespondPath);
        Request getcsrf = new Request.Builder()
                .url(csrfUrl)
                .get()
                .addHeader("Cookie", COOKIE)
                .build();
        Response csrfResponse = client.newCall(getcsrf).execute();
        String csrfbady = csrfResponse.body().string();
        Matcher CSRFZZ = Pattern.compile("(?<=id=\"1-name\">).*(?=</div><div)").matcher(csrfbady);
        String refresh_csrf = null;
        while (CSRFZZ.find()) {
            refresh_csrf = CSRFZZ.group();
            System.out.println(CSRFZZ.group());
        }
        return refresh_csrf;
    }
    public static Map<String, Object> getUserInfo(String COOKIE) throws IOException {
        String refreshUrl=String.format("https://api.live.bilibili.com/User/getUserInfo");
        Request getUserresh =new Request.Builder()
                .url(refreshUrl)
                .get()
                .addHeader("Cookie", COOKIE)
                .build();
        Response refreshRes = client.newCall(getUserresh).execute();
        Map<String, Object> UserMap = mapper.readValue(refreshRes.body().string(), new TypeReference<>(){});
        Map<String, Object> Info = (Map<String, Object>) UserMap.get("data");
        return Info;
    }
    public static Map<String, Object> getCDK(int activity_id,String COOKIE,String csrf) throws IOException {
        String cdklistURL=String.format("https://api.bilibili.com/x/lottery/rewards/awards/mylist?activity_id=%s&csrf=%s",activity_id,csrf);
        Request getcdklist =new Request.Builder()
                .url(cdklistURL)
                .get()
                .addHeader("Cookie", COOKIE)
                .build();
        Response cdklistRes = client.newCall(getcdklist).execute();
        Map<String, Object> listMap = mapper.readValue(cdklistRes.body().string(), new TypeReference<>(){});
        Map<String, Object> list = (Map<String, Object>) listMap.get("data");
        return list;
    }
    public static Map<String, Object> NewGetCDK(String activity_id,String COOKIE,String csrf) throws IOException {
        String NewCdkListURL=String.format("https://api.bilibili.com/x/activity_components/mission/mylist?activity_id=%s&csrf=%s",activity_id,csrf);
        Request NewGetCdkList =new Request.Builder()
                .url(NewCdkListURL)
                .get()
                .addHeader("Cookie", COOKIE)
                .build();
        Response NewCdkListRes = client.newCall(NewGetCdkList).execute();
        Map<String, Object> listMap = mapper.readValue(NewCdkListRes.body().string(), new TypeReference<>(){});
        Map<String, Object> list = (Map<String, Object>) listMap.get("data");
        return list;
    }
    public static String sendmsg(String COOKIE,String roomid,String CSRF) throws Exception {
        List<String> list = getFileContext("smg.txt");
        String msg = list.get(new Random().nextInt(list.size()));
        String rnd = String.valueOf((long) ((Math.random() * 9.0 + 1) * (Math.pow(10, 10 - 1))));
        FormBody senddmBody = new FormBody.Builder()
                .add("msg", msg) //去除csrf中的id字段
                .add("roomid", roomid)
                .add("csrf", CSRF)
                .add("csrf_token", CSRF)
                .add("rnd", rnd)
                .add("color", "16777215")
                .add("fontsize", "25")
                .build();
        Request senddmRequest = new Request.Builder()
                .url("https://api.live.bilibili.com/msg/send")
                .post(senddmBody)
                .addHeader("Cookie", COOKIE)
                .build();
        Response senddmResponse = client.newCall(senddmRequest).execute();
        Map<String, Object> senddmMap = mapper.readValue(senddmResponse.body().string(), new TypeReference<>() {});
        return msg;
    }
    //sendGold(Cookie,赠送用户uid,接收用户uid,直播间号,CSRF,礼物编号,礼物数量,礼物价格)
    public static String sendGold(String COOKIE,String uid,String ruid,String biz_id,String CSRF,String gift_id,String gift_num,String price) throws Exception {
        FormBody senddmBody = new FormBody.Builder()
                .add("uid" , uid) //去除csrf中的id字段
                .add("gift_id" , gift_id)
                .add("ruid" , ruid)
                .add("send_ruid" , "0")
                .add("gift_num" , gift_num)
                .add("coin_type" , "gold")
                .add("bag_id" , "0")
                .add("platform" , "pc")
                .add("biz_code" , "Live")
                .add("biz_id" , biz_id)
                .add("storm_beat_id" , "0")
                .add("price" , price)
                .add("csrf" , CSRF)
                .add("csrf_token" , CSRF)
                .build();
        Request senddmRequest = new Request.Builder()
                .url("https://api.live.bilibili.com/xlive/revenue/v1/gift/sendGold")
                .post(senddmBody)
                .addHeader("Cookie", COOKIE)
                .build();
        Response senddmResponse = client.newCall(senddmRequest).execute();
        Map<String, Object> senddmMap = mapper.readValue(senddmResponse.body().string(), new TypeReference<>() {});
        String msg = (String) senddmMap.get("message");
        return msg;
    }
}
