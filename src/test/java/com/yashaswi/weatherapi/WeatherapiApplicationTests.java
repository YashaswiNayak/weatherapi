package com.yashaswi.weatherapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "weather.api.api-key=test-key-12345"
})
class WeatherapiApplicationTests {

	@Test
	void contextLoads() {
	}

}
