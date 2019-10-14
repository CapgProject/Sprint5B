package com.cg.otm.OnlineTestManagementRestful.config;
/*
 * Author - Priya
 * Description - Awaring the program about audit trail
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef="auditorProvider")
public class OTMConfiguration {
	
	@Bean
	public AuditorAware<String> auditorProvider(){
		return new AuditTrail();
	}


}
