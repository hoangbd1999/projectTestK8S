package com.elcom.adminconsolebackend.config;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Value("${uri.prefix}")
    private String uriPrefix;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public FilterRegistrationBean<ApiPrefixFilter> apiPrefixFilterRegistration() {
        FilterRegistrationBean<ApiPrefixFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new ApiPrefixFilter(uriPrefix));

        // Lấy URL pattern từ biến môi trường, nếu không có thì dùng giá trị mặc định "/adminconsole-api/*"
        String urlPattern = System.getenv("API_PREFIX_URL");
        if (urlPattern == null || urlPattern.isEmpty()) {
            urlPattern = "/" + uriPrefix + "/*";
        }
        registrationBean.addUrlPatterns(urlPattern);

        // Sắp xếp thứ tự chạy filter (nếu cần)
        registrationBean.setOrder(1);

        return registrationBean;
    }
}