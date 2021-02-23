package sdcmos.net.openfeign.stock.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务消费者，消费net-boot-user的服务
 * @author wangzz
 * @date 2021年02月20日 17:47
 */
@Slf4j
@RestController
@RequestMapping("stock")
public class StockController {

    @Value("${server.port}")
    private String port;

    @GetMapping("update")
    public String update() {
        return "库存更新成功！" + port;
    }

}
