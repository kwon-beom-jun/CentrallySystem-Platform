<template>
  <div class="custom-month-picker" :class="{ 'full-width': size === 'full' }" :style="marginStyle">
    <!-- Input 필드 -->
    <div class="month-input-wrapper" @click="togglePicker">
      <input
        type="text"
        :value="displayValue"
        readonly
        :placeholder="placeholder"
        :class="['form-control', sizeClass, { 'is-invalid': errorToShow }]"
        :disabled="disabled"
        :style="[customHeightStyle, bgStyle]"
      />
      <div class="calendar-icon">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8" y1="2" x2="8" y2="6"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
      </div>
    </div>

    <!-- 커스텀 Month Picker -->
    <div v-if="showPicker" class="month-picker-dropdown" ref="pickerRef">
      <div class="picker-header">
        <button type="button" class="nav-btn" @click="previousYear">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15,18 9,12 15,6"/>
          </svg>
        </button>
        <h3 class="year-title">{{ currentYear }}</h3>
        <button type="button" class="nav-btn" @click="nextYear">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </button>
      </div>
      
      <div class="months-grid">
        <button
          v-for="(month, index) in months"
          :key="index"
          type="button"
          class="month-btn"
          :class="{
            'selected': isSelected(index),
            'current': isCurrent(index)
          }"
          @click="selectMonth(index)"
        >
          {{ month }}
        </button>
      </div>
      
      <div class="picker-footer">
        <button type="button" class="today-btn" @click="selectToday">
          오늘
        </button>
      </div>
    </div>

    <!-- 에러 메시지 영역 -->
    <template v-if="reserveErrorSpace">
      <div class="error-message reserved">
        <span v-if="errorToShow">{{ errorToShow }}</span>
      </div>
    </template>
    <template v-else>
      <div v-if="touched && !isValid" class="error-message">
        <span v-if="errorToShow">{{ errorToShow }}</span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, defineProps, defineEmits, ref, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  modelValue: {
    type: [String, Date],
    default: ''
  },
  placeholder: {
    type: String,
    default: 'YYYY-MM 선택'
  },
  size: {
    type: String,
    default: 'medium',
    validator: value => ['xsmall', 'small', 'medium', 'large', 'full'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  reserveErrorSpace: {
    type: Boolean,
    default: false
  },
  errorMessage: {
    type: String,
    default: '올바른 월을 선택해주세요.'
  },
  externalError: {
    type: String,
    default: ''
  },
  externalInvalid: {
    type: Boolean,
    default: false
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
  bgColor: {
    type: String,
    default: ''
  }
});

const emits = defineEmits(['update:modelValue', 'change']);

const showPicker = ref(false);
const currentYear = ref(new Date().getFullYear());
const touched = ref(false);
const pickerRef = ref(null);

const months = [
  '1월', '2월', '3월', '4월', '5월', '6월',
  '7월', '8월', '9월', '10월', '11월', '12월'
];

// 현재 선택된 값을 표시하는 computed
const displayValue = computed(() => {
  if (!props.modelValue) return '';
  
  let date;
  if (typeof props.modelValue === 'string') {
    date = new Date(props.modelValue + '-01');
  } else {
    date = props.modelValue;
  }
  
  if (isNaN(date.getTime())) return '';
  
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  return `${year}-${month.toString().padStart(2, '0')}`;
});

// Size 클래스
const sizeClass = computed(() => {
  switch (props.size) {
    case 'xsmall': return 'xsmall-input';
    case 'small': return 'small-input';
    case 'large': return 'large-input';
    case 'full': return 'full-input';
    case 'medium':
    default: return 'medium-input';
  }
});

// 스타일
const marginStyle = computed(() => ({
  marginLeft: `${props.marginLeft} !important`,
  marginRight: `${props.marginRight} !important`,
  marginTop: `${props.marginTop} !important`,
  marginBottom: `${props.marginBottom} !important`
}));

const customHeightStyle = computed(() => {
  if (props.customHeight) {
    return { height: props.customHeight + ' !important' };
  }
  return {};
});

const bgStyle = computed(() =>
  props.bgColor ? { backgroundColor: props.bgColor } : {}
);

// 에러 관련
const hasExternalError = computed(() => props.externalInvalid || props.externalError);
const hasPatternError = computed(() => touched.value && !isValid.value);

const errorToShow = computed(() => {
  if (hasExternalError.value) return props.externalError;
  if (hasPatternError.value) return props.errorMessage;
  return '';
});

const isValid = computed(() => {
  if (!props.modelValue) return true;
  return displayValue.value !== '';
});

// 월 선택 관련 메서드
const isSelected = (monthIndex) => {
  if (!props.modelValue) return false;
  
  let date;
  if (typeof props.modelValue === 'string') {
    date = new Date(props.modelValue + '-01');
  } else {
    date = props.modelValue;
  }
  
  if (isNaN(date.getTime())) return false;
  
  return date.getFullYear() === currentYear.value && date.getMonth() === monthIndex;
};

const isCurrent = (monthIndex) => {
  const today = new Date();
  return today.getFullYear() === currentYear.value && today.getMonth() === monthIndex;
};

const selectMonth = (monthIndex) => {
  const selectedValue = `${currentYear.value}-${(monthIndex + 1).toString().padStart(2, '0')}`;
  emits('update:modelValue', selectedValue);
  emits('change', selectedValue);
  touched.value = true;
  showPicker.value = false;
};

const previousYear = () => {
  currentYear.value--;
};

const nextYear = () => {
  currentYear.value++;
};

const selectToday = () => {
  const today = new Date();
  currentYear.value = today.getFullYear();
  selectMonth(today.getMonth());
};

const togglePicker = () => {
  if (props.disabled) return;
  showPicker.value = !showPicker.value;
  touched.value = true;
  
  if (showPicker.value && props.modelValue) {
    let date;
    if (typeof props.modelValue === 'string') {
      date = new Date(props.modelValue + '-01');
    } else {
      date = props.modelValue;
    }
    
    if (!isNaN(date.getTime())) {
      currentYear.value = date.getFullYear();
    }
  }
};

// 외부 클릭시 닫기
const handleClickOutside = (event) => {
  if (pickerRef.value && !pickerRef.value.contains(event.target) && 
      !event.target.closest('.month-input-wrapper')) {
    showPicker.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
.custom-month-picker {
  position: relative;
  display: inline-block;
}

.custom-month-picker.full-width {
  display: block;
  width: 100%;
}

.month-input-wrapper {
  position: relative;
  cursor: pointer;
}

.month-input-wrapper input {
  border-radius: 3px;
  font-size: 0.8rem;
  height: 38px;
  cursor: pointer;
  padding-right: 28px;
  border: 1px solid #ced4da;
  transition: all 0.2s ease;
  background: white;
}

.month-input-wrapper input:hover {
  border-color: #adb5bd;
}

.month-input-wrapper input:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
  outline: 0;
}

.calendar-icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #494949;
  pointer-events: none;
}

.calendar-icon svg {
  width: 13px;
  height: 13px;
}

.month-picker-dropdown {
  position: absolute;
  top: calc(100% + 1px);
  left: 0;
  background: white;
  border: 1px solid #dadce0;
  border-radius: 4px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  padding: 8px;
  min-width: 200px;
  max-width: 220px;
  width: auto;
  animation: slideDown 0.15s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 8px 4px;
}

.year-title {
  font-size: 0.8rem;
  font-weight: 500;
  color: #202124;
  margin: 0;
}

.nav-btn {
  background: none;
  border: none;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  color: #5f6368;
  transition: all 0.1s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.nav-btn:hover {
  background-color: #f1f3f4;
  color: #202124;
}

.nav-btn svg {
  width: 12px;
  height: 12px;
}

.months-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  margin-bottom: 8px;
}

.month-btn {
  padding: 8px 4px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.685rem;
  font-weight: 400;
  color: #202124;
  transition: all 0.1s ease;
  text-align: center;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.month-btn:hover {
  background: #f1f3f4;
}

.month-btn.current {
  background: #e8f0fe;
  color: #1a73e8;
  font-weight: 500;
}

.month-btn.selected {
  background: #1a73e8;
  color: white;
  font-weight: 500;
}

.month-btn.selected:hover {
  background: #1557b0;
  color: white;
}

.picker-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 5px;
  border-top: 1px solid #dadce0;
}

.today-btn {
  background: none;
  border: none;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 500;
  color: #1a73e8;
  transition: all 0.1s ease;
}

.today-btn:hover {
  background: #f1f3f4;
}

/* 사이즈별 input 스타일 */
.xsmall-input { width: 130px; }
.small-input { width: 160px; }
.medium-input { width: 250px; }
.large-input { width: 350px; }
.full-input { width: 100%; }

.xsmall-input::placeholder,
.small-input::placeholder,
.medium-input::placeholder,
.large-input::placeholder,
.full-input::placeholder {
  font-size: 0.7rem;
  color: #8f8f8f;
}

/* 에러 스타일 */
.is-invalid {
  border-color: red;
}

.error-message {
  margin-top: 5px;
  color: red;
  font-size: 0.65rem;
}

.error-message.reserved {
  min-height: 1rem;
}

/* 모바일 반응형 */
@media (max-width: 650px) {
  .month-input-wrapper input {
    border-radius: 3px;
    font-size: 0.6rem;
    height: 33px;
    /* cursor: pointer;
    padding-right: 28px;
    border: 1px solid #ced4da;
    transition: all 0.2s ease;
    background: white; */
  }

  .calendar-icon {
    position: absolute;
    right: 10px;
  }

  .calendar-icon svg {
    width: 11px;
    height: 11px;
  }
  
  .month-picker-dropdown {
    min-width: 180px;
    max-width: 200px;
    padding: 6px;
  }
  
  .months-grid {
    gap: 3px;
  }
  
  .month-btn {
    padding: 6px 3px;
    font-size: 0.63rem;
    min-height: 28px;
  }
  
  .year-title {
    font-size: 0.75rem;
  }
  
  .picker-header {
    padding: 6px 3px;
    margin-bottom: 6px;
  }
  
  .nav-btn {
    width: 20px;
    height: 20px;
  }
  
  .nav-btn svg {
    width: 10px;
    height: 10px;
  }
  
  .today-btn {
    padding: 5px 6px;
    font-size: 0.7rem;
  }
}

@media (max-width: 500px) {
  .month-picker-dropdown {
    min-width: 170px;
    max-width: 190px;
    padding: 5px;
  }
  
  .months-grid {
    gap: 2px;
  }
  
  .month-btn {
    padding: 5px 2px;
    font-size: 0.63rem;
    min-height: 26px;
  }
  
  .today-btn {
    padding: 4px 5px;
    font-size: 0.65rem;
  }

  .xsmall-input { width: 90px; }
  .small-input { width: 120px; }
  .medium-input { width: 180px; }
  .large-input { width: 210px; }
}
</style>