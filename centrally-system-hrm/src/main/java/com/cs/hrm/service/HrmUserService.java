package com.cs.hrm.service;

import com.cs.hrm.controller.response.HrmUserBasicDto;
import com.cs.hrm.controller.response.HrmUserIdNameView;
import com.cs.hrm.controller.response.HrmUserWithRolesDto;
import com.cs.hrm.entity.HrmUser;
import com.cs.hrm.entity.HrmUserProfileImg;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 사용자 서비스 인터페이스
 */
public interface HrmUserService {

    /**
     * ID 목록으로 사용자 정보 조회
     * @param ids 사용자 ID 목록
     * @return 사용자 ID-이름 뷰 목록
     */
    List<HrmUserIdNameView> getUsersByIds(List<Integer> ids);

    /**
     * ID 목록으로 사용자 정보 조회 (비활성 포함)
     * @param ids 사용자 ID 목록
     * @return 사용자 ID-이름 뷰 목록
     */
    List<HrmUserIdNameView> getUsersByIdsIncludingDisabled(List<Integer> ids);

    /**
     * ID 목록으로 사용자 정보와 권한 조회 (비활성 포함)
     * @param ids 사용자 ID 목록
     * @return 사용자 권한 DTO 목록
     */
    List<HrmUserWithRolesDto> getUsersByIdsIncludingRoleAndDisabled(List<Integer> ids);

    /**
     * [활성 사용자만] ID 목록으로 사용자 정보와 권한 목록을 조회 (범용 방식)
     * @param ids 사용자 ID 목록
     * @return 사용자 권한 DTO 목록
     */
    List<HrmUserWithRolesDto> getActiveUsersByIdsIncludingRole(List<Integer> ids);

    /**
     * 전체 사용자 조회 (비활성 포함)
     * @return 사용자 목록
     */
    List<HrmUser> getUsersAll();

    /**
     * 간단 목록 반환
     * @return 사용자 기본 DTO 목록
     */
    List<HrmUserBasicDto> getUsersBasic();

    /**
     * ID 목록으로 간단 목록 반환
     * @param ids 사용자 ID 목록
     * @return 사용자 기본 DTO 목록
     */
    List<HrmUserBasicDto> getUsersBasicByIds(List<Integer> ids);

    /**
     * 일반 CRUD 예시 - 전체 사용자 조회
     * @return 사용자 목록
     */
    List<HrmUser> getUsers();

    /**
     * ID로 사용자 조회
     * @param id 사용자 ID
     * @return 사용자 엔티티
     */
    HrmUser getUserById(Integer id);

    /**
     * 사용자 정보 업데이트
     * @param id 사용자 ID
     * @param userData 사용자 데이터
     * @return 업데이트된 사용자 엔티티
     */
    HrmUser updateUser(Integer id, HrmUser userData);

    /**
     * 사용자 삭제
     * @param id 사용자 ID
     */
    void deleteUser(Integer id);

    /**
     * 사용자 정보 부분 업데이트
     * @param id 사용자 ID
     * @param updates 업데이트할 필드 Map
     * @param syncReceipt Receipt 동기화 여부
     * @return 업데이트된 사용자 엔티티
     */
    HrmUser patchUser(Integer id, Map<String, Object> updates, boolean syncReceipt);

    /**
     * 사용자 검색
     * @param kw 검색 키워드
     * @param limit 결과 제한 수
     * @param includeDisabled 비활성 사용자 포함 여부
     * @param service 서비스명
     * @param roles 권한 목록
     * @return 사용자 기본 DTO 목록
     */
    List<HrmUserBasicDto> searchUsers(String kw, int limit, boolean includeDisabled, String service, List<String> roles);

    /**
     * 프로필 이미지 저장
     * @param userId 사용자 ID
     * @param file 업로드 파일
     * @return 저장된 이미지 URL
     * @throws IOException 파일 처리 중 오류
     */
    String saveProfileImage(Integer userId, MultipartFile file) throws IOException;

    /**
     * 기본 프로필 이미지 조회 또는 생성
     * @return 프로필 이미지 엔티티
     */
    HrmUserProfileImg getOrCreateDefaultImg();

    /**
     * 사용자 카드 스타일 변경
     * @param userId 사용자 ID
     * @param cardStyle 카드 스타일 코드
     */
    void updateCardStyle(Integer userId, String cardStyle);
}
