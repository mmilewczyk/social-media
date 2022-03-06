package pl.mmilewczyk.commentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"pl.mmilewczyk.commentservice", "pl.mmilewczyk.amqp"})
@EnableFeignClients(basePackages = "pl.mmilewczyk.clients")
public class CommentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}
