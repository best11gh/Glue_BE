package org.glue.glue_be.guestbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.guestbook.dto.request.*;
import org.glue.glue_be.guestbook.dto.response.*;
import org.glue.glue_be.guestbook.service.GuestBookService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guestbooks")
@Tag(name = "Guestbook", description = "방명록 API")
public class GuestBookController {

    private final GuestBookService guestBookService;


    // 작성하기
    @PostMapping
    @Operation(summary = "방명록 작성")
    public BaseResponse<GuestBookResponse> createGuestBook(@AuthenticationPrincipal CustomUserDetails auth,
                                                           @Valid @RequestBody CreateGuestBookRequest request) {
        GuestBookResponse response = guestBookService.create(auth.getUserId(), request);
        return new BaseResponse<>(response);
    }

    // 방명록 조회
    @GetMapping()
    @Operation(summary = "방명록 조회")
    public BaseResponse<GuestBookThreadResponse[]> getGuestBooks(@RequestParam Long hostId,
                                                           @RequestParam(required = false) Long cursorId,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                           @AuthenticationPrincipal CustomUserDetails auth) {
        GuestBookThreadResponse[] response = guestBookService.getGuestBooks(hostId, auth.getUserId(), cursorId, pageSize);
        return new BaseResponse<>(response);
    }

    // 방명록 개수
    @GetMapping("/count")
    @Operation(summary = "방명록 개수 조회")
    public BaseResponse<Long> getGuestBookCount(@RequestParam Long hostId) {
        long total = guestBookService.countGuestBooks(hostId);
        return new BaseResponse<>(total);
    }

    // 수정하기
    @PutMapping("/{guestBookId}")
    @Operation(summary = "방명록 수정")
    public BaseResponse<GuestBookResponse> updateGuestBook(@PathVariable Long guestBookId,
                                                           @Valid @RequestBody UpdateGuestBookRequest request,
                                                           @AuthenticationPrincipal CustomUserDetails auth) {
        GuestBookResponse response = guestBookService.update(request, auth.getUserId(), guestBookId);
        return new BaseResponse<>(response);
    }

    // 삭제하기
    @DeleteMapping("/{guestBookId}")
    @Operation(summary = "방명록 삭제")
    public BaseResponse<Void> deleteGuestBook(@PathVariable Long guestBookId,
                                              @AuthenticationPrincipal CustomUserDetails auth) {
        guestBookService.delete(auth.getUserId(), guestBookId);
        return new BaseResponse<>();
    }


}
