<template>
  <div class="content content-wrapper">
    <PageTitle 
      :title="$t('hrm.orgChartDetail.title')"
      :subtitle="$t('hrm.orgChartDetail.subtitle')"
      icon="ri-group-line"
    />

    <!-- 검색 바 -->
    <div class="search-bar">
      <input
        type="text"
        class="search-input"
        v-model.trim="searchQuery"
        :placeholder="$t('hrm.orgChartDetail.searchPlaceholder')"
        @input="onSearchInput"
      />
      <button class="search-clear" v-if="searchQuery" @click="clearSearch">{{ $t('hrm.orgChartDetail.clear') }}</button>
    </div>

    <!-- 이용 팁: 접기/펼치기 가능 -->
    <section class="tips-box">
      <header class="tips-header" @click="toggleTips">
        <div class="tips-left">
          <i class="ri-information-line"></i>
          <span>{{ $t('hrm.orgChartDetail.tips') }}</span>
          <i class="ri-arrow-down-s-line tips-arrow" :class="{ rotated: tipsOpen }"></i>
        </div>
        <div class="tips-header__actions">
          <button
            class="toggle-small"
            @click.stop="expandAllTeamsForMobile"
            :class="{ active: areAllExpanded }"
          >
            <span class="label">{{ $t('hrm.orgChartDetail.expandAll') }}</span>
          </button>
          <button
            class="toggle-small"
            @click.stop="collapseAllTeamsForMobile"
            :class="{ active: areAllCollapsed }"
          >
            <span class="label">{{ $t('hrm.orgChartDetail.collapseAll') }}</span>
          </button>
        </div>
      </header>
      <div class="tips-body" v-show="tipsOpen">
        <ul>
          <li>{{ $t('hrm.orgChartDetail.tip1') }}</li>
          <li>{{ $t('hrm.orgChartDetail.tip2') }}</li>
        </ul>
      </div>
    </section>

    <!-- 로딩 -->
    <div v-if="loading" class="loading-box">{{ $t('hrm.orgChartDetail.loading') }}</div>

    <!-- 조직도 -->
    <div v-else>
      <section v-for="dept in displayTree" :key="dept.departmentId" class="dept-card">
        <!-- ▣ 부서 헤더(클릭하면 접기) -->
        <header class="dept-header" @click="toggleDept(dept.departmentId)">
          <!-- 좌측: 부서명 + 메타 -->
          <div class="dept-header-left">
            <h3 class="dept-title">
              {{ dept.departmentName }}
              <!-- 우측: 화살표 아이콘 -->
              <i
                :class="[
                  'ri-arrow-down-s-line',
                  { rotated: !collapsed.has(dept.departmentId) },
                ]"
              ></i>
            </h3>
            <small class="dept-meta">
              {{ dept.teamCount }}{{ $t('hrm.orgChartDetail.team') }} · {{ dept.empCount }}{{ $t('hrm.orgChartDetail.people') }}
            </small>
          </div>
        </header>

        <!-- 팀 섹션(접힘 처리) -->
        <div v-show="searchActive || !collapsed.has(dept.departmentId)">
          <div v-for="team in dept.teams" :key="team.teamId" class="team-section">
            <h4 class="team-title" @click="toggleTeam(dept.departmentId, team.teamId)">
              {{ team.teamName }}
              <i
                class="ri-arrow-down-s-line"
                :class="{ rotated: !isTeamCollapsed(dept.departmentId, team.teamId) }"
              ></i>
            </h4>

            <div
              class="emp-grid"
              v-show="searchActive || !isTeamCollapsed(dept.departmentId, team.teamId)"
            >
              <article
                v-for="emp in team.emps"
                :key="emp.userId"
                class="emp-card clickable"
                @click="showUserDetail(emp)"
              >
                <div class="avatar-wrapper">
                  <img
                    v-if="emp.profileImgUrl"
                    :src="emp.profileImgUrl"
                    :alt="emp.name"
                    class="avatar"
                  />
                  <i v-else class="ri-user-line avatar-placeholder"></i>
                </div>
                <p class="emp-name" v-html="highlightName(emp.name)"></p>
                <p class="emp-pos">{{ emp.positionName }}</p>
              </article>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 사원 상세 정보 모달 -->
    <UserDetailModal
      :visible="isUserDetailModalVisible"
      :user="selectedUser"
      @close="closeUserDetailModal"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useViewStateStore } from '@/store/viewState';
import HrmUserApi from '@/api/hrm/UsersApi';
import UserDetailModal from '@/components/hrm/UserDetailModal.vue';

const { t } = useI18n();

const loading = ref(true);
const tree = ref([]);
const collapsed = ref(new Set()); // 👈 접힘 상태 저장
const viewState = useViewStateStore();
const users = ref([]); // 전체 사용자 데이터 저장
const selectedUser = ref({}); // 선택된 사용자
const isUserDetailModalVisible = ref(false); // 사용자 상세 모달 표시 여부
const tipsOpen = ref(false); // 이용 팁 열림 상태
const teamCollapsed = ref(new Set()); // 팀 접힘 상태 (depId:teamId)
const searchQuery = ref(''); // 검색어
const searchActive = computed(() => (searchQuery.value || '').length > 0);
// 모바일 여부 (반응형)
const isMobile = ref(false);
onMounted(() => {
  const mq = window.matchMedia('(max-width:650px)');
  const setFlag = () => (isMobile.value = mq.matches);
  setFlag();
  mq.addEventListener?.('change', setFlag);
});

function toggleDept(id) {
  collapsed.value.has(id) ? collapsed.value.delete(id) : collapsed.value.add(id);
  viewState.saveState('hrmOrgDirectory', {
    collapsedIds: Array.from(collapsed.value),
    scrollY: window.scrollY,
  });
}

/**
 * 이용 팁 토글
 */
function toggleTips() {
  tipsOpen.value = !tipsOpen.value;
  viewState.saveState('hrmOrgDirectoryTips', { tipsOpen: tipsOpen.value });
}

/**
 * 팀 접기/펼치기 토글
 */
function toggleTeam(depId, teamId) {
  const key = `${depId}:${teamId}`;
  if (teamCollapsed.value.has(key)) {
    teamCollapsed.value.delete(key);
  } else {
    teamCollapsed.value.add(key);
  }
  viewState.saveState('hrmOrgDirectoryTeamCollapsed', {
    keys: Array.from(teamCollapsed.value),
  });
}

function isTeamCollapsed(depId, teamId) {
  const key = `${depId}:${teamId}`;
  return teamCollapsed.value.has(key);
}

/** 모바일: 모두 펼치기/접기 */
function expandAllTeamsForMobile() {
  // 팀/부서 모두 펼치기
  teamCollapsed.value = new Set();
  collapsed.value = new Set();
  viewState.saveState('hrmOrgDirectoryTeamCollapsed', { keys: [] });
  viewState.saveState('hrmOrgDirectory', {
    collapsedIds: Array.from(collapsed.value),
    scrollY: window.scrollY,
  });
}
function collapseAllTeamsForMobile() {
  const keys = [];
  for (const dept of tree.value) {
    for (const team of dept.teams) keys.push(`${dept.departmentId}:${team.teamId}`);
  }
  // 팀/부서 모두 접기
  teamCollapsed.value = new Set(keys);
  collapsed.value = new Set(tree.value.map((d) => d.departmentId));
  viewState.saveState('hrmOrgDirectoryTeamCollapsed', { keys });
  viewState.saveState('hrmOrgDirectory', {
    collapsedIds: Array.from(collapsed.value),
    scrollY: window.scrollY,
  });
}

/**
 * 모두 펼쳐짐/접힘 상태 계산 (모바일에서 버튼 색상 표시용)
 */
const areAllCollapsed = computed(() => {
  // 모든 부서가 접혀 있고, 모든 팀도 접힘 상태로 기록되어 있으면 true
  if (!tree.value.length) return false;
  const allDeptCount = tree.value.length;
  let allTeamCount = 0;
  for (const dept of tree.value) allTeamCount += dept.teams?.length || 0;
  const deptAllClosed = collapsed.value.size === allDeptCount && allDeptCount > 0;
  const teamAllClosed = teamCollapsed.value.size === allTeamCount && allTeamCount >= 0;
  return deptAllClosed && teamAllClosed;
});
const areAllExpanded = computed(() => {
  // 부서/팀 모두 펼쳐져 있으면 true
  return (
    tree.value.length > 0 && collapsed.value.size === 0 && teamCollapsed.value.size === 0
  );
});

/**
 * 검색 인풋 이벤트 핸들러
 */
function onSearchInput() {
  viewState.saveState('hrmOrgDirectorySearch', { q: searchQuery.value });
}

/**
 * 검색어 초기화
 */
function clearSearch() {
  searchQuery.value = '';
  onSearchInput();
}

/**
 * 표시용 트리 (검색 시 필터링 적용)
 */
const displayTree = computed(() => {
  const q = (searchQuery.value || '').toLowerCase();
  if (!q) return tree.value;
  const filtered = [];
  for (const dept of tree.value) {
    const teams = [];
    for (const team of dept.teams) {
      const emps = (team.emps || []).filter((e) =>
        (e.name || '').toLowerCase().includes(q),
      );
      if (emps.length) teams.push({ ...team, emps });
    }
    if (teams.length) filtered.push({ ...dept, teams });
  }
  return filtered;
});

/**
 * 이름 하이라이트 렌더링
 */
function highlightName(name) {
  const q = (searchQuery.value || '').trim();
  if (!q) return name;
  const esc = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  return String(name).replace(
    new RegExp(esc, 'gi'),
    (m) => `<mark class="mark">${m}</mark>`,
  );
}

// 사용자 상세 정보 표시
function showUserDetail(emp) {
  // users 배열에서 해당 사용자의 전체 정보 찾기
  const fullUserData = users.value.find((user) => user.userId === emp.userId);
  if (fullUserData) {
    selectedUser.value = fullUserData;
    isUserDetailModalVisible.value = true;
  }
}

// 사용자 상세 모달 닫기
function closeUserDetailModal() {
  isUserDetailModalVisible.value = false;
  selectedUser.value = {};
}

/* 직책 우선순위 */
const posRank = {
  대표: 1,
  전무: 2,
  상무: 3,
  이사: 4,
  수석: 5,
  팀장: 6,
  책임: 7,
  선임: 8,
};
const rankOf = (pos) => posRank[pos] ?? 99;

/* 데이터 로드 */
onMounted(async () => {
  const { data: usersData = [] } = await HrmUserApi.getUsers();
  users.value = usersData; // 전체 사용자 데이터 저장
  const deptMap = new Map();

  usersData.forEach((u) => {
    const depId = u?.team?.department?.departmentId ?? 0;
    const depName = u?.team?.department?.departmentName ?? t('hrm.orgChartDetail.unspecified');
    const teamId = u?.team?.teamId ?? 0;
    const teamName = u?.team?.teamName ?? t('hrm.orgChartDetail.unspecified');

    if (!deptMap.has(depId)) {
      deptMap.set(depId, {
        departmentId: depId,
        departmentName: depName,
        teams: new Map(),
      });
    }
    const dept = deptMap.get(depId);

    if (!dept.teams.has(teamId)) {
      dept.teams.set(teamId, { teamId, teamName, emps: [] });
    }
    const team = dept.teams.get(teamId);

    team.emps.push({
      userId: u.userId,
      name: u.name,
      positionName: u.position?.positionName ?? '',
      profileImgUrl: u.profileImg?.fileUrl || null,
    });
  });

  /* 정렬 및 카운트 */
  deptMap.forEach((dept) => {
    let total = 0;
    dept.teams.forEach((team) => {
      team.emps.sort((a, b) => rankOf(a.positionName) - rankOf(b.positionName));
      total += team.emps.length;
    });
    dept.empCount = total;
    dept.teamCount = dept.teams.size;
  });

  tree.value = Array.from(deptMap.values()).map((d) => ({
    ...d,
    teams: Array.from(d.teams.values()),
  }));
  loading.value = false;
  // 복원은 세부로부터의 뒤로가기일 때만
  const saved = viewState.getState('hrmOrgDirectory');
  const restore = viewState.canRestore('hrmOrgDirectory');
  if (restore && saved && Array.isArray(saved.collapsedIds)) {
    collapsed.value = new Set(saved.collapsedIds);
  }
  if (restore && saved && typeof saved.scrollY === 'number') {
    requestAnimationFrame(() => {
      window.scrollTo(0, saved.scrollY);
    });
  }

  // 팁 열림 상태 복원
  const savedTips = viewState.getState('hrmOrgDirectoryTips');
  if (savedTips && typeof savedTips.tipsOpen === 'boolean') {
    tipsOpen.value = !!savedTips.tipsOpen;
  }

  const savedTeams = viewState.getState('hrmOrgDirectoryTeamCollapsed');
  if (savedTeams && Array.isArray(savedTeams.keys)) {
    teamCollapsed.value = new Set(savedTeams.keys);
  }

  // 검색어 복원
  const savedSearch = viewState.getState('hrmOrgDirectorySearch');
  if (savedSearch && typeof savedSearch.q === 'string') {
    searchQuery.value = savedSearch.q;
  }
});
</script>

<style scoped>
/* 부서 카드 */
.dept-card {
  background: #fff;
  border: 1px solid #e0e0e0d8;
  border-radius: 14px;
  padding: 24px 22px 28px;
  margin-bottom: 18px;
  box-shadow: 0 4px 14px rgba(0, 68, 151, 0.12);
}
.dept-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
  margin-bottom: 24px;
}
.dept-header-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.dept-title {
  font-size: 1.1rem;
  font-weight: 800;
  color: #4b4b4b;
}

/* 검색 바 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0 12px 0;
  flex-wrap: wrap; /* 모바일에서 줄바꿈 허용 */
}
.search-input {
  flex: 1 1 auto;
  border: 1px solid #e0e0e0d8;
  border-radius: 8px;
  padding: 8px 15px;
  outline: none;
  font-size: 0.85rem;
  line-height: 2;
}
.search-input:focus {
  border-color: #4b4b4b;
  box-shadow: 0 0 0 3px rgba(0, 68, 151, 0.12);
}
.search-clear {
  border: 1px solid #e0e0e0d8;
  background: #f8fafc;
  border-radius: 8px;
  padding: 7px 10px;
  cursor: pointer;
  color: #4b4b4b;
  line-height: 2;
}
.mark {
  background: #ffec99;
  padding: 0 2px;
}

/* ─── 이용 팁 (접이식) ─── */
.tips-box {
  border: 1px solid #e0e0e0d8;
  border-radius: 10px;
  background: #ffffff;
  margin: 8px 0 16px 0;
  box-shadow: 0 2px 8px rgba(0, 68, 151, 0.06);
}
.tips-header {
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  cursor: pointer;
  user-select: none;
  color: #4b4b4b;
  font-weight: 700;
}
.tips-left {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.tips-header__actions {
  margin-left: auto;
  display: inline-flex;
  gap: 6px;
}
.tips-header__actions .toggle-small {
  font-size: 0.75rem;
  padding: 8px 10px;
  border: 1px solid #e0e0e0d8;
  background: #ffffff;
  color: #4b4b4b;
  border-radius: 8px;
}
.tips-header__actions .toggle-small.active {
  background: #646464;
  color: #ffffff;
  border-color: #616161b0;
}
.tips-header .tips-arrow {
  margin-left: auto;
  transition: transform 0.2s;
}
.tips-body {
  padding: 6px 20px 20px 20px;
}
.tips-body ul {
  margin: 0;
  padding-left: 18px;
  color: #334155;
  font-size: 0.82rem;
}
.tips-body li {
  line-height: 1.8;
}
.dept-meta {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 4px;
}

/* 화살표 */
.ri-arrow-down-s-line {
  font-size: 1.1rem;
  transition: transform 0.2s;
  color: #4b4b4b;
}
/* 화살표가 실제로 회전하도록 inline‑block 지정 */
.dept-title .ri-arrow-down-s-line {
  display: inline-block; /* ▶ transform 적용 대상 */
  transition: transform 0.2s;
}
/* 펼쳤을 때 ▲ 로 보이도록 180 deg 회전 */
.dept-title .ri-arrow-down-s-line.rotated {
  transform: rotate(180deg);
}

.rotated {
  transform: rotate(180deg);
}

/* 팀 섹션 */
.team-section {
  margin-bottom: 28px;
}
.team-title {
  font-size: 0.9rem;
  font-weight: 700;
  margin-bottom: 16px;
  border-bottom: 1px dashed #d8d8d8;
  padding-bottom: 6px;
  padding-top: 6px;
  color: #666666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  position: sticky;
  top: 65px; /* 헤더/검색바 높이 고려 (모바일에서 버튼 줄바꿈 대응) */
  background: #ffffff;
  z-index: 2;
}
.team-title .ri-arrow-down-s-line {
  display: inline-block;
  transition: transform 0.2s;
  margin-left: 6px;
}

/* 사원 카드 */
.emp-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(88px, 1fr));
  gap: 14px;
}
.emp-card {
  text-align: center;
  font-size: 0.75rem;
  border: 1px solid #e0e0e0d8;
  border-radius: 10px;
  padding: 8px 0 12px;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s, background 0.2s;
  background: #ffffff;
}
.emp-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 18px rgba(0, 68, 151, 0.14);
}
.emp-card.clickable {
  cursor: pointer;
}
.emp-card.clickable:hover {
  border-color: #4b4b4b;
  background-color: #f3f6fc;
}
.avatar-wrapper {
  width: 56px;
  height: 56px;
  margin: 0 auto 6px;
  border: 1px solid #979797;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #e9f0fb 0%, #ffffff 100%);
  box-shadow: inset 0 0 0 2px #e9f0fb;
}
.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder {
  font-size: 26px;
  color: #6b7280;
}
.emp-name {
  font-weight: 700;
  margin: 0;
  color: #4b4b4b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.emp-pos {
  margin: 0;
  color: #4b4b4b;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 로딩 */
.loading-box {
  padding: 60px 0;
  font-size: 1rem;
  text-align: center;
  color: #6b7280;
}

/* ─── 📱 모바일 650px 이하 ─── */
@media (max-width: 650px) {
  .tips-header {
    padding: 8px 10px;
    font-size: 0.78rem;
  }
  /* 모바일에서는 텍스트 라벨을 보여주고 아이콘은 숨김 */
  .tips-header__actions {
    gap: 4px;
  }
  .tips-header__actions .label {
    display: inline;
  }
  .tips-header__actions .toggle-small i {
    display: none;
  }
  .tips-header__actions .toggle-small {
    font-size: 0.6rem;
    padding: 6px 8px;
  }
  .tips-body ul {
    font-size: 0.68rem;
  }
  .search-bar {
    gap: 6px;
  }
  .search-input {
    font-size: 0.75rem;
    padding: 11px 15px;
    line-height: 1.4;
  }
  .search-input::placeholder {
    font-size: 0.75rem;
  }
  .search-clear {
    font-size: 0.72rem;
    padding: 11px 15px;
    line-height: 1.4;
  }
  .toggle-small {
    font-size: 0.72rem;
    padding: 11px 10px;
    border: 1px solid #e0e0e0d8;
    background: #ffffff;
    color: #4b4b4b;
    border-radius: 8px;
  }
  .toggle-small.active {
    background: #4b4b4b;
    color: #ffffff;
    border-color: #4b4b4b;
  }
  .dept-title {
    font-size: 0.875rem;
  }
  .dept-meta {
    font-size: 0.65rem;
  }

  .team-title {
    font-size: 0.75rem;
    margin-bottom: 14px;
  }

  .emp-grid {
    gap: 10px;
    grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  }
  .emp-card {
    font-size: 0.65rem;
    padding: 8px 0 8px;
  }

  .avatar-wrapper {
    width: 40px;
    height: 40px;
    margin-bottom: 4px;
  }
  .avatar-placeholder {
    font-size: 22px;
  }
}
/* ─── 📱 모바일 650px 이하 ─── */
@media (max-width: 450px) {
  .emp-grid {
    gap: 10px;
    grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
  }
}
</style>
