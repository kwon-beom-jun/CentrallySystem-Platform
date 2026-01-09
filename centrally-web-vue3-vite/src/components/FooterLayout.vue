<template>
  <footer class="footer">
    <div class="container">
      <!-- Updated footer-info for right alignment -->
      <div class="footer-info">
        <span>{{ $t('common.footer.copyright') }}</span>
        <!-- <img src="/img/common/header/header-logo.png" alt="Logo" class="footer-logo" /> -->
      </div>
    </div>
  </footer>
</template>

<script setup>
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<style>
.footer {
  background-color: var(--theme-header-bg);
  text-align: center;
  padding: 10px 0 10px 310px;
  box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1);
  position: fixed;
  width: 100%;
  bottom: 0;
  z-index: 10 !important;
  transition: padding-left 0.3s ease;
}

/* 세컨더리 사이드바가 접혔을 때 */
@media (min-width: 1301px) {
  .sidebar-collapsed .footer {
    padding-left: 120px;
  }
}

.footer-info {
  align-items: center;
  justify-content: flex-end;
  color: var(--theme-text-primary);
  font-size: 0.75rem;
}

.footer-logo {
  width: 30px;
  margin-left: 10px;
}

/* 반응형 스타일 */
@media (max-width: 650px) {
  .footer-info {
    font-size: 0.5rem; /* 모바일에서 글자 크기를 작게 */
  }

  .footer-logo {
    width: 25px; /* 모바일에서 로고 크기 비율에 맞춰 작게 */
    height: 10px; /* 모바일에서 로고 크기 비율에 맞춰 작게 */
  }
}
</style>
