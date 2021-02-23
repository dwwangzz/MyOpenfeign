### 〇、需求
1. 整合Spring Cloud Alibaba Nacos Discovery和Spring Cloud OpenFeign
2. 修改负载均衡器为Spring Cloud LoadBalance
> PS: Spring Cloud 默认使用Ribbon作为负载均衡器，目前Ribbon已经处于维护状态。Spring Cloud官方推荐使用Spring Cloud自己开发的负载均衡器`spring-cloud-loadbalancer`，这里是官方给出的一个负载均衡Client [Spring Cloud LoadBalancer client](https://github.com/spring-cloud/spring-cloud-commons/blob/master/spring-cloud-loadbalancer/src/main/java/org/springframework/cloud/loadbalancer/blocking/client/BlockingLoadBalancerClient.java)
### 一、环境：
> 1. spring boot 版本【2.4.2】
> 2. spring cloud 版本【2020.0.1】
> 3. spring cloud alibaba 版本 【2.2.1.RELEASE】

### 二、默认使用的负载均衡
> 1. NacosDiscovery【2.2.1.RELEASE】默认使用的客户端负载均衡是ribbon
>  2. OpenFeign【v3.0.1】默认使用的是Spring Cloud LoadBalance
### 三、服务提供者
> 创建服务提供者，服务提供者只是一个在nacos注册中心注册的一个普通的服务
1. pom引入
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!--引入spring-cloud核心包-->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter</artifactId>
    </dependency>
    <!--SpringCloud Ailibaba Nacos 服务发现-->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        <!-- 使用spring loadbalancer，弃用ribbon -->
        <exclusions>
            <exclusion>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```
2. 修改配置文件

```yml
server:
  port: 8081
spring:
  application:
    name: stock-service
  profiles:
    active: dev
  jackson:
    time-zone: GMT+8
    date-format: yyyy-MM-dd HH:mm:ss
  cloud:
    nacos:
      discovery:
        server-addr: 192.168.230.12:8848,192.168.230.121:8848,192.168.230.122:8848
      config:
        server-addr: 192.168.230.12:8848,192.168.230.121:8848,192.168.230.122:8848
# Nacos Discovery 内部提供了一个 Endpoint, url: http://ip:port/actuator/nacos-discovery
management:
  endpoints:
    web:
      exposure:
        include: "*"
```
3. 正常写controller

```java
/**
 * 服务提供者提供更新库存服务
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
```
### 四、服务消费者
#### 整合方式一：
> 这里的配置都是针对Client端【服务消费者】的，服务提供端不用做任何特殊配置，只需要服务注册到nacos即可

1. 引用pom
```xml
<!--SpringCloud Ailibaba Nacos 2.2.1.RELEASE 服务发现-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
        <!--引入loadbalancer v3.0.1-->
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-loadbalancer</artifactId>
</dependency>
        <!--这里必须引入，因为loadbalancer依赖webflux v2.4.2-->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
2. 修改启动类注解，**一定要排除ribbon自动配置**`RibbonAutoConfiguration.class`
```java
/**
 * @author wangzz
 */
@EnableFeignClients
@EnableDiscoveryClient
// 在启动类中排除Ribbon自动配置类 
@SpringBootApplication(exclude = RibbonAutoConfiguration.class)
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
```
3. 禁止nacos使用ribbon

```yaml
ribbon:
  nacos:
    enabled: false
```
4. 禁止SpringCloud使用ribbon负载均衡

```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
```
5. 消费服务

```java
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


/**
 * 服务消费者，像调用本地service一样调用微服务接口
 * @author wangzz
 * @date 2021年02月20日 17:47
 */
@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {

    @Resource
    private StockService stockService;

    @GetMapping("add")
    public String add() {
        return stockService.update();
    }

}

```


#### 整合方式二：
采用排除`Ribbon`包的形式排除`ribbon`负载均衡器，使用`spring-cloud-loadbalancer`负载均衡
> 1. 这里的配置都是针对Client端【服务消费者】的，服务提供端不用做任何特殊配置，只需要服务注册到nacos即可
> 2. 因为我的项目有两个如下图，都是单独引入的`spring-cloud-starter-alibaba-nacos-discovery`，这样每个项目都会自动引入，ribbon相关的包，**如果`spring-cloud-starter-alibaba-nacos-discovery`是每个工程引入一次的话，每个工程都需要排除ribbon，只要有一个工程中引入ribbon，那么所有的工程在加载的时候还是会去加载ribbon自动配置类**，`Idea`可以使用`Ctrl+Shift+N`搜索一下`ribbon`看看是否移除干净ribbon相关的包。

![项目结构](https://img-blog.csdnimg.cn/20210223152044803.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTM1Mzk1NjY=,size_16,color_FFFFFF,t_70)
1. 引入pom
```xml
<!--SpringCloud Ailibaba Nacos 服务发现-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <!-- 使用spring loadbalancer，弃用ribbon -->
    <exclusions>
        <exclusion>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
        </exclusion>
    </exclusions>
</dependency>
        <!--openfeign-->
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
        <!--引入loadbalancer-->
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-loadbalancer</artifactId>
</dependency>
        <!--loadbalancer依赖webflux，这里必须引入-->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

2. 修改启动类注解
```java
/**
 * @author wangzz
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
```

3. 消费服务

```java
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


/**
 * 服务消费者，像调用本地service一样调用微服务接口
 * @author wangzz
 * @date 2021年02月20日 17:47
 */
@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {

    @Resource
    private StockService stockService;

    @GetMapping("add")
    public String add() {
        return stockService.update();
    }

}
```
### 五、测试
1. 启动order-service
2. 启动两个stock-service，如下图，指定端口启动
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210223162610817.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTM1Mzk1NjY=,size_16,color_FFFFFF,t_70)
3. 访问：localhost:8080/order/add2
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210223163117373.png)
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210223163256789.gif#pic_center)
