<template>
  <div class="time-card">
    <div class="time-content">
      <div class="current-time">{{ currentTime }}</div>
      <div class="current-date">{{ currentDate }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

/**
 * 현재 시간/날짜
 */
const currentTime = ref('');
const currentDate = ref('');
let timeInterval = null;

/**
 * 현재 시간과 날짜를 업데이트하는 함수
 */
function updateDateTime() {
  const now = new Date();
  
  // 시간 포맷 (HH:MM:SS)
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');
  currentTime.value = `${hours}:${minutes}:${seconds}`;
  
  // 날짜 포맷 (YYYY년 MM월 DD일 요일)
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const date = String(now.getDate()).padStart(2, '0');
  const days = [
    t('common.weekday.sun'), t('common.weekday.mon'), t('common.weekday.tue'),
    t('common.weekday.wed'), t('common.weekday.thu'), t('common.weekday.fri'), t('common.weekday.sat')
  ];
  const dayName = days[now.getDay()];
  currentDate.value = t('common.sidebar.time.dateFormat', { year, month, date, day: dayName });
}

/**
 * 마운트 시 시간 업데이트 시작
 */
onMounted(() => {
  updateDateTime();
  timeInterval = setInterval(updateDateTime, 1000);
});

/**
 * 언마운트 시 interval 정리
 */
onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval);
  }
});
</script>

<style scoped>
.time-card {
  margin: 12px 5px 12px 15px;
  padding: 16px;
  /* background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); */
  /* background: linear-gradient(90deg, #5d9bff 0%, #004394 100%); */
  background: linear-gradient(135deg, #99a0ad 0%, #58606D 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
  color: white;
  text-align: center;
  cursor: default;
  will-change: opacity, transform;
  transform: translate3d(0, 0, 0);
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s, 
              visibility 0s linear,
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
}

.time-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.current-time {
  font-size: 1.6rem;
  font-weight: 900;
  letter-spacing: 2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.current-date {
  font-size: 0.75rem;
  opacity: 0.95;
  font-weight: 500;
}
</style>

