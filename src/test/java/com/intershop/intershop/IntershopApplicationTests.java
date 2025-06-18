package com.intershop.intershop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisInstance;

@SpringBootTest
@ActiveProfiles("test")
public class  IntershopApplicationTests {
	@MockBean
	private RedisInstance redisInstance;
	@Test
	void contextLoads() {
	}

}
