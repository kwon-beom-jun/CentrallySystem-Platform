package com.cs.core.kafka.event.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * INFO 게시글 메일 발송 요청 이벤트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostMailRequestEvent {
    private Long postId;
    private String title;
    private String content;
    
    // 메일 발송 대상
    private Boolean mailToAll;
    private List<Integer> departmentIds;
    private List<Integer> teamIds;
    
    // 역할별 포함 여부
    private Boolean mailIncludeGuest;
    private Boolean mailIncludeUser;
    private Boolean mailIncludeManager;
    private Boolean mailIncludeAdmin;
}

