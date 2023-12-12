package kr.sml.masking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(MaskingProperties.class)
public class MaskingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaskingApplication.class, args);
	}

	@Bean
	CommandLineRunner init(MaskingServiceInterface maskingService) {
		return (args) -> {
			maskingService.deleteAll();
			maskingService.init();
		};
	}

}
