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
import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
public class LiveTool6 {
	//【每次启动前都要配置最新的cookie和csrf，否则可能有bug】
	private static String f = "config.json";
	private static String wj = "6";
	private static String taskId="";
	private static String act_name="";
	private static String task_name="";
	private static String reward_name="";
	private static String COOKIE;
	private static String CSRF;
	private static String value = "";
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

	public static Map<String,String> cookieToMap(String value) {
		Map<String, String> map = new HashMap<String, String>();
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

	public static void readconfig (Map<String, Object> config,Map<String, Object> configmap){
		//↓判断配置是否存在,不存在则用全局配置
		if(configmap.containsKey("taskId")&&configmap.containsKey("interval")&&configmap.containsKey("time")){
			COOKIE= (String) config.get("cookie");
			debug=Integer.parseInt(config.get("debug").toString());
			ac_time_value = (String) config.get("ac_time_value");
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
	public static void timeing(int hours,int Minutes,int Seconds) throws InterruptedException {
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


	@SuppressWarnings({"ConstantConditions","deprecation","unchecked"})
	public static void main(String[] args) throws IOException,InterruptedException{
		//String fileName = "src/main/resources/config.json";
		Map<String, Object> config = readJsonFile(f);
		Map<String, Object> configmap = (Map<String, Object>) config.get(wj); //读取本文件配置
		//↓判断配置是否存在,不存在则用全局配置
		if(configmap.containsKey("taskId")&&configmap.containsKey("interval")&&configmap.containsKey("time")){
			COOKIE= (String) config.get("cookie");
			debug=Integer.parseInt(config.get("debug").toString());
			ac_time_value = (String) config.get("ac_time_value");
			taskId = (String) configmap.get("taskId");
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
		timeing(hours,Minutes,Seconds);

		config = readJsonFile(f);
		configmap = (Map<String, Object>) config.get(wj); //读取本文件配置
		//↓判断配置是否存在,不存在则用全局配置
		readconfig(config,configmap);
		Map<String, String> cookiemap = cookieToMap(COOKIE);
		CSRF = cookiemap.get("bili_jct");

		boolean refresh = FFL.refrefrsh(COOKIE);
		refresh = false;
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
				Map<String, String> ccmap = cookieToMap(cc);
				//String cookiejson = JSON.toJSONString(cookiemap);
				//JSONObject jsonObject = JSONObject.parseObject(cookiejson);
				cookiemap.put("SESSDATA", ccmap.get("SESSDATA"));
				cookiemap.put("bili_jct", ccmap.get("bili_jct"));
				cookiemap.put("DedeUserID", ccmap.get("DedeUserID"));
				cookiemap.put("DedeUserID__ckMd5", ccmap.get("DedeUserID__ckMd5"));
				cookiemap.put("sid", ccmap.get("sid"));
			}
			String newcookie = MapTocookie(cookiemap);
			config.put("cookie", newcookie);
			config.put("ac_time_value", newrefresh_token);
			String newconfig = JSON.toJSONString(config, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat);
			writeFile(f,newconfig,false);

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
			Response infoResponse = client.newCall(infoRequest).execute();
			//response body见info-response.json
			infoMap = mapper.readValue(infoResponse.body().string(), new TypeReference<>(){});
			taskInfoMap = (Map<String, Object>) ((Map<String, Object>) infoMap.get("data")).get("task_info");
			receiveId=(int)taskInfoMap.get("receive_id");
			if(receiveId==0){
				satisfied=false;
				System.out.println(dateFormat.format(new Date())+"领取条件仍不满足");
			}else{break;}
			Thread.sleep(500); //一秒查询一次领取条件是否满足
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
		writeFile(f,newconfig,false);
		config = readJsonFile(f);
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
			writeFile("dhm.txt",dhm,true);
			System.out.printf("抢奖品成功,获得【%s】,兑换码【%s】\n",prizeName,key);
		}
		System.exit(1);
	}
}
