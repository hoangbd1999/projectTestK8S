package com.elcom.adminconsolebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableScheduling
@SpringBootApplication
//@ImportAutoConfiguration({FeignAutoConfiguration.class, HttpClientConfiguration.class})
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AdminConsoleBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminConsoleBackendApplication.class, args);
	}

}
