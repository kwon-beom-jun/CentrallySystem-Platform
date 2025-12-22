package com.cs.info.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.util.FileNameUtils;
import com.cs.core.kafka.event.info.PostMailRequestEvent;
import com.cs.info.dto.InfoPostListDto;
import com.cs.info.dto.InfoPostListPage;
import com.cs.info.entity.InfoBoardDefinition;
import com.cs.info.entity.InfoPost;
import com.cs.info.entity.InfoPostAttachment;
import com.cs.info.enums.VisibilityScope;
import com.cs.info.kafka.producer.InfoEventProducer;
import com.cs.info.repository.InfoBoardDefinitionRepository;
import com.cs.info.repository.InfoPostAttachmentRepository;
import com.cs.info.repository.InfoPostRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 게시글 서비스
 */
@Service
@Slf4j
public class InfoPostService {

    @Autowired
    private InfoPostRepository infoPostRepository;

    @Autowired
    private InfoPostAttachmentRepository infoPostAttachmentRepository;

    @Autowired
    private InfoBoardDefinitionRepository infoBoardDefinitionRepository;

    @Autowired
    private InfoEventProducer infoEventProducer;

    @Value("${file.upload.total.size}")
    private int totalSize;

    @Value("${info.file.upload.path}")
    private String infoFileUploadPath;

    @Value("${info.file.upload.url}")
    private String infoFileUploadURL;

    /**
     * 게시글 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public InfoPostListPage getPosts(Long boardId, int page, int size, boolean pinnedFirst, List<VisibilityScope> allowedScopes) {
        Sort sort = pinnedFirst
                ? Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("regTime"))
                : Sort.by(Sort.Order.desc("regTime"));

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InfoPostListDto> postPage;
        
        // 권한이 제공되면 필터링, 아니면 전체 조회
        if (allowedScopes != null && !allowedScopes.isEmpty()) {
            postPage = infoPostRepository.findAllForListByBoardIdAndVisibilityScope(boardId, allowedScopes, pageable);
        } else {
            postPage = infoPostRepository.findAllForListByBoardId(boardId, pageable);
        }

        List<InfoPostListDto> processedContent = postPage.getContent()
                .stream()
                .peek(this::processContentPreview)
                .collect(Collectors.toList());

        InfoPostListPage result = new InfoPostListPage();
        result.setContent(processedContent);
        result.setTotalPages(postPage.getTotalPages());
        result.setTotalElements(postPage.getTotalElements());
        result.setPageNumber(postPage.getNumber());
        result.setPageSize(postPage.getSize());

        return result;
    }

    /**
     * 게시글 단건 조회
     */
    @Transactional
    public InfoPost getPost(Long postId) {
        InfoPost post = infoPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시글을 찾을 수 없습니다: " + postId));
        
        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        infoPostRepository.save(post);
        
        return post;
    }

    /**
     * 게시글 생성
     */
    @Transactional
    public InfoPost createPost(InfoPost post, Integer writerId, String writerName, List<MultipartFile> files) {
        if (files != null && files.size() > totalSize) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "첨부파일은 최대 " + totalSize + "개까지 업로드 가능합니다.");
        }

        post.setWriterId(writerId);
        post.setWriterName(writerName);

        InfoPost savedPost = infoPostRepository.save(post);

        if (files != null && !files.isEmpty()) {
            List<InfoPostAttachment> attachmentList = new ArrayList<>();
            for (MultipartFile file : files) {
                String filename = file.getOriginalFilename();
                String safeFileName = FileNameUtils.safeName(filename);

                File dest = new File(getFileBasePath(), safeFileName);

                try {
                    file.transferTo(dest);
                } catch (IllegalStateException | IOException e) {
                    log.error("파일 업로드 실패: " + filename, e);
                }

                String fileUrl = getFileBaseUrl() + FileNameUtils.encodePathSegment(safeFileName);

                InfoPostAttachment attachment = InfoPostAttachment.builder()
                        .fileUrl(fileUrl)
                        .fileType(file.getContentType())
                        .uploadDate(java.sql.Date.valueOf(LocalDate.now()))
                        .fileOriginName(safeFileName)
                        .fileName(filename)
                        .post(savedPost)
                        .build();

                attachmentList.add(attachment);
            }
            infoPostAttachmentRepository.saveAll(attachmentList);
            savedPost.getAttachments().addAll(attachmentList);
        }

        // 메일 발송 요청 이벤트 발행
        trySendPostMailEvent(post, savedPost.getId());

        return savedPost;
    }

    /**
     * 게시글 메일 발송 요청 이벤트 발행
     */
    private void trySendPostMailEvent(InfoPost post, Long postId) {
        if (post.getSendMail() == null || !post.getSendMail()) {
            return;
        }

        PostMailRequestEvent event = new PostMailRequestEvent(
                postId,
                post.getTitle(),
                post.getContent(),
                post.getMailToAll(),
                post.getDepartmentIds(),
                post.getTeamIds(),
                post.getMailIncludeGuest(),
                post.getMailIncludeUser(),
                post.getMailIncludeManager(),
                post.getMailIncludeAdmin()
        );

        infoEventProducer.sendPostMailRequestEvent(event);
    }

    /**
     * 게시글 수정 (권한 검증 필요)
     */
    @Transactional
    public InfoPost updatePost(Long postId, InfoPost postDetails, Integer editorUserId, boolean isAdmin, List<MultipartFile> files, List<Long> deletedFileIds) {
        InfoPost existingPost = infoPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시글을 찾을 수 없습니다: " + postId));
        
        // 권한 검증: 작성자 또는 ADMIN만 수정 가능
        if (!existingPost.getWriterId().equals(editorUserId) && !isAdmin) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "게시글을 수정할 권한이 없습니다.");
        }

        int existingCount = existingPost.getAttachments().size();
        int newCount = (files != null) ? files.size() : 0;
        int deletedCount = (deletedFileIds != null) ? deletedFileIds.size() : 0;
        int totalAfter = existingCount - deletedCount + newCount;

        if (totalAfter > totalSize) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "첨부파일은 최대 " + totalSize + "개까지 업로드 가능합니다.");
        }

        existingPost.setTitle(postDetails.getTitle());
        existingPost.setContent(postDetails.getContent());
        existingPost.setIsPrivate(postDetails.getIsPrivate());
        existingPost.setVisibilityScope(postDetails.getVisibilityScope());

        if (deletedFileIds != null && !deletedFileIds.isEmpty()) {
            List<String> failedDeletes = new ArrayList<>();
            for (Long fileId : deletedFileIds) {
                InfoPostAttachment attachment = infoPostAttachmentRepository.findById(fileId).orElse(null);
                if (attachment != null && attachment.getPost().getId().equals(postId)) {
                    File file = new File(getFileBasePath(), attachment.getFileOriginName());
                    if (!file.exists()) {
                        failedDeletes.add("-" + attachment.getFileName() + " (존재하지 않음)");
                    } else if (!file.delete()) {
                        failedDeletes.add("-" + attachment.getFileName() + " (삭제 실패)");
                    }
                    infoPostAttachmentRepository.delete(attachment);
                }
            }
            if (!failedDeletes.isEmpty()) {
                String failedFileNames = String.join("\n", failedDeletes);
                log.error(GlobalExceptionHandler.CC + "파일 삭제 실패\n" + failedFileNames + "\n");
            }
            existingPost.getAttachments().removeIf(attachment ->
                    deletedFileIds.contains(attachment.getId())
            );
        }

        if (files != null && !files.isEmpty()) {
            List<InfoPostAttachment> newAttachments = new ArrayList<>();

            for (MultipartFile file : files) {
                String filename = file.getOriginalFilename();
                String safeFileName = FileNameUtils.safeName(filename);

                File dest = new File(getFileBasePath(), safeFileName);
                try {
                    file.transferTo(dest);
                } catch (IllegalStateException | IOException e) {
                    log.error("파일 업로드 실패: " + filename, e);
                }

                String fileUrl = getFileBaseUrl() + FileNameUtils.encodePathSegment(safeFileName);

                InfoPostAttachment newAttach = InfoPostAttachment.builder()
                        .fileUrl(fileUrl)
                        .fileType(file.getContentType())
                        .uploadDate(java.sql.Date.valueOf(LocalDate.now()))
                        .fileOriginName(safeFileName)
                        .fileName(filename)
                        .post(existingPost)
                        .build();

                newAttachments.add(newAttach);
            }
            infoPostAttachmentRepository.saveAll(newAttachments);
            existingPost.getAttachments().addAll(newAttachments);
        }

        return infoPostRepository.save(existingPost);
    }

    /**
     * 게시글 삭제 (소프트 딜리트, 권한 검증 필요)
     */
    @Transactional
    public void deletePost(Long postId, Integer userId, boolean isAdmin) {
        InfoPost post = infoPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시글을 찾을 수 없습니다: " + postId));
        
        // 권한 검증: 작성자 또는 ADMIN만 삭제 가능
        if (!post.getWriterId().equals(userId) && !isAdmin) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "게시글을 삭제할 권한이 없습니다.");
        }

        List<String> failedFiles = new ArrayList<>();

        if (post.getAttachments() != null && !post.getAttachments().isEmpty()) {
            for (InfoPostAttachment attachment : post.getAttachments()) {
                File file = new File(getFileBasePath(), attachment.getFileOriginName());
                if (!file.exists()) {
                    failedFiles.add("-" + attachment.getFileName() + " (존재하지 않음)");
                } else if (!file.delete()) {
                    failedFiles.add("-" + attachment.getFileName() + " (삭제 실패)");
                }
            }

            if (!failedFiles.isEmpty()) {
                String failedFileNames = String.join("\n", failedFiles);
                log.error(GlobalExceptionHandler.CC + "파일 삭제 실패\n" + failedFileNames + "\n");
            }

            infoPostAttachmentRepository.deleteAll(post.getAttachments());
        }

        infoPostRepository.delete(post);
    }

    /**
     * 게시글 고정 설정/해제
     */
    @Transactional
    public void pinPost(Long postId, Boolean pinned) {
        InfoPost post = infoPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시글을 찾을 수 없습니다: " + postId));

        post.setPinned(pinned);
        infoPostRepository.save(post);
    }

    /**
     * 파일 저장 경로 반환
     */
    private String getFileBasePath() {
        return infoFileUploadPath + "/";
    }

    /**
     * 파일 접근 URL 반환
     */
    private String getFileBaseUrl() {
        return infoFileUploadURL + "/";
    }

    /**
     * HTML 태그 제거
     */
    private String stripHtmlTags(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim();
    }

    /**
     * 목록 조회용 contentPreview 처리 (HTML 제거 + 50자 자르기)
     */
    private void processContentPreview(InfoPostListDto dto) {
        String content = dto.getContentPreview();
        if (content == null || content.trim().isEmpty()) {
            dto.setContentPreview("");
            return;
        }

        String plainText = stripHtmlTags(content);
        plainText = plainText.replaceAll("\\s+", " ").trim();

        if (plainText.length() > 50) {
            dto.setContentPreview(plainText.substring(0, 50) + "...");
        } else {
            dto.setContentPreview(plainText);
        }
    }
}

