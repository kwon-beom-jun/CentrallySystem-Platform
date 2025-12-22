<template>
  <header class="navbar">
    <div class="navbar-inner">
      <!-- 로고 -->
      <RouterLink to='/' class="logo">
        <img class="logo-desktop" src="/img/common/logo/centrally-logo_2.png" alt="logo" />
        <img class="logo-mobile" src="/img/common/logo/centrally-logo2_2.png" alt="logo-mobile" />
        <img class="logo-compact" src="/img/common/logo/centrally_logo_3.png" alt="logo-compact" />
      </RouterLink>

      <!-- 확성기 아이콘 -->
      <div class="megaphone-icon">
        <i class="ri-megaphone-line"></i>
      </div>

      <!-- 유저 영역 -->
      <div class="user-box">
        <LanguageSwitcher mode="front-header-mode" />
      </div>
    </div>
  </header>
</template>

<script setup>
import { RouterLink } from 'vue-router';
import LanguageSwitcher from '@/components/common/LanguageSwitcher.vue';
</script>

<style scoped>
/* ───── 헤더 기본 ───── */
.navbar {
  height: 65px;
  background: #fff;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.06);
  position: fixed !important;
  inset: 0 0 auto 0;
  width: 100%;
  min-width: 360px !important;
  padding: 0;
  z-index: 1015;
}

.navbar-inner {
  width: 100%;
  height: 100%;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 15px; /* 간격 줄임 */
}

/* 로고 */
.logo-desktop {
  width: 170px;
}

.logo-mobile {
  display: none;
  width: 170px;
}
.logo-compact {
  display: none;
}

/* ───── 유저 영역 ───── */
.user-box {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0;
  font-size: 0.875rem;
}

/* 확성기 아이콘 */
.megaphone-icon {
  margin-left: 0;
  display: flex;
  align-items: center;
  align-self: flex-end;
  padding-bottom: 5px;
  font-weight: 900 !important;
  color: #888;
}

.megaphone-icon i {
  font-size: 30px; /* 2배 크기 (20px -> 40px) */
}

/* ───── 반응형 ───── */
@media (max-width: 1300px) {
  /* ───── 유저 영역 ───── */
  .user-box {
    padding: 0px 0px 0px 0px;
    gap: 7px;
  }
  
  /* 로고 */
  .logo-desktop {
    display: none;
    width: 170px;
  }
  
  .logo-mobile {
    display: block;
    width: 80px;
  }
  
  .navbar-inner {
    padding: 0 18px;
    gap: 5px;
  }

  .megaphone-icon {
    margin-left: 0;
    padding-bottom: 10px;
  }

  .megaphone-icon i {
    font-size: 25px; /* 모바일에서도 2배 크기 (18px -> 36px) */
  }
  
}

@media (max-width: 500px) {
  .logo-mobile {
    display: none;
  }

  .logo-compact {
    display: block;
    width: 30px !important;
    margin-right: 3px;
  }
}
</style>

