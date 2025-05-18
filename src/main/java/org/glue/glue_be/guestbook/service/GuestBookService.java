package org.glue.glue_be.guestbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.guestbook.dto.request.*;
import org.glue.glue_be.guestbook.dto.response.*;
import org.glue.glue_be.guestbook.entity.GuestBook;
import org.glue.glue_be.guestbook.repository.GuestBookRepository;
import org.glue.glue_be.guestbook.response.GuestBookResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.glue.glue_be.util.fcm.dto.FcmSendDto;
import org.glue.glue_be.util.fcm.service.FcmService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class GuestBookService {

    private final UserRepository userRepository;
    private final GuestBookRepository guestBookRepository;
    private final FcmService fcmService;


    // 방명록 작성
    public GuestBookResponse create(
            Long writerId,
            CreateGuestBookRequest request) {
        // 호스트, 작성자 확인
        User writer = findUser(writerId);
        User host = findUser(request.hostId());

        // 부모 방명록 확인
        GuestBook parent = Optional.ofNullable(request.parentId())
                .map(this::findGuestBook)
                .orElse(null);

        // 방명록 생성
        GuestBook guestBook = GuestBook.builder()
                .host(host)
                .writer(writer)
                .parent(parent)
                .content(request.content())
                .secret(request.secret())
                .build();

        GuestBook savedGuestBook = guestBookRepository.save(guestBook);

        // 알림 보내기
        sendNotification(savedGuestBook, host, parent);

        return GuestBookResponse.fromEntity(savedGuestBook, true);
    }


    // 방명록 수정
    public GuestBookResponse update(UpdateGuestBookRequest request, Long currentUserId, Long guestBookId) {
        GuestBook guestBook = findGuestBook(guestBookId);

        if (!currentUserId.equals(guestBook.getWriter().getUserId())) {
            throw new BaseException(GuestBookResponseStatus.GUESTBOOK_NOT_AUTHOR);
        }

        guestBook.update(request);
        return GuestBookResponse.fromEntity(guestBook, true);
    }

    // 방명록 삭제(작성자 or 호스트만 가능)
    public void delete(Long currentUserId, Long guestBookId) {
        GuestBook guestBook = findGuestBook(guestBookId);

        Long writerId = guestBook.getWriter().getUserId();
        Long hostId = guestBook.getHost().getUserId();

        if (!currentUserId.equals(writerId) && !currentUserId.equals(hostId)) {
            throw new BaseException(GuestBookResponseStatus.GUESTBOOK_NOT_AUTHOR);
        }

        guestBookRepository.delete(guestBook);
    }

    // 방명록 조회
    @Transactional(readOnly = true)
    public GuestBookThreadResponse[] getGuestBooks(
            Long hostId,
            Long currentUserId,
            Long cursorId,
            Integer pageSize
    ) {
        // 1) 호스트 존재 확인
        findUser(hostId);

        // 2) 부모 댓글만 pageSize+1 건 조회 (커서 페이징)
        Pageable pageable = PageRequest.of(0, pageSize + 1);
        List<GuestBook> parents = (cursorId == null)
                ? guestBookRepository
                .findByHost_UserIdAndParentIsNullOrderByGuestBookIdDescCreatedAtDesc(hostId, pageable)
                : guestBookRepository
                        .findByHost_UserIdAndParentIsNullAndGuestBookIdLessThanOrderByGuestBookIdDescCreatedAtDesc(
                                hostId, cursorId, pageable);

        boolean hasNext = parents.size() > pageSize;
        if (hasNext) {
            parents = parents.subList(0, pageSize);
        }

        // 3) 부모 ID 목록 추출
        List<Long> parentIds = parents.stream()
                .map(GuestBook::getGuestBookId)
                .toList();

        // 4) 한 번에 자식 댓글 조회
        List<GuestBook> children = parentIds.isEmpty()
                ? List.of()
                : guestBookRepository.findByParent_GuestBookIdInOrderByCreatedAtAsc(parentIds);

        // 5) 부모ID → 자식 매핑
        Map<Long, GuestBook> childMap = children.stream()
                .collect(Collectors.toMap(
                        c -> c.getParent().getGuestBookId(),
                        Function.identity()
                ));

        // 6) DTO 변환
        List<GuestBookThreadResponse> threads = new ArrayList<>();
        for (GuestBook parent : parents) {
            // 부모 DTO 빌드
            GuestBookThreadResponse.GuestBookThreadResponseBuilder parentB =
                    GuestBookThreadResponse.builder()
                            .id(parent.getGuestBookId())
                            .writer(UserSummary.builder()
                                    .userId(parent.getWriter().getUserId())
                                    .userName(parent.getWriter().getUserName())
                                    .profileImageUrl(parent.getWriter().getProfileImage().getProfileImageUrl())
                                    .build())
                            .content(parent.getContent())
                            .secret(parent.isSecret())
                            .createdAt(parent.getCreatedAt());

            // 자식 댓글이 있으면 DTO로 빌드
            GuestBook child = childMap.get(parent.getGuestBookId());
            if (child != null) {
                GuestBookThreadResponse childDto = GuestBookThreadResponse.builder()
                        .id(child.getGuestBookId())
                        .writer(UserSummary.builder()
                                .userId(child.getWriter().getUserId())
                                .userName(child.getWriter().getUserName())
                                .profileImageUrl(child.getWriter().getProfileImage().getProfileImageUrl())
                                .build())
                        .content(child.getContent())
                        .secret(child.isSecret())
                        .createdAt(child.getCreatedAt())
                        .child(null)
                        .build();
                parentB.child(childDto);
            } else {
                parentB.child(null);
            }

            threads.add(parentB.build());
        }

        return threads.toArray(new GuestBookThreadResponse[0]);
    }

    // 방명록 개수
    public Long countGuestBooks(Long hostId) {
        findUser(hostId);
        return guestBookRepository.countByHost_UserId(hostId);
    }


    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));
    }

    private GuestBook findGuestBook(Long guestBookId) {
        return guestBookRepository.findById(guestBookId)
                .orElseThrow(() -> new BaseException((GuestBookResponseStatus.GUESTBOOK_NOT_FOUND)));

    }

    private void sendNotification(GuestBook book, User host, GuestBook parent) {
        User recipient;
        String title;
        String body = book.getContent();

        if (parent == null) {
            recipient = host;
            title = "새로운 방명록";
        } else {
            recipient = parent.getWriter();
            title = "내가 남긴 방명록 답글";
        }

        fcmService.sendMessage(FcmSendDto.builder()
                .title(title)
                .body(body)
                .token(recipient.getFcmToken())
                .build());
    }


}
