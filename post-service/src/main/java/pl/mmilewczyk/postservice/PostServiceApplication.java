package pl.mmilewczyk.postservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"pl.mmilewczyk.postservice", "pl.mmilewczyk.amqp"})
@EnableFeignClients(basePackages = "pl.mmilewczyk.clients")
public class PostServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostServiceApplication.class, args);
    }

}
