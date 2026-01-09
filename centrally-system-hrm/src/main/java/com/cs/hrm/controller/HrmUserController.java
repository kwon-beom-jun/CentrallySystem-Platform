package com.cs.hrm.controller;

import com.cs.core.vo.event.AuthUserJoinedEvent;
import com.cs.hrm.controller.response.HrmUserBasicDto;
import com.cs.hrm.controller.response.HrmUserIdNameView;
import com.cs.hrm.controller.response.HrmUserWithRolesDto;
import com.cs.hrm.entity.HrmUser;
import com.cs.hrm.service.HrmUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * HRM 전용 사용자 API
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class HrmUserController {

    private final HrmUserService hrmUserService;

    // GET /users
    @GetMapping
    public ResponseEntity<List<HrmUser>> getUsers(
            @RequestParam(name = "includeDisabled", defaultValue = "false")
            boolean includeDisabled) {

        List<HrmUser> list = includeDisabled
            ? hrmUserService.getUsersAll()   // 유저 비활성화도 조회
            : hrmUserService.getUsers();     // 기존(활성만)
        return ResponseEntity.ok(list);
    }

    // GET /users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<HrmUser> getUserById(@PathVariable("id") Integer id) {
        HrmUser user = hrmUserService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
    
    // GET /users/basic → 전 직원 간단 목록
    @GetMapping("/basic")
    public ResponseEntity<List<HrmUserBasicDto>> getUsersBasic() {
        return ResponseEntity.ok(hrmUserService.getUsersBasic());
    }

    // POST /users/basic → 특정 ID들만 간단 목록
    @PostMapping("/basic")
    public ResponseEntity<List<HrmUserBasicDto>> getUsersBasicByIds(
            @RequestBody List<Integer> ids) {
        return ResponseEntity.ok(hrmUserService.getUsersBasicByIds(ids));
    }
    
    /**
     * [비활성 미포함] 여러 사용자 조회 (Bulk Lookup)
     * @param ids 사용자 ID 목록
     * @return 해당 ID에 해당하는 사용자 목록
     */
    @PostMapping("/bulk-lookup/active")
    public ResponseEntity<List<HrmUserIdNameView>> getUsersByIds(@RequestBody List<Integer> ids) {
        List<HrmUserIdNameView> users = hrmUserService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }

    /**
     * [비활성 포함] 여러 사용자 조회 (Bulk Lookup)
     * @param ids 사용자 ID 목록
     * @return 해당 ID에 해당하는 사용자 목록
     */
    @PostMapping("/bulk-lookup")
    public ResponseEntity<List<HrmUserIdNameView>> getUsersByIdsIncludingDisabled(@RequestBody List<Integer> ids) {
        List<HrmUserIdNameView> users = hrmUserService.getUsersByIdsIncludingDisabled(ids);
        return ResponseEntity.ok(users);
    }


    /**
     * [비활성 포함] 여러 사용자 조회 권한 포함 (Bulk Lookup)
     * @param ids 사용자 ID 목록
     * @return 해당 ID에 해당하는 사용자 목록
     */
    @PostMapping("/bulk-lookup/role")
    public ResponseEntity<List<HrmUserWithRolesDto>> getUsersByIdsIncludingRoleAndDisabled(@RequestBody List<Integer> ids) {
        List<HrmUserWithRolesDto> users = hrmUserService.getUsersByIdsIncludingRoleAndDisabled(ids);
        return ResponseEntity.ok(users);
    }
    
    /**
     * [활성 사용자만 & 범용 쿼리] 여러 사용자 조회 권한 포함
     */
    @PostMapping("/bulk-lookup/active-role")
    public ResponseEntity<List<HrmUserWithRolesDto>> getActiveUsersWithRoles(@RequestBody List<Integer> ids) {
        List<HrmUserWithRolesDto> users = hrmUserService.getActiveUsersByIdsIncludingRole(ids);
        return ResponseEntity.ok(users);
    }
    
    // POST /users
//    @PostMapping
//    public ResponseEntity<Void> createHrmUser(@RequestBody AuthUserJoinedEvent evt) {
//        hrmUserService.createUserFromAuthEvent(evt);
//        return ResponseEntity.ok().build();
//    }

    // 특정 사용자 일부 필드만 업데이트 (PATCH /users/{id})
    @PatchMapping("/{id}")
    public ResponseEntity<HrmUser> patchUser(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> updates,
            @RequestParam(name="syncReceipt", defaultValue="false") boolean syncReceipt
    ) {
        HrmUser updated = hrmUserService.patchUser(id, updates, syncReceipt);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
        hrmUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    /* ------------------- 프로필 이미지 업로드 ------------------- */
    @PostMapping("/{id}/profile-img")
    public ResponseEntity<?> uploadProfileImg(
            @PathVariable("id") Integer id,
            @RequestPart("file") MultipartFile file) throws IOException {

        String url = hrmUserService.saveProfileImage(id, file);
        return ResponseEntity.ok(Map.of("url", url));
    }
    
    
    /* 실시간 검색  ------------------------------------------------------ */
    @GetMapping("/search")
    public ResponseEntity<List<HrmUserBasicDto>> searchUsers(
            @RequestParam("kw") String kw,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "includeDisabled", defaultValue = "false") boolean includeDisabled,
            @RequestParam(value = "service", required = false) String service,
            @RequestParam(value = "roles", required = false) List<String> roles) {

        List<HrmUserBasicDto> list = hrmUserService.searchUsers(
                kw, limit, includeDisabled, service, roles);
        return ResponseEntity.ok(list);
    }

    /**
     * 사용자 카드 스타일 변경
     * PATCH /users/{userId}/card-style
     */
    @PatchMapping("/{userId}/card-style")
    public ResponseEntity<Void> updateCardStyle(
            @PathVariable("userId") Integer userId,
            @RequestBody java.util.Map<String, String> request) {
        String cardStyle = request.get("cardStyle");
        hrmUserService.updateCardStyle(userId, cardStyle);
        return ResponseEntity.ok().build();
    }

}
