<!--

  ──────────────── 1) 필수 / 선택 프롭 ───────────────
    • columns      : 배열 [ { key, label, width, minWidth?, mobile? }, … ]
                     width: 숫자(픽셀) 또는 'auto'(나머지 공간 차지)
                     minWidth: auto 사용 시 최소 너비 지정 (기본값: 100)
                     예: { key: 'name', label: '이름', width: 100 }
                     예: { key: 'description', label: '설명', width: 'auto' } ← 나머지 공간, 최소 100px
                     예: { key: 'function', label: '기능', width: 'auto', minWidth: 150 } ← 나머지 공간, 최소 150px
    • data         : 배열 [ { key: value, … }, … ]
    • footerData   : (옵션) ['합계', '…'] 
    • mobileCard   : true → 650 px 이하에서 카드 UI (기본 false)
    • selectable   : 행 체크박스 활성화
    • rowClick     : 행 클릭 콜백
    • openSidebar  : 행 클릭 → 사이드바 실행 함수
    • usePagination: true → 페이지네이션 표시 (currentPage, totalPages 필요)
    • 기타         : bodyFontSize / fixedHeader / scroll 등


  ──────────────── 테이블 로우 활성화/비활성화 ───────────────
  컬럼이 enabled이고 값이 true, false이면 테이블 로우가 활성화/비활성화 처리됨


  ──────────────── 테이블 로우 고정 ───────────────
  data 배열의 객체에 isFixed: true 속성(변수)이 있으면 해당 행에 고정 스타일이 적용됩니다.
  이 속성이 true이면 해당 <tr>에 'fixed-row' 클래스가 추가되어, <style>에서 별도 디자인이 가능합니다.
  (예: data: [ { name: '고정 공지', isFixed: true }, { name: '일반 공지' } ])


  ──────────────── 2) 컬럼 예시 (mobile 옵션) ───────────────
  <DefaultTable
    :columns="[
      { key:'id',     label:'번호',   width:50,
        mobile:{ line:1, inline:true, prefix:'',    suffix:' | '       } },
      {
        key: 'permission',
        label: '권한',
        width: 150,
        type: 'select',
        getOptions: (row) => {
          // 예시: row.projects (서비스명)에 따라 해당 서비스의 권한 옵션을 반환
          // (실제 옵션은 필요에 따라 변경)
          const rolesByService = {
            hrm: [
              { value: '사원', label: '사원' },
              { value: '관리자', label: '관리자' }
            ],
            receipt: [
              { value: '관리자', label: '관리자' },
              { value: '결재자', label: '결재자' }
            ],
            system: [
              { value: '시스템', label: '시스템' }
            ]
          };
          return rolesByService[row.projects] || [];
        },
        mobile:{ line:3, inline:false }
      }
    ]"
    :data="userData"                   // 테이블에 표시할 데이터 배열 (각 행에 { name, email, department, team, projects, permission } 등 포함)
    :footerData="footerData"           // (옵션) 테이블 푸터 데이터
    :openSidebar="openSidebar"         // 행 클릭 시 사이드바를 열기 위한 함수
    :rowClick="handleRowClick"         // 행 클릭 시 실행할 콜백 함수
    :bodyFontSize="'0.75rem'"         // 테이블 본문 폰트 사이즈
    :fixedHeader="true"                // 헤더 고정 여부
    @row-updated="handleRowUpdated"    // 셀렉트 변경 시, 해당 행(row)의 업데이트를 전달받음
  />

  ※ line, inline 기능이 현재 하자가 있음
    ▸ line    : 카드 안에서 몇 번째 줄에 놓일지(1 부터)
    ▸ inline  : true → 같은 줄에 나란히 / false → 개별 줄
  ▸ prefix  : 값 앞에 붙일 문자열 (공백은 \u00A0 사용)
  ▸ suffix  : 값 뒤에 붙일 문자열
  ▸ bold    : true → font-weight:900
  ▸ fontSize: '0.8rem' 등 CSS 폰트 크기 값
  ▸ paddingTop   : '10px' 등 CSS 상 여백 값
  ▸ paddingBottom: '10px' 등 CSS 하 여백 값

  ──────────────── 2-1) 모바일 표시 제어 옵션 ───────────────
  ▸ mobileDisable    : true → 모바일에서 해당 컬럼 숨김 (기존 속성)
  ▸ mobile.hidden    : true → 모바일에서 해당 컬럼 숨김 (별칭, mobileDisable과 동일 효과)
  ▸ mobile.divider       : 'top' | 'bottom' | 'both' | true
                          true 또는 'bottom' → 하단 실선
                          'top'              → 상단 실선
                          'both'             → 상·하단 실선
  ▸ mobile.dividerTop    : true → 상단 실선 (별칭)
  ▸ mobile.dividerBottom : true → 하단 실선 (별칭)
  ▸ mobile.dividerTopGap      : '10px' 등 – 위쪽(상단) 실선의 위/아래 간격(동일 값)
                                 · 위 = margin-top, 아래 = padding-top
  ▸ mobile.dividerTopGapAbove : '6px'  등 – 위쪽(상단) 실선의 윗쪽 간격만
  ▸ mobile.dividerTopGapBelow : '8px'  등 – 위쪽(상단) 실선의 아랫쪽 간격만
  ▸ mobile.dividerBottomGap   : '10px' 등 – 아래쪽(하단) 실선의 위/아래 간격(동일 값)
                                 · 위 = padding-bottom, 아래 = margin-bottom
  ▸ mobile.dividerBottomGapAbove : '8px'  등 – 아래쪽(하단) 실선의 윗쪽 간격만
  ▸ mobile.dividerBottomGapBelow : '12px' 등 – 아래쪽(하단) 실선의 아랫쪽 간격만

  예시)
    { mobile:{ divider:'top',    dividerTopGap:'20px' } }                         // 위 실선 위·아래 20px
    { mobile:{ divider:'bottom', dividerBottomGap:'12px' } }                      // 아래 실선 위·아래 12px
    { mobile:{ divider:'both',   dividerTopGap:'6px', dividerBottomGap:'14px' } } // 둘 다 개별 동일값
    { mobile:{ divider:'top',    dividerTopGapAbove:'10px', dividerTopGapBelow:'4px' } } // 위 실선 윗/아랫값 분리
    { mobile:{ divider:'bottom', dividerBottomGapAbove:'6px', dividerBottomGapBelow:'16px' } } // 아래 실선 분리
    ※ 권장: dividerTopGap / dividerBottomGap 만 사용


  ──────────────── 3) 컴포넌트 사용 샘플 ───────────────
    <DefaultTable
      :columns="columns"
      :data="rows"
      :mobileCard="true"
      selectable
      :rowClick="handleRow"
    />


설명:
- 만약 행의 셀렉트 박스를 클릭하면, @click.stop 처리되어 행 클릭 이벤트가 발생하지 않습니다.
- 행 전체 클릭 시에는 openSidebar와 rowClick 함수가 호출됩니다.
- 셀렉트 값 변경 시 onSelectChange()에서 row-updated 이벤트가 emit되어 상위에서 변경된 행 정보를 받을 수 있습니다.
-->
<template>
  <div v-bind="$attrs">
    <!-- 데이터 없을 때 이미지 표시 -->
    <div v-if="noData && props.showNoDataImage" class="no-data-container" :style="{
      '--no-data-height': typeof props.noDataImageHeight === 'number' ? `${props.noDataImageHeight}px` : props.noDataImageHeight
    }">
      <v-img
        class="mx-width-700 no-results-found"
        src="/img/common/state/001.png"
      />
    </div>

    <!-- 데이터 있을 때 테이블과 페이징 -->
    <div v-else>
    <div
      class="table-container"
      :class="[
        { 'card-enabled': useCard }, 
        cardClass,
        { 'mobile-height-fixed': shouldFixMobileHeight },
        { 'dynamic-style': props.dynamicStyle }
      ]"
      :style="tableContainerStyles"
    >
      <table
        v-if="showTable"
        :class="['table', { 'no-header': noColumns }]"
        :style="{ '--table-body-font-size': bodyFontSize }"
      >
      <!-- thead -->
      <thead v-if="!noColumns" :class="{ 'fixed-header': fixedHeader }">
        <tr>
          <th v-if="selectable" class="checkbox-col">
            <input
              type="checkbox"
              :checked="allSelected"
              :indeterminate="someSelected"
              @click.stop="toggleSelectAll($event.target.checked)"
            />
          </th>
          <th
            v-for="column in visibleColumns"
            :key="column.key"
            :style="columnStyles(column)"
          >
            {{ column.label }}
          </th>
        </tr>
      </thead>

      <!-- tbody -->
      <tbody>
        <!-- tr @click="onRowClick(item)" 
             → 행을 클릭하면 openSidebar와 rowClick을 둘 다 시도 -->
        <!-- :key="`${rowIndex}-${currentPage}`" -->
        <tr
          v-for="(item, rowIndex) in data"
          :key="rowIndex"
          @click="onRowClick(item)"
          :class="{ 
            'disabled-row': item.enabled === false, 
            'fixed-row': item.isFixed
          }"
        >
          <!-- 각 행 체크박스 -->
          <td 
            v-if="selectable" 
            class="checkbox-col" 
            @click.stop
            :class="useCard ? 'mobile-checkbox' : ''"
          >
            <input
              type="checkbox"
              :checked="isRowSelected(item)"
              @change="toggleRowSelect(item, $event.target.checked)"
            />
          </td>
          <td
            v-for="column in visibleColumns"
            :key="column.key"
            :data-label="column.label"
            :data-prefix="useCard ? column.mobile?.prefix ?? '' : ''"
            :data-suffix="useCard ? column.mobile?.suffix ?? '' : ''"
            :class="[
              column.customClass ? column.customClass(item[column.key]) : '',
              column.type === 'select' || column.type === 'button' ? '' : 'ellipsis',
              'tooltip-cell',
              useCard ? `mobile-line-${column.mobile?.line ?? 99}` : '',
              useCard && column.mobile?.inline ? 'mobile-inline' : '',
              useCard && column.mobile?.align === 'right' ? 'mobile-align-right' : '',
              useCard && (
                    column.mobile?.divider === true   || column.mobile?.divider === 'bottom' || 
                    column.mobile?.divider === 'both' || column.mobile?.dividerBottom
                  ) ? 'mobile-divider-bottom' : '',
              useCard && (
                    column.mobile?.divider === 'top' || column.mobile?.divider === 'both' || 
                    column.mobile?.dividerTop
                  ) ? 'mobile-divider-top' : '',
              !useCard && column.align === 'left'   ? 'align-left'   : '',
              !useCard && column.align === 'center' ? 'align-center' : '',
              !useCard && column.align === 'right'  ? 'align-right'  : '',
            ]"
            :style="[
              columnStyles(column),
              useCard ? mobileCellStyle(column) : {},
              useCard ? dividerInlineStyle(column) : {},
              !useCard && column.desktopStyle ? column.desktopStyle : {}
            ]"
            @mouseenter="(event) => showTooltip(event, item, column, `${rowIndex}-${column.key}`)"
            @mouseleave="() => hideTooltip(`${rowIndex}-${column.key}`)"
          >
            <!-- (A) type='select': <DefaultSelect> -->
            <!-- type='select' → DefaultSelect
                 여기서 @click.stop으로 행 클릭 이벤트 전파 중단 -->
            <template v-if="column.type === 'select'">
              <div @click.stop style="width: 100%">
                <DefaultSelect
                  v-model="item[column.key]"
                  :options="getSelectOptions(column, item)"
                  size="full"
                  style="width: 100%"
                  :customHeight="selectHeight"
                  @change="(val) => onSelectChange(item, column, val)"
                />
              </div>
            </template>

            <!-- (B) type='button': '삭제' 버튼 -->
            <template v-else-if="column.type === 'button'">
              <!-- 클릭 시 이벤트 전파 중단 (@click.stop) -->
              <div @click.stop style="width: 100%; display: flex; align-items: center;" class="button-cell-wrapper">
                <DefaultButton
                  :size="column.buttonSize || 'full-small'"
                  :color="column.buttonColor || 'gray'"
                  :customHeight="buttonHeight"
                  @click="() => onButtonClick(item, column)"
                >
                  {{ column.buttonText || "" }}
                </DefaultButton>
                <!-- 모바일 카드 모드에서 suffix 수동 추가 -->
                <span v-if="useCard && column.mobile?.suffix" class="button-suffix">{{ column.mobile.suffix }}</span>
              </div>
            </template>

            <!-- (C) 일반 텍스트 -->
            <template v-else>
              <span class="cell-text">
                {{
                  column.customValue ? column.customValue(item) : item[column.key]
                }}
              </span>
            </template>

          </td>
        </tr>

        <!-- Placeholder 행 -->
        <tr
          v-for="n in fillerRows"
          :key="'placeholder-' + n"
          class="placeholder-row"
          aria-hidden="true"
        >
          <td v-if="selectable" class="checkbox-col"></td>
          <td
            v-for="column in visibleColumns"
            :key="column.key"
            :data-label="column.label"
            :data-prefix="(useCard && column.mobile?.prefix) || ''"
            :data-suffix="(useCard && column.mobile?.suffix) || ''"
            :class="[
              'ellipsis',
              useCard ? `mobile-line-${column.mobile?.line || 99}` : '',
              useCard && column.mobile?.inline ? 'mobile-inline' : '',
              useCard && column.mobile?.align === 'right' ? 'mobile-align-right' : '',
              useCard && (
                    column.mobile?.divider === true   || column.mobile?.divider === 'bottom' || 
                    column.mobile?.divider === 'both' || column.mobile?.dividerBottom
                  ) ? 'mobile-divider-bottom' : '',
              useCard && (
                    column.mobile?.divider === 'top' || column.mobile?.divider === 'both' || 
                    column.mobile?.dividerTop
                  ) ? 'mobile-divider-top' : '',
            ]"
            :style="[
              columnStyles(column),
              useCard ? mobileCellStyle(column) : {},
              useCard ? dividerInlineStyle(column) : {},
              !useCard && column.desktopStyle ? column.desktopStyle : {}
            ]"
          ></td>
        </tr>
      </tbody>

      <!-- tfoot -->
      <tfoot v-if="visibleFooterData.length && !useCard"
              :class="{ 'mobile-footer': isMobileWidth }">
        <tr>
          <td v-if="selectable"></td>
          <td
            v-for="(footerItem, index) in visibleFooterData"
            :key="'f-'+index"
            :class="footerAlignClassVisible(index)"
          >
            {{ footerItem }}
          </td>
        </tr>
      </tfoot>
      </table>
    </div>

    <!-- 페이지네이션 (usePagination이 true일 때만 표시) -->
    <DefaultPagination
      v-if="props.usePagination"
      :currentPage="props.currentPage"
      :totalPages="props.totalPages"
      :visiblePageCount="props.visiblePageCount"
      @pageChange="(page) => emit('pageChange', page)"
    />
    </div>

    <!-- Body 레벨에 툴팁 렌더링 (Teleport 사용) -->
    <Teleport to="body">
      <div
        v-if="currentTooltip.show && !isMobileWidth"
        class="global-tooltip"
        :style="currentTooltip.style"
      >
        {{ currentTooltip.content }}
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed, watch, ref, nextTick, onMounted, onUnmounted } from 'vue';
import DefaultButton from "@/components/common/button/DefaultButton.vue";
import DefaultSelect from "@/components/common/select/DefaultSelect.vue";
import DefaultPagination from "@/components/common/pagination/DefaultPagination.vue";

defineOptions({
  inheritAttrs: false
});

const props = defineProps({
  columns: {
    type: Array,
    required: false,
  },
  data: {
    type: Array,
    required: true,
  },
  footerData: {
    type: Array,
    default: () => [],
  },
  // 기존 로직처럼 openSidebar와 rowClick을 모두 받을 수 있게 함
  openSidebar: {
    type: Function,
    required: false,
  },
  rowClick: {
    type: Function,
    required: false,
    default: null,
  },
  showTable: {
    type: Boolean,
    default: true,
  },
  noColumns: {
    type: Boolean,
    default: false,
  },
  bodyFontSize: {
    type: String,
    default: "0.75rem",
  },
  fixedHeader: {
    type: Boolean,
    default: true,
  },
  buttonHeight: {
    type: String,
    default: "",
  },
  selectHeight: {
    type: String,
    default: "",
  },
  // 데이터 항목별 인원당 금액을 계산하는 함수 (옵션)
  calculateAmountPerPerson: {
    type: Function,
    required: false,
    default: (item) => item.amount,
  },
  scroll: {
    type: Boolean,
    default: false,
  },
  scrollHeight: {
    type: [String, Number],
    default: 360,
  },
  // 행 선택(체크박스) 기능 on/off
  selectable: {
    type: Boolean,
    default: false,
  },
  // v-model 대상
  selectedRows: {
    type: Array,
    default: () => [],
  },
  // 데이터 없을 때 더미(Placeholder) 행을 넣을지 여부
  usePlaceholder: {
    type: Boolean,
    default: true,
  },
  // 최소 행 수(더미 포함)
  minRows: {
    type: Number,
    default: 10,
  },
  mobileCard: {
    type: Boolean,
    default: false,
  },
  cardVariant: {
    /* 'default' | 'ticket' | '…'  – 새 디자인이 늘면 값만 늘리면 됨 */
    type: String,
    default: 'default'
  },
  mobileLineGap: {
    type: String,
    default: '3px'
  },
  /* 동적 높이 조정 – 680px 구간 시작값(300px)에 더해질 px 값 */
  heightAdjust: {
    type: [Number, String],
    default: 0,
  },
  /* 모바일에서 카드가 아닐 때 테이블 높이 고정 */
  fixMobileHeight: {
    type: Boolean,
    default: false,
  },
  // 동적 스타일 모드 – minRows=0 & 회색배경·점선 테두리
  dynamicStyle: {
    type: [Boolean, String],
    default: false,
  },
  // 페이지네이션 사용 여부
  usePagination: {
    type: Boolean,
    default: false,
  },
  // 페이지네이션 - 현재 페이지
  currentPage: {
    type: Number,
    default: 1,
  },
  // 페이지네이션 - 전체 페이지 수
  totalPages: {
    type: Number,
    default: 1,
  },
  // 페이지네이션 - 보여줄 페이지 버튼 수
  visiblePageCount: {
    type: Number,
    default: 5,
  },
  // 노데이터 이미지 표시 여부
  showNoDataImage: {
    type: Boolean,
    default: true,
  },
  // 노데이터 이미지 컨테이너 높이 (데스크탑)
  noDataImageHeight: {
    type: [Number, String],
    default: 484,
  }
});

/* 뷰포트 & 프롭 둘 다 만족할 때만 카드 레이아웃 */
const useCard = ref(false);
const cardClass = ref('');
const mq = 650;
const isMobileWidth = ref(window.innerWidth < mq);

function handleResize () {
  isMobileWidth.value = window.innerWidth < mq;      // ◆ 모바일 폭 여부
  const enabled       = props.mobileCard && isMobileWidth.value;
  useCard.value       = enabled;                     // 카드 모드 여부
  cardClass.value     = enabled ? `card-${props.cardVariant}` : '';
}

/* 모바일에서 카드가 아닐 때 높이 고정 여부 */
const shouldFixMobileHeight = computed(() => {
  return isMobileWidth.value && !useCard.value && props.fixMobileHeight;
});
onMounted(() => {
  window.addEventListener('resize', handleResize);
  handleResize();
});
onUnmounted(() => window.removeEventListener('resize', handleResize));

function mobileCellStyle(column) {
  if (!useCard.value) return {};
  const style = {
    order: column.mobile?.line ?? 99,
    fontWeight: column.mobile?.bold ? "900" : "500",
  };

  // 현재 라인 번호가 최대 라인 번호보다 작을 경우에만 마진을 추가
  const currentLine = column.mobile?.line ?? 99;
  if (currentLine < maxMobileLine.value) {
    style.marginBottom = props.mobileLineGap;
  }

  // 개별 폰트 크기 설정
  if (column.mobile?.fontSize) {
    style.fontSize = column.mobile.fontSize;
  }

  // 개별 상하 여백(padding) 설정
  if (column.mobile?.paddingTop) {
    style.paddingTop = column.mobile.paddingTop;
  }
  if (column.mobile?.paddingBottom) {
    style.paddingBottom = column.mobile.paddingBottom;
  }

  return style;
}

/** divider 간격 커스텀 여부 */
function hasCustomDividerSpacing(column){
  const m = column.mobile || {};
  return !!(
    m.dividerTopGap || m.dividerBottomGap
  );
}

/** divider inline 스타일 (모바일 카드 전용) */
function dividerInlineStyle(column){
  if (!useCard.value) return {};
  const m = column.mobile || {};
  const style = {};
  // bottom
  if (m.divider === true || m.divider === 'bottom' || m.divider === 'both' || m.dividerBottom) {
    const gap = m.dividerBottomGap;
    const gapAbove = m.dividerBottomGapAbove; // 실선 위쪽(내용과 실선 사이) = padding-bottom
    const gapBelow = m.dividerBottomGapBelow; // 실선 아래쪽(실선과 다음 요소 사이) = margin-bottom
    if (gapAbove) style['--divider-bottom-padding'] = gapAbove;
    if (gapBelow) style['--divider-bottom-margin']  = gapBelow;
    if (!gapAbove && !gapBelow && gap) { style['--divider-bottom-padding'] = gap; style['--divider-bottom-margin'] = gap; }
  }
  // top
  if (m.divider === 'top' || m.divider === 'both' || m.dividerTop) {
    const gap = m.dividerTopGap;
    const gapAbove = m.dividerTopGapAbove; // 실선 윗쪽(윗 요소와 실선 사이) = margin-top
    const gapBelow = m.dividerTopGapBelow; // 실선 아랫쪽(실선과 내용 사이)  = padding-top
    if (gapAbove) style['--divider-top-margin']  = gapAbove;
    if (gapBelow) style['--divider-top-padding'] = gapBelow;
    if (!gapAbove && !gapBelow && gap) { style['--divider-top-padding'] = gap; style['--divider-top-margin'] = gap; }
  }
  return style;
}

// 모바일 뷰에서 사용될 최대 라인 번호를 계산
const maxMobileLine = computed(() => {
  if (!props.columns || props.columns.length === 0) return 0;
  // 각 컬럼의 mobile.line 값 중 가장 큰 값을 찾습니다.
  return Math.max(...props.columns.map(c => c.mobile?.line ?? 0));
});

// 어떤 이벤트가 올지 모르므로 “열 때 선언된 emit”을 그대로 가격
// 타입 검사를 포기하고 eslint-plugin-vue 규칙도 통과
// (아래 주석 제거 시 에러)
// eslint-disable-next-line vue/valid-define-emits
const emit = defineEmits();

/* const emit = defineEmits([
  'row-updated', 
  'delete-row',
  'update:selectedRows',
  'selection-change',
  'pageChange',
  'rowClick',
  'applyRow'
]); */

/** table-container 인라인 스타일 (스크롤 옵션) */
const tableContainerStyles = computed(() => {
  const styles = { 
    '--height-adjust': `${props.heightAdjust || 0}px`,
    // 페이지네이션 사용 시 높이 조정 (페이지네이션 높이: 약 60px)
    '--pagination-height': props.usePagination ? '60px' : '0px'
  };
  
  // dynamicStyle이 문자열인 경우 모바일에서만 적용할 높이 값 추가
  if (props.dynamicStyle && typeof props.dynamicStyle === 'string') {
    styles['--dynamic-mobile-height'] = props.dynamicStyle;
  }
  
  if (props.scroll) {
    const h =
      typeof props.scrollHeight === "number"
        ? `${props.scrollHeight}px`
        : props.scrollHeight;
    /* !important 를 붙여 동적 CSS보다 높은 우선순위 확보 */
    Object.assign(styles, { 'max-height': `${h} !important`, overflowY: 'auto' });
  }
  return styles;
});

/** 컬럼 폭 지정 함수 */
function columnStyles(column) {
  const style = {};
  
  // 카드 모드일 때는 columnStyles 적용하지 않음 (카드 CSS에서 처리)
  if (useCard.value) {
    return {};
  }
  
  // 데스크톱 테이블 모드: width가 'auto'이면 나머지 공간을 모두 차지
  if (column.width === 'auto') {
    // minWidth 커스텀 지정 가능 (기본값: 100)
    const minWidth = column.minWidth || 100;
    style.flex = '1 1 0'; // flex-grow: 1, flex-shrink: 1, flex-basis: 0 (중요!)
    style.minWidth = minWidth + 'px'; // 커스텀 최소 너비
    style.maxWidth = 'none'; // 최대 너비 제한 없음
  } else {
    // 고정 너비 사용
    const fixedWidth = column.width || 150;
    style.flex = `0 0 ${fixedWidth}px`; // 고정 크기
    style.width = fixedWidth + "px";
    style.minWidth = fixedWidth + "px";
    style.maxWidth = fixedWidth + "px";
  }
  
  return style;
}

/** Select 컬럼 옵션 얻기 */
function getSelectOptions(column, row) {
  if (typeof column.getOptions === "function") {
    return column.getOptions(row);
  }
  return [];
}

/** Select 변경 시 → 상위로 알림 */
// function onSelectChange(item, column, newValue) {
function onSelectChange(item) {
  // item[column.key]가 이미 바뀌었음
  // 필요하다면 row-updated 이벤트 발생
  emit("row-updated", { ...item });
}

// 삭제 버튼 클릭 시
function onButtonClick(item, column) {
  // 열 정의에 emit이 있으면 그걸, 없으면 'button-click'
  const evt = column.emit ?? "button-click";
  emit(evt, { ...item });
}

/** 행 전체 클릭 → openSidebar & rowClick 모두 시도 */
function onRowClick(item) {
  if (props.openSidebar) {
    props.openSidebar(item);
  }
  if (props.rowClick) {
    props.rowClick(item);
  }
}

/* ────────────────────────────────────────────────
   ‘보여 줄 컬럼’ 계산
    • 데스크톱  → 전달받은 columns 그대로
    • 모바일    → mobileDisable 이 false 인 것만 남김
   ─────────────────────────────────────────────── */
const visibleColumns = computed(() => {
  if (isMobileWidth.value) {
    return props.columns.filter(c => {
      const hiddenByAlias = c.mobile && c.mobile.hidden === true;
      return !c.mobileDisable && !hiddenByAlias;
    });  // 모바일(카드·테이블 공통): mobileDisable 또는 mobile.hidden 이 true면 숨김
  }
  return props.columns;                                  // 데스크톱
});

/* ───────── 데이터 없음 상태 ───────── */
const noData = computed(() => {
  return !props.data || props.data.length === 0;
});

/* ───────── 버튼 컬럼 존재 여부 ───────── */
const hasButtons = computed(() => {
  return props.columns && props.columns.some(col => col.type === 'button');
});

/* ───────── 테이블 더미 행 ───────── */
const fillerRows = computed(() => {
  if (!props.usePlaceholder || useCard.value) return 0;
  const minRowsToUse = props.dynamicStyle ? 0 : props.minRows;
  return Math.max(minRowsToUse - props.data.length, 0);
});

const rowHeight = ref(0);

/** 데이터·윈도 크기 변화 → 행 높이 재계산 */
async function syncRowHeight() {
  await nextTick(); // DOM 그려진 뒤
  const firstRow = document.querySelector(
    ".table tbody tr:not(.placeholder-row)"
  );
  rowHeight.value = firstRow ? firstRow.offsetHeight : 0;
  document.documentElement.style.setProperty("--row-h", `${rowHeight.value}px`);
}

/* ───────── 푸터 정렬 클래스 계산 ───────── */
function footerAlignClassVisible(idx) {
  const col = visibleColumns.value[idx];
  const align = col?.align ?? 'left';
  switch (align) {
    case 'center': return 'align-center';
    case 'right':  return 'align-right';
    default:       return 'align-left';
  }
}
const footerIndexMap = computed(() =>
  visibleColumns.value.map(c => props.columns.indexOf(c))
);

const visibleFooterData = computed(() => {
  // footerData가 없을 수도 있으니 방어
  if (!props.footerData || !props.footerData.length) return [];
  return footerIndexMap.value.map(i => props.footerData[i] ?? '');
});


/* 데이터 바뀔 때 */
watch(() => props.data, syncRowHeight, { deep: true });

/* 최초·리사이즈 때 */
onMounted(() => {
  syncRowHeight();
  window.addEventListener("resize", syncRowHeight);
});

/* ───────── 선택 로직 ───────── */
const internalSelected = ref(new Set(props.selectedRows));

/* 부모 → 자식 동기화 */
watch(
  () => props.selectedRows,
  (rows) => {
    internalSelected.value = new Set(rows);
  },
  { deep: true }
);

/* 행이 선택됐는지 판단 */
function isRowSelected(row) {
  return internalSelected.value.has(row);
}

/* 단일 행 토글 */
function toggleRowSelect(row, checked) {
  if (checked) internalSelected.value.add(row);
  else internalSelected.value.delete(row);
  exportSelection();
}

/* 전체 선택/해제 */
function toggleSelectAll(checked) {
  if (checked) internalSelected.value = new Set(props.data);
  else internalSelected.value.clear();
  exportSelection();
}

/* 선택 배열을 부모에 전달 */
function exportSelection() {
  const arr = Array.from(internalSelected.value);
  emit("update:selectedRows", arr); // v-model
  emit("selection-change", arr); // (옵션) 추가 이벤트
}

/* 헤더 체크박스 상태 */
const allSelected = computed(
  () =>
    internalSelected.value.size === props.data.length && props.data.length > 0
);
const someSelected = computed(
  () => internalSelected.value.size > 0 && !allSelected.value
);

/* ───────── 툴팁 관련 로직 ───────── */
const currentTooltip = ref({
  show: false,
  content: '',
  style: {}
});

/**
 * 텍스트가 ellipsis로 잘렸는지 확인하는 함수
 */
function isTextTruncated(element) {
  return element.scrollWidth > element.clientWidth;
}

/**
 * 툴팁 표시 함수 - 데스크탑에서 텍스트가 잘린 경우에만 표시
 */
function showTooltip(event, item, column, cellId) {
  // 모바일이거나 카드 모드일 때는 툴팁 표시하지 않음
  if (isMobileWidth.value || useCard.value) return;
  
  let target = event.target;
  
  // event.target이 td인 경우, 내부의 .cell-text를 찾음
  if (target.tagName === 'TD') {
    const cellText = target.querySelector('.cell-text');
    if (cellText) {
      target = cellText;
    }
  }
  
  // 텍스트가 잘렸는지 확인
  if (!isTextTruncated(target)) return;
  
  // 툴팁에 표시할 내용 결정
  const content = column.customValue ? column.customValue(item) : item[column.key];
  
  // 내용이 없으면 툴팁 표시하지 않음
  if (!content) return;
  
  // 화면 기준 절대 위치 계산
  const rect = target.getBoundingClientRect();
  const left = rect.left;
  const top = rect.bottom + 10; // 셀 아래 7px
  
  currentTooltip.value = {
    show: true,
    content: String(content),
    style: {
      position: 'fixed',
      left: `${left}px`,
      top: `${top}px`,
      zIndex: 9999
    }
  };
}

/**
 * 툴팁 숨기기 함수
 */
function hideTooltip(cellId) {
  currentTooltip.value.show = false;
}

</script>

<style scoped>
.table-container {
  width: 100%;
  overflow-x: auto !important;
  /* height: 300px;
  overflow-x: auto;
  overflow-y: auto; */
}

.table-container.dynamic-style {
  background: linear-gradient(135deg, var(--theme-table-header-bg) 0%, var(--theme-bg-main) 100%);
  border-bottom: 2px solid var(--theme-border-light);
  border-radius: 0;
  overflow: visible; /* sticky header를 위해 visible로 변경 */
  display: flex;
  flex-direction: column;
}

/* dynamic-style일 때 tbody에만 스크롤 적용 */
.table-container.dynamic-style .table {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-container.dynamic-style .table tbody {
  overflow-y: auto;
  display: block;
}

.table {
  /*
    기본적으로 테이블은 border-collapse: collapse;가 적용되어 인접한 셀의 경계선이 합쳐져 보임
    sticky 헤더 등의 효과를 위해 각 셀의 경계를 분리하여 개별 스타일을 적용하려면
    border-collapse: separate;를 사용해야 함
    기본값: collapse → separate로 변경
  */
  border-collapse: separate;

  /* 
    border-spacing은 separate 모드에서 셀 간의 간격을 지정하는 속성
    0으로 설정하면 셀들 사이에 추가적인 간격 없이 딱 붙어 있게 됨
    셀 간 간격 0
  */
  border-spacing: 0;
  width: 100%;
  margin-bottom: 0px !important;
  /* auto width 컬럼을 위해 table-layout: fixed 필수! */
  table-layout: fixed;
}

/* auto width 컬럼을 위한 flex 레이아웃 - 데스크톱 테이블 모드에서만 적용 */
/* 카드 모드가 아닐 때만 적용 (카드 모드는 별도 flex 레이아웃 사용) */
.table-container:not(.card-enabled) .table thead tr,
.table-container:not(.card-enabled) .table tbody tr {
  display: flex !important;
  width: 100%;
}

.table-container:not(.card-enabled) .table th,
.table-container:not(.card-enabled) .table td {
  /* flex 아이템으로 동작 */
  overflow: hidden; /* 자식 요소의 overflow를 감싸기 위함 */
  min-width: 0; /* flex 아이템의 암묵적 min-width: auto를 0으로 재설정 (중요!) */
}

/* 셀 내부 텍스트 - ellipsis 처리 */
.table-container:not(.card-enabled) .table td .cell-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  display: block;
}

/* select, button이 있는 td는 overflow visible */
.table-container:not(.card-enabled) .table td:has(> div > .default-select),
.table-container:not(.card-enabled) .table td:has(> div > .default-button) {
  overflow: visible !important;
}

/* th, td 기본 패딩 및 수평 경계선 */
.table th,
.table td {
  padding: 12px 8px 12px 8px;
  vertical-align: middle; /* 추가: 세로 중앙 정렬 */
  /* border-top: 1px solid #ccc; */
  /* border-bottom: 1px solid #ccc; */
}

/* 데스크톱 테이블 모드에서 td 내용 수직 중앙 정렬 */
.table-container:not(.card-enabled) .table td {
  display: flex;
  align-items: center; /* flex 아이템 수직 중앙 정렬 */
}

/* 체크박스 컬럼 고정 너비 */
.table-container:not(.card-enabled) .table th.checkbox-col,
.table-container:not(.card-enabled) .table td.checkbox-col {
  flex: 0 0 40px;
  width: 40px;
  min-width: 40px;
  max-width: 40px;
  justify-content: center;
}

/* td 내부의 cell-text - align에 따라 너비 조정 */
.table-container:not(.card-enabled) .table td .cell-text {
  display: block;
  width: 100%;
}

/* align-center, align-right일 때는 td의 justify-content가 작동하도록 width auto */
.table-container:not(.card-enabled) .table td.align-center .cell-text,
.table-container:not(.card-enabled) .table td.align-right .cell-text {
  width: auto;
}

/* 테이블 데이터 셀 기본 font-weight 500 */
.table td {
  font-weight: 500;
  font-size: var(--table-body-font-size, 0.75rem);
}

/* 첫 번째 셀에는 왼쪽 경계선 제거, 나머지 셀에는 왼쪽 경계선 (얇은 회색) 적용 */
.table th:not(:first-child),
.table td:not(:first-child) {
  /* border-left: 1px solid #e0e0e0; */
  border-left: 1px solid var(--theme-border-light);
}


/* th는 상하 경계선을 굵게 처리 (1px solid) */
.table th {
  text-align: center;
  font-size: var(--table-body-font-size, 0.8rem) !important;
  border-top: none !important;
  border-bottom: none !important;
  border-left: none !important;
  border-right: none !important;
  font-weight: 600;
  letter-spacing: 0.3px;
  padding: 12px 8px;
  position: relative;
}

.table thead { border-bottom: 1px solid var(--theme-border-dark) !important; }

.no-header {
  border-top: 1px solid #dee2e6;
}

/* 헤더 고정 (fixedHeader 프롭이 true인 경우 적용) */
.table-container:not(.card-enabled) .table thead.fixed-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #f9fafb;
}

.table-container:not(.card-enabled) .fixed-header th {
  position: sticky;
  top: 0;
  background: #f9fafb;
  z-index: 10;
  border-top: none !important;
  border-bottom: 1px solid var(--theme-border-dark) !important;
  border-left: none !important;
  border-right: none !important;
  font-weight: 600;
  letter-spacing: 0.3px;
  font-size: var(--table-body-font-size, 0.8rem) !important;
  padding: 12px 8px;
}

.table-container:not(.card-enabled) .fixed-header th::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, #9ca3af 50%, transparent 100%);
}

/* 항상 ellipsis 적용 (텍스트가 넘치면 ... 표시) */
/* 기존 ellipsis 클래스는 카드 모드에서만 사용 */
.card-enabled .ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 테이블 본문 폰트 사이즈 */
tbody {
  font-size: var(--table-body-font-size, 1rem);
}

/* 기본 텍스트 색상 (예시) */
/* .table-container table tbody td {
  color: rgb(27, 27, 27);
} */

/* 여러가지 커스텀 텍스트 색상 예시 */
.text-red {
  /* color: red !important; */
  color: #ff5252 !important;
}
.text-black {
  color: black !important;
}
.text-blue {
  /* color: blue !important; */
  color:#2266e6 !important;
}
.text-green {
  /* color: green !important; */
  color: #1fb355 !important;
}

/* 행 Hover 시 배경색 */
/* tbody tr:hover td {
  background-color: var(--theme-table-row-hover) !important;
} */
/* ── 실제 데이터 행만 Hover ─────────── */
/* 행 Hover 시 배경색 – placeholder-row 제외 */
/* (모바일 카드 레이아웃) Hover 효과 – placeholder-row 제외 */
.card-enabled .table tbody tr:not(.placeholder-row):hover {
  border-color: #b5b9c0;
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* 체크박스 전용 폭 50 px */
.checkbox-col {
  width: 40px;
  min-width: 40px;
  max-width: 40px;
  text-align: center; /* 가운데 정렬(선택) */
  padding: 0; /* 필요 시 패딩 조절 */
}

/* 체크박스 확대 */
.checkbox-col input[type="checkbox"] {
  transform: scale(1);
  transform-origin: center; /* 가운데 기준으로 확대 */
  cursor: pointer; /* 마우스 오버 시 포인터 */
}

/* Placeholder Row 스타일 */
.placeholder-row td {
  height: var(--row-h, 40px); /* ★ 실제 행 높이와 100% 동일 */
  /* 데이터 행과 같은 여백·라인하이트 적용 */
  padding: 12px 8px 12px 8px;
  line-height: 1.5; /* 필요 시 조정 */
  background: var(--theme-table-placeholder-bg); /* 테마 변수 사용 */
  border-left: 1px solid var(--theme-border-light);
}
.placeholder-row td:first-child {
  border-left: none;
}
.placeholder-row td::after {
  content: "\00a0"; /* NBSP = non-breaking space */
}

/* 테이블 데이터 정렬 */
/* Flex 레이아웃에서 정렬 처리 */
.table th.align-left,
.table td.align-left {
  justify-content: flex-start;
  text-align: left;
}

.table th.align-center,
.table td.align-center {
  justify-content: center;
  text-align: center;
}

.table th.align-right,
.table td.align-right {
  justify-content: flex-end;
  text-align: right;
}

/* 데스크톱 테이블 */
.table tbody tr.disabled-row td {
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);   /* 행 배경 */
  color:#9ca3af;              /* 글자색도 살짝 흐리게 */
}

/* Hover 무효화 — 색이 다시 파란-배경으로 바뀌지 않게 */
.table tbody tr.disabled-row:hover td {
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%) !important;
  transform: none;
  box-shadow: none;
}

/* 모바일 카드 레이아웃 */
.card-enabled .table tbody tr.disabled-row{
  background:#f1f1f1;
  border-color:#d0d0d0;       /* 그림자 대신 연한 테두리 */
  box-shadow:none;            /* lift 애니메이션 제거 */
  transform:none;             /* lift 애니메이션 제거 */
}

/* 카드 Hover 도 무효화 */
.card-enabled .table tbody tr.disabled-row:hover{
  background:#f1f1f1;
  box-shadow:none;
  transform:none;
}

/* ───────── 데이터 없음 이미지 스타일 ───────── */
.no-data-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
  min-height: 300px;
}

.no-results-found {
  max-width: 400px;
  width: 80%;
  height: auto;
  object-fit: contain;
}

/* ───────── 툴팁 셀 스타일 ───────── */
.tooltip-cell {
  position: relative;
}

/* ───────── 글로벌 툴팁 스타일 (body 레벨) ───────── */
.global-tooltip {
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.75rem;
  line-height: 1.4;
  max-width: 300px;
  word-wrap: break-word;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  pointer-events: none;
  white-space: normal;
  opacity: 0;
  transform: translateY(-5px);
  animation: tooltipFadeIn 0.2s ease-out forwards;
}

.global-tooltip::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 12px;
  width: 8px;
  height: 8px;
  background: rgba(0, 0, 0, 0.9);
  transform: rotate(45deg);
  z-index: -1;
}


/* @media (max-width: 700px) {
  .table th,
  .table td {
    padding: 5px;
  }
  .table-container {
    max-height: 360px !important;
    overflow-y: auto;
  }
} */

/* 고정 행은 호버(hover) 시에도 배경색 유지 */
/* .table tbody tr.fixed-row:hover td {
  background-color: #f5f5f5 !important;
} */

/* ───────── 카드 레이아웃 공통 ───────── */
@media (max-width:650px){
  /* 일반 테이블(카드 형식이 아닐 때) 아래 마진 */
  .table-container:not(.card-enabled) {
    padding-bottom: 15px !important;
  }

  /* th, td 기본 패딩 및 수평 경계선 */
  .table th,
  .table td {
    padding: 8px;
    vertical-align: middle; /* 추가: 세로 중앙 정렬 */
    /* border-top: 1px solid #ccc; */
    /* border-bottom: 1px solid #ccc; */
  }
  .placeholder-row td {
    padding: 8px;
  }

  /* 0. 스크롤·헤더 – y축은 그대로 두고 x축만 열어둠 */
  .card-enabled              { overflow-x:visible!important; }   /* ✱ y축 NO TOUCH */
  .card-enabled .table thead { display:none; }
  .card-enabled .table,
  .card-enabled .table *     { border-left:none!important; border-right:none!important; }
  .card-enabled .table       { table-layout: fixed; }

  /* 1. tr = 카드(배경·테두리는 variant에서) */
  .card-enabled .table tbody tr{
    display:flex !important; flex-wrap:wrap !important; width:100%;
    margin:20px 0; padding:18px 20px 22px;
    position:relative; overflow:hidden;

    transform:translateY(24px); opacity:0;
    animation:fadeLift .45s cubic-bezier(.25,.8,.35,1) forwards;
  }

  /* 고정 행 – 카드 모드 전용 (라이트/다크 각각) */
  .card-enabled .table tbody tr.fixed-row {
    background: #FFF3F3 !important;            /* 라이트: 연한 빨강 */
    border-color: #F9CACA !important;          /* 라이트: 연한 테두리 */
  }
  body[data-theme="dark"] .card-enabled .table tbody tr.fixed-row {
    background: rgba(239, 68, 68, 0.18) !important; /* 다크: 살짝 더 진한 투명 빨강 */
    border-color: var(--theme-border-dark) !important;
  }
  .card-enabled .table tbody tr.fixed-row td {
    background: transparent !important;        /* 카드 내부 셀은 투명 유지 */
    color: inherit;
  }

  /* 2. 셀 공통 레이아웃 */
  .card-enabled .table tbody td {
    display: block !important; /* margin-top/bottom 적용 위해 블록화 */
    width: 100% !important;
    min-width: 0 !important;
    max-width: 100% !important;
    padding: 2px 0;
    border: none !important;
    font-size: .78rem;
    font-weight: 500; /* 모바일 테이블 데이터 기본 font-weight 500 */
    /* white-space: normal !important; */
  }
  /* 실선 옵션 */
  .card-enabled .table tbody td.mobile-divider-bottom{
    border-bottom: 1px solid #e9eef3 !important;
    padding-bottom: var(--divider-bottom-padding, 10px) !important;
    margin-bottom: var(--divider-bottom-margin, 10px) !important;
  }
  .card-enabled .table tbody td.mobile-divider-top{
    border-top: 1px solid #e9eef3 !important;
    margin-top: var(--divider-top-margin, 10px) !important;
    padding-top: var(--divider-top-padding, 10px) !important;
  }
  .card-enabled .table tbody td.mobile-inline {
    width: auto !important;
    display: inline-flex;
    align-items: center;
  }

  /* 3. 기본 정렬 클래스 그대로 사용 */
  .card-enabled .align-left   { text-align:left;  }
  .card-enabled .align-center { text-align:center;}
  .card-enabled .align-right  { text-align:right; }
  .card-enabled .mobile-align-right{ margin-left:auto; text-align:right; }

  /* 4. prefix / suffix */
  .card-enabled .table tbody td::before{ content:attr(data-prefix); }
  .card-enabled .table tbody td::after { content:attr(data-suffix); }
  
  /* 버튼 타입은 ::after로 suffix가 제대로 안 보이므로 수동 추가한 span 사용 */
  .card-enabled .table tbody td:has(.button-cell-wrapper)::after { 
    content: none !important; 
  }
  
  /* 버튼 suffix 스타일 */
  .card-enabled .button-suffix {
    display: inline-block;
    margin-left: 0;
  }

  /* 5. 체크박스 위치 */
  .card-enabled .table tbody tr:has(> .mobile-checkbox){ padding-left:46px; }
  .card-enabled .table tbody td.mobile-checkbox{
    position:absolute; left:14px; top:20px;
    width:24px!important; padding:0!important; background:transparent!important;
  }
  .card-enabled .table tbody td.mobile-checkbox input[type=checkbox]{
    transform:scale(1.15); cursor:pointer;
  }
  .card-enabled .table tbody tr.placeholder-row td.mobile-checkbox{
    display:none!important;
  }
  .card-enabled.table-container{
    max-height:none   !important;   /* 높이 제한 해제 */
    overflow-y:visible!important;   /* 세로 스크롤 제거 */
  }
  .mobile-footer td{
    font-size:.75rem;
    padding:4px 6px;
  }
  /* 모바일 테이블 헤더 글자 크기 */
  .table th{
    font-size: .78rem !important;
  }
}

/* 등장 애니메이션 */
@keyframes fadeLift{ to{ transform:translateY(0); opacity:1; } }

/* ===== 데스크톱 전용 모던 스타일 ===== */
@media (min-width: 651px) {
  .table th,
  .table td {
    border-left: none !important;
    border-right: none !important;
  }
  .table tbody tr.fixed-row td {
    /* background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%); */
    background: linear-gradient(135deg, #e6edf5 0%, #d6dee7 100%) !important;
    font-weight: 700;
    color: #374151;
  }
  .table-container {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
    border-top: 1px solid #e5e7eb;
    border-bottom: 1px solid #e5e7eb;
    border-left: none !important;
    border-right: none !important;
    border-radius: 0;
    overflow: hidden;
  }
  
  .table tbody tr {
    transition: all 0.3s ease;
  }
  
  .table tbody tr:not(.placeholder-row):hover {
    cursor: pointer;
  }
  
  /* 테이블 본문 행 배경 짝수, 홀수 구분 */
  .table tbody tr:nth-child(even):not(.placeholder-row):not(.disabled-row):not(.fixed-row) td {
    background: var(--theme-table-row-even);
  }
  .table tbody tr:nth-child(odd):not(.placeholder-row):not(.disabled-row):not(.fixed-row) td {
    background: var(--theme-table-bg);
  }
  /* 테이블 본문 행 배경 짝수, 홀수 구분 없이 통일 */
  /* .table tbody tr:not(.placeholder-row):not(.disabled-row):not(.fixed-row) td {
    background: var(--theme-table-bg) !important;
  } */

  /* 데스크톱 호버 효과 - 짝수/홀수 모두 동일하게 적용 */
  .table tbody tr:not(.placeholder-row):not(.disabled-row):not(.fixed-row):hover td {
    background: var(--theme-table-row-hover) !important;
    transform: translateY(-1px);
  }
  
  /* 좌우 경계선 완전 제거 */
  .table th:first-child,
  .table td:first-child {
    border-left: none !important;
  }
  
  .table th:last-child,
  .table td:last-child {
    border-right: none !important;
  }

  /* 데스크탑에서 더 큰 이미지 - 프롭으로 받은 높이 사용 */
  .no-data-container {
    height: var(--no-data-height, 484px);
    background: var(--theme-table-bg);
    border-top: 1px solid var(--theme-table-border);
    border-bottom: 1px solid var(--theme-table-border);
    border-left: none !important;
    border-right: none !important;
    border-radius: 0;
    box-shadow: 0 4px 16px var(--theme-shadow-sm);
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .no-results-found {
    max-width: 500px;
    width: 50%;
    opacity: 0.8;
  }
}


/* ───────── 동적 높이 규칙 (모바일 주소창 up/down 실시간 대응) ───────── */
/* 모바일에서 카드가 아닐 때 높이 고정 속성이 있으면 동적 높이 제한을 적용하지 않음 */
/* dynamicStyle이 문자열일 때 데스크톱에서 해당 픽셀값으로 고정 */
@media (min-width: 651px) {
  .table-container.dynamic-style[style*="--dynamic-mobile-height"] {
    /* dynamicStyle이 문자열인 경우 헤더+바디 포함해서 해당 높이를 데스크톱에서 무조건 고정 */
    height: var(--dynamic-mobile-height, 300px) !important;
    overflow: hidden !important;
    display: flex !important;
    flex-direction: column !important;
  }
}

/* ❶ 680px 이하 - dynamicStyle이 Boolean일 때만 적용 (데스크톱 제외) */
@media (max-width: 650px) and (max-height: 679px) {
  .table-container.dynamic-style .table tbody {
    /* ① 실가시높이 계산값이 300px 미만이면 300px 로 고정 */
    /* 페이지네이션 높이(--pagination-height)도 고려 */
    max-height: max(
      calc(300px + var(--height-adjust, 0px)),
      calc(var(--vvh, 1dvh) * 100 - 470px - var(--pagination-height, 0px) + var(--height-adjust, 0px))
    ) !important;
    overflow-y: auto !important;
    /* -------------- min-height 추가(테이블 영역 차지) -------------- */
    min-height: max(
      calc(300px + var(--height-adjust, 0px)),
      calc(var(--vvh, 1dvh) * 100 - 470px - var(--pagination-height, 0px) + var(--height-adjust, 0px))
    ) !important;
  }
}

/* ❷ 680px 초과 - dynamicStyle이 Boolean일 때만 적용 (데스크톱 제외) */
@media (max-width: 650px) and (min-height: 680px) {
  .table-container.dynamic-style .table tbody {
    /* ② 300px ~ 420px 범위로 clamp  (중간값은 실가시높이 비례) */
    /* 페이지네이션 높이(--pagination-height)도 고려 */
    max-height: clamp(
      calc(300px + var(--height-adjust, 0px)),                    /* 최소 */
      calc(var(--vvh, 1dvh) * 100 - 470px - var(--pagination-height, 0px) + var(--height-adjust, 0px)), /* 선형 증가값 */
      calc(420px + var(--height-adjust, 0px))                     /* 최대 */
    ) !important;
    overflow-y: auto !important;
    /* -------------- min-height 추가(테이블 영역 차지) -------------- */
    min-height: clamp(
      calc(300px + var(--height-adjust, 0px)),                    /* 최소 */
      calc(var(--vvh, 1dvh) * 100 - 470px - var(--pagination-height, 0px) + var(--height-adjust, 0px)), /* 선형 증가값 */
      calc(420px + var(--height-adjust, 0px))                     /* 최대 */
    ) !important;
  }
}

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>