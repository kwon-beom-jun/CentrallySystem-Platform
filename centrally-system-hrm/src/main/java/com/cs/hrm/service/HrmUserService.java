package com.cs.hrm.service;

import com.cs.core.vo.event.AuthUserJoinedEvent;
import com.cs.hrm.controller.response.HrmUserBasicDto;
import com.cs.hrm.controller.response.HrmUserIdNameView;
import com.cs.hrm.controller.response.HrmUserWithRolesDto;
import com.cs.hrm.controller.response.HrmUserWithRolesView;
import com.cs.hrm.entity.HrmPositions;
import com.cs.hrm.entity.HrmUser;
import com.cs.hrm.entity.HrmUserProfileImg;
import com.cs.hrm.kafka.producer.HrmDeptTeamEventProducer;
import com.cs.core.kafka.event.hrm.UserDeptTeamChangedEvent;
import com.cs.hrm.repository.HrmPositionsRepository;
import com.cs.hrm.repository.HrmTeamsRepository;
import com.cs.hrm.repository.HrmUserProfileImgRepository;
import com.cs.hrm.repository.HrmUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HrmUserService {

    private final HrmUserRepository hrmUserRepository;
    private final HrmStyleService hrmStyleService;
    private final HrmPositionsRepository hrmPositionsRepository;
    private final HrmTeamsRepository hrmTeamsRepository;
    private final HrmUserProfileImgRepository profileImgRepository;
    // 비밀번호 암호화용
    private final PasswordEncoder passwordEncoder;

    private final HrmDeptTeamEventProducer hrmDeptTeamEventProducer;

    @Value("${file.upload.path}")
    private String uploadRoot;

    @Value("${file.upload.url}")
    private String uploadUrl;
    
    @Value("${hrm.default.profile.img}")
    private String defaultProfileImg;

    /**
     * hrm.default.profile.img 프로퍼티가
     *   /img/profile/random/001~009.png 형태처럼 '~' 기호로 범위를 지정할 경우,
     *   1) 파일명 부분(001~009)을 추출하여 시작·끝 값을 구하고
     *   2) 범위 안에서 무작위 숫자를 선택한 뒤, 자릿수(0 패딩)를 유지해 문자열로 변환합니다.
     *   3) 원본 프로퍼티 문자열 중 범위 부분을 선택된 숫자로 치환하여 최종 URL을 반환합니다.
     * '~' 기호가 없으면 단일 파일 경로이므로 프로퍼티 값을 그대로 반환합니다.
     */
    private String resolveRandomImgUrl() {
        if (defaultProfileImg.contains("~")) {
            int lastSlash = defaultProfileImg.lastIndexOf('/');
            int dotIdx = defaultProfileImg.lastIndexOf('.');
            if (lastSlash != -1 && dotIdx != -1) {
                String filePart = defaultProfileImg.substring(lastSlash + 1, dotIdx); // e.g. 001~009
                String[] parts = filePart.split("~");
                if (parts.length == 2) {
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    int random = new java.util.Random().nextInt(end - start + 1) + start;
                    String randomStr = String.format("%0" + parts[0].length() + "d", random);
                    return defaultProfileImg.replace(filePart, randomStr);
                }
            }
        }
        return defaultProfileImg;
    }
    
    /**
     * Kafka 이벤트(AuthUserJoinedEvent)를 받아 HRM Users 테이블에 저장
     */
//    @Transactional
//    public void createUserFromAuthEvent(AuthUserJoinedEvent event) {
//        // HrmUser 엔티티 생성
//        HrmUser newHrmUser = HrmUser.builder()
//                .userId(event.getUserId())       // AuthUser와 동일 ID 사용 or 별도 오토증가
//                .email(event.getEmail())
//                .name(event.getName())
//                .phoneNumber(event.getPhone())
//                .profileImg(getOrCreateDefaultImg())   // 기본 이미지 지정
//                .build();
//
//        hrmUserRepository.save(newHrmUser);
//    }

    @Transactional(readOnly = true)
    public List<HrmUserIdNameView> getUsersByIds(List<Integer> ids) {
    	// 변경 전
        // return hrmUserRepository.findAllById(ids);
        // 변경 후 (DB 단계에서 name ASC, email ASC 로 정렬)
        return hrmUserRepository.getUsersByIds(ids);
    }
    
    @Transactional(readOnly = true)
    public List<HrmUserIdNameView> getUsersByIdsIncludingDisabled(List<Integer> ids) {
    	// 변경 전
    	// return hrmUserRepository.findAllById(ids);
    	// 변경 후 (DB 단계에서 name ASC, email ASC 로 정렬)
    	return hrmUserRepository.getUsersByIdsIncludingDisabled(ids);
    }
    
    @Transactional(readOnly = true)
    public List<HrmUserWithRolesDto> getUsersByIdsIncludingRoleAndDisabled(List<Integer> ids) {
        // 1. Repository에서 그룹화되지 않은 데이터를 조회합니다.
        List<HrmUserWithRolesView> flatResults = hrmUserRepository.findUsersWithRolesByIdsIncludingDisabled(ids);

        // 2. Java Stream API를 사용하여 사용자 ID와 이름으로 그룹화하고,
        //    각 그룹의 권한들을 리스트로 수집합니다.
        Map<String, List<String>> userToRolesMap = flatResults.stream()
            .collect(Collectors.groupingBy(
                view -> view.getUserId() + ":" + view.getName(), // Key: "userId:name"
                Collectors.mapping(HrmUserWithRolesView::getRoles, Collectors.toList())
            ));
            
        // 3. 그룹화된 Map을 최종 DTO 리스트 형태로 변환합니다.
        return userToRolesMap.entrySet().stream()
            .map(entry -> {
                String[] parts = entry.getKey().split(":");
                Integer userId = Integer.parseInt(parts[0]);
                String name = parts[1];
                // role이 null인 경우(권한 없는 사용자)를 대비해 필터링
                List<String> roles = entry.getValue().stream()
                                          .filter(Objects::nonNull)
                                          .collect(Collectors.toList());
                return new HrmUserWithRolesDto(userId, name, roles);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * [활성 사용자만] ID 목록으로 사용자 정보와 권한 목록을 조회 (범용 방식)
     */
    @Transactional(readOnly = true)
    public List<HrmUserWithRolesDto> getActiveUsersByIdsIncludingRole(List<Integer> ids) {
        // 1. Repository에서 그룹화되지 않은 데이터를 조회합니다.
        List<HrmUserWithRolesDto> flatResults = hrmUserRepository.findActiveUsersWithRolesByIds(ids);

        // 2. Java Stream API를 사용하여 사용자 ID로 그룹화하고, 권한들을 리스트로 합칩니다.
        Map<Integer, List<String>> rolesByUserId = flatResults.stream()
            .collect(Collectors.groupingBy(
                HrmUserWithRolesDto::getUserId,
                Collectors.mapping(dto -> dto.getRoles().isEmpty() ? null : dto.getRoles().get(0), Collectors.toList())
            ));

        // 3. 그룹화된 Map을 기반으로 사용자 이름 정보를 합쳐 최종 DTO 리스트를 생성합니다.
        Map<Integer, String> nameByUserId = flatResults.stream()
            .collect(Collectors.toMap(HrmUserWithRolesDto::getUserId, HrmUserWithRolesDto::getName, (name1, name2) -> name1));

        return nameByUserId.entrySet().stream()
            .map(entry -> {
                Integer userId = entry.getKey();
                String name = entry.getValue();
                List<String> roles = rolesByUserId.get(userId).stream()
                                                  .filter(Objects::nonNull)
                                                  .collect(Collectors.toList());
                return new HrmUserWithRolesDto(userId, name, roles);
            })
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<HrmUser> getUsersAll() {
        return hrmUserRepository.findAllIncludingDisabled();
    }

    /* 간단 목록 반환 */
    @Transactional(readOnly = true)
    public List<HrmUserBasicDto> getUsersBasic() {
        return hrmUserRepository.findAllBasic();
    }

    @Transactional(readOnly = true)
    public List<HrmUserBasicDto> getUsersBasicByIds(List<Integer> ids) {
        return hrmUserRepository.findBasicByIds(ids);
    }
    
    /**
     * 일반 CRUD 예시
     */
    @Transactional(readOnly = true)
    public List<HrmUser> getUsers() {
        return hrmUserRepository.findAll(
                Sort.by(
                    Sort.Order.asc("name"),
                    Sort.Order.asc("email")
                )
        );
    }

    @Transactional(readOnly = true)
    public HrmUser getUserById(Integer id) {
        return hrmUserRepository.findById(id).orElse(null);
    }

//    public HrmUser createHrmUser(HrmUser user) {
//        return hrmUserRepository.save(user);
//    }

    // FIXME : 비밀번호 변경 시 AUTH User 테이블의 비밀번호도 업데이트 되야함
    @Transactional
    public HrmUser updateUser(Integer id, HrmUser userData) {
        HrmUser existing = hrmUserRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setEmail(userData.getEmail());
            existing.setName(userData.getName());
            
            // 직책 업데이트
            if (userData.getPosition() != null) {
                Integer positionId = userData.getPosition().getPositionId();
                if (positionId != null) {
                    // positions 테이블에서 해당 positionId로 검색
                    HrmPositions newPosition = hrmPositionsRepository.findById(positionId)
                            .orElse(null); 
                    existing.setPosition(newPosition);
                }
            }
            return hrmUserRepository.save(existing);
        }
        return null;
    }

    // FIXME : 유저 삭제 시 Auth 유저 관련 테이블 데이터도 삭제되야함
    @Transactional
    public void deleteUser(Integer id) {
        hrmUserRepository.deleteById(id);
    }

    @Transactional
    public HrmUser patchUser(Integer id, 
				    		 Map<String, Object> updates,
				             boolean syncReceipt) {

        /* 활성화 플래그가 있으면 → 비활성 레코드까지 포함해 조회 */
        Optional<HrmUser> optional = updates.containsKey("enabled")
            ? hrmUserRepository.findByIdIncludingDisabled(id)
            : hrmUserRepository.findById(id);
        
        if (optional.isEmpty()) {
            return null;
        }
        HrmUser existingUser = optional.get();
        
        /* ── 수정 전 부서/팀 스냅샷 ─────────────────────── */
        Integer oldDeptId = existingUser.getTeam() != null
                ? existingUser.getTeam().getDepartment().getDepartmentId()
                : null;
        Integer oldTeamId = existingUser.getTeam() != null
                ? existingUser.getTeam().getTeamId()
                : null;
        
        /* --- 활성/비활성 토글 처리 ------------------------------------ */
        if (updates.containsKey("enabled")) {
            boolean enabled = Boolean.parseBoolean(updates.get("enabled").toString());
            existingUser.setEnabled(enabled);
            existingUser.setDeletedAt(enabled ? null : java.time.LocalDateTime.now());
        }

        if (updates.containsKey("birth")) {
            existingUser.setBirth((String) updates.get("birth"));
        }
        if (updates.containsKey("phoneNumber")) {
            existingUser.setPhoneNumber((String) updates.get("phoneNumber"));
        }
        if (updates.containsKey("addressDetail")) {
            existingUser.setAddressDetail((String) updates.get("addressDetail"));
        }
        if (updates.containsKey("address")) {
            existingUser.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("zipCode")) {
            // zipCode는 int이므로 변환 필요
            Object zipObj = updates.get("zipCode");
            if (zipObj instanceof Number) {
                existingUser.setZipCode(((Number) zipObj).intValue());
            } else {
                // 문자열이라면 parseInt 시도
                existingUser.setZipCode(Integer.parseInt(zipObj.toString()));
            }
        }

        /* 프로필 이미지 ID (null 가능) */
        if (updates.containsKey("profileImgId")) {
            Object val = updates.get("profileImgId");
            // null 또는 빈 값이면 → 기본 이미지  
            if (val == null || val.toString().isBlank()) {  
                existingUser.setProfileImg(getOrCreateDefaultImg());  
            } else {  
                /* 숫자·문자 구분 없이 ID 를 Integer 로 변환 → 이미지 조회 */  
                Integer imgId = Integer.valueOf(val.toString());  
                HrmUserProfileImg img = profileImgRepository.findById(imgId).orElse(null);  
                existingUser.setProfileImg(img);             // 여기서만 한 번 호출  
            }  
        }
        // 팀(teamId)
        //  - user.team → ManyToOne(HrmTeams), HrmTeams.department → ManyToOne(HrmDepartments)
        if (updates.containsKey("teamId")) {
            Object teamIdObj = updates.get("teamId");
            if (teamIdObj != null) {
                Integer teamId = Integer.valueOf(teamIdObj.toString());
                // 0 이면 "미지정"으로 간주 → 팀 null
                if (teamId == 0) {
                    existingUser.setTeam(null);
                } else {
                    // 팀 찾기
                    existingUser.setTeam(hrmTeamsRepository.findById(teamId).orElse(null));
                }
            } else {
                // 명시적으로 null
                existingUser.setTeam(null);
            }
        }
        if (updates.containsKey("joiningDate")) {
            // Date/LocalDate 처리 (SQL Date / LocalDate 등 상황에 맞게)
            Object val = updates.get("joiningDate");
            if (val != null) {
            	// 예: '2022-01-01' 형식
            	// 선택적으로 변환 로직 추가
            	existingUser.setJoiningDate(java.sql.Date.valueOf(val.toString()));
			}
        }
        if (updates.containsKey("leavingDate")) {
            // Date/LocalDate 처리 (SQL Date / LocalDate 등 상황에 맞게)
            Object val = updates.get("leavingDate");
            if (val != null) {
            	// 예: '2022-01-01' 형식
            	// 선택적으로 변환 로직 추가
            	existingUser.setLeavingDate(java.sql.Date.valueOf(val.toString()));
			}
        }
        if (updates.containsKey("employmentTypeId")) {
            Object v = updates.get("employmentTypeId");
            int num = (v instanceof Number) ? ((Number)v).intValue() : Integer.parseInt(String.valueOf(v));
            // 0/1/2 범위 가정 → enum 매핑
            existingUser.setEmploymentTypeId(com.cs.hrm.enums.EmploymentType.values()[num]);
        }
        // positionId가 넘어오면 처리
        if (updates.containsKey("positionId")) {
            Object posIdObj = updates.get("positionId");
            if (posIdObj.equals("")) {
            	existingUser.setPosition(null);
			} else if (posIdObj != null) {
                Integer posId = Integer.valueOf(posIdObj.toString());
                HrmPositions foundPos = hrmPositionsRepository.findById(posId)
                        .orElse(null);
                existingUser.setPosition(foundPos);
            }
        }
        // dispatchLocations가 넘어오면 처리
        if (updates.containsKey("dispatchLocations")) {
            existingUser.setDispatchLocations((String) updates.get("dispatchLocations"));
        }
        

        /* ── 변경 후 부서/팀 ─────────────────────────────── */
        Integer newDeptId   = existingUser.getTeam() != null
                ? existingUser.getTeam().getDepartment().getDepartmentId()
                : null;
        String  newDeptName = existingUser.getTeam() != null
                ? existingUser.getTeam().getDepartment().getDepartmentName()
                : null;
        Integer newTeamId   = existingUser.getTeam() != null
                ? existingUser.getTeam().getTeamId()
                : null;
        String  newTeamName = existingUser.getTeam() != null
                ? existingUser.getTeam().getTeamName()
                : null;

        /* ── 부서‧팀 값이 달라졌고 syncReceipt=true 면 호출 ── */
        if (syncReceipt && (!Objects.equals(oldDeptId, newDeptId)
                         || !Objects.equals(oldTeamId, newTeamId))) {
            UserDeptTeamChangedEvent evt = new UserDeptTeamChangedEvent(
                    existingUser.getUserId(),
                    newDeptId, newDeptName,
                    newTeamId, newTeamName);
            hrmDeptTeamEventProducer.sendUserDeptTeamChangedEvent(evt);
        }
        
        return hrmUserRepository.save(existingUser);
    }
    
    
    @Transactional(readOnly = true)
    public List<HrmUserBasicDto> searchUsers(String  kw,
                                             int     limit,
                                             boolean includeDisabled,
                                             String  service,
                                             List<String> roles) {

        Pageable pg  = PageRequest.of(0, limit);
        String   like = "%" + kw.toLowerCase() + "%";

        return hrmUserRepository.searchUsers(
                like, includeDisabled, service, roles == null || roles.isEmpty() ? null : roles, pg);
    }

    /* ===============================================
     *  프로필 이미지 저장
     *  =============================================== */
    @Transactional
    public String saveProfileImage(Integer userId, MultipartFile file) throws IOException {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("file is empty");
//        }
//        
//        HrmUser user = hrmUserRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("user not found"));

    	/* 업로드 파일이 아예 없으면 ⇒ 기본 이미지로 교체 후 반환 */
        /* null을 사용한다면 이부분에서 수정하면 될듯함 */
	    if (file == null || file.isEmpty()) {
	        HrmUser user = hrmUserRepository.findById(userId)
	                .orElseThrow(() -> new IllegalArgumentException("user not found"));
	
	        HrmUserProfileImg def = getOrCreateDefaultImg();
	        user.setProfileImg(def);
			hrmUserRepository.save(user);
			return defaultProfileImg;          // 프런트에 기본 URL 반환
	    }

	    /* 실제 파일이 존재하면 ⇒ 아래 기존 업로드 로직 진행 */
	    HrmUser user = hrmUserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found"));

        // 저장 폴더: {uploadRoot}/profile
        String subDir = uploadRoot + File.separator + "profile";
        File dir = new File(subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일명: UUID + 확장자
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String newName = UUID.randomUUID() + ext;
        Path target = Path.of(dir.getAbsolutePath(), newName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // URL 구성
        String url = uploadUrl + "/profile/" + newName;

        // DB 저장
        com.cs.hrm.entity.HrmUserProfileImg img = com.cs.hrm.entity.HrmUserProfileImg.builder()
                .fileUrl(url)
                .fileType(file.getContentType())
                .fileOriginName(original)
                .fileName(newName)
                .uploadDate(new java.sql.Date(System.currentTimeMillis()))
                .build();
        profileImgRepository.save(img);

        user.setProfileImg(img);
        hrmUserRepository.save(user);

        return url;
    }

    @Transactional
    public HrmUserProfileImg getOrCreateDefaultImg() {
        String url = resolveRandomImgUrl();
        return profileImgRepository.findByFileUrl(url)
            .orElseGet(() -> {
                try {
                    HrmUserProfileImg def = HrmUserProfileImg.builder()
                            .fileUrl(url)
                            .fileType("image/png")
                            .fileOriginName("default.png")
                            .fileName("default.png")
                            .uploadDate(new Date(System.currentTimeMillis()))
                            .build();
                    return profileImgRepository.saveAndFlush(def);  // 즉시 INSERT
                } catch (DataIntegrityViolationException e) {       // 동시 생성 충돌
                    return profileImgRepository.findByFileUrl(url).orElseThrow();
                }
            });
    }

    /**
     * 사용자 카드 스타일 변경
     */
    @Transactional
    public void updateCardStyle(Integer userId, String cardStyle) {
        hrmStyleService.updateMainCardStyle(userId, cardStyle);
    }
}
