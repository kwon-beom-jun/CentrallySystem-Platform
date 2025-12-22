<template>
  <div class="search-wrapper" :class="{ disabled: props.disabled }">
    <!-- 검색창 -->
    <DefaultTextfield
      :id="inputId"
      v-model="searchTerm"
      :size="inputSize"
      :placeholder="ph"
      type="text"
      autocomplete="off"
      :disabled="props.disabled"
      :bgColor="props.disabled ? '#f5f5f5' : ''"
      @keydown="onKey"
    />

    <!-- 로딩 -->
    <!-- <div v-if="loading" class="loading-box">검색 중…</div> -->

    <!-- 결과 목록 -->
    <ul
      ref="ulRef"
      v-show="dropdownOpen && list.length"
      class="search-result list-group"
    >
      <li
        v-for="(o, i) in list"
        :key="o.value"
        :class="['list-group-item', { active: i === idx }]"
        @click="choose(o)"
      >
        <span>{{ o.name }}</span>
        <span class="email-part">&nbsp;({{ o.email }})</span>
      </li>
      <!-- 0 건일 때 -->
      <li v-if="!list.length" class="list-group-item text-muted text-center">
        {{ $t('auth.userSearch.noResults') }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import debounce from 'lodash/debounce'
import UsersApi from '@/api/hrm/UsersApi'
import { useAuthStore } from '@/store/auth'
import DefaultTextfield from '@/components/common/textfield/DefaultTextfield.vue'

const { t } = useI18n()

/* ===== props / emits ================================================== */
const props = defineProps({
  inputId: { type: String, default: 'userSearch' },
  placeholder: { type: String, default: '' },
  inputSize: { type: String, default: 'medium' },
  keepSearchValue: { type: Boolean, default: true },
  includeCurrentUser: { type: Boolean, default: false },
  /* { service:'receipt', roleDetails:['ROLE_…'] } */
  filterBy: { type: Object, default: () => ({}) },
  disabled: { type: Boolean, default: false },
  initialValue: { type: String, default: '' }
})
const emit = defineEmits(['userSelected'])

/* ===== reactive states ================================================= */
const authStore = useAuthStore()
const searchTerm = ref(props.initialValue || '')
const list = ref([])
const loading = ref(false)
const idx = ref(-1)
const ulRef = ref(null)
const dropdownOpen = ref(true)

/* ===== placeholder ===================================================== */
const ph = computed(() => {
  const defaultPlaceholder = props.placeholder || t('auth.userSearch.placeholder');
  return props.includeCurrentUser
    ? defaultPlaceholder
    : `${defaultPlaceholder} | ${t('auth.userSearch.excludeSelf')} |`
})

/* ===== 서버 호출 (debounce 300ms) ====================================== */
const fetch = debounce(async kw => {
  if (props.disabled) { list.value = []; dropdownOpen.value = false; return }
  if (kw.trim().length < 2) { list.value = []; return }

  dropdownOpen.value = true;
  loading.value = true
  try {
    const { data } = await UsersApi.searchUsers(kw.trim(), {
      limit : 10,
      service: props.filterBy.service ?? null,
      roles : props.filterBy.roleDetails ?? null,
      includeDisabled: false
    })

    list.value = map(data).slice(0, 10)
    idx.value  = list.value.length ? 0 : -1
  } catch (e) {
    console.error(e)
    list.value = []
  } finally {
    loading.value = false
  }
}, 100)

watch(searchTerm, v => {
  if (props.disabled) { return }
  // dropdownOpen.value = true;
  fetch(v);
});

// initialValue prop 변경 감지
watch(() => props.initialValue, (newVal) => {
  if (newVal) {
    searchTerm.value = newVal
    dropdownOpen.value = false
  }
});

/* ===== DTO → option ==================================================== */
function map(arr) {
  return arr
    .filter(
      u =>
        props.includeCurrentUser || u.userId !== authStore.getUserId /*본인 제외*/
    )
    .filter(u => {
      const wanted = props.filterBy.roleDetails
      if (!Array.isArray(wanted) || !wanted.length) return true
      if (!u.roles) return true
      return u.roles.some(
        r => wanted.includes(r.roleName) || wanted.includes(r.roleNameDetail)
      )
    })
    .map(u => ({
      value: u.userId,
      name: u.name, 
      email: u.email,
      label: `${u.name} (${u.email})`,
      ...u
    }))
}

/* ===== 키보드 입력 ====================================================== */
function scroll() {
  nextTick(() => {
    const items = ulRef.value?.querySelectorAll('li') ?? []
    if (idx.value >= 0 && idx.value < items.length)
      items[idx.value].scrollIntoView({ block: 'nearest' })
  })
}

function onKey(e) {
  if (props.disabled) return
  if (!dropdownOpen.value || !list.value.length) return
  const max = list.value.length - 1
  switch (e.key) {
    case 'ArrowDown':
      idx.value = Math.min(max, idx.value + 1)
      scroll()
      e.preventDefault()
      break
    case 'ArrowUp':
      idx.value = Math.max(0, idx.value - 1)
      scroll()
      e.preventDefault()
      break
    case 'Enter':
      choose(list.value[idx.value])
      e.preventDefault()
      break
    case 'Escape':
      reset()
      break
    case 'Tab':
      dropdownOpen.value = false
      break
  }
}

/* ===== 선택 처리 ======================================================= */
function choose(opt) {
  if (props.disabled) return
  if (!opt) return
  emit('userSelected', opt)

  if (!props.keepSearchValue) searchTerm.value = ''
  else searchTerm.value = opt.label

  dropdownOpen.value = false
  idx.value = -1
}

/* ===== 외부로 reset 메서드 노출 ======================================== */
function reset() {
  searchTerm.value = ''
  list.value = []
  idx.value = -1
  dropdownOpen.value = true
}

/**
 * 외부에서 검색어 설정 (복원용)
 */
function setSearchTerm(value) {
  searchTerm.value = value || ''
  dropdownOpen.value = false
}

defineExpose({ resetSearch: reset, setSearchTerm })
</script>



<style scoped>
.search-wrapper {
  position: relative;
}
.search-wrapper.disabled {
  opacity: 0.7;
  pointer-events: none;
}
.search-result {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  border: 1px solid #e3e7ef;
  border-radius: 0;
  box-shadow: 0 8px 24px rgba(16,24,40,.08);
  max-height: 220px;
  overflow-y: auto;
  margin: 0;
  padding: 6px 0;
  z-index: 1000;
  list-style: none;
  width: auto;
  min-width: 100%;
}
.search-result li {
  padding: 10px 12px;
  cursor: pointer;
  font-size: 0.75rem;
  border-radius: 0 !important;
}
.search-result li:first-child { border-radius: 0 !important; }
.search-result li:last-child { border-radius: 0 !important; }
.search-result li:hover,
.search-result li.active {
  background-color: #f3f6ff;
  color: #0b66ff;
}
.search-result li .email-part {
  pointer-events: none;
  color: #6b7a90;
}
.loading-box{
  position:absolute; top:100%; left:50%;
  transform:translateX(-50%);
  padding:6px 12px; font-size:.72rem;
  background:#fff; border:1px solid #ccc;
  border-top:none; border-radius:0 0 4px 4px;
  z-index:1000;
}
@media (max-width: 650px) {
  .list-group-item,
  .list-group {
    font-size: 0.6rem !important;
  }
  .search-result li {
    font-size: 0.6rem !important;
  }
}

@media (max-width: 500px) {
  .list-group-item,
  .list-group {
    font-size: 0.6rem !important;
  }
  .search-result li {
    font-size: 0.6rem !important;
  }
}
</style>
