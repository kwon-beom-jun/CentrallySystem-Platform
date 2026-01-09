<template>
  <div>
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle
        :title="$t('menu.info.scheduleStatistics')"
        subtitle="Schedule Statistics"
        icon="ri-bar-chart-line"
        desktopMarginBottom="20px"
      />

      <!-- 향후 개발을 위한 플레이스홀더 -->
      <div class="placeholder-container">
        <div class="placeholder-content">
          <i class="ri-bar-chart-line placeholder-icon"></i>
          <h3>{{ $t('menu.info.scheduleStatistics') }}</h3>
          <p>일정 통계 기능이 곧 제공될 예정입니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n';
import PageTitle from '@/components/common/title/PageTitle.vue';

const { t } = useI18n();
</script>

<style scoped>
.placeholder-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 40px 20px;
}

.placeholder-content {
  text-align: center;
  color: var(--theme-text-secondary);
}

.placeholder-icon {
  font-size: 4rem;
  color: var(--theme-text-muted);
  margin-bottom: 20px;
}

.placeholder-content h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--theme-text-primary);
  margin-bottom: 12px;
}

.placeholder-content p {
  font-size: 1rem;
  color: var(--theme-text-secondary);
}

/* 모바일에서 글자 크기 작게 */
@media (max-width: 650px) {
  .placeholder-content h3 {
    font-size: 1rem !important;
  }
  .placeholder-content p {
    font-size: 0.75rem !important;
  }
  .placeholder-icon {
    font-size: 3rem !important;
  }
}
</style>

