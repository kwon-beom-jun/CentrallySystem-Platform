import { defineStore } from 'pinia';
import { useAuthStore } from '@/store/auth';
import HrmUsersApi from '@/api/hrm/UsersApi';
import StylesApi from '@/api/hrm/StylesApi';
import { STYLE_CATEGORY, MAIN_CARD_STYLE, INFO_MOBILE_STYLE } from '@/constants';

export const useHrmStore = defineStore('hrm', {
    // HRM 서비스 전용 상태 (필요한 경우 추가)
    state: () => ({
        // 로그인 사용자 HRM 프로필(보안 민감 정보 제외)
        /**
         * 로그인 사용자의 HRM 프로필 데이터(민감 정보 제외)
         *  - name, email, phoneNumber, birth, joiningDate
         *  - employmentType( id, name ), departmentName, teamName, positionName
         *  - profileImgUrl
         */
        myProfile: null,
    }),
    // 게터를 통해 HRM 관리자 여부 계산
    getters: {
        // authStore의 역할(roles)을 확인하여, HRM 관리자인지 여부를 반환
        isHrmAdmin() {
            const authStore = useAuthStore();
            return authStore.getRoles.includes('ROLE_GATE_SYSTEM') ||
                   authStore.getRoles.includes('ROLE_HRM_MANAGER');
        },
        /** 내 HRM 프로필 반환 */
        getMyProfile: (state) => state.myProfile,
        /** 정규직 여부 (employmentTypeId === 1 기준) */
        isRegularEmployee: (state) => {
            const typeId = state.myProfile?.employmentTypeId ?? null;
            return Number(typeId) === 1;
        },
    },
    // HRM 관련 액션 (필요한 경우 추가)
    actions: {
        // 예시: hrmAction() { ... }

        /**
         * 내 HRM 프로필 상태 초기화
         */
        clearMyProfile() {
            this.myProfile = null;
        },

        /**
         * 내 HRM 프로필을 강제로 세팅
         * @param {object} profile - 가공된 HRM 프로필 객체
         */
        setMyProfile(profile) {
            this.myProfile = profile ?? null;
        },

        /**
         * 사용자 ID로 HRM 사용자 정보를 조회하여 민감정보 제외 후 스토어에 저장
         * @param {number|string} userId - 조회할 사용자 ID
         */
        async loadMyProfileByUserId(userId) {
            if (!userId) return;
            // 백엔드 공통 에러 핸들러 사용: 로컬 try/catch 지양
            const res = await HrmUsersApi.getUserById(userId);
            const user = res?.data;
            if (!user) {
                this.myProfile = null;
                return;
            }
            
            // 사용자 스타일 조회
            let cardStyle = MAIN_CARD_STYLE.DEFAULT;
            let infoMobileStyle = INFO_MOBILE_STYLE.LIGHT;
            const styles = await StylesApi.getUserStyles(userId);
            cardStyle = styles[STYLE_CATEGORY.MAIN_CARD] || MAIN_CARD_STYLE.DEFAULT;
            infoMobileStyle = styles[STYLE_CATEGORY.INFO_MOBILE] || INFO_MOBILE_STYLE.LIGHT;
            
            const filtered = {
                userId: user.userId,
                name: user.name,
                email: user.email,
                phoneNumber: user.phoneNumber ?? null,
                birth: user.birth ?? null,
                joiningDate: user.joiningDate ?? null,
                employmentTypeId: (user.employmentTypeId ?? user.employmentType?.employmentTypeId ?? 0),
                employmentTypeName: user.employmentType?.employmentTypeName ?? null,
                departmentId: user.team?.department?.departmentId ?? null,
                departmentName: user.team?.department?.departmentName ?? null,
                teamId: user.team?.teamId ?? null,
                teamName: user.team?.teamName ?? null,
                positionName: user.position?.positionName ?? null,
                profileImgUrl: user.profileImg?.fileUrl ?? null,
                cardStyle: cardStyle,
                infoMobileStyle: infoMobileStyle,
            };
            this.myProfile = filtered;
        },
    }
,
    // Pinia 상태 퍼시스트
    persist: true,
});
