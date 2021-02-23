package sdcmos.net.openfeign.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author wangzz
 * @date 2021年02月22日 18:06
 */
@Component
@FeignClient(name = "stock-service")
public interface StockService {

    @GetMapping("/stock/update")
    public String update();

}
