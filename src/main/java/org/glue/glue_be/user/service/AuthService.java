package org.glue.glue_be.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.common.jwt.JwtTokenProvider;
import org.glue.glue_be.common.jwt.UserAuthentication;
import org.glue.glue_be.common.response.BaseResponseStatus;
import org.glue.glue_be.user.dto.request.KakaoSignInRequestDto;
import org.glue.glue_be.user.dto.request.KakaoSignUpRequestDto;
import org.glue.glue_be.user.dto.response.KakaoSignInResponseDto;
import org.glue.glue_be.user.dto.response.KakaoSignUpResponseDto;
import org.glue.glue_be.user.dto.response.KakaoUserInfoResponseDto;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {

	// services
	private final KakaoService kakaoService;

	// utils
	private final JwtTokenProvider jwtTokenProvider;

	// repositories
	private final UserRepository userRepository;


	// todo: 추후 리팩토링 시 과정 중 역할의 책임을 나눌만한 부분이 있는지 보기
	public KakaoSignUpResponseDto kakaoSignUp(KakaoSignUpRequestDto requestDto) {

		// 1. 중복되는 oauthID의 유저가 db에 있는지 재검증
		userRepository.findByOauthId(requestDto.getOauthId())
			.ifPresent(user -> {
				throw new IllegalStateException("해당 OAuth ID의 사용자가 이미 존재합니다.");
			});

		// 2. 입력받은 정보를 취합해 User 객체 조립
		User user = User.builder()
			.uuid(UUID.randomUUID())
			.oauthId(requestDto.getOauthId())
			.userName(requestDto.getUserName())
			.nickname(requestDto.getNickName())
			.gender(requestDto.getGender())
			.birth(requestDto.getBirthDate())
			.nation(requestDto.getNation())
			.description(requestDto.getDescription())
			.certified(0)
			.major(requestDto.getMajor())
			.majorVisibility(requestDto.getMajorVisibility())
			.build();

		// 3. DB에 신규 유저 저장
		User newUser = userRepository.save(user);

		// 4. 자체 엑세스 토큰 발급
		UserAuthentication userAuthentication = new UserAuthentication(newUser.getUserId(), null, null);
		String jwtToken = jwtTokenProvider.generateToken(userAuthentication);
		log.info("[authservice - SignUpAPI] jwtToken => {}", jwtToken);

		//5. dto에 토큰을 담아 리턴
		return KakaoSignUpResponseDto.builder().accessToken(jwtToken).build();


	}

	public KakaoSignInResponseDto kakaoSignIn(KakaoSignInRequestDto requestDto){

		// 1. 토큰을 카카오 서버에 보내 유저 정보를 받는다.
		KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(requestDto.getKakaoToken());

		// 2. oauthId를 가져와 유저가 있는지 db를 조회한다.
		Long oauthId = userInfo.id();

		log.info("[authService - signin API] oauthID -> {}", oauthId);

		// 2.5. 유저를 조회하고 없다면 401 예외 발생
		User user = userRepository.findByOauthId(oauthId).orElseThrow(
			() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자는 존재하지 않습니다."));

		// 3. 자체 엑세스 토큰을 발행 후 리턴
		UserAuthentication userAuthentication = new UserAuthentication(user.getUserId(), null, null);

		String jwtToken = jwtTokenProvider.generateToken(userAuthentication);

		log.info("[authservice - SignInAPI] jwtToken => {}", jwtToken);

		return KakaoSignInResponseDto.builder().accessToken(jwtToken).build();

	}

}
