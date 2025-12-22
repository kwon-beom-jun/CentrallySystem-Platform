<template>
  <div class="default-textfield" :style="marginStyle">
    <!-- month 타입일 때는 커스텀 month picker 사용 -->
    <CustomMonthPicker
      v-if="type === 'month'"
      :modelValue="internalValue"
      @update:modelValue="handleMonthPickerUpdate"
      @change="$emit('change', $event)"
      :placeholder="placeholder"
      :size="size"
      :disabled="disabled"
      :reserveErrorSpace="reserveErrorSpace"
      :errorMessage="errorMessage"
      :externalError="externalError"
      :externalInvalid="externalInvalid"
      :customHeight="customHeight"
      :bgColor="bgColor"
    />
    
    <!-- 다른 타입들은 기본 input 사용 (DefaultTextfield와 동일) -->
    <input
      v-else
      :type="type"
      :id="computedId"
      :name="name"
      :placeholder="placeholder"
      v-model="internalValue"
      v-bind="type === 'radio' ? { value: value } : {}"
      :class="type !== 'radio'
          ? ['form-control', sizeClass, { 'is-invalid': errorToShow } ] : ''"
      :disabled="disabled"
      :required="required"
      autocomplete="off"
      @blur="touched = true"
      @change="$emit('change', $event)"
      @keydown="$emit('keydown', $event)"
      :style="[customHeightStyle, bgStyle]"
    />
    
    <!-- 에러 메시지 영역: month 타입이 아닐 때만 표시 (CustomMonthPicker가 자체 에러 처리) -->
    <template v-if="type !== 'month'">
      <template v-if="reserveErrorSpace">
        <!-- 에러 메시지 영역을 항상 렌더링하여 공간을 확보 -->
        <div class="error-message reserved">
          <span v-if="errorToShow">{{ errorToShow }}</span>
        </div>
      </template>
      <template v-else>
        <!-- 에러 메시지가 있을 때만 렌더링 -->
        <div v-if="touched && !isValid" class="error-message">
          <span v-if="errorToShow">{{ errorToShow }}</span>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits, ref } from 'vue';
import CustomMonthPicker from '../datepicker/CustomMonthPicker.vue';

const props = defineProps({
  // v-model로 사용할 값
  // 문자열이나 Date 타입을 받을 수 있으며, 기본값은 빈 문자열
  modelValue: {
    type: [String, Date],
    default: ''
  },
  // input 요소의 타입을 지정
  // 예를 들어, "text", "date", "month", "radio" 등 다양한 타입을 사용할 수 있음
  // 기본값은 "text"
  type: {
    type: String,
    default: 'text'
  },
  // input 요소의 id를 지정
  // 주로 label의 for 속성과 연계되어 접근성과 DOM 식별에 사용
  id: {
    type: String,
    default: ''
  },
  // input 요소의 placeholder(자리 표시자) 텍스트
  // 사용자가 아무런 입력을 하지 않았을 때 안내 문구로 표시됨됨
  placeholder: {
    type: String,
    default: ''
  },
  // input 요소의 넓이를 제어하는 프롭
  // 'small', 'medium', 'large', 'full' 중 하나의 값을 가지며,
  // 각각 미리 정의된 CSS 클래스(예: small-input, medium-input 등)를 적용
  size: {
    type: String,
    default: 'medium',
    validator: value => ['xsmall', 'small', 'medium', 'large', 'full'].includes(value)
  },
  /**
   * validationType 프롭:
   * 입력값의 검증 유형을 지정
   * 사용 가능한 값은 다음과 같음음
   * - ''         : 검증 없음
   * - 'english'  : 영어만 입력 가능
   * - 'number'   : 숫자만 입력 가능
   * - 'korean'   : 한글만 입력 가능
   * - 'email'    : 이메일 형식 검증 (예: user@example.com)
   */
  validationType: {
    type: String,
    default: '',
    validator: value => ['', 'english', 'number', 'korean', 'email', 'money'].includes(value)
  },
  // validationType에 따른 검증 실패 시 표시할 에러 메시지
  // 기본 메시지는 "입력 형식이 올바르지 않습니다."로 설정
  errorMessage: {
    type: String,
    default: '입력 형식이 올바르지 않습니다.'
  },
  /**
   * reserveErrorSpace 프롭:
   * true이면, 에러 메시지 영역이 항상 렌더링되어 빈 공간이라도 차지
   * false이면, 에러가 실제로 있을 때만 에러 메시지 영역이 렌더링됨
   * 이를 통해 레이아웃이 변경되지 않도록 미리 공간을 확보할 수 있음
   */
  reserveErrorSpace: {
    type: Boolean,
    default: false
  },
  // disabled 프롭:
  // true일 경우, 해당 입력 필드를 비활성화하여 사용자가 값을 변경할 수 없게 함
  disabled: {
    type: Boolean,
    default: false
  },
  // required 프롭:
  // true일 경우, HTML5의 required 속성이 적용되어 필수 입력 필드로 동작
  required: {
    type: Boolean,
    default: false
  },
  // name 프롭:
  // 주로 라디오 버튼과 같이 여러 요소가 그룹으로 묶여야 할 때 사용
  // 같은 그룹에 속하는 라디오 버튼들은 동일한 name 값을 가져야 함
  name: {
    type: String,
    default: ''
  },
  // value 프롭:
  // 라디오 버튼 등에서 사용되는 값으로, 해당 요소의 고유 값을 나타냄
  // [String, Number, Boolean] 타입을 지원
  value: {
    type: [String, Number, Boolean],
    required: false
  },
  marginLeft: {
    type: String,
    default: '0'
  },
  marginRight: {
    type: String,
    default: '0'
  },
  marginTop: {
    type: String,
    default: '0'
  },
  marginBottom: {
    type: String,
    default: '0'
  },
  customHeight: {
    type: String,
    default: ''
  },
  /* (A) 외부에서 직접 에러 문자열을 넣어 줄 수 있도록 */
   externalError: {
     type: String,
     default: ''
   },
   /* (B) 외부에서 is-invalid 강제 적용하고 싶을 때 */
   externalInvalid: {
     type: Boolean,
     default: false
   },
   bgColor: {            // ← 새 프롭
    type: String,
    default: ''         // 미지정 시 기본색 사용
  },
});

// computedId: id가 빈 문자열이면 undefined를 반환
const computedId = computed(() => {
  return props.id !== '' ? props.id : undefined;
});

const emits = defineEmits(['update:modelValue', 'change', 'keydown']);

// v-model 양방향 바인딩을 위한 computed
const internalValue = computed({
  get() {
    return props.modelValue;
  },
  set(newValue) {
    emits('update:modelValue', newValue);
  }
});

const bgStyle = computed(() =>
  props.bgColor ? { backgroundColor: props.bgColor } : {} );

// size 프롭에 따른 클래스 계산
const sizeClass = computed(() => {
  switch (props.size) {
    case 'xsmall':
      return 'xsmall-input';
    case 'small':
      return 'small-input';
    case 'large':
      return 'large-input';
    case 'full':
      return 'full-input';
    case 'medium':
    default:
      return 'medium-input';
  }
});

// validationType에 따른 정규식 패턴
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
    case 'money':
      return /^[0-9,]*$/;
    default:
      return null;
  }
});

const hasPatternError = computed(() => touched.value && !isValid.value);
const hasExternalError = computed(() => props.externalInvalid || props.externalError);

const errorToShow = computed(() => {
  if (hasExternalError.value) return props.externalError;   // 비즈니스 오류
  if (hasPatternError.value)  return props.errorMessage;    // 패턴 오류
  return '';
});

// 입력값의 유효성 여부 판단 (검증 대상이 없으면 true)
const isValid = computed(() => {
  if (!pattern.value) return true;
  return pattern.value.test(internalValue.value);
});

// 필드 터치 여부 (blur 이벤트에서 true로 변경)
const touched = ref(false);

const marginStyle = computed(() => {
  return {
    marginLeft: `${props.marginLeft} !important`,
    marginRight: `${props.marginRight} !important`,
    marginTop: `${props.marginTop} !important`,
    marginBottom: `${props.marginBottom} !important`
  };
});

const customHeightStyle = computed(() => {
  if (props.customHeight) {
    return {
      height: props.customHeight + ' !important'
    };
  }
  return {};
});

// CustomMonthPicker의 값 변경 핸들러
const handleMonthPickerUpdate = (value) => {
  emits('update:modelValue', value);
};
</script>

<style scoped>
input {
  border-radius: 3px;
  font-size: 0.8rem;
}

input:not([type="file"]) {
  height: 38px;
}

.xsmall-input {
  width: 130px;
}

.small-input {
  width: 160px;
}

.medium-input {
  width: 250px;
}

.large-input {
  width: 350px;
}

.full-input {
  width: 100%;
}

.xsmall-input::placeholder,
.small-input::placeholder,
.medium-input::placeholder,
.large-input::placeholder,
.full-input::placeholder {
  font-size: 0.7rem;
  color: #8f8f8f;
}

/* 유효하지 않은 경우 빨간 테두리 적용 */
.is-invalid {
  border-color: red;
}

/* 에러 메시지 영역: reserveErrorSpace가 true일 때 적용 */
.error-message.reserved {
  min-height: 1rem; /* 항상 일정한 공간 확보 (원하는 높이로 조정 가능) */
  margin-top: 5px;
  color: red;
  font-size: 0.65rem;
}

/* reserveErrorSpace가 false인 경우 기본 error-message 스타일 */
.error-message {
  margin-top: 5px;
  color: red;
  font-size: 0.65rem;
}

.default-textfield input[type="radio"] {
  vertical-align: middle; 
  transform-origin: center center;
}

@media (max-width: 700px) {
  .default-textfield input[type="radio"] {
    transform: scale(0.8);
  }
}

@media (max-width: 650px) {
  .default-textfield input {
    height: 33px !important;
  }

  input {
    /* height: 33px !important; */
    font-size: 0.7rem !important;
  }

  .default-textfield input[type="month"],
  .default-textfield input[type="date"] {
    min-width: 105px;
    font-size: 0.6rem !important;
  }

  /* 사이즈에 따른 input 넓이 설정 */
  .xsmall-input { 
    width: 90px;
  }

  .small-input {
    width: 150px;
  }

  .medium-input {
    font-size: 0.75rem;
    width: 220px;
  }

  .large-input {
    font-size: 0.8rem;
    width: 280px;
  }

  .full-input {
    font-size: 0.8rem;
  }

  /* 모든 사이즈 placeholder 0.65rem */
  .xsmall-input::placeholder,
  .small-input::placeholder,
  .medium-input::placeholder,
  .large-input::placeholder,
  .full-input::placeholder,
  .error-message,
  .error-message.reserved  {
    font-size: 0.65rem;
  }
}

@media (max-width: 500px) {
  /* input {
    height: 28px !important;
  } */

  .default-textfield input[type="month"],
  .default-textfield input[type="date"] {
    min-width: 105px;
    font-size: 0.6rem !important;
  }

  /* 사이즈에 따른 input 넓이 설정 */
  .xsmall-input {
    width: 80px;
  }

  .small-input {
    width: 120px;
  }

  .medium-input {
    width: 180px;
    padding: 0.4rem 0.6rem !important;
    line-height: 1.5;
  }

  .large-input {
    width: 210px;
    line-height: 1.5;
  }

  .full-input {
    line-height: 1.5;
  }

  .default-textfield input[type="month"] {
    font-size: 0.6rem;
  }

  .default-textfield input[type="date"] {
    font-size: 0.6rem;
  }
  /* 모든 사이즈 placeholder 0.6rem */
  .xsmall-input::placeholder,
  .small-input::placeholder,
  .medium-input::placeholder,
  .large-input::placeholder,
  .full-input::placeholder,
  .error-message,
  .error-message.reserved {
    font-size: 0.6rem;
  }
}
@media (max-width: 400px) {
  .default-textfield input[type="month"],
  .default-textfield input[type="date"] {
    min-width: 102px;
    font-size: 0.6rem !important;
  }
}
</style>