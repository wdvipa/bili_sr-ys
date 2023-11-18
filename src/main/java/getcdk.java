import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class getcdk {

    public static String getTimeStampString(long timestamp, String format, int timestampType) {
        if (format == null || format.length() == 0)
        {
            return  null;
        }
        if (timestampType == 1)
        {
            //如果时间戳格式是秒，需要江时间戳变为毫秒
            timestamp = timestamp * 1000L;
        }
        Date dateTime = new Date(timestamp);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(dateTime);
    }
    public static void main(String[] args) throws IOException,InterruptedException{
        String ydhm = "                  【原神 兑换码列表】";
        StringBuilder srdhm = new StringBuilder("              【崩坏：星穹铁道 兑换码列表】");
        String dhmall = "=============兑换码列表=============";
        String fldhm = "=============兑换码列表=============";
        StringBuilder ysdhm = new StringBuilder("=============原石=============");
        StringBuilder dyxdhm = new StringBuilder("========大英雄的经验========");
        StringBuilder mjkdhm = new StringBuilder("=========精锻用魔矿=========");
        StringBuilder mxjdhm = new StringBuilder("========冒险家的经验========");
        StringBuilder mldhm = new StringBuilder("=============摩拉=============");
        StringBuilder xqdhm = new StringBuilder("=============星琼=============");
        StringBuilder mydhm = new StringBuilder("=============漫游指南=============");
        StringBuilder mxdhm = new StringBuilder("=============冒险记录=============");
        StringBuilder tcdhm = new StringBuilder("=============提纯以太=============");
        StringBuilder xyddhm = new StringBuilder("=============信用点=============");
        int y = 0,ys = 0,dyx = 0,mjk = 0,mxj = 0,ml = 0;
        int xqsl = 0,sr =0,xq = 0,my = 0,xyd = 0,mx = 0,tc = 0;
        String f = "config.json";
        Map<String, Object> config = FFL.readJsonFile(f);
        String wj = "get";
        Map<String, Object> configmap = null; //读取本文件配置
        if (config != null) {
            configmap = (Map<String, Object>) config.get(wj);
        }
        String COOKIE = null;
        if (config != null) {
            COOKIE = (String) config.get("cookie");
        }
        int activity_id = 0;
        if (configmap != null) {
            activity_id = Integer.parseInt(configmap.get("ys").toString());
        }
        Map<String, String> cookiemap = FFL.cookieToMap(COOKIE);
        String CSRF = cookiemap.get("bili_jct");
        JSONObject listob = new JSONObject(FFL.getCDK(activity_id, COOKIE, CSRF));
        ArrayList list = (ArrayList) listob.get("list");
        for (Object o : list) {
            Map<String, Object> temp = (Map<String, Object>) o;
            String name = (String) temp.get("award_name");
            int timeStamp = (int) temp.get("receive_time");
            String time = getTimeStampString(timeStamp, "MM月dd日 HH:mm:ss", 1);
            Map<String, Object> info = (Map<String, Object>) temp.get("extra_info");
            String cdk = (String) info.get("cdkey_content");
            y = y + 1;
            ydhm = ysdhm + "\n" + time + "  [" + name + "]    " + cdk;
            if (name.startsWith("原石")) {
                ys = ys + 1;
                ysdhm.append("\n").append(time).append("  [").append(name).append("]    ").append(cdk);
            } else if (name.startsWith("大英雄的经验")) {
                dyx = dyx + 1;
                dyxdhm.append("\n").append(time).append("  [").append(name).append("]    ").append(cdk);
            } else if (name.startsWith("精锻用魔矿")) {
                mjk = mjk + 1;
                mjkdhm.append("\n").append(time).append("  [").append(name).append("]    ").append(cdk);
            } else if (name.startsWith("冒险家的经验")) {
                mxj = mxj + 1;
                mxjdhm.append("\n").append(time).append("  [").append(name).append("]    ").append(cdk);
            } else if (name.startsWith("摩拉")) {
                ml = ml + 1;
                mldhm.append("\n").append(time).append("  [").append(name).append("]    ").append(cdk);
            }
        }

        if (configmap != null) {
            activity_id = Integer.parseInt(configmap.get("sr").toString());
        }
        listob = new JSONObject(FFL.getCDK(activity_id, COOKIE, CSRF));
        list = (ArrayList) listob.get("list");
        for (Object o : list) {
            Map<String, Object> temp = (Map<String, Object>) o;
            String name = (String) temp.get("award_name");
            int timeStamp = (int) temp.get("receive_time");
            String time = getTimeStampString(timeStamp, "MM月dd日 HH:mm:ss", 1);
            String cdk;
            try {
                Map<String, Object> info = (Map<String, Object>) temp.get("extra_info");
                cdk = (String) info.get("cdkey_content");
            } catch (NullPointerException e) {
                cdk = "Null";
            }
            sr = sr + 1;
            srdhm.append("\n").append(time).append("  [").append(name).append("]    |").append(cdk);
            if (name.startsWith("星琼")) {
                xq = xq + 1;
                xqsl = Integer.parseInt(name.substring(3)) + xqsl;
                xqdhm.append("\n").append(time).append("  [").append(name).append("]    |").append(cdk);
            } else if (name.startsWith("漫游指南")) {
                my = my + 1;
                mydhm.append("\n").append(time).append("  [").append(name).append("]    |").append(cdk);
            } else if (name.startsWith("冒险记录")) {
                mx = mx + 1;
                mxdhm.append("\n").append(time).append("  [").append(name).append("]    |").append(cdk);
            } else if (name.startsWith("提纯以太")) {
                tc = tc + 1;
                tcdhm.append("\n").append(time).append("  [").append(name).append("]    |").append(cdk);
            } else if (name.startsWith("信用点")) {
                xyd = xyd + 1;
                xyddhm.append("\n").append(time).append("  [").append(name).append("]    |").append(cdk);
            }
        }
        dhmall = dhmall + "\n" + ydhm;
        dhmall = dhmall+ "\n" + srdhm;
        fldhm = fldhm + "\n" + ysdhm + "\n" + dyxdhm + "\n" + mjkdhm + "\n" + mxjdhm  + "\n" + mldhm + "\n" + xqdhm + "\n" + mydhm + "\n" + tcdhm + "\n" + mxdhm + "\n" + xyddhm;

        FFL.writeFile("ysdhm.txt",ydhm,false);
        FFL.writeFile("srdhm.txt", srdhm.toString(),false);
        FFL.writeFile("kjdhm.txt",fldhm,false);
        FFL.writeFile("dhmall.txt",dhmall,false);
        System.out.println(dhmall);
        System.out.println("星琼 " + xq + "个\n漫游指南*2 " + my + "个\n冒险记录*6 " + mx + "个\n提纯以太*5 " + tc + "个\n信用点*11111 " + xyd + "个\n");
        System.out.println("星琼共" + xqsl + "个\n漫游指南共" + my*2 + "个\n冒险记录共" + mx*6 + "个\n提纯以太共" + tc*5 + "个\n信用点共" + xyd*11111 + "个");
    }
}
