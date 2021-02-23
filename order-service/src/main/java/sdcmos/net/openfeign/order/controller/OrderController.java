package sdcmos.net.openfeign.order.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import sdcmos.net.openfeign.order.feign.StockService;


/**
 * 服务消费者，消费net-boot-user的服务
 * @author wangzz
 * @date 2021年02月20日 17:47
 */
@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StockService stockService;

    /**
     * restTemplate方式调用
     * @return
     */
    @GetMapping("add1")
    public String add1() {
        //使用 LoadBalanceClient 和 RestTemolate 结合的方式来访问
        ServiceInstance serviceInstance = loadBalancerClient.choose("stock-service");
        String url = String.format("http://%s:%s/success", serviceInstance.getHost(), serviceInstance.getPort());
        log.info("request url:{}", url);
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * Openfeign方式调用
     * @return
     */
    @GetMapping("add2")
    public String add2() {
        String success = stockService.update();
        log.error(success);
        return success;
    }

}
