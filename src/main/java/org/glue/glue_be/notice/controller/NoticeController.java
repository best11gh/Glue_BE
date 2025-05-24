package org.glue.glue_be.notice.controller;


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
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    public BaseResponse<NoticeResponse> create(@Valid @RequestBody NoticeRequest request) {
        NoticeResponse response = noticeService.create(request);
        return new BaseResponse<>(response);
    }

    @PutMapping("/{noticeId}")
    public BaseResponse<NoticeResponse> update(@PathVariable Long noticeId, @Valid @RequestBody NoticeRequest request){
        NoticeResponse response = noticeService.update(noticeId, request);
        return new BaseResponse<>(response);
    }

    @GetMapping
    public BaseResponse<NoticeResponse[]> getNotices(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        NoticeResponse[] notices = noticeService.getNotices(cursorId, pageSize);
        return new BaseResponse<>(notices);
    }

    @GetMapping("/{noticeId}")
    public BaseResponse<NoticeResponse> getNotice(@PathVariable Long noticeId){
        NoticeResponse notice = noticeService.getNotice(noticeId);
        return new BaseResponse<>(notice);
    }


    @DeleteMapping("/{noticeId}")
    public BaseResponse<Void> delete(@PathVariable Long noticeId){
        noticeService.delete(noticeId);
        return new BaseResponse<>();
    }


}
