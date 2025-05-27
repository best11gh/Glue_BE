package org.glue.glue_be.notice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.notice.dto.request.NoticeRequest;
import org.glue.glue_be.notice.dto.response.NoticeResponse;
import org.glue.glue_be.notice.service.NoticeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
@Tag(name = "Notice", description = "공지 API")
public class NoticeController {

    private final NoticeService noticeService;
    @PostMapping
    @Operation(summary = "[관리자] 공지 등록")
    public BaseResponse<NoticeResponse> create(@Valid @RequestBody NoticeRequest request) {
        NoticeResponse response = noticeService.create(request);
        return new BaseResponse<>(response);
    }

    @PutMapping("/{noticeId}")
    @Operation(summary = "[관리자] 공지 수정")
    public BaseResponse<NoticeResponse> update(@PathVariable Long noticeId, @Valid @RequestBody NoticeRequest request){
        NoticeResponse response = noticeService.update(noticeId, request);
        return new BaseResponse<>(response);
    }

    @GetMapping
    @Operation(summary = "공지 전체 조회")
    public BaseResponse<NoticeResponse[]> getNotices(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        NoticeResponse[] notices = noticeService.getNotices(cursorId, pageSize);
        return new BaseResponse<>(notices);
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지 상세 조회")
    public BaseResponse<NoticeResponse> getNotice(@PathVariable Long noticeId){
        NoticeResponse notice = noticeService.getNotice(noticeId);
        return new BaseResponse<>(notice);
    }


    @DeleteMapping("/{noticeId}")
    @Operation(summary = "[관리자] 공지 삭제")
    public BaseResponse<Void> delete(@PathVariable Long noticeId){
        noticeService.delete(noticeId);
        return new BaseResponse<>();
    }


}
