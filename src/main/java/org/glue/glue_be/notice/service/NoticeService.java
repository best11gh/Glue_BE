package org.glue.glue_be.notice.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.notice.dto.request.*;
import org.glue.glue_be.notice.dto.response.*;
import org.glue.glue_be.notice.entity.*;
import org.glue.glue_be.notice.repository.*;
import org.glue.glue_be.notice.response.NoticeResponseStatus;
import org.glue.glue_be.notification.dto.request.BulkNotificationRequest;
import org.glue.glue_be.notification.service.NotificationService;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final UserRepository userRepository;

    private final NotificationService notificationService;

    // TODO: 작성, 수정, 삭제는 어드민만 가능

    // 공지사항 작성
    public NoticeResponse create(NoticeRequest request) {

        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .build();

        Notice savedNotice = noticeRepository.save(notice);
        saveNoticeImages(savedNotice, request.imageUrls());

        // TODO: 관리자와 정지된 사람 제외로 바꾸기
        // 전체 사용자에게 알림 발송
        List<User> users = userRepository.findAll();

        List<Long> receiverIds = users.stream()
                .map(User::getUserId)
                .toList();

        BulkNotificationRequest notificationRequest = BulkNotificationRequest.builder()
                .receiverIds(receiverIds)
                .type("notice")
                .title(notice.getTitle())
                .content(notice.getContent())
                .targetId(savedNotice.getNoticeId())
                .build();

        // 4. 알림 전송
        notificationService.createBulk(notificationRequest);


        return toNoticeResponse(savedNotice);
    }

    // 공지사항 전체 조회
    @Transactional(readOnly = true)
    public NoticeResponse[] getNotices(Long cursorId, Integer pageSize) {
        // 1. 페이징 설정 (pageSize + 1건으로 hasNext 판단용)
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 2. 커서 기반 공지 조회
        List<Notice> notices = (cursorId == null)
                ? noticeRepository.findAllByOrderByNoticeIdDesc(pageable)
                : noticeRepository.findByNoticeIdLessThanOrderByNoticeIdDesc(cursorId, pageable);

        // 3. 다음 페이지 존재 확인
        boolean hasNext = notices.size() > pageSize;
        if (hasNext) {
            notices = notices.subList(0, pageSize);
        }

        // 4. dto 반환
        return notices.stream()
                .map(this::toNoticeResponse)
                .toArray(NoticeResponse[]::new);
    }

    // 공지사항 단건 조회
    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        return toNoticeResponse(findNotice(noticeId));
    }


    // 공지사항 수정
    public NoticeResponse update(Long noticeId, NoticeRequest request) {
        Notice notice = findNotice(noticeId);

        // 제목과 내용 수정
        notice.update(request);

        // 기존 이미지 전부 삭제
        noticeImageRepository.deleteAll(notice.getImages());
        notice.getImages().clear();

        // 새 이미지 추가
        saveNoticeImages(notice, request.imageUrls());

        return toNoticeResponse(notice);
    }

    // 공지사항 삭제
    public void delete(Long noticeId) {
        Notice notice = findNotice(noticeId);
        noticeRepository.delete(notice);
    }


    private Notice findNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(NoticeResponseStatus.NOTICE_NOT_FOUND));
    }

    private void saveNoticeImages(Notice notice, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        int order = 0;
        for (String imageUrl : imageUrls) {
            NoticeImage noticeImage = NoticeImage.builder()
                    .notice(notice)
                    .imageUrl(imageUrl)
                    .imageOrder(order++)
                    .build();

            noticeImageRepository.save(noticeImage);
            notice.getImages().add(noticeImage);
        }
    }

    // Notice => NoticeResponse
    private NoticeResponse toNoticeResponse(Notice notice) {
        List<NoticeImageResponse> imageResponses = notice.getImages() != null
                ? notice.getImages().stream()
                .map(img -> NoticeImageResponse.builder()
                        .noticeImageId(img.getId())
                        .imageUrl(img.getImageUrl())
                        .imageOrder(img.getImageOrder())
                        .build())
                .toList()
                : List.of();

        return NoticeResponse.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrl(imageResponses)
                .build();
    }

}
