<template>
  <Teleport to="body">
    <!-- opacity 만 살짝 주는 트랜지션 -->
    <transition name="alert-fade">
      <div
        v-if="isVisible"
        class="custom-modal-pop-up"
        :style="{ zIndex: 1100 + zIndex }"
        @click.self="handleBackgroundClick"
      >
        <div class="custom-modal-pop-up-dialog">
          <div class="custom-modal-pop-up-content">
            <!-- ── 헤더 ─────────────────────────────── -->
            <h5 v-if="title" class="custom-modal-pop-up-title">{{ title }}</h5>

            <!-- ── 바디 슬롯 ────────────────────────── -->
            <div class="custom-modal-pop-up-body" :class="{ 'no-title': !title }">
              <slot name="body" />
            </div>

            <!-- ── 버튼 ─────────────────────────────── -->
            <DefaultFormRow align="right">
              <DefaultButton
                v-if="cancelTextComputed"
                color="gray"
                marginRight="5px"
                size="small"
                @click="emitClose"
              >
                {{ cancelTextComputed }}
              </DefaultButton>

              <DefaultButton 
                size="small" 
                @click="$emit('confirm')"
              >
                {{ confirmTextComputed }}
              </DefaultButton>

              <!-- 예비 버튼 -->
              <DefaultButton
                v-if="extraButtonText && !disableExtra"
                color="primary"
                marginLeft="5px"
                size="small"
                @click="emitExtra"
              >
                {{ extraButtonText }}
              </DefaultButton>
            </DefaultFormRow>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { defineProps, defineEmits, onMounted, onUnmounted, computed } from "vue";
import { useI18n } from 'vue-i18n';
import DefaultButton from "@/components/common/button/DefaultButton.vue";
import DefaultFormRow from "@/components/common/DefaultFormRow.vue";

/* ────────── props / emits ────────── */
const props = defineProps({
  isVisible: Boolean,
  title: {
    type: String,
    default: "",
  },
  confirmText: {
    type: String,
    default: "",
  },
  cancelText: {
    type: String,
    default: "", // 기본값을 빈 문자열로 설정
  },
  // 백그라운드 클릭을 허용할지 제어하는 prop
  disableBackgroundClose: {
    type: Boolean,
    default: false,
  },
  extraButtonText: {
    type: String,
    default: "",
  },
  disableExtra: {
    type: Boolean,
    default: false,
  },
  zIndex: {
    type: Number,
    default: 0,
  },
});
const emit = defineEmits(["confirm", "close", "extra"]);

/* i18n */
const { t } = useI18n();

/* 표시용 버튼 텍스트 (미지정 시 i18n 기본값) */
const confirmTextComputed = computed(() => props.confirmText || t('common.button.confirm'));
const cancelTextComputed = computed(() => props.cancelText || '');

/* ────────── body 스크롤 잠금 관리 ────────── */
const BODY = document.body;
/* 이 인스턴스가 body 잠금을 추가했는지 여부 */
let addedBySelf = false;

onMounted(() => {
  /* 이미 다른 모달이 body 를 잠궈두었으면 건드리지 않음 */
  if (!BODY.classList.contains("modal-open")) {
    const gap = window.innerWidth - document.documentElement.clientWidth;
    BODY.classList.add("modal-open");
    if (gap > 0) BODY.style.paddingRight = `${gap}px`;
    addedBySelf = true; // 🔸 우리가 잠금·패딩을 넣었다!
  }
});

onUnmounted(() => {
  /* 우리가 넣은 잠금/패딩만 정상적으로 원복 */
  if (addedBySelf) {
    BODY.classList.remove("modal-open");
    BODY.style.paddingRight = "";
  }
});

/* ────────── 헬퍼 ────────── */
function emitClose() {
  emit("close");
}
function handleBackgroundClick() {
  if (!props.disableBackgroundClose) emitClose();
}
function emitExtra() {
  emit("extra");
}
</script>

<style scoped>
/* 기존 스타일 */
.custom-modal-pop-up {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: var(--theme-modal-overlay);
  z-index: 1100; /* Bootstrap 모달의 기본 z-index(1050)보다 높게 설정 */
}

.custom-modal-pop-up-dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  border-radius: 8px;
  max-width: 400px;
  margin: 0 auto;
}

.custom-modal-pop-up-content {
  background-color: var(--theme-modal-bg);
  color: var(--theme-text-primary);
  border-radius: 8px;
  padding: 30px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 5px 15px var(--theme-shadow-lg);
}

/* ───── 타이틀 폰트 크기 조정 ───── */
.custom-modal-pop-up-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 0px;
}

.custom-modal-pop-up-header h5 {
  font-size: 1rem;
  text-align: left;
  margin-bottom: 20px;
}

.custom-modal-pop-up-body {
  padding: 30px 0;
  text-align: left;
  font-size: 0.9rem;
  color: var(--theme-text-secondary);
}

.custom-modal-pop-up-body.no-title {
  padding-top: 10px;
}

.custom-modal-pop-up-footer {
  display: flex;
  justify-content: flex-end;
}

.custom-modal-pop-up-footer .btn {
  min-width: 80px;
  margin-left: 10px;
}

.custom-modal-pop-up-header {
  border-bottom: none;
}

.custom-modal-pop-up-footer {
  border-top: none;
  padding: 0;
  margin: 0;
}

/* 애니메이션 효과 */
/* .fade-enter-active {
  transition: opacity 0.2s ease;
} */

/* .fade-enter-from {
  opacity: 0;
} */

/* ------------------------------------------------------------------ */
/* 트랜지션 : opacity만                                                */
/* ------------------------------------------------------------------ */
/* .alert-fade-enter-active,
.alert-fade-leave-active {
  transition: opacity 0.2s ease;
}
.alert-fade-enter-from,
.alert-fade-leave-to {
  opacity: 0;
} */
/* 반응형: 모바일 환경에서 폰트 크기 조정 */
@media (max-width: 650px) {
  .custom-modal-pop-up-content {
    width: 70%;
    padding: 20px;
  }

  .custom-modal-pop-up-footer .btn {
    font-size: 0.6rem !important; /* 모바일에서 버튼 글자 크기 */
    padding: 0.2rem 0.4rem !important; /* 모바일에서 버튼 크기 */
    min-width: 50px;
  }

  .custom-modal-pop-up-header h5 {
    font-size: 0.8rem;
    margin-bottom: 8px;
  }

  .custom-modal-pop-up-body {
    font-size: 0.75rem;
    padding: 25px 0;
  }

  .custom-modal-pop-up-title {
    font-size: 0.875rem;
    font-weight: 900;
  }
}
</style>
