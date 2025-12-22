<template>
  <div v-if="visible" class="modal-overlay" @click="closeModal">
    <div class="modal-container" @click.stop>
      <div class="modal-header">
        <h3>{{ $t('hrm.userInfoDetail.mobileCardDesign') }}</h3>
        <button class="close-btn" @click="closeModal">
          <i class="ri-close-line"></i>
        </button>
      </div>

      <div class="modal-body">
        <p class="modal-desc">{{ $t('hrm.favoriteMenu.mobileCardDesign') || $t('hrm.userInfoDetail.subtitle') }}</p>
        
        <div class="style-grid">
          <!-- 기본 스타일 -->
          <div 
            class="style-option" 
            :class="{ active: selectedStyle === 'default' }"
            @click="selectStyle('default')"
          >
            <div class="preview-wrapper">
              <div class="preview-container">
                <MobileUserCard 
                  :favoriteMenus="sampleMenus"
                  class="preview-card"
                />
              </div>
            </div>
            <div class="style-label">
              <i class="ri-checkbox-circle-fill" v-if="selectedStyle === 'default'"></i>
              <span>{{ $t('common.label.default') }}</span>
            </div>
          </div>

          <!-- 버전 1 -->
          <div 
            class="style-option" 
            :class="{ active: selectedStyle === 'ver1' }"
            @click="selectStyle('ver1')"
          >
            <div class="preview-wrapper">
              <div class="preview-container">
                <MobileUserCardVer1 
                  :favoriteMenus="sampleMenus"
                  class="preview-card"
                />
              </div>
            </div>
            <div class="style-label">
              <i class="ri-checkbox-circle-fill" v-if="selectedStyle === 'ver1'"></i>
              <span>{{ $t('common.label.style1') }}</span>
            </div>
          </div>

          <!-- 버전 2 -->
          <div 
            class="style-option" 
            :class="{ active: selectedStyle === 'ver2' }"
            @click="selectStyle('ver2')"
          >
            <div class="preview-wrapper">
              <div class="preview-container">
                <MobileUserCardVer2 
                  :favoriteMenus="sampleMenus"
                  class="preview-card"
                />
              </div>
            </div>
            <div class="style-label">
              <i class="ri-checkbox-circle-fill" v-if="selectedStyle === 'ver2'"></i>
              <span>{{ $t('common.label.style2') }}</span>
            </div>
          </div>

          <!-- 버전 3 -->
          <div 
            class="style-option" 
            :class="{ active: selectedStyle === 'ver3' }"
            @click="selectStyle('ver3')"
          >
            <div class="preview-wrapper">
              <div class="preview-container">
                <MobileUserCardVer3 
                  :favoriteMenus="sampleMenus"
                  class="preview-card"
                />
              </div>
            </div>
            <div class="style-label">
              <i class="ri-checkbox-circle-fill" v-if="selectedStyle === 'ver3'"></i>
              <span>{{ $t('common.label.style3') }}</span>
            </div>
          </div>

          <!-- 버전 4 -->
          <div 
            class="style-option" 
            :class="{ active: selectedStyle === 'ver4' }"
            @click="selectStyle('ver4')"
          >
            <div class="preview-wrapper">
              <div class="preview-container">
                <MobileUserCardVer4 
                  :favoriteMenus="sampleMenus"
                  class="preview-card"
                />
              </div>
            </div>
            <div class="style-label">
              <i class="ri-checkbox-circle-fill" v-if="selectedStyle === 'ver4'"></i>
              <span>{{ $t('common.label.style4') }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">{{ $t('common.button.cancel') }}</button>
        <button class="btn-confirm" @click="confirmSelection">{{ $t('common.button.apply') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import MobileUserCard from '@/components/common/card/MobileUserCard.vue';
import MobileUserCardVer1 from '@/components/common/card/MobileUserCard-ver1.vue';
import MobileUserCardVer2 from '@/components/common/card/MobileUserCard-ver2.vue';
import MobileUserCardVer3 from '@/components/common/card/MobileUserCard-ver3.vue';
import MobileUserCardVer4 from '@/components/common/card/MobileUserCard-ver4.vue';

/**
 * Props
 */
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  currentStyle: {
    type: String,
    default: 'default'
  }
});

/**
 * Emits
 */
const emit = defineEmits(['close', 'confirm']);

/**
 * 선택된 스타일
 */
const selectedStyle = ref(props.currentStyle);
const { t } = useI18n();

/**
 * 샘플 메뉴 데이터 (미리보기용)
 */
const sampleMenus = [
  {
    favoriteId: 1,
    menuLabel: '영수증 등록',
    menuIcon: 'ri-file-add-line',
    category: '등록자',
    color: 'blue',
    menuPath: '/sample'
  },
  {
    favoriteId: 2,
    menuLabel: '결재 신청',
    menuIcon: 'ri-survey-line',
    category: '결재자',
    color: 'green',
    menuPath: '/sample'
  },
  {
    favoriteId: 3,
    menuLabel: '사용자 관리',
    menuIcon: 'ri-user-settings-line',
    category: '사용자·권한',
    color: 'purple',
    menuPath: '/sample'
  }
];

/**
 * currentStyle이 변경되면 selectedStyle 업데이트
 */
watch(() => props.currentStyle, (newVal) => {
  selectedStyle.value = newVal;
});

/**
 * 스타일 선택
 */
function selectStyle(style) {
  selectedStyle.value = style;
}

/**
 * 모달 닫기
 */
function closeModal() {
  emit('close');
}

/**
 * 선택 확인
 */
function confirmSelection() {
  emit('confirm', selectedStyle.value);
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.modal-container {
  background: #ffffff;
  border-radius: 16px;
  max-width: 1100px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  font-size: 1.1rem;
  font-weight: 800;
  color: #ffffff !important;
  margin: 0;
}

.close-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  line-height: 1;
  border: none;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #e5e7eb;
}

.close-btn i {
  font-size: 1.2rem;
  color: #64748b;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.modal-desc {
  font-size: 0.85rem;
  color: #64748b;
  margin: 0 0 24px 0;
  text-align: center;
}

.style-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.style-option {
  cursor: pointer;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  overflow: hidden;
  transition: all 0.2s;
}

.style-option:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.style-option.active {
  border-color: #3b82f6;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.25);
}

.preview-wrapper {
  background: #f8fafc;
  padding: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 350px;
  position: relative;
}

.preview-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  user-select: none;
}

.preview-card {
  margin: 0;
  width: 100%;
  max-width: 500px;
  transform: scale(0.8);
}

.style-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
  font-size: 0.85rem;
  font-weight: 700;
  color: #475569;
}

.style-option.active .style-label {
  color: #3b82f6;
  background: #eff6ff;
}

.style-label i {
  font-size: 1rem;
  color: #3b82f6;
  font-weight: 500;
  -webkit-text-stroke: 0.1px #ffffff;
  text-stroke: 0.5px #3b82f6;
}

.modal-footer {
  display: flex;
  gap: 10px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #fafbfc;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 12px;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-cancel {
  background: #f3f4f6;
  color: #64748b;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-confirm {
  background: #3b82f6;
  color: #ffffff;
}

.btn-confirm:hover {
  background: #2563eb;
}

/* ═══════════════════════════════════════════════════════════════
 * 다크모드 스타일
 * ═══════════════════════════════════════════════════════════════ */
body[data-theme="dark"] .modal-overlay {
  background: rgba(0, 0, 0, 0.8) !important;
}

body[data-theme="dark"] .modal-container {
  background: var(--theme-card-bg) !important;
  border: 1px solid var(--theme-border) !important;
  box-shadow: 0 20px 60px var(--theme-shadow-lg) !important;
}

body[data-theme="dark"] .modal-header {
  background: var(--theme-bg-secondary) !important;
  border-bottom: 1px solid var(--theme-border) !important;
}

body[data-theme="dark"] .modal-header h3 {
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .close-btn {
  background: var(--theme-bg-tertiary) !important;
}

body[data-theme="dark"] .close-btn:hover {
  background: var(--theme-bg-hover) !important;
}

body[data-theme="dark"] .close-btn i {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .modal-body {
  background: var(--theme-card-bg) !important;
}

body[data-theme="dark"] .modal-desc {
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .style-option {
  border: 2px solid var(--theme-border) !important;
  background: var(--theme-bg-secondary) !important; /* 더 밝은 배경 */
}

body[data-theme="dark"] .style-option:hover {
  border-color: var(--theme-primary) !important;
  background: #4a4a4a !important; /* 호버시 더 밝은 회색 */
  box-shadow: 0 4px 12px var(--theme-shadow-sm) !important;
}

body[data-theme="dark"] .style-option.active {
  border-color: var(--theme-primary) !important;
  background: #807f7f !important; /* 선택된 버튼도 밝은 회색 */
  box-shadow: 0 4px 16px var(--theme-shadow-md) !important;
}

body[data-theme="dark"] .preview-wrapper {
  background: var(--theme-bg-tertiary) !important; /* 미리보기 영역도 더 밝게 */
}

body[data-theme="dark"] .style-label {
  background: #4a4a4a !important; /* 호버시 더 밝은 회색 */
  border-top: 1px solid var(--theme-border) !important;
  color: var(--theme-text-primary) !important;
}

body[data-theme="dark"] .style-option.active .style-label {
  color: var(--theme-primary) !important;
  background: #807f7f !important; /* 선택된 라벨도 밝은 회색 */
}

body[data-theme="dark"] .style-label i {
  color: #ffffff !important;
  font-weight: 500 !important;
  -webkit-text-stroke: 0.1px #000000;
  text-stroke: 0.5px #ffffff;
}

body[data-theme="dark"] .modal-footer {
  background: var(--theme-bg-secondary) !important;
  border-top: 1px solid var(--theme-border) !important;
}

body[data-theme="dark"] .btn-cancel {
  background: var(--theme-bg-tertiary) !important;
  color: var(--theme-text-secondary) !important;
}

body[data-theme="dark"] .btn-cancel:hover {
  background: var(--theme-bg-hover) !important;
}

body[data-theme="dark"] .btn-confirm {
  background: var(--theme-primary) !important;
  color: #ffffff !important;
}

body[data-theme="dark"] .btn-confirm:hover {
  background: var(--theme-primary-dark) !important;
}

/* 모바일 반응형 */
@media (max-width: 650px) {
  .modal-container {
    max-width: 100%;
    margin: 0;
    border-radius: 16px 16px 0 0;
    max-height: 95vh;
  }
  
  .modal-body {
    flex: 1;
    overflow-y: auto;
    padding: 24px 15px;
  }

  .close-btn {
    width: 25px;
    height: 25px;
    line-height: 1;
  }

  .close-btn i {
    font-size: 1rem;
    color: #64748b;
  }

  .style-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .preview-wrapper {
    height: 300px;
  }
  
  .preview-card {
    max-width: 100%;
    transform: scale(0.8);
  }
}

/* 작은 모바일 (380px 이하) */
@media (max-width: 380px) {
  .modal-overlay {
    padding: 10px;
  }

  .modal-header {
    padding: 16px 18px;
  }

  .modal-header h3 {
    font-size: 1rem;
  }

  .modal-body {
    padding: 20px 12px;
  }

  .modal-desc {
    font-size: 0.8rem;
    margin-bottom: 20px;
  }

  .style-grid {
    gap: 14px;
  }

  .style-label {
    padding: 10px;
    font-size: 0.8rem;
  }

  .modal-footer {
    padding: 16px 18px;
  }

  .btn-cancel,
  .btn-confirm {
    padding: 10px;
    font-size: 0.85rem;
  }

  .preview-wrapper {
    height: 280px;
  }

  .preview-card {
    transform: scale(0.75);
  }
}
</style>

