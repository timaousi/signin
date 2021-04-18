package com.gupao.signin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author wangy
 */
@RestController
public class K8SController {

    @RequestMapping("/k8s")
    public String k8s(){
        // 签到开关 签到成功关闭 每天根据重置时间重置成打开
        final Boolean[] status = {true};
        // 重置时间随机生成
        String time = "08:";
        // 创建任务队列  10为线程数量
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
        // 执行任务 1s 后开始执行，每 10s 执行一次
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                if(status[0]){
                    Map html = signIn(getMap());
                    System.out.println(html);
                    if("0".equals(html.get("appcode").toString())){
                        status[0] = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 1, 10, TimeUnit.SECONDS);

        // 执行任务 1s 后开始执行，每天 执行一次
        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay  = getTimeMillis("08:15:00") - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                Thread.sleep(new Random().nextInt(300000));
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date nowTime = null;
                if(!status[0]){
                    System.out.println("重置签到状态"+" "+df.format(new Date()));
                    status[0] = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, initDelay, oneDay, TimeUnit.MILLISECONDS);
        //}, 1,1, TimeUnit.SECONDS);

        return "hello K8s <br/>start signIn";
    }

    public Map signIn(HashMap map) throws IOException {
        Connection connect = Jsoup.connect("http://gskq.tjyinhai.cn/yhwx/attendance/toClockIn");
        Map<String, String> header = new HashMap<String, String>();
        header.put("User-Agent","Mozilla/5.0 (Linux; Android 9; SM-G9550 Build/PPR1.180610.011; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045529 Mobile Safari/537.36 MMWEBID/9142 MicroMessenger/8.0.2.1860(0x280002A0) Process/tools WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64");
        header.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        header.put("Referer","http://gskq.tjyinhai.cn/yhwx/attendance/clockIn?sign_type=01");
        header.put("Cookie","yhwx_userid=80130");
        connect.data("signType", "01");
        //"39.138312090670766"
        connect.data("latitude", map.get("latitude").toString());
        //"117.14580467127678"
        connect.data("longitude", map.get("longitude").toString());
        connect.data("employeeid", "80130");
        //"2021/04/14 08:08:08"
        connect.data("time", map.get("time").toString());
        Connection data = connect.headers(header);
        Document document = data.timeout(3000).post();

        ObjectMapper objectMapper = new ObjectMapper();
        Map html = objectMapper.readValue(document.select("body").html(),Map.class);
        html.putAll(map);
        return html;
    }

    public HashMap getMap(){
        Random r = new Random();
        int ran1 = r.nextInt(50)+10;
        int ran2 = r.nextInt(50)+10;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap map = new HashMap();
        map.put("latitude","39.1383120906707"+ran1);
        map.put("longitude","117.145804671276"+ran2);
        map.put("time",format.format(new Date()));
        return map;
    }

    private static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
