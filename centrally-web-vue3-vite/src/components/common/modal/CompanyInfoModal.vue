<template>
  <Teleport to="body">
    <transition name="alert-fade">
      <div
        v-if="isVisible"
        class="custom-modal-pop-up"
        :style="{ zIndex: 1100 }"
        @click.self="handleClose"
      >
        <div class="custom-modal-pop-up-dialog">
          <div class="custom-modal-pop-up-content">
            <!-- 헤더 -->
            <!-- <h5 class="custom-modal-pop-up-title mb-3">회사 정보</h5> -->

            <!-- 바디 -->
            <div class="custom-modal-pop-up-body no-title">
              <!-- 회사 로고 -->
              <div class="company-logo-section">
                <img src="/img/common/logo/centrally-logo.png" :alt="t('common.companyInfo.logoAlt')" class="company-logo" />
              </div>

              <!-- 회사 기본 정보 -->
              <div class="company-info-section">
                <div class="info-row">
                  <span class="info-label">Project Name</span>
                  <span class="info-value">CENTRALLY COMPANY</span>
                </div>

                <div class="info-row">
                  <span class="info-label">Project Manager</span>
                  <span class="info-value">권 범 준</span>
                </div>

                <div class="info-row">
                  <span class="info-label">{{ t('common.companyInfo.address') }}</span>
                  <span class="info-value">
                    서울시 신풍역 3번출구 근처 <br/>
                    (개인 프로젝트 진행)
                  </span>
                </div>

                <div class="info-row">
                  <span class="info-label">{{ t('common.companyInfo.phone') }}</span>
                  <span class="info-value">02.123.1234</span>
                </div>

                <div class="info-row">
                  <span class="info-label">{{ t('common.companyInfo.email') }}</span>
                  <span class="info-value">qjawns0619@gmail.com</span>
                </div>

                <div class="info-row">
                  <span class="info-label">Career</span>
                  <span class="info-value">
                    <div class="info-value-title">Experience</div>
                    <div class="info-value-line">
                      2019.08 - 2020.06 · KPCNC · RPA 개발 (삼성전기 BrityWork 파트너)
                    </div>
                    <div class="info-value-line">
                      2020.06 - 현재 · KPCNC · Anyframe 기반 프레임워크 운영/개발
                    </div>

                    <div class="info-value-title">Projects & Contributions</div>
                    <div class="info-value-line">
                      사내 업무자동화 RPA 구축 및 운영 표준 수립
                    </div>
                    <div class="info-value-line">
                      엔터프라이즈 공통 프레임워크(Anyframe) 기능 개선 및 배포 파이프라인 관리
                    </div>
                    <div class="info-value-line">
                      Vue.js 기반 사내 서비스 UI 컴포넌트 개발 및 유지보수
                    </div>

                    <div class="info-value-title">Tech Stack</div>
                    <div class="info-value-line">
                      Backend · Spring, Spring Boot, Tomcat, JEUS, Oracle
                    </div>
                    <div class="info-value-line">
                      Frontend · Vue.js, JavaScript, HTML/CSS
                    </div>
                    <div class="info-value-line">
                      DevOps · Linux, Git, Jenkins, Nexus, AWS, Network 운영
                    </div>

                    <div class="info-value-title" style="margin-top: 18px;">GitHub Repository</div>
                    <div class="info-value-line">
                      <a href="https://github.com/kwon-beom-jun" target="_blank" rel="noopener noreferrer">
                        https://github.com/kwon-beom-jun
                      </a>
                    </div>
                  </span>
                </div>
              </div>
            </div>

            <!-- 푸터 -->
            <DefaultFormRow align="right">
              <DefaultButton size="small" @click="handleClose">
                {{ t('common.button.close') }}
              </DefaultButton>
            </DefaultFormRow>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { defineProps, defineEmits, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';
import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';

/**
 * Props
 */
const props = defineProps({
  isVisible: {
    type: Boolean,
    default: false
  }
});

/**
 * Emits
 */
const emit = defineEmits(['close']);
const { t } = useI18n();

/**
 * body 스크롤 잠금 관리
 */
const BODY = document.body;
let addedBySelf = false;

onMounted(() => {
  if (!BODY.classList.contains('modal-open')) {
    const gap = window.innerWidth - document.documentElement.clientWidth;
    BODY.classList.add('modal-open');
    if (gap > 0) BODY.style.paddingRight = `${gap}px`;
    addedBySelf = true;
  }
});

onUnmounted(() => {
  if (addedBySelf) {
    BODY.classList.remove('modal-open');
    BODY.style.paddingRight = '';
  }
});

/**
 * 모달 닫기
 */
const handleClose = () => {
  emit('close');
};
</script>

<style scoped>
/* 기본 모달 스타일 - 다른 모달들과 동일 */
.custom-modal-pop-up {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: var(--theme-modal-overlay);
  z-index: 1100;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 40px 20px;
  -webkit-overflow-scrolling: touch;
}

.custom-modal-pop-up-dialog {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  max-width: 700px;
  margin: 0 auto;
  border-radius: 10px !important;
}

.custom-modal-pop-up-content {
  background-color: var(--theme-modal-bg);
  color: var(--theme-text-primary);
  border-radius: 10px !important;
  padding: 0px 40px 30px 40px;
  width: 100%;
  max-width: 700px;
  box-shadow: 0 5px 15px var(--theme-shadow-lg);
}

/* 타이틀 */
.custom-modal-pop-up-title {
  font-size: 1.2rem;
  font-weight: 600;
}

/* 바디 */
.custom-modal-pop-up-body {
  padding: 20px 0;
  text-align: left;
  font-size: 0.9rem;
  color: var(--theme-text-secondary);
}

.custom-modal-pop-up-body.no-title {
  padding-top: 0px;
}

/* 로고 섹션 */
.company-logo-section {
  text-align: center;
  margin-bottom: 25px;
  padding: 30px 0 30px 0;
  margin-left: -40px;
  margin-right: -40px;
  background: var(--theme-bg-secondary);
  border-radius: 6px;
}

.company-logo {
  max-width: 200px;
  height: auto;
}

/* 회사 정보 섹션 */
.company-info-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 12px;
  align-items: start;
  padding: 8px 0;
  border-bottom: 1px solid var(--theme-border);
  font-size: 0.9rem;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 600;
  color: var(--theme-text-secondary);
}

.info-value {
  color: var(--theme-text-primary);
  line-height: 1.5;
  font-size: 0.8rem;
}

.info-value-title {
  display: block;
  font-weight: 900;
  margin-top: 0;
  margin-bottom: 8px;
  color: var(--theme-text-secondary);
}

.info-value-title:not(:first-of-type) {
  margin-top: 18px;
}

.info-value a {
  color: #007bff;
  text-decoration: none;
}

.info-value a:hover {
  text-decoration: underline;
}

.info-value-line {
  font-size: 0.73rem;
  color: var(--theme-text-muted);
  line-height: 1.5;
  margin-bottom: 5px;
  margin-top: 5px;
  margin-left: 10px;
}

/* 트랜지션 */
.alert-fade-enter-active,
.alert-fade-leave-active {
  transition: opacity 0.2s ease;
}

.alert-fade-enter-from,
.alert-fade-leave-to {
  opacity: 0;
}

/* 반응형 디자인 */
@media (max-width: 650px) {
  .custom-modal-pop-up-content {
    width: 90%;
    padding: 15px;
    max-height: 90vh;
  }

  .custom-modal-pop-up-title {
    font-size: 1rem;
  }

  .company-logo {
    max-width: 150px;
  }

  .custom-modal-pop-up-body {
    font-size: 0.85rem;
  }

  .info-row {
    grid-template-columns: 1fr;
    gap: 5px;
    font-size: 0.85rem;
  }

  .info-label {
    font-size: 0.85rem;
  }

  .info-value {
    font-size: 0.8rem;
  }
}
</style>


