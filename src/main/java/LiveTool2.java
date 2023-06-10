import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("SpellCheckingInspection")
public class LiveTool2 {
	//【每次启动前都要配置最新的cookie和csrf，否则可能有bug】
	private static final String COOKIE="buvid3=1F1F7921-F2D6-4EBF-AE2E-940EF3B15C2F167645infoc; b_nut=1638577021; LIVE_BUVID=AUTO1816391426835696; blackside_state=0; i-wanna-go-back=-1; AMCV_98CF678254E93B1B0A4C98A5%40AdobeOrg=-2121179033%7CMCMID%7C90963336535157033360013591568695033276%7CMCAAMLH-1642902775%7C11%7CMCAAMB-1642902775%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1642305175s%7CNONE%7CvVersion%7C5.3.0; buvid_fp_plain=undefined; CURRENT_BLACKGAP=0; buvid4=7B0B3A66-E66C-ED0B-5511-89246BA0DA9824208-022012117-nt%2F2CNKonVBoS3WxE6jcIw%3D%3D; fingerprint3=6b7de698001c8c25680e4976744b76f6; is-2022-channel=1; hit-new-style-dyn=0; rpdid=|(u)~mmuu~|l0J'uYY)~~mmJR; _uuid=7D10B10E35-7DD3-E110D-862A-2A9F3F93572890225infoc; DedeUserID=353379484; DedeUserID__ckMd5=057d2bff43481fec; CURRENT_FNVAL=4048; header_theme_version=CLOSE; nostalgia_conf=-1; CURRENT_PID=00b009f0-d2d4-11ed-b880-cfd981227f14; FEED_LIVE_VERSION=V8; hit-dyn-v2=1; CURRENT_QUALITY=0; fingerprint=3d81c3c5d7d4725fd1e689257136ef8d; buvid_fp=3d81c3c5d7d4725fd1e689257136ef8d; PVID=7; SESSDATA=c0ba8db7%2C1701663766%2C69a54%2A62; bili_jct=c5d21ab43c3a6f667ba691cae7f9c5e9; sid=6i2ye9jx; home_feed_column=5; innersign=0; bp_video_offset_353379484=805228590446673900; b_lsid=1010102A561_188A092455A; browser_resolution=1865-969; bsource=search_bing";
	private static final String CSRF="c5d21ab43c3a6f667ba691cae7f9c5e9";
	private static final String taskId="23268e79";
	private static final String act_name="%E6%98%9F%E7%A9%B9%E9%93%81%E9%81%931.1%E7%89%88%E6%9C%AC%E4%BB%BB%E5%8A%A1%E3%80%90%E7%9B%B4%E6%92%AD%E3%80%91";
	private static final String task_name="%E6%AF%8F%E6%97%A5%E5%BC%80%E6%92%AD%E6%BB%A1120%E5%88%86%E9%92%9F";
	private static final String reward_name="%E6%8F%90%E7%BA%AF%E4%BB%A5%E5%A4%AA*5";
	private static final int interval=1; //调速，隔xx毫秒发送一次请求

	private static final int printInterval=5000/(interval+9); //打印信息的间隔次数，防止打印信息刷屏
	volatile static boolean end = false; //抢奖品程序是否结束
	static String key = null;
	static String prizeName = null;
	static boolean satisfied=true; //**脚本运行前**领取条件是否满足

	@SuppressWarnings({"ConstantConditions","deprecation","unchecked"})
	public static void main(String[] args) throws IOException,InterruptedException{

		OkHttpClient client=new OkHttpClient.Builder()
											.readTimeout(1, TimeUnit.MINUTES)
											.build();
		ObjectMapper mapper = new ObjectMapper();
		System.out.println("获取到task_id:" + taskId);

		/*先验证领取条件的原因是，如果不满足领取条件，那么`infoUrl`的查询结果中的`receive_id`字段为0
		  这是直播系统的一个安全措施，只有满足领取条件系统才会告诉你真正的`receive_id`*/
		//1.等待领取条件满足
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss");
		System.out.println("["+dateFormat1.format(new Date())+"] 等待时间满足...");
		int a=1;
		while(a==1) {
			Date curTime1 = new Date();
			if (curTime1.getHours() == 1) {
				while(a==1) {
					Date curTime3 = new Date();
					if (curTime3.getMinutes() == 59) {
						while(a==1) {
							Date curTime4 = new Date();
							if (curTime4.getSeconds() == 59) {
								a=0;
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
		System.out.println("等待领取条件满足...");
		String infoUrl=String.format("https://api.bilibili.com/x/activity/mission/single_task?csrf=%s&id=%s",CSRF,taskId);
		Request infoRequest =new Request.Builder()
										.url(infoUrl)
										.get()
										.addHeader("Cookie", COOKIE)
										.build();
		int receiveId;
		Map<String, Object> taskInfoMap;
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		while(true){
			Response infoResponse = client.newCall(infoRequest).execute();
			//response body见info-response.json
			Map<String, Object> infoMap = mapper.readValue(infoResponse.body().string(), new TypeReference<>(){});
			taskInfoMap = (Map<String, Object>) ((Map<String, Object>) infoMap.get("data")).get("task_info");
			receiveId=(int)taskInfoMap.get("receive_id");
			if(receiveId==0){
				satisfied=false;
				System.out.println(dateFormat.format(new Date())+"领取条件仍不满足");
			}else{break;}
			Thread.sleep(60000); //一秒查询一次领取条件是否满足
		}
		Map<String,Object> groupListMap = ((ArrayList<Map<String,Object>>)taskInfoMap.get("group_list")).get(0);
		int actId=(int)groupListMap.get("act_id");
		int bodyTaskId=(int)groupListMap.get("task_id");
		int groupId=(int)groupListMap.get("group_id");

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
						prizeName=(String)dataMap.get("name");
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
		}else{System.out.printf("抢奖品成功,获得【%s】,兑换码【%s】\n",prizeName,key);}
		System.exit(1);
	}
}
