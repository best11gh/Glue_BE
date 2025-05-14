package org.glue.glue_be.redis;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;


@RequiredArgsConstructor
@Service
public class RedisUtil {

	@Value("${spring.mail.auth-code-expiration-millis}")
	private int mailCodeDuration;

	private final StringRedisTemplate template; // 문자열 키 조작에 특화된 템플릿 객체


	// key에 대응하는 value값 가져오기
	public String getData(String key) {
		ValueOperations<String, String> valueOperations = template.opsForValue();
		return valueOperations.get(key);
	}

	// key에 해당하는 데이터 존재여부 확인
	public boolean existData(String key) {
		return template.hasKey(key);
	}

	// 유효기간 존재하는 key-value 데이터 생성
	public void setDataExpire(String key, String value) {
		ValueOperations<String, String> valueOperations = template.opsForValue();
		valueOperations.set(key, value, Duration.ofMillis(mailCodeDuration));
	}

	// key에 해당하는 데이터 삭제
	public void deleteData(String key) {
		template.delete(key);
	}

	// 이메일 인증용 6자리 난수 코드 생성
	public String createdCertifyNum() {
		Random random = new Random();

		// random.nextInt는 인자값 미만의 난수 정수를 생성.
		return String.format("%06d", random.nextInt(1000000));
	}

	public void setCodeData(String email, String code) {
		// 1. 만약 이메일을 키로 하는 데이터가 redis에 존재 시 일단 삭제
		if (existData(email)) deleteData(email);

		// 2. key를 이메일, code를 value로 하는 데이터 생성해 redis에 저장
		setDataExpire(email, code);
	}
}
