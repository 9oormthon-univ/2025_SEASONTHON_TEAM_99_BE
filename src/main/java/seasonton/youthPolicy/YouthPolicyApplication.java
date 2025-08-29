package seasonton.youthPolicy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class YouthPolicyApplication {

	public static void main(String[] args) {
		SpringApplication.run(YouthPolicyApplication.class, args);
	}

}
