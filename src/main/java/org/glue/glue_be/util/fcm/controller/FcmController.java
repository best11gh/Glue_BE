package org.glue.glue_be.util.fcm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.util.fcm.dto.FcmSendDto;
import org.glue.glue_be.util.fcm.dto.MultiFcmSendDto;
import org.glue.glue_be.util.fcm.service.FcmService;
import org.springframework.web.bind.annotation.*;

// 테스트 용
@RestController
@Slf4j
@RequestMapping("/api/fcm")
@Tag(name = "FCM", description = "FCM 테스트용 API")
public class FcmController {

    final FcmService fcmService;

    public FcmController(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    // 단일 기기로 fcm 발송
    @PostMapping("/single")
    @Operation(summary = "단일 기기 FCM 발송 (테스트)")
    public BaseResponse<Void> sendSingle(@RequestBody @Valid FcmSendDto dto) {
        fcmService.sendMessage(dto);
        return new BaseResponse<>();
    }

    // 여러 기기로 fcm 발송
    @PostMapping("/multi")
    @Operation(summary = "다중 기기 FCM 발송 (테스트)")
    public BaseResponse<Void> sendMulti(@RequestBody @Valid MultiFcmSendDto dto) {
        fcmService.sendMultiMessage(dto);
        return new BaseResponse<>();
    }

}
