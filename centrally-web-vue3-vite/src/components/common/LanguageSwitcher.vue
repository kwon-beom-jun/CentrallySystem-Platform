<template>
  <div class="language-switcher" :class="mode" ref="dropdownRef">
    <button type="button" class="switcher-trigger" @click="toggleDropdown">
      <i class="ri-translate-2 trigger-icon"></i>
      <span class="trigger-label">{{ currentLabel }}</span>
      <i class="ri-arrow-down-s-line trigger-caret" :class="{ open: isOpen }"></i>
    </button>

    <transition name="language-fade">
      <ul v-if="isOpen" class="switcher-list">
        <li
          v-for="option in options"
          :key="option.value"
          class="switcher-item"
          :class="{ active: option.value === selectedLanguage }"
          @click="selectLanguage(option.value)"
        >
          <span>{{ option.label }}</span>
          <i v-if="option.value === selectedLanguage" class="ri-check-line"></i>
        </li>
      </ul>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useI18n } from 'vue-i18n';
import { changeLocale } from '@/locales';

/**
 * mode props
 * - 'header-icon-mode': HeaderLayout에서 사용 (아이콘만 표시)
 * - 'front-header-mode': FrontHeader에서 사용 (기본 스타일, 650px 이하 숨김)
 */
const props = defineProps({
  mode: {
    type: String,
    default: ''
  }
});

const options = [
  { value: 'ko', label: '한글' },
  { value: 'en', label: 'English' },
];

const { locale } = useI18n();
const selectedLanguage = ref(locale.value);
const isOpen = ref(false);
const dropdownRef = ref(null);

const currentLabel = computed(() => {
  const found = options.find(option => option.value === selectedLanguage.value);
  return found ? found.label : selectedLanguage.value;
});

const closeDropdown = () => {
  isOpen.value = false;
};

const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
};

const selectLanguage = value => {
  if (selectedLanguage.value === value) {
    closeDropdown();
    return;
  }
  selectedLanguage.value = value;
  changeLocale(value);
  closeDropdown();
};

const handleClickOutside = event => {
  if (!dropdownRef.value) return;
  if (!dropdownRef.value.contains(event.target)) {
    closeDropdown();
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside, true);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside, true);
});
</script>

<style scoped>
.language-switcher {
  position: relative;
  display: inline-block;
}

.switcher-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid rgba(79, 112, 255, 0.18);
  background: linear-gradient(135deg, rgba(236, 243, 255, 0.95), rgba(212, 224, 255, 0.95));
  box-shadow: 0 4px 12px rgba(79, 112, 255, 0.12);
  color: #363f5d;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.switcher-trigger:hover {
  border-color: rgba(79, 112, 255, 0.38);
  box-shadow: 0 10px 18px rgba(79, 112, 255, 0.18);
  transform: translateY(-1px);
}

.switcher-trigger:active {
  transform: translateY(0);
}

.trigger-icon {
  font-size: 16px;
  color: #4f70ff;
}

.trigger-label {
  min-width: 70px;
  text-align: left;
}

.trigger-caret {
  font-size: 14px;
  color: #6f7fb8;
  transition: transform 0.2s ease;
}

.trigger-caret.open {
  transform: rotate(180deg);
}

.switcher-list {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 140px;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 18px 35px rgba(26, 32, 44, 0.16);
  padding: 8px;
  list-style: none;
  margin: 0;
  z-index: 1050;
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.switcher-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  color: #2d3748;
  cursor: pointer;
  transition: all 0.15s ease;
}

.switcher-item:hover {
  background: rgba(79, 112, 255, 0.08);
  color: #2b41ff;
}

.switcher-item.active {
  background: rgba(79, 112, 255, 0.12);
  color: #2b41ff;
  font-weight: 600;
}

.switcher-item i {
  font-size: 14px;
}

.language-fade-enter-active,
.language-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.language-fade-enter-from,
.language-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* HeaderLayout 전용: 1300px 이상에서는 기본 스타일, 1300px 이하에서는 아이콘 모드 */
.header-icon-mode .switcher-trigger {
  /* 1300px 이상: 기본 스타일 그대로 */
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, rgba(236, 243, 255, 0.95), rgba(212, 224, 255, 0.95));
  box-shadow: 0 4px 12px rgba(79, 112, 255, 0.12);
  color: #363f5d;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.header-icon-mode .switcher-trigger:hover {
  box-shadow: 0 10px 18px rgba(79, 112, 255, 0.18);
  transform: translateY(-1px);
}

.header-icon-mode .trigger-icon {
  font-size: 16px;
  color: #4f70ff;
}

.header-icon-mode .trigger-label {
  min-width: 70px;
  text-align: left;
}

.header-icon-mode .trigger-caret {
  font-size: 14px;
  color: #6f7fb8;
}

/* FrontHeader 전용: 모바일에서도 기본 스타일 유지, 작은 화면에서만 축소 */
.front-header-mode {
  display: inline-block;
  margin-top: 2px;
}

.front-header-mode .switcher-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, rgba(236, 243, 255, 0.95), rgba(212, 224, 255, 0.95));
  box-shadow: 0 4px 12px rgba(79, 112, 255, 0.12);
  color: #363f5d;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.front-header-mode .switcher-list {
  width: 140px;
}

.front-header-mode .switcher-trigger:hover {
  box-shadow: 0 10px 18px rgba(79, 112, 255, 0.18);
  transform: translateY(-1px);
}

.front-header-mode .trigger-icon {
  font-size: 16px;
  color: #4f70ff;
}

.front-header-mode .trigger-label {
  min-width: 70px;
  text-align: left;
}

.front-header-mode .trigger-caret {
  font-size: 14px;
  color: #6f7fb8;
}


/* 1300px 이하: 아이콘 버튼으로 전환 */
@media (max-width: 1300px) {
  .header-icon-mode .switcher-trigger {
    justify-content: center;
    width: 35px;
    height: 35px;
    border-radius: 12px;
    background: var(--theme-bg-tertiary);
    border: 1px solid #e5e7eb;
    color: #6b7280;
    padding: 0;
    gap: 0;
  }
  
  .header-icon-mode .switcher-trigger:hover {
    background: #2563eb;
    border-color: #2563eb;
    color: #fff;
    transform: translateY(-1px) scale(1.05);
    box-shadow: 0 4px 12px rgba(37,99,235,.3);
  }
  
  .header-icon-mode .switcher-trigger:active {
    transform: translateY(0) scale(.98);
    box-shadow: 0 2px 6px rgba(37,99,235,.2);
  }
  
  .header-icon-mode .trigger-icon {
    font-size: 18px;
    color: inherit;
  }
  
  .header-icon-mode .trigger-label,
  .header-icon-mode .trigger-caret {
    display: none;
  }
  
  .header-icon-mode .switcher-list {
    width: 130px;
    font-size: 0.78rem;
  }
  
  .header-icon-mode .switcher-item {
    padding: 8px 10px;
    font-size: 0.78rem;
  }
  
  .header-icon-mode .switcher-item i {
    font-size: 0.78rem;
  }
}
/* 모바일에서 FrontHeader 다국어 버튼 축소 */
@media (max-width: 650px) {
  .front-header-mode .switcher-trigger {
    padding: 5px 10px;
    gap: 4px;
    font-size: 11px;
  }
  
  .front-header-mode .trigger-icon {
    font-size: 14px;
  }
  
  .front-header-mode .trigger-label {
    min-width: 50px;
    font-size: 11px;
  }
  
  .front-header-mode .trigger-caret {
    font-size: 12px;
  }
  
  .front-header-mode .switcher-list {
    width: 105px;
    font-size: 11px;
  }
  
  .front-header-mode .switcher-item {
    padding: 8px 10px;
    font-size: 11px;
  }
  
  .front-header-mode .switcher-item i {
    font-size: 12px;
  }
  .front-header-mode {
    display: inline-block;
    margin-top: 5px;
  }
}

/* 400px 이하: 더 작게 */
@media (max-width: 400px) {
  
  .front-header-mode .trigger-label {
    min-width: 40px;
    font-size: 10px;
  }
  
  .front-header-mode .switcher-list {
    width: 95px;
    font-size: 11px;
  }
  
  .front-header-mode {
    display: inline-block;
    margin-top: 10px;
  }
}

/* ───── 다크 테마 (HeaderLayout 전용 아이콘 모드) ───── */
body[data-theme="dark"] .header-icon-mode .switcher-trigger {
  background: #F3F4F6;
  border: 1px solid #F3F4F6;
  color: #797F8C;
  box-shadow: none;
}

body[data-theme="dark"] .header-icon-mode .switcher-trigger:hover {
  background: #2563eb;
  border-color: #2563eb;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
}

body[data-theme="dark"] .header-icon-mode .switcher-trigger:active {
  transform: translateY(0) scale(.98);
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.25);
}

body[data-theme="dark"] .header-icon-mode .trigger-icon {
  color: inherit;
}
</style>


