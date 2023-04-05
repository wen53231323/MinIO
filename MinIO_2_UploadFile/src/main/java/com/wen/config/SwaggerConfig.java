package com.wen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashSet;
import java.util.Set;

/**
 * Swagger2接口文档信息配置类
 */
@Configuration // 标识配置类，让Spring来加载该类的配置
@EnableSwagger2 // 通过注解@EnableSwagger2来启用Swagger2
public class SwaggerConfig implements WebMvcConfigurer {
    /**
     * 创建API应用
     * 指定扫描的包路径来定义指定要建立API的目录。
     */
    @Bean
    public Docket createRestApi() {
        Set<String> set = new HashSet<>();
        set.add("https");
        set.add("http");
        Docket docket = new Docket(DocumentationType.SWAGGER_2);//指定Api类型为Swagger2
        docket.apiInfo(apiInfo())// 通过调用自定义方法apiInfo，获得文档的主要信息
                .select()  // 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现
                .apis(RequestHandlerSelectors.basePackage("com.wen.controller"))// 扫描指定controller包路径，设置哪些包下的类生产api接口文档
                .paths(PathSelectors.any()) //指定展示所有controller
                // .paths(PathSelectors.regex("/admin/.*")) //只显示admin路径下的页面
                .build();

        return docket;
    }

    /**
     * 创建该API的基本信息（这些基本信息会展现在文档页面中）
     * 默认文档地址为 http://localhost:8088/swagger-ui.html
     * 配置后访问地址：http://项目实际地址/doc.html
     */
    private ApiInfo apiInfo() {
        ApiInfo apiinfo = new ApiInfoBuilder()
                .title("接口文档名称")// 接口文档页标题
                .description("接口文档描述")// 对于接口文档的相关描述
                .termsOfServiceUrl("http://localhost:8181/doc.html")//接口访问地址
                .contact(new Contact("用户名", null, "邮箱"))// 接口文档内容的一些补充
                .version("1.0")// 文档版本号
                .termsOfServiceUrl("https://www.xxx.com") //网站地址
                .build();
        return apiinfo;
    }
}


