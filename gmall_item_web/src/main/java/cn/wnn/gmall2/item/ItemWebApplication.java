package cn.wnn.gmall2.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "cn.wnn.gmall2")
public class ItemWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemWebApplication.class, args);
	}
}
