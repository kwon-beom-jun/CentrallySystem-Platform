package com.cs.info.service;

import com.cs.info.controller.response.PostListPageResponse;
import com.cs.info.entity.InfoPost;
import com.cs.info.enums.VisibilityScope;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 게시글 서비스 인터페이스
 */
public interface InfoPostService {

    /**
     * 게시글 목록 조회 (페이징)
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param pinnedFirst 고정 게시글 우선 정렬 여부
     * @param allowedScopes 허용된 공개 범위 목록
     * @return 게시글 목록 페이지 응답
     */
    PostListPageResponse getPosts(Long boardId, int page, int size, boolean pinnedFirst, List<VisibilityScope> allowedScopes);

    /**
     * 게시글 단건 조회
     * @param postId 게시글 ID
     * @return 게시글 엔티티
     */
    InfoPost getPost(Long postId);

    /**
     * 게시글 생성
     * @param post 게시글 엔티티
     * @param writerId 작성자 ID
     * @param writerName 작성자 이름
     * @param files 첨부 파일 목록
     * @return 생성된 게시글 엔티티
     */
    InfoPost createPost(InfoPost post, Integer writerId, String writerName, List<MultipartFile> files);

    /**
     * 게시글 수정 (권한 검증 필요)
     * @param postId 게시글 ID
     * @param postDetails 수정할 게시글 정보
     * @param editorUserId 수정자 ID
     * @param isAdmin 관리자 여부
     * @param files 새로 추가할 첨부 파일 목록
     * @param deletedFileIds 삭제할 첨부 파일 ID 목록
     * @return 수정된 게시글 엔티티
     */
    InfoPost updatePost(Long postId, InfoPost postDetails, Integer editorUserId, boolean isAdmin, List<MultipartFile> files, List<Long> deletedFileIds);

    /**
     * 게시글 삭제 (소프트 딜리트, 권한 검증 필요)
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @param isAdmin 관리자 여부
     */
    void deletePost(Long postId, Integer userId, boolean isAdmin);

    /**
     * 게시글 고정 설정/해제
     * @param postId 게시글 ID
     * @param pinned 고정 여부
     */
    void pinPost(Long postId, Boolean pinned);
}
