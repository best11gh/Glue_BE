package org.glue.glue_be.user.dto.request;


import lombok.Getter;

import java.time.LocalDate;


@Getter
public class KakaoSignUpRequestDto {

	private Long oauthId;

	private String userName;

	private String nickName;

	private Integer gender;

	private LocalDate birthDate;

	private Integer nation;

	private String description;

	// certified는 default가 0이라 dto에 없음. 회원가입단에서 필드에 직접 0 박아넣음.

	private Integer major;

	private Integer majorVisibility;

}
