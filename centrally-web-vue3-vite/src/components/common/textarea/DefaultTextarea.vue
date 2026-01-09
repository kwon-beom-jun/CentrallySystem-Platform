<!--
DefaultTextarea 컴포넌트
--------------------------
사용 가능한 Props:
- modelValue (String | Number): 양방향 바인딩할 값 (기본값: '')
- id (String): textarea의 id
- name (String): textarea의 name
- placeholder (String): 자리 표시자 텍스트
- size (String): 텍스트에리어의 사이즈. 가능한 값: 'xsmall', 'small', 'medium', 'large', 'full' (기본값: 'medium')
- errorMessage (String): 유효성 검사 실패 시 표시할 에러 메시지 (기본값: "입력 형식이 올바르지 않습니다.")
- reserveErrorSpace (Boolean): true이면 에러 메시지 영역의 공간을 항상 확보 (기본값: false)
- disabled (Boolean): 텍스트에리어 비활성화 여부 (기본값: false)
- required (Boolean): 필수 입력 여부 (기본값: false)
- rows (Number): textarea의 행 수 (기본값: 4)
- validationType (String): 입력값 검증 유형. 가능한 값: '', 'english', 'number', 'korean', 'email' (기본값: '')

사용 예시:
<template>
  <DefaultTextarea
    id="myTextarea"
    name="description"
    v-model="description"
    placeholder="내용을 입력하세요"
    size="full"
    :disabled="false"
    :required="true"
    rows="5"
    validationType="english"
    errorMessage="영어만 입력할 수 있습니다."
    :reserveErrorSpace="true"
  />
</template>
-->
<template>
  <div class="default-textarea">
    <textarea 
      :id="id"
      :name="name"
      :placeholder="placeholder"
      v-model="internalValue"
      :class="['form-control', sizeClass, { 'is-invalid': touched && !isValid }]"
      :style="textAreaStyle"
      @blur="touched = true"
      @change="$emit('change', $event)"
      :disabled="disabled"
      :required="required"
      :rows="rows"
    ></textarea>

    <template v-if="reserveErrorSpace">
      <!-- 에러 메시지 영역을 항상 렌더링 -->
      <div class="error-message reserved">
        <span v-if="touched && !isValid">{{ errorMessage }}</span>
      </div>
    </template>
    <template v-else>
      <!-- 에러가 있을 때만 메시지 표시 -->
      <div v-if="touched && !isValid" class="error-message">
        {{ errorMessage }}
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits, ref } from 'vue';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  id: {
    type: String,
    default: ''
  },
  name: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'medium',
    validator: value => ['xsmall', 'small', 'medium', 'large', 'full'].includes(value)
  },
  errorMessage: {
    type: String,
    default: '입력 형식이 올바르지 않습니다.'
  },
  reserveErrorSpace: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  rows: {
    type: Number,
    default: 4
  },
  validationType: {
    type: String,
    default: '',
    validator: value => ['', 'english', 'number', 'korean', 'email'].includes(value)
  },
  whiteDisabled: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const internalValue = computed({
  get() {
    return props.modelValue;
  },
  set(newValue) {
    emit('update:modelValue', newValue);
  }
});

const sizeClass = computed(() => {
  switch (props.size) {
    case 'xsmall':
      return 'xsmall-textarea';
    case 'small':
      return 'small-textarea';
    case 'large':
      return 'large-textarea';
    case 'full':
      return 'full-textarea';
    case 'medium':
    default:
      return 'medium-textarea';
  }
});

/** validation pattern */
const pattern = computed(() => {
  switch (props.validationType) {
    case 'english':
      return /^[A-Za-z]*$/;
    case 'number':
      return /^[0-9]*$/;
    case 'korean':
      return /^[ㄱ-ㅎ가-힣]*$/;
    case 'email':
      return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    default:
      return null;
  }
});

const isValid = computed(() => {
  if (!pattern.value) return true;
  return pattern.value.test(internalValue.value);
});

const touched = ref(false);

// disabled 상태일 때 배경색을 흰색으로 설정하는 computed 속성
const textAreaStyle = computed(() => {
    if (props.disabled && props.whiteDisabled) {
        return { backgroundColor: '#ffffff' };
    }
    return {};
});
</script>

<style scoped>
.default-textarea textarea {
  resize: vertical;
}

/* 기본 사이즈 지정 */
.xsmall-textarea {
  font-size: 0.8rem;
  width: 130px;
}

.small-textarea {
  font-size: 0.8rem;
  width: 160px;
}

.medium-textarea {
  font-size: 0.8rem;
  width: 300px;
}

.large-textarea {
  font-size: 0.8rem;
  width: 500px;
}

.full-textarea {
  font-size: 0.8rem;
  width: 100%;
}

/* 에러 메시지 */
.error-message {
  color: red;
  font-size: 0.85rem;
  margin-top: 5px;
}
.error-message.reserved {
  min-height: 1.2rem;
}

/* 유효하지 않은 경우 빨간 테두리 */
.is-invalid {
  border-color: red;
}

.xsmall-textarea::placeholder,
.small-textarea::placeholder,
.medium-textarea::placeholder,
.large-textarea::placeholder,
.full-textarea::placeholder {
  font-size: 0.8rem;
}

/* ───────────────────────────────────────────────────────────────────
   반응형 (max-width: 650px) 
   DefaultTextfield.vue와 동일한 컨셉
   ───────────────────────────────────────────────────────────────────*/
@media (max-width: 650px) {
  textarea {
    font-size: 0.7rem !important;
  }
  .xsmall-textarea {
    width: 80px;
    /* 필요 시 padding, height 조정 가능 */
  }

  .small-textarea {
    width: 130px;
    /* 필요 시 padding, height 조정 가능 */
  }

  .medium-textarea {
    width: 220px;
  }

  .large-textarea {
    width: 280px;
  }

  /* placeholder 폰트 크기 조정 */
  .xsmall-textarea::placeholder,
  .small-textarea::placeholder,
  .medium-textarea::placeholder,
  .large-textarea::placeholder,
  .full-textarea::placeholder {
    font-size: 0.65rem;
  }
}

/* ───────────────────────────────────────────────────────────────────
   반응형 (max-width: 500px)
   DefaultTextfield.vue와 동일한 컨셉
   ───────────────────────────────────────────────────────────────────*/
@media (max-width: 500px) {
  textarea {
    font-size: 0.65rem !important;
  }
  .xsmall-textarea {
    width: 70px;
  }

  .small-textarea {
    width: 98px;
  }

  .medium-textarea {
    width: 150px;
  }

  .large-textarea {
    width: 180px;
  }

  /* placeholder 폰트 크기 조정 */
  .xsmall-textarea::placeholder,
  .small-textarea::placeholder,
  .medium-textarea::placeholder,
  .large-textarea::placeholder,
  .full-textarea::placeholder {
    font-size: 0.6rem;
  }
}
</style>
