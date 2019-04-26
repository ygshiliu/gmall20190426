package cn.wnn.gmall2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "cn.wnn.gmall2.usermanager.mapper")
public class GmallUsermanagerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUsermanagerServiceApplication.class, args);
	}
}
