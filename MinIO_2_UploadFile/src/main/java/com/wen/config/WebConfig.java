package com.wen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Springboot配置接口WebMvcConfigurer
 */
// ------------------------------Springboot配置接口WebMvcConfigurer------------------------------
// @Configuration注解：将类标识为配置类
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 此种设置跨域的方式，在自定义拦截器的情况下可能导致跨域失效
     * 原因：当跨越请求在跨域请求拦截器之前的拦截器处理时就异常返回了，那么响应的response报文头部关于跨域允许的信息就没有被正确设置，导致浏览器认为服务不允许跨域，而造成错误。
     * 解决：自定义跨域过滤器解决跨域问题（该过滤器最好放在其他过滤器之前）
     *
     * @param registry
     */
    // 重写父类提供的跨域请求处理的接口
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry.addMapping("/**")
                // 设置允许跨域请求的域名
                .allowedOrigins("*")
                // 是否允许cookie
                .allowCredentials(true)
                // 设置允许的请求方式
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                // 设置允许的header属性
                .allowedHeaders("*")
                // 跨域允许时间
                .maxAge(3600);
    }

}