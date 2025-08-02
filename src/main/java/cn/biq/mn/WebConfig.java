
package cn.biq.mn;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.FormContentFilter;

import jakarta.servlet.Filter;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<Filter> formContentFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new FormContentFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        // 排除multipart请求
        registration.addInitParameter("exclusions", "*.multipart.*");
        return registration;
    }
}