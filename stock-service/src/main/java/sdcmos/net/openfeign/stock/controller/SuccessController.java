package sdcmos.net.openfeign.stock.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author wangzz
 */
@RestController
public class SuccessController {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    @Value("${server.port}")
    private String port;

    /**
     * 启动时间
     */
    public static String startTime = format.format(new Date());
    /**
     * uuid
     */
    public static String uuid = UUID.randomUUID().toString();

    @GetMapping("/success")
    public Object wangzz() {
        Map map = new HashMap(16);
        map.put("msg", "net-boot-demo 启动成功！");
        try {
            Map data = new HashMap(16);
            data.put("启动时间", startTime);
            data.put("端口号", port);
            data.put("唯一识别码", uuid);
            map.put("data", data);
        } catch (Exception e) {
        }
        return map;
    }

}
