package elec.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ElecShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElecShopApplication.class, args);
    }
}
