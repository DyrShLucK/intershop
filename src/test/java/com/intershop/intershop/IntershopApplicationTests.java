package com.intershop.intershop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisInstance;

@SpringBootTest
@ActiveProfiles("test")
public class  IntershopApplicationTests {
	@MockBean
	private RedisInstance redisInstance;
	@MockBean
	private ReactiveClientRegistrationRepository clientRegistrationRepository;
	@MockBean
	private ReactiveOAuth2AuthorizedClientService authorizedClientService;

	@Test
	void contextLoads() {
	}

}
