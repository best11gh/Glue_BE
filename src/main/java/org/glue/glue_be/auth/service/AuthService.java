package org.glue.glue_be.auth.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.auth.dto.request.*;
import org.glue.glue_be.auth.dto.response.*;
import org.glue.glue_be.auth.jwt.*;
import org.glue.glue_be.auth.response.AuthResponseStatus;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.redis.RedisUtil;
import org.glue.glue_be.report.dto.response.ReportResponse;
import org.glue.glue_be.report.repository.ReportRepository;
import org.glue.glue_be.user.entity.*;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {

    // services
    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final GoogleService googleService;

    // utils
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtil redisUtil;

    // repositories
    private final UserRepository userRepository;
    private final MailService mailService;
    private final ReportRepository reportRepository;


    // todo: 추후 리팩토링 시 과정 중 역할의 책임을 나눌만한 부분이 있는지 보기
    public SignUpResponseDto kakaoSignUp(KakaoSignUpRequestDto requestDto) {

        // 1. 중복되는 oauthID의 유저가 db에 있는지 재검증
        userRepository.findByOauthId(requestDto.oauthId())
                .ifPresent(user -> {
                    throw new BaseException(UserResponseStatus.ALREADY_EXISTS, "해당 소셜 계정으로 가입된 유저가 이미 존재합니다.");
                });

        // 2. 입력받은 정보를 취합해 User 객체 조립
        User user = requestDto.toEntity();

        // 3. DB에 신규 유저 저장
        User newUser = userRepository.save(user);

        // 4. 자체 엑세스 토큰 발급 및 리턴
        return SignUpResponseDto.builder()
                .accessToken(getToken(newUser))
                .build();

    }

    public SignInResponseDto kakaoSignIn(KakaoSignInRequestDto requestDto) {

        // 1. 토큰을 카카오 서버에 보내 유저 정보를 받는다.
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(requestDto.kakaoToken());

        // 2. oauthId를 가져와 유저가 있는지 db를 조회한다.
        String oauthId = userInfo.id();

        // 2.5. 유저를 조회하고 없다면 401 예외 발생
        User user = userRepository.findByOauthId(oauthId).orElseThrow(
                () -> new BaseException(UserResponseStatus.USER_NOT_FOUND)
        );

        validateUserCanLogin(user);
        // 2.75. 로그인 시 fcm 토큰을 엔티티에 넣어줌
        user.changeFcmToken(requestDto.fcmToken());

        // 3. 자체 엑세스 토큰을 발행 후 리턴

        return SignInResponseDto.builder()
                .accessToken(getToken(user))
                .acceptedReportCount(user.getAcceptedReportCount())
                .acceptedReports(getAcceptedReportResponses(user))
                .build();

    }

    public SignUpResponseDto appleSignUp(AppleSignUpRequestDto requestDto) {

        AppleUserInfoResponseDto appleUserInfo = appleService.getAppleUserProfile(requestDto.idToken());

        String appleOauthId = appleUserInfo.getSubject();

        userRepository.findByOauthId(appleOauthId)
                .ifPresent(user -> {
                    throw new BaseException(UserResponseStatus.ALREADY_EXISTS, "해당 소셜 계정으로 가입된 유저가 이미 존재합니다.");
                });

        User user = requestDto.toEntity(appleOauthId);

        User newUser = userRepository.save(user);

        return SignUpResponseDto.builder()
                .accessToken(getToken(newUser))
                .build();
    }


    public SignInResponseDto appleSignIn(AppleSignInRequestDto requestDto) {
        AppleUserInfoResponseDto appleUserInfo = appleService.getAppleUserProfile(requestDto.idToken());

        User user = userRepository.findByOauthId(appleUserInfo.getSubject())
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        validateUserCanLogin(user);
        user.changeFcmToken(requestDto.fcmToken());

        return SignInResponseDto.builder()
                .accessToken(getToken(user))
                .acceptedReportCount(user.getAcceptedReportCount())
                .acceptedReports(getAcceptedReportResponses(user))
                .build();
    }

    public SignUpResponseDto googleSignUp(GoogleSignUpRequestDto requestDto) {

        // 1. Authorization Code로 Google 유저 정보 획득
        GoogleUserInfoResponseDto userInfo = googleService.getGoogleUserInfo(requestDto.authorizationCode());

        // 2. 동일 oauthId 존재하는지 확인
        String oauthId = userInfo.id();
        userRepository.findByOauthId(oauthId)
                .ifPresent(user -> {
                    throw new BaseException(UserResponseStatus.ALREADY_EXISTS, "해당 소셜 계정으로 가입된 유저가 이미 존재합니다.");
                });

        // 3. 유저 생성 및 저장
        User user = requestDto.toEntity(oauthId);
        User newUser = userRepository.save(user);

        // 4. Access Token 발급
        return SignUpResponseDto.builder()
                .accessToken(getToken(newUser))
                .build();
    }

    public SignInResponseDto googleSignIn(GoogleSignInRequestDto requestDto) {

        // 1. Authorization Code로 유저 정보 획득
        GoogleUserInfoResponseDto userInfo = googleService.getGoogleUserInfo(requestDto.authorizationCode());

        String oauthId = userInfo.id();

        // 2. 유저 존재 여부 확인
        User user = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        // 3. 로그인 유효성 검사 및 FCM 업데이트
        validateUserCanLogin(user);
        user.changeFcmToken(requestDto.fcmToken());

        // 4. Access Token 발급
        return SignInResponseDto.builder()
                .accessToken(getToken(user))
                .acceptedReportCount(user.getAcceptedReportCount())
                .acceptedReports(getAcceptedReportResponses(user))
                .build();
    }


    public void sendCode(String email) {

//         1. 이미 해당 email 유저가 있는지 검증
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new BaseException(UserResponseStatus.ALREADY_EXISTS, "해당 이메일을 가진 유저가 이미 존재합니다.");
        });

        // 2. 코드 생성
        String code = redisUtil.createdCertifyNum();

        // 3. 이메일 송신
        mailService.sendCodeEmail(email, code);

        // 4. Redis에 이메일-코드 데이터 저장
        redisUtil.setCodeData(email, code);

    }


    public void verifyCode(String email, String code) {

        // 1. redis에서 null을 받아오면 코드값이 만료했다 판단
        String redisCode = Optional.ofNullable(redisUtil.getData(email))
                .orElseThrow(() -> new BaseException(AuthResponseStatus.EXPIRE_CODE));

        // 2. 가져온 코드값과 입력 코드값이 다르면 예외 발생
        if (!redisCode.equals(code)) {
            throw new BaseException(AuthResponseStatus.FALSE_CODE);
        }

        // 3. 코드가 통과됐으므로 재사용 방지하게끔 기존 코드 삭제
        redisUtil.deleteData(email);

    }


    // 닉네임 중복체크(중복말고도 추후 적절한 닉넴 검증등에 대한 추가 로직이 들어올수도 있단 생각에 일반화된 네이밍 사용)
    public void checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new BaseException(UserResponseStatus.ALREADY_EXISTS, "중복된 닉네임입니다.");
        }
    }


    public void checkEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BaseException(UserResponseStatus.ALREADY_EXISTS, "중복된 이메일입니다.");
        }
    }

    public String toggleRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

        UserRole current = user.getRole();
        UserRole newRole = (current == UserRole.ROLE_USER) ? UserRole.ROLE_ADMIN : UserRole.ROLE_USER;
        user.changeRole(newRole);

        return newRole.name();
    }

    /// / 내부 유틸

    private String getToken(User user) {
        CustomUserDetails customUserDetails = new CustomUserDetails(user.getUserId(), user.getNickname(),
                user.getRole());
        UserAuthentication authentication = new UserAuthentication(customUserDetails, null, null);

        return jwtTokenProvider.generateToken(authentication);
    }

    private void validateUserCanLogin(User user) {
        if (user.isBlockedByReport()) {
            throw new BaseException(AuthResponseStatus.BLOCKED_BY_REPORT);
        }
    }

    private List<ReportResponse> getAcceptedReportResponses(User user) {
        return reportRepository.findByReportedAndAcceptedTrue(user)
                .stream()
                .map(ReportResponse::from)
                .toList();
    }
}
