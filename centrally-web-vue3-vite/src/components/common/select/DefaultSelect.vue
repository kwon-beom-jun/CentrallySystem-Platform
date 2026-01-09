<!--
  예시 코드:
    <DefaultSelect
      v-model="selectedValue"                   // 선택된 값 (v-model)
      :options="[
         { value: '', label: '옵션 선택' },
         { value: 'option1', label: '옵션 1' },
         { value: 'option2', label: '옵션 2' }
      ]"                                        // 옵션 배열: 각 옵션은 value와 label을 포함함
      placeholder="옵션을 선택하세요"            // 선택되지 않았을 때 보여질 안내 문구
      size="medium"                             // 컴포넌트 크기: 'small', 'medium', 'large', 'full'
      :disabled="false"                         // 컴포넌트 비활성화 여부
      @change="handleSelectChange"              // 선택 값 변경 시 호출되는 이벤트
    />
-->

<template>
  <div class="default-select-wrapper" :style="marginStyle">
    <select
      :id="computedId"
      :value="modelValue"
      @change="onChange"
      :class="['default-select', sizeClass]"
      :disabled="disabled"
      :style="customHeightStyle"
    >
      <!-- placeholder가 있을 경우, 선택이 안 된 상태에 표시할 옵션 (선택 불가) -->
      <option
        v-if="placeholder"
        value=""
        disabled
        hidden
        :selected="modelValue === ''"
      >
        {{ placeholder }}
      </option>
      <!-- 옵션 목록 렌더링 -->
      <option
        v-for="(option, index) in options"
        :key="index"
        :value="option.value"
      >
        {{ option.label }}
      </option>
    </select>
  </div>
</template>

<script setup>
// import computed, defineProps, defineEmits from Vue
import { computed } from 'vue';

// ------------------------------
// Props 정의 (각 prop 위에 상세 주석 포함)
// ------------------------------

// modelValue: v-model로 바인딩되는 값 (선택된 옵션의 값)
// 타입: String 또는 Number, 기본값은 빈 문자열 ('')
const props = defineProps({
  // id: select 요소에 적용할 id 값 (문자열)
  id: {
    type: String,
    required: false,
    default: ''
  },
  modelValue: {
    type: [String, Number, null],
    required: false,
    default: null
  },
  // options: 선택 옵션 배열. 각 옵션은 { value: any, label: string } 형태의 객체여야 함.
  options: {
    type: Array,
    required: true,
    default: () => []
  },
  // placeholder: 옵션이 선택되지 않았을 때 보여질 안내 문구 (문자열).
  placeholder: {
    type: String,
    required: false,
    default: ''
  },
  // size: 컴포넌트의 사이즈를 지정
  // ('xsmall', 'small', 'medium', 'large', 'full', full-xsmall, full-small, full-medium, full-large).
  // 선택된 값에 따라 내부 스타일 (padding, font-size 등)을 조절함.
  size: {
    type: String,
    default: 'medium',
    validator: (value) => [
      'xsmall', 'small', 'medium', 'large', 'full',
      /* 'full-xsmall', 'full-small', 'full-medium', 'full-large' */
    ].includes(value)
  },
  // disabled: 컴포넌트의 비활성화 여부. true이면 선택할 수 없음.
  disabled: {
    type: Boolean,
    required: false,
    default: false
  },
  // 마진 (왼쪽, 오른쪽, 위, 아래)
  marginLeft: {
    type: String,
    required: false,
    default: '0'
  },
  marginRight: {
    type: String,
    required: false,
    default: '0'
  },
  marginTop: {
    type: String,
    required: false,
    default: '0'
  },
  marginBottom: {
    type: String,
    required: false,
    default: '0'
  },
  customHeight: {
    type: String,
    default: ''
  }
});

// computedId: id prop이 빈 문자열이면 undefined를 반환하여 id 속성을 렌더링하지 않음
const computedId = computed(() => {
  return props.id !== '' ? props.id : undefined;
});

// defineEmits: v-model 업데이트와 change 이벤트를 발생시키기 위한 이벤트 정의
const emit = defineEmits([
  'update:modelValue',  // v-model 업데이트 이벤트
  'change'              // 선택 값 변경 시 외부에 알리기 위한 이벤트
]);

// sizeClass: size prop에 따른 CSS 클래스 결정 (computed 속성)
const sizeClass = computed(() => {
  return `default-select--${props.size}`;
});

// margin 관련 computed 스타일 객체 생성
const marginStyle = computed(() => {
  return `
    margin-left: ${props.marginLeft} !important;
    margin-right: ${props.marginRight} !important;
    margin-top: ${props.marginTop} !important;
    margin-bottom: ${props.marginBottom} !important;
  `;
});

const customHeightStyle = computed(() => {
  if (props.customHeight) {
    return {
      height: props.customHeight + ' !important'
    };
  }
  return {};
});

/**
 * onChange: select 요소의 change 이벤트 핸들러
 * 빈 문자열 선택 시 null로 변환하여 emit
 */
function onChange(event) {
  let selectedValue = event.target.value;
  // 빈 문자열이면 null로 변환
  if (selectedValue === '') {
    selectedValue = null;
  }
  // v-model 업데이트 이벤트 발생
  emit('update:modelValue', selectedValue);
  // change 이벤트 발생 (외부 핸들러에서 사용 가능)
  emit('change', selectedValue);
}
</script>

<style scoped>
/* DefaultSelect의 기본 래퍼 */
.default-select-wrapper {
  position: relative;
  display: inline-block;
}

/* 기본 select 스타일 */
.default-select {
  display: block;               /* 블록 요소로 만들어 줄 바꿈 보장 */
  /* width: auto;                  w-auto: 컨텐츠 크기에 맞게 너비 자동 조절 */
  /*padding: 0.375rem 0.75rem;      내부 여백 */
  padding-left: 0.8rem;
  line-height: 1.5;             /* 줄 높이 */
  color: #495057;               /* 텍스트 색상 */
  background-color: #fff;       /* 배경색 */
  background-clip: padding-box; /* 패딩 영역까지 배경색 적용 */
  border: 1px solid #ced4da;    /* 경계선 */
  border-radius: 0.25rem;       /* 약간의 둥근 모서리 */
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
  appearance: none; /* 기본 브라우저 스타일 제거 */
  /* 기본 높이 (부트스트랩 기본 높이 참고) */
  /* height: calc(1.5em + 0.75rem + 2px); */
  height: 38px;
  /* 화살표 아이콘 크기 축소 */
  background-size: 10px !important;
  /* 우측 패딩도 조금 줄여서 아이콘이 너무 들뜨지 않도록 조절 */
  padding-right: 20px !important;
}

/* 드롭다운 화살표 아이콘 추가 (Bootstrap 스타일) */
.default-select {
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg viewBox='0 0 16 16' fill='%23495057' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M7.247 11.14L2.451 5.658c-.566-.614-.106-1.658.633-1.658h10.433c.74 0 1.2 1.044.633 1.658l-4.796 5.482a.75.75 0 0 1-1.137 0z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 12px;
  padding-right: 25px !important; /* 화살표 아이콘 공간 확보 */
}

/* 옵션 텍스트 기본: 통일된 0.8rem */
.default-select option {
  font-size: 0.8rem;
  background-color: #fff;
  color: #495057;
}

/* 포커스 시 테두리 색상 변경 */
.default-select:focus {
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

/* 비활성화(disabled) 상태 스타일 */
.default-select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 사이즈에 따른 스타일 조절 */
.default-select--xsmall {
  width: 100px;
  font-size: 0.8rem;
}
.default-select--small {
  width: 127px;
  font-size: 0.8rem;
}
.default-select--medium {
  width: 200px;
  font-size: 0.8rem;
}
.default-select--large {
  width: 300px;
  font-size: 0.8rem;
}
.default-select--full {
  width: 100%;
  font-size: 0.8rem;
}

@media (max-width: 650px) {
  .default-select {
    height: 33px !important;
    font-size: 0.7rem !important;
  }

  /* xsmall */
  .default-select--xsmall {
    width: 85px;
  }
  /* small */
  .default-select--small {
    width: 95px;
  }
  /* medium */
  .default-select--medium {
    width: 150px;
  }
  /* large */
  .default-select--large {
    width: 210px;
  }
  /* full */
  .default-select--full {
    width: 100%;
  }

  .default-select option {
    font-size: 0.65rem;
  }

  .default-select {
    /* 화살표 아이콘 크기 축소 */
    background-size: 9px !important;
    /* 우측 패딩도 조금 줄여서 아이콘이 너무 들뜨지 않도록 조절 */
    padding-right: 33px !important;
  }
}

@media (max-width: 500px) {
  .default-select {
    min-width: 75px;
    /* height: 28px !important; */
    /* font-size: 0.65rem !important; */
    /* 화살표 아이콘 크기 축소 */
    background-size: 8px !important;
    /* 우측 패딩도 조금 줄여서 아이콘이 너무 들뜨지 않도록 조절 */
    padding-right: 20px !important;
  }
  /* xsmall */
  .default-select--xsmall {
    width: 75px;
  }
  /* small */
  .default-select--small {
    width: 90px;
  }
  /* medium */
  .default-select--medium {
    width: 120px;
  }
  /* large */
  .default-select--large {
    width: 150px;
  }
  /* full */
  .default-select--full {
    width: 100%;
  }

  /* .default-select option {
    font-size: 0.65rem;
  } */
}
</style>
