<template>
  <div class="recent-activity-card box-shadow-gray" @click="goToHistory">
    <div class="card-header">
      <i class="ri-history-line"></i>
      <span>{{ $t('common.sidebar.activity.recentHistory') }}</span>
    </div>
    
    <!-- 로딩 상태 -->
    <div v-if="loading" class="loading-state">
      <i class="ri-loader-4-line spinner"></i>
      <span>{{ $t('common.message.loading') }}</span>
    </div>
    
    <!-- 데이터 없음 -->
    <div v-else-if="!loading && activities.length === 0" class="empty-state">
      <i class="ri-inbox-line"></i>
      <span>{{ $t('common.sidebar.activity.noHistory') }}</span>
    </div>
    
    <!-- 활동 이력 목록 -->
    <div v-else class="activity-list">
      <div 
        v-for="activity in activities" 
        :key="activity.activityId" 
        class="activity-item"
      >
        <div 
          class="activity-icon"
          :style="{ 
            background: `linear-gradient(135deg, ${getMenuIcon(activity).color}15 0%, ${getMenuIcon(activity).color}25 100%)` 
          }"
        >
          <i 
            :class="getMenuIcon(activity).icon"
            :style="{ color: getMenuIcon(activity).color }"
          ></i>
        </div>
        <div class="activity-content">
          <div class="activity-title">{{ getFunctionLabel(activity.function) }}</div>
          <div class="activity-meta">
            <span 
              class="activity-menu"
              :style="{ 
                background: `${getMenuIcon(activity).color}10`,
                color: getMenuIcon(activity).color,
                borderColor: `${getMenuIcon(activity).color}30`
              }"
            >
              {{ getMenuLabel(activity.menu) }}
            </span>
            <span class="activity-time">{{ formatTime(activity.timestamp) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ROUTES } from '@/config/menuConfig';
import ActivityLogApi from '@/api/system/ActivityLogApi';
import { getMenuStyle } from '@/config/activityConfig';
import { translateMenuKey, translateFunctionKey } from '@/utils/i18nDisplay';

const { t } = useI18n();

/**
 * 활동 이력 데이터
 */
const activities = ref([]);
const loading = ref(true);

/**
 * 최신 활동 이력 로드
 */
const loadRecentActivities = async () => {
  try {
    loading.value = true;
    const response = await ActivityLogApi.getRecentActivities(5);
    activities.value = response.data || [];
  } catch (error) {
    console.error('최근 활동 이력 조회 실패:', error);
    activities.value = [];
  } finally {
    loading.value = false;
  }
};

/**
 * 카드 클릭 → 사용자 이력 페이지로 이동
 */
const router = useRouter();
function goToHistory() {
  router.push(ROUTES.USER.PERSONAL_HISTORY);
}

/**
 * 메뉴명을 기반으로 서비스 아이콘 반환
 * activityConfig.js에서 중앙 관리되는 스타일 사용
 */
const getMenuIcon = (activity) => {
  const menu = activity.menu || '';
  return getMenuStyle(menu);
};

/**
 * 기능명 번역
 */
const getFunctionLabel = (functionKey) => {
  const translated = translateFunctionKey(functionKey, t);
  return translated || t('common.sidebar.activity.unknownAction');
};

/**
 * 메뉴명 번역
 */
const getMenuLabel = (menuKey) => {
  const translated = translateMenuKey(menuKey, t);
  return translated || t('common.sidebar.activity.unclassified');
};

/**
 * 시간 포맷팅 (상대 시간)
 */
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  try {
    const now = new Date();
    const date = new Date(timestamp);
    const diffMs = now - date;
    const diffSecs = Math.floor(diffMs / 1000);
    const diffMins = Math.floor(diffSecs / 60);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffSecs < 60) {
      return t('common.sidebar.activity.timeAgo.justNow');
    } else if (diffMins < 60) {
      return t('common.sidebar.activity.timeAgo.minutesAgo', { minutes: diffMins });
    } else if (diffHours < 24) {
      return t('common.sidebar.activity.timeAgo.hoursAgo', { hours: diffHours });
    } else if (diffDays < 7) {
      return t('common.sidebar.activity.timeAgo.daysAgo', { days: diffDays });
    } else {
      // 7일 이상은 날짜 표시 (MM-DD HH:mm)
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${month}-${day} ${hours}:${minutes}`;
    }
  } catch (error) {
    return timestamp;
  }
};

/**
 * 컴포넌트 마운트 시 데이터 로드
 */
onMounted(() => {
  loadRecentActivities();
});
</script>

<style scoped>
.recent-activity-card {
  margin: 12px 5px 12px 15px;
  background: var(--theme-card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.recent-activity-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-1px);
}

/* 카드 헤더 */
.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  background: var(--theme-bg-secondary);
  border-bottom: 1px solid var(--theme-border);
  font-size: 11px;
  font-weight: 600;
  color: var(--theme-text-primary);
}

.card-header > i {
  font-size: 13px;
  color: #6c757d;
}

/* 로딩 상태 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 12px;
  gap: 6px;
  color: #a0aec0;
  font-size: 10px;
  cursor: pointer;
}

.spinner {
  font-size: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 빈 상태 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 12px;
  gap: 6px;
  color: #a0aec0;
  font-size: 10px;
  cursor: pointer;
}

.empty-state > i {
  font-size: 26px;
  opacity: 0.5;
}

/* 활동 목록 */
.activity-list {
  padding: 6px 0;
  max-height: 280px;
  overflow-y: auto;
  
  /* 스크롤바 스타일 */
  scrollbar-width: thin;
  scrollbar-color: rgba(160,174,192,.35) transparent;
}

.activity-list::-webkit-scrollbar {
  width: 6px;
}

.activity-list::-webkit-scrollbar-track {
  background: transparent;
}

.activity-list::-webkit-scrollbar-thumb {
  background: rgba(160,174,192,.35);
  border-radius: 3px;
}

.activity-list::-webkit-scrollbar-thumb:hover {
  background: rgba(160,174,192,.5);
}

/* 활동 아이템 */
.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  transition: background 0.2s ease;
  cursor: pointer;
}

.activity-item:hover {
  background: var(--theme-bg-hover);
}

/* 활동 아이콘 */
.activity-icon {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: transform 0.2s ease;
}

.activity-item:hover .activity-icon {
  transform: scale(1.1);
}

.activity-icon > i {
  font-size: 13px;
  transition: color 0.2s ease;
}

/* 활동 내용 */
.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: 11px;
  font-weight: 500;
  color: var(--theme-text-primary);
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 9px;
  color: #a0aec0;
}

.activity-menu {
  padding: 1px 5px;
  border-radius: 2px;
  font-weight: 500;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.activity-time {
  color: #94a3b8;
}
</style>

