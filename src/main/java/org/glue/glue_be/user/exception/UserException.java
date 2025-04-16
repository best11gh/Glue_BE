package org.glue.glue_be.user.exception;

// 사용자 관련 예외 처리를 위한 클래스
public class UserException extends RuntimeException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    // 사용자를 찾을 수 없는 경우의 예외
    public static class UserNotFoundException extends UserException {
        public UserNotFoundException(Long userId) {
            super("사용자를 찾을 수 없습니다: " + userId);
        }
    }

    // 이미 존재하는 사용자인 경우의 예외
    public static class UserAlreadyExistsException extends UserException {
        public UserAlreadyExistsException(String identifier) {
            super("이미 존재하는 사용자입니다: " + identifier);
        }
    }

    // 사용자 권한 부족 예외
    public static class UserUnauthorizedException extends UserException {
        public UserUnauthorizedException() {
            super("해당 작업에 대한 권한이 없습니다.");
        }
    }
}