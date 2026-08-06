package recruitment.dev.jobofferservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobOfferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobOfferServiceApplication.class, args);
    }

}
