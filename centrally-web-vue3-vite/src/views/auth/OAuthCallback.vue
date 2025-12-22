<template>
  <div />
</template>

<script setup>
import { onBeforeMount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ROUTES } from '@/config/menuConfig'
import { useAuthStore } from '@/store/auth'
import LoginApi from '@/api/auth/LoginApi'
import UsersApi from '@/api/hrm/UsersApi'
import { useI18n } from 'vue-i18n'

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const { t } = useI18n();

// 페이지 뜨기전 로딩
onBeforeMount(async () => {
  
  // 피니아 로그인 정보 제거
  authStore.logout()

  // 쿼리 파라미터(error) 확인
  // 기존 기본 로그인이 Auth 서버 시큐리티에 저장되어있어서 중복로그인으로 판정 재로그인 해야함
  // 일반 로그인은 RestAPI로 아이디, 비번을 보내줘서 시큐리티에 넣어주기에 문제없음
  const errorParam = route.query.error;
  if (errorParam) {
    // 만약 error=alreadyLoggedIn 이면 사용자에게 메시지
    // 이후 로그인 페이지로 이동
    if (errorParam === 'alreadyLoggedIn') {
      const errorMsg = t('auth.oauth.duplicateLogin')
      router.push(ROUTES.LOGIN + `?error=${errorMsg}`)
    } else {
      const errorMsg = `[${t('auth.oauth.failed')}]\n${errorParam}`
      router.push(ROUTES.LOGIN + `?error=${errorMsg}`)
    }
    return // 아래 로직은 더 이상 진행 X
  }

  // (1) 소셜 로그인 성공 후, 서버에서 JWT 쿠키가 이미 발급되었을 것
  // (2) /me 호출하여 사용자 정보 가져오기
  const response = await LoginApi.getMe();
  
  // HRM 프로필 먼저 로드 (단건 조회 1회)
  const { useHrmStore } = await import('@/store/hrm');
  const hrmStore = useHrmStore();
  await hrmStore.loadMyProfileByUserId(response.data.userId);
  
  // HRM 프로필에서 이미지 URL과 cardStyle 가져오기
  const imgUrl = hrmStore.getMyProfile?.profileImgUrl || null;
  const cardStyle = hrmStore.getMyProfile?.cardStyle || 'default';

  authStore.login({
    userId: response.data.userId,
    username: response.data.username,
    userEmail: response.data.userEmail,
    userProfileImgUrl: imgUrl,
    roles: response.data.roles,
    cardStyle: cardStyle,
  });
  
  // (3) 메인 페이지로 이동
  router.push(ROUTES.MAIN);
});
</script>

<style scoped>
</style>
