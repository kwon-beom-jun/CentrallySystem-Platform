// src/utils/session.ts
import LoginApi      from '@/api/auth/LoginApi'
import UsersApi      from '@/api/hrm/UsersApi'
import StylesApi     from '@/api/hrm/StylesApi'
import { useAuthStore } from '@/store/auth'
import { STYLE_CATEGORY, MAIN_CARD_STYLE, INFO_MOBILE_STYLE } from '@/constants'

/**
 * JWT 유효성 체크 및 authStore 관리
 * 로그인/회원가입 화면에서 사용
 * JWT가 무효하면 자동으로 authStore 리셋
 */
export async function checkJwtValidity() {
  const authStore = useAuthStore();
  
  try {
    await LoginApi.getMeWithoutErrorHandling();
    return true; // JWT 유효
  } catch (error) {
    // JWT 무효 또는 없으면 authStore 리셋
    authStore.$reset();
    return false;
  }
}

/**
 * 라우터 가드용 JWT 검증 함수
 * authStore가 있고 JWT가 유효하면 true 반환
 */
export async function checkAuthForRouterGuard() {
  const authStore = useAuthStore();
  
  // authStore에 데이터가 없으면 바로 false
  if (!authStore.isAuthenticated) {
    return false;
  }
  
  // JWT 유효성 체크
  return await checkJwtValidity();
}

/**  
 * JWT 유효성을 검증하고 사용자 정보를 갱신
 * 공통 인터셉터에서 에러 처리를 담당하므로 try-catch 없이 단순하게 처리
 */
export async function refreshSession() {
  const authStore = useAuthStore()

  // /auth/me 호출 (실패시 공통 인터셉터에서 자동으로 로그아웃 처리)
  const me = (await LoginApi.getMe()).data
  const users = await UsersApi.getUsersByIds([me.userId])
  const img = users[0]?.profileImgUrl ?? null
  
  // 사용자 스타일 조회
  let cardStyle = MAIN_CARD_STYLE.DEFAULT
  const styles = await StylesApi.getUserStyles(me.userId)
  cardStyle = styles[STYLE_CATEGORY.MAIN_CARD] || MAIN_CARD_STYLE.DEFAULT

  /* Pinia에 최신 정보 저장 */
  authStore.login({
    userId: me.userId,
    username: me.username,
    userEmail: me.userEmail,
    userProfileImgUrl: img,
    roles: me.roles,
    cardStyle: cardStyle,
  })
  return true
}
