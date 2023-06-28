import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
public class LiveTool5 {
    //【每次启动前都要配置最新的cookie和csrf，否则可能有bug】
    private static String f = "config.json";
    private static String wj = "5";
    private static String taskId="";
    private static String act_name="";
    private static String task_name="";
    private static String reward_name="";
    private static boolean rw=true;
    private static String ruid;
    private static String COOKIE;
    private static String COOKIE2;
    private static String COOKIE3;
    private static String CSRF;
    private static String CSRF2;
    private static String CSRF3;

    private static String roomid = "23860299";
    private static String value = "";
    private static String refresh_csrf;
    private static String ac_time_value;
    private static int debug = 0;
    private static int hours=1;
    private static int Minutes=59;
    private static int Seconds=59;
    private static int interval=1; //调速，隔xx毫秒发送一次请求

    private static final int printInterval=5000/(interval+9); //打印信息的间隔次数，防止打印信息刷屏
    volatile static boolean end = false; //抢奖品程序是否结束
    static String key = null;
    static String prizeName = null;
    static boolean satisfied=true; //**脚本运行前**领取条件是否满足
    static OkHttpClient client=new OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .build();
    static ObjectMapper mapper = new ObjectMapper();

    public static void readconfig (Map<String, Object> config,Map<String, Object> configmap){
        //↓判断配置是否存在,不存在则用全局配置
        if(configmap.containsKey("taskId")&&configmap.containsKey("interval")&&configmap.containsKey("time")){
            COOKIE= (String) config.get("cookie");
            COOKIE2= (String) config.get("cookie2");
            COOKIE3= (String) config.get("cookie3");
            debug=Integer.parseInt(config.get("debug").toString());
            ac_time_value = (String) config.get("ac_time_value");
            roomid = (String) config.get("roomid");
            taskId = (String) configmap.get("taskId");
            interval = Integer.parseInt(configmap.get("interval").toString());
            Map<String, Object> time = (Map<String, Object>) configmap.get("time");
            hours = Integer.parseInt(time.get("h").toString());
            Minutes = Integer.parseInt(time.get("m").toString());
            Seconds = Integer.parseInt(time.get("s").toString());
            //更改变量为config配置
        }else{
            System.out.println("config.json配置文件错误或不存在");
        }
    }


    @SuppressWarnings({"ConstantConditions","deprecation","unchecked"})
    public static void main(String[] args) throws Exception {
        //String fileName = "src/main/resources/config.json";
        Map<String, Object> config = FFL.readJsonFile(f);
        Map<String, Object> configmap = (Map<String, Object>) config.get(wj); //读取本文件配置
        //↓判断配置是否存在,不存在则用全局配置
        if(configmap.containsKey("taskId")&&configmap.containsKey("interval")&&configmap.containsKey("time")){
            COOKIE= (String) config.get("cookie");
            COOKIE2= (String) config.get("cookie2");
            COOKIE3= (String) config.get("cookie3");
            debug=Integer.parseInt(config.get("debug").toString());
            ac_time_value = (String) config.get("ac_time_value");
            roomid = (String) config.get("roomid");
            ruid = (String) config.get("ruid");
            taskId = (String) configmap.get("taskId");
            interval = Integer.parseInt(configmap.get("interval").toString());
            Map<String, Object> time = (Map<String, Object>) configmap.get("time");
            hours = Integer.parseInt(time.get("h").toString());
            Minutes = Integer.parseInt(time.get("m").toString());
            Seconds = Integer.parseInt(time.get("s").toString());
            //更改变量为config配置
        }else{
            System.out.println("config.json配置文件错误或不存在");
        }
        System.out.println("获取到task_id:" + taskId);
        System.out.println("获取到定时:" + hours + "时" + Minutes + "分" + Seconds + "秒");

		/*先验证领取条件的原因是，如果不满足领取条件，那么`infoUrl`的查询结果中的`receive_id`字段为0
		  这是直播系统的一个安全措施，只有满足领取条件系统才会告诉你真正的`receive_id`*/
        //1.等待领取条件满足
        FFL.timeing(debug,hours,Minutes,Seconds);

        config = FFL.readJsonFile(f);
        configmap = (Map<String, Object>) config.get(wj); //读取本文件配置
        //↓判断配置是否存在,不存在则用全局配置
        readconfig(config,configmap);
        Map<String, String> cookiemap = FFL.cookieToMap(COOKIE);
        Map<String, String> cookie2map = FFL.cookieToMap(COOKIE2);
        Map<String, String> cookie3map = FFL.cookieToMap(COOKIE3);
        CSRF = cookiemap.get("bili_jct");
        CSRF2 = cookie2map.get("bili_jct");
        CSRF3 = cookie3map.get("bili_jct");

        boolean refresh = FFL.refrefrsh(COOKIE);
        refresh=false;
        if(refresh) {
            System.out.println("更新cookie...");
            String CorrespondPath = FFL.getCorrespondPath();
            System.out.println("CorrespondPath:" + CorrespondPath);

            String refresh_csrf = FFL.getrefresh_csrf(COOKIE,CorrespondPath);
            System.out.println("refresh_csrf:" + refresh_csrf);

            String refresh_token = ac_time_value;
            String url = "https://passport.bilibili.com/x/passport-login/web/cookie/refresh?csrf="+CSRF.split("&")[0]+"&refresh_csrf="+refresh_csrf+"&source=main_web&refresh_token="+refresh_token;
            FormBody refreshBody = new FormBody.Builder()
                    .add("csrf", CSRF.split("&")[0]) //去除csrf中的id字段
                    .add("refresh_csrf", refresh_csrf)
                    .add("source", "main_web")
                    .add("refresh_token", refresh_token)
                    .build();
            Request refreshRequest = new Request.Builder()
                    .url(url)
                    .post(refreshBody)
                    .addHeader("Cookie", COOKIE)
                    .build();
            Response refreshResponse = client.newCall(refreshRequest).execute();
            Map<String, Object> refreshMap = mapper.readValue(refreshResponse.body().string(), new TypeReference<>() {
            });
            String newrefresh_token = (String) ((Map<String, Object>) refreshMap.get("data")).get("refresh_token");
            System.out.println(newrefresh_token);
            if (refreshResponse.isSuccessful()) {//response 请求成功
                Headers headers = refreshResponse.headers();
                List<String> cookies = headers.values("Set-Cookie");
                String cc = "";
                for (String c : cookies) {
                    String s = c.split(";")[0];
                    cc = cc + s + ";";
                }
                Map<String, String> ccmap = FFL.cookieToMap(cc);
                //String cookiejson = JSON.toJSONString(cookiemap);
                //JSONObject jsonObject = JSONObject.parseObject(cookiejson);
                cookiemap.put("SESSDATA", ccmap.get("SESSDATA"));
                cookiemap.put("bili_jct", ccmap.get("bili_jct"));
                cookiemap.put("DedeUserID", ccmap.get("DedeUserID"));
                cookiemap.put("DedeUserID__ckMd5", ccmap.get("DedeUserID__ckMd5"));
                cookiemap.put("sid", ccmap.get("sid"));
            }
            String newcookie = FFL.MapTocookie(cookiemap);
            config.put("cookie", newcookie);
            config.put("ac_time_value", newrefresh_token);
            String newconfig = JSON.toJSONString(config, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
            FFL.writeFile(f,newconfig,false);

            FormBody confirmBody = new FormBody.Builder()
                    .add("csrf", CSRF.split("&")[0]) //去除csrf中的id字段
                    .add("refresh_token", refresh_token)
                    .build();
            Request confirmRequest = new Request.Builder()
                    .url("https://passport.bilibili.com/x/passport-login/web/confirm/refresh")
                    .post(confirmBody)
                    .addHeader("Cookie", COOKIE)
                    .build();
            Response confirmResponse = client.newCall(confirmRequest).execute();
            Map<String, Object> confirmMap = mapper.readValue(confirmResponse.body().string(), new TypeReference<>() {
            });
            int code = (int) confirmMap.get("code");
            if (code == 0) {
                System.out.println("刷新cookie和CSRF成功");
            } else {
                System.out.println("发生错误:" + confirmMap.get("message"));
            }
        }

        Thread.sleep(3000);

        if(rw) {
            System.out.println("正在完成任务要求 2人送“牛哇牛哇”...");
            String msg;
            Map<String, Object> UserInfo = FFL.getUserInfo(COOKIE2);
            Map<String, String> info2 = FFL.userinst(UserInfo);
            Map<String, Object> UserInfo2 = FFL.getUserInfo(COOKIE3);
            Map<String, String> info3 = FFL.userinst(UserInfo2);
            String name2 = info2.get("name");
            String uid2 = info2.get("uid");
            String name3 = info3.get("name");
            String uid3 = info3.get("uid");
            //sendGold(Cookie,赠送用户uid,接收用户uid,直播间号,CSRF,礼物编号 1:辣条 31039:牛哇牛哇,礼物数量,礼物价格)
            msg = FFL.sendGold(COOKIE2,uid2,ruid,roomid,CSRF2,"31039","1","100");
            if (msg.equals("0")){
                msg = "成功";
            }
            System.out.println( name2 + "：牛哇牛哇赠送：" + msg +",更换账号发送下一条...");
            msg = FFL.sendGold(COOKIE3,uid3,ruid,roomid,CSRF3,"31039","1","100");
            if (msg.equals("0")) {
                msg = "成功";
            }
            System.out.println( name3 + "：牛哇牛哇赠送：" + msg +",即将完成任务...");
            Thread.sleep(5000);
        }


        System.out.println("等待领取条件满足...");
        String infoUrl=String.format("https://api.bilibili.com/x/activity/mission/single_task?csrf=%s&id=%s",CSRF,taskId);
        Request infoRequest =new Request.Builder()
                .url(infoUrl)
                .get()
                .addHeader("Cookie", COOKIE)
                .build();
        int receiveId;
        Map<String, Object> taskInfoMap;
        Map<String, Object> infoMap;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        while(true){
            try(Response infoResponse = client.newCall(infoRequest).execute();){
                //response body见info-response.json
                infoMap = mapper.readValue(infoResponse.body().string(), new TypeReference<>(){});
                Object message = infoMap.get("message");
                if ((int)infoMap.get("code")==0){
                    taskInfoMap = (Map<String, Object>) ((Map<String, Object>) infoMap.get("data")).get("task_info");
                    receiveId=(int)taskInfoMap.get("receive_id");
                    if(receiveId==0){
                        satisfied=false;
                        System.out.println(dateFormat.format(new Date())+"领取条件仍不满足");
                    }else{break;}
                }
            }catch (IOException e){
                System.out.println("结果为空");
            }
            Thread.sleep(1000); //一秒查询一次领取条件是否满足
        }
        Map<String,Object> groupListMap = ((ArrayList<Map<String,Object>>)taskInfoMap.get("group_list")).get(0);
        int actId=(int)groupListMap.get("act_id");
        int bodyTaskId=(int)groupListMap.get("task_id");
        int groupId=(int)groupListMap.get("group_id");
        act_name = URLEncoder.encode((String) ((Map<String, Object>)((Map<String, Object>) infoMap.get("data")).get("act_info")).get("act_name"));
        task_name = URLEncoder.encode((String) taskInfoMap.get("task_name"));
        reward_name = URLEncoder.encode((String) ((Map<String, Object>) taskInfoMap.get("reward_info")).get("reward_name"));
        System.out.println(act_name);
        System.out.println(task_name);
        System.out.println(reward_name);
        prizeName = URLDecoder.decode(reward_name);
        System.out.println(URLDecoder.decode(act_name));
        System.out.println(URLDecoder.decode(task_name));
        System.out.println(URLDecoder.decode(reward_name));
        ((Map<String, Object>) config.get(wj)).put("act_name",URLDecoder.decode(act_name));
        ((Map<String, Object>) config.get(wj)).put("task_name",URLDecoder.decode(task_name));
        ((Map<String, Object>) config.get(wj)).put("reward_name",URLDecoder.decode(reward_name));
        String newconfig = JSON.toJSONString(config, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
        FFL.writeFile(f,newconfig,false);
        config = FFL.readJsonFile(f);
        configmap = (Map<String, Object>) config.get(wj); //读取本文件配置
        //↓判断配置是否存在,不存在则用全局配置
        readconfig(config,configmap);

        //2.领取条件满足后，脚本触发，CPU使用率会接近100%
        System.out.printf("领取条件满足，脚本启动于%s\n",dateFormat.format(new Date()));
        FormBody clickBody =new FormBody.Builder()
                .add("csrf", CSRF.split("&")[0]) //去除csrf中的id字段
                .add("act_id", String.valueOf(actId))
                .add("task_id", String.valueOf(bodyTaskId))
                .add("group_id", String.valueOf(groupId))
                .add("receive_id", String.valueOf(receiveId))
                .add("receive_from","missionPage")
                .add("act_name", act_name)
                .add("task_name", task_name)
                .add("reward_name", reward_name)
                .build();
        Request clickRequest=new Request.Builder()
                .url("https://api.bilibili.com/x/activity/mission/task/reward/receive")
                .post(clickBody)
                .addHeader("Cookie",COOKIE)
                .build();
        AtomicInteger requestCount = new AtomicInteger();
        while(!end){
            new Thread(()->{
                try(Response response = client.newCall(clickRequest).execute()){
                    String responseStr = response.body().string();
                    Map<String,Object> jsonMap=mapper.readValue(responseStr,new TypeReference<>(){});
                    Object message = jsonMap.get("message");
                    if(message.equals("来晚了，奖品已被领完~")){
                        //Response: {"code":75154,"message":"来晚了，奖品已被领完~","ttl":1,"data":null}
                        Date curTime = new Date();
                        if(satisfied){
                            //当前为0:01之后判定为失败，这个判断条件用于[第一天没抢到，第二天0:00刷新剩余量]的情况
                            if(curTime.getHours()==0 && curTime.getMinutes()>=1){
                                end=true;
                            }else if(requestCount.get()%printInterval==0){
                                System.out.println(dateFormat.format(new Date())+"当日剩余量仍未刷新");
                            }
                        }else{end=true;}
                    }else if((int)jsonMap.get("code")==0){
                        //Response见success-response.json
                        System.out.println("Success by "+Thread.currentThread().getName());
                        Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");
                        key=((Map<String, String>)dataMap.get("extra")).get("cdkey_content");
                        prizeName = URLDecoder.decode(reward_name);
                        end=true;
                    }else if(message.equals("请求过于频繁，请稍后再试")){
                        //Response: {"code":-509,"message":"请求过于频繁，请稍后再试","ttl":1}
                        if(requestCount.get()%printInterval==0)
                            System.out.println("服务器繁忙");
                    }else if(message.equals("超出领取数量限制")){
                        //Response: {"code":75256,"message":"超出领取数量限制","ttl":1,"data":null}
                        end=true;
                    }else if(message.equals("任务奖励已领取")){
                        //Response: {"code":75086,"message":"任务奖励已领取","ttl":1,"data":null}
                        end=true;
                    }else if(requestCount.get()>0){
                        System.err.println("未考虑到的情况: "+responseStr);
                    }
                }catch (IOException e){
                    System.err.println("IOException at "+Thread.currentThread().getName());
                }
            },"Thread-"+ requestCount.incrementAndGet()).start();
            if(requestCount.get()%printInterval==0){
                System.out.printf("已发送%d次请求\n", requestCount.get());
            }
            Thread.sleep(interval);
        }
        System.out.printf("共发送了%d次请求，脚本结束于%s\n",requestCount.get(),dateFormat.format(new Date()));
        Thread.sleep(2*1000); //等待所有线程执行完毕
        if(key==null){
            System.out.println("奖品已被领完，抢奖品失败");
        }else{
            String dhm = "\n" + "【"+prizeName+"】 " + key;
            FFL.writeFile("dhm.txt",dhm,true);
            System.out.printf("抢奖品成功,获得【%s】,兑换码【%s】\n",prizeName,key);
        }
        System.exit(1);
    }
}
