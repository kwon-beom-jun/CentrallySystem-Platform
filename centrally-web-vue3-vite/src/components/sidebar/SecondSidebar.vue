<template>
  <aside 
    class="second-sidebar"
    :class="{ 
      'show-scrollbar': showScrollbar,
      'collapsed': isCollapsed 
    }"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <!-- 상단: 현재 선택된 워크스페이스 표시 -->
    <div class="sidebar-header">
      <i :class="currentWorkspace.icon"></i>
      <span>{{ headerWorkspaceLabel }}</span>
      <div class="header-controls" v-if="sidebarStore.selectedWorkspace !== 'favorites' && sidebarStore.selectedWorkspace !== 'home'">
        <button 
          v-if="hasAnyExpanded"
          class="control-btn" 
          @click="collapseAll"
          :title="$t('common.sidebar.controls.collapseAll')"
        >
          <i class="ri-subtract-line"></i>
        </button>
        <button 
          v-else
          class="control-btn" 
          @click="expandAll"
          :title="$t('common.sidebar.controls.expandAll')"
        >
          <i class="ri-add-line"></i>
        </button>
      </div>
    </div>

    <!-- 하단: 세부 메뉴 트리 -->
    <nav class="sidebar-nav">
      <!-- 홈 워크스페이스 -->
      <template v-if="sidebarStore.selectedWorkspace === 'home'">
        <!-- 현재 시간/날짜 카드 -->
        <TimeCard />

        <!-- 내 정보 카드 -->
        <UserInfoCard />

        <!-- 사원 검색 카드 -->
        <div class="home-widget-card search-card">
          <EmployeeSearchCard />
        </div>

        <!-- 최근 활동 이력 카드 -->
        <RecentActivityCard />
      </template>

      <!-- INFO 워크스페이스 -->
      <template v-else-if="sidebarStore.selectedWorkspace === 'info'">
        <!-- INFO 일정 관리 카테고리 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.INFO_GUEST))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('info-schedule-management-menu')"
          >
            <i class="ri-calendar-line"></i>
            <span>{{ $t('common.sidebar.category.scheduleManagement') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['info-schedule-management-menu'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['info-schedule-management-menu']" class="sub-menu">
              <div 
                v-for="menu in SCHEDULE_MENUS"
                :key="menu.path"
                v-show="!menu.roles || canShow(menu.roles)"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>
        
        <!-- INFO 게시판 카테고리 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.INFO_GUEST))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('info-board-menu')"
          >
            <i class="ri-chat-3-line"></i>
            <span>{{ $t('common.sidebar.category.board') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['info-board-menu'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['info-board-menu']" class="sub-menu">
              <div 
                v-for="menu in INFO_MENUS"
                :key="menu.path"
                v-show="!menu.roles || canShow(menu.roles)"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>
        
        <!-- INFO 가이드 카테고리 -->
        <div class="nav-item">
          <div 
            class="category-header has-children"
            @click="toggleCategory('info-guide-menu')"
          >
            <i class="ri-guide-line"></i>
            <span>{{ $t('common.sidebar.category.guide') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['info-guide-menu'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['info-guide-menu']" class="sub-menu">
              <div 
                v-if="!GUIDE_MENU.roles || canShow(GUIDE_MENU.roles)"
                class="sub-nav-link"
                :class="{ active: route.path === GUIDE_MENU.path }"
                @click="goTo(GUIDE_MENU.path)"
              >
                <i :class="GUIDE_MENU.icon"></i>
                <span>{{ $t(GUIDE_MENU.i18nKey || GUIDE_MENU.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(GUIDE_MENU.path) }"
                  @click="toggleFavorite(GUIDE_MENU, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(GUIDE_MENU.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>
      </template>

      <!-- 영수증 워크스페이스 - 역할별 카테고리 -->
      <template v-else-if="sidebarStore.selectedWorkspace === 'receipt'">
        <!-- 등록자 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.RECEIPT_REGISTRAR))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('receipt-register')"
          >
            <i class="ri-user-line"></i>
            <span>{{ $t('common.sidebar.receipt.registrar') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['receipt-register'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['receipt-register']" class="sub-menu">
              <div 
                v-for="menu in RECEIPT_REGISTRAR_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>

        <!-- 결재자 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('receipt-approver')"
          >
            <i class="ri-checkbox-circle-line"></i>
            <span>{{ $t('common.sidebar.receipt.approver') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['receipt-approver'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['receipt-approver']" class="sub-menu">
              <div 
                v-for="menu in RECEIPT_APPROVER_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>

        <!-- 대리 결재자 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.RECEIPT_APPROVER))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('receipt-proxy')"
          >
            <i class="ri-user-shared-line"></i>
            <span>{{ $t('common.sidebar.receipt.proxy') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['receipt-proxy'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['receipt-proxy']" class="sub-menu">
              <div 
                v-for="menu in RECEIPT_PROXY_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>

        <!-- 검수자 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.RECEIPT_INSPECTOR))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('receipt-inspector')"
          >
            <i class="ri-search-eye-line"></i>
            <span>{{ $t('common.sidebar.receipt.inspector') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['receipt-inspector'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['receipt-inspector']" class="sub-menu">
              <div 
                v-for="menu in RECEIPT_INSPECTOR_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>

        <!-- 관리자 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.RECEIPT_MANAGER))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('receipt-admin')"
          >
            <i class="ri-admin-line"></i>
            <span>{{ $t('common.sidebar.receipt.manager') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['receipt-admin'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['receipt-admin']" class="sub-menu">
              <div 
                v-for="menu in RECEIPT_MANAGER_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>
      </template>

      <!-- 사원관리 워크스페이스 - 기능별 카테고리 (헤더 레이아웃과 동일) -->
      <template v-else-if="sidebarStore.selectedWorkspace === 'management'">
        <!-- 사용자·권한 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.HRM_MANAGER)) || canShow(getRolesFrom(ROLE_GROUPS.HRM_ASSISTANT_MANAGER))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('hrm-user-auth')"
          >
            <i class="ri-user-settings-line"></i>
            <span>{{ $t('common.sidebar.hrm.userAuth') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['hrm-user-auth'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['hrm-user-auth']" class="sub-menu">
              <template v-for="menu in HRM_USER_AUTH_MENUS" :key="menu.path">
                <div 
                  v-if="!menu.roles || canShow(menu.roles)"
                  class="sub-nav-link"
                  :class="{ active: route.path === menu.path }"
                  @click="goTo(menu.path)"
                >
                  <i :class="menu.icon"></i>
                  <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                  <button 
                    class="favorite-btn"
                    :class="{ 'active': checkIsFavorite(menu.path) }"
                    @click="toggleFavorite(menu, $event)"
                    :title="$t('common.sidebar.favorite.add')"
                  >
                    <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                  </button>
                </div>
              </template>
            </div>
          </transition>
        </div>

        <!-- 조직 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.HRM_EMPLOYEE))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('hrm-organization')"
          >
            <i class="ri-building-line"></i>
            <span>{{ $t('common.sidebar.hrm.organization') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['hrm-organization'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['hrm-organization']" class="sub-menu">
              <template v-for="menu in HRM_ORGANIZATION_MENUS" :key="menu.path">
                <div 
                  v-if="!menu.roles || canShow(menu.roles)"
                  class="sub-nav-link"
                  :class="{ active: route.path === menu.path }"
                  @click="goTo(menu.path)"
                >
                  <i :class="menu.icon"></i>
                  <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                  <button 
                    class="favorite-btn"
                    :class="{ 'active': checkIsFavorite(menu.path) }"
                    @click="toggleFavorite(menu, $event)"
                    :title="$t('common.sidebar.favorite.add')"
                  >
                    <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                  </button>
                </div>
              </template>
            </div>
          </transition>
        </div>

        <!-- 실적 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.HRM_MANAGER))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('hrm-performance')"
          >
            <i class="ri-bar-chart-2-line"></i>
            <span>{{ $t('common.sidebar.hrm.performance') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['hrm-performance'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['hrm-performance']" class="sub-menu">
              <div 
                v-for="menu in HRM_PERFORMANCE_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>

        <!-- 가입 승인 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.HRM_ASSISTANT_MANAGER))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('hrm-approval')"
          >
            <i class="ri-user-add-line"></i>
            <span>{{ $t('common.sidebar.hrm.approval') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['hrm-approval'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['hrm-approval']" class="sub-menu">
              <div 
                v-for="menu in HRM_APPROVAL_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>
      </template>

      <!-- 개인정보 워크스페이스 -->
      <template v-else-if="sidebarStore.selectedWorkspace === 'user'">
        <!-- 사용자 정보 -->
        <div class="nav-item">
          <div 
            class="category-header has-children"
            @click="toggleCategory('user-info')"
          >
            <i class="ri-user-line"></i>
            <span>{{ $t('common.sidebar.user.personal') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['user-info'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['user-info']" class="sub-menu">
              <div 
                v-for="menu in USER_INFO_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
              <div 
                v-for="menu in SYSTEM_USER_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>
      </template>

      <!-- 시스템 워크스페이스 -->
      <template v-else-if="sidebarStore.selectedWorkspace === 'system'">
        <!-- 시스템 관리 -->
        <div class="nav-item" v-if="canShow(getRolesFrom(ROLE_GROUPS.SYSTEM_GATE_SYSTEM))">
          <div 
            class="category-header has-children"
            @click="toggleCategory('system-admin')"
          >
            <i class="ri-settings-3-line"></i>
            <span>{{ $t('common.sidebar.system.admin') }}</span>
            <i
              class="toggle-icon"
              :class="expandedCategories['system-admin'] ? 'ri-arrow-down-s-line' : 'ri-arrow-right-s-line'"
            ></i>
          </div>
          <transition name="expand">
            <div v-if="expandedCategories['system-admin']" class="sub-menu">
              <div 
                v-for="menu in SYSTEM_ADMIN_MENUS"
                :key="menu.path"
                class="sub-nav-link"
                :class="{ active: route.path === menu.path }"
                @click="goTo(menu.path)"
              >
                <i :class="menu.icon"></i>
                <span>{{ $t(menu.i18nKey || menu.label) }}</span>
                <button 
                  class="favorite-btn"
                  :class="{ 'active': checkIsFavorite(menu.path) }"
                  @click="toggleFavorite(menu, $event)"
                  :title="$t('common.sidebar.favorite.add')"
                >
                  <i :class="checkIsFavorite(menu.path) ? 'ri-star-fill' : 'ri-star-line'"></i>
                </button>
              </div>
            </div>
          </transition>
        </div>
      </template>

      <!-- 즐겨찾기 워크스페이스 -->
      <template v-else-if="sidebarStore.selectedWorkspace === 'favorites'">
        <!-- 즐겨찾기가 없을 때 -->
        <div v-if="favoritesStore.favoritesCount === 0" class="empty-state">
          <i class="ri-star-line"></i>
          <p v-html="$t('common.sidebar.favorites.emptyTitle')"></p>
          <small v-html="$t('common.sidebar.favorites.emptyDesc')"></small>
        </div>
        
        <!-- 즐겨찾기 목록 (드래그 가능) -->
        <draggable
          v-else
          v-model="favoritesStore.favoriteMenus"
          item-key="id"
          handle=".drag-handle"
          @start="handleDragStart"
          @end="handleDragEnd"
          class="favorites-list"
        >
          <template #item="{ element }">
            <div class="sub-nav-link draggable-favorite" @click="goTo(element.path)">
              <div class="favorite-top-row">
                <span class="drag-handle" @click.stop>
                  <i class="ri-draggable"></i>
                </span>
                <span class="workspace-badge">
                  <i :class="WORKSPACE_METADATA[element.workspace]?.icon || 'ri-folder-line'"></i>
                  {{ getWorkspaceLabel(element.workspace) }}
                </span>
                    <span class="category-text">{{ $t(element.category) }}</span>
                <button 
                  class="favorite-btn active"
                  @click.stop="toggleFavorite(element, $event)"
                  title="즐겨찾기 제거"
                >
                  <i class="ri-star-fill"></i>
                </button>
              </div>
              <div 
                class="favorite-bottom-row" 
                :class="`favorite-color-${element.color || 'gray'}`"
              >
                <!-- <span class="drag-handle-spacer"></span> -->
                <i :class="element.icon"></i>
                <span class="menu-label">{{ $t(element.label) }}</span>
              </div>
            </div>
          </template>
        </draggable>
      </template>
    </nav>

    <!-- 하단: 즐겨찾기 안내 (고정) -->
    <div 
      v-if="sidebarStore.selectedWorkspace === 'favorites'" 
      class="sidebar-footer"
    >
      <div class="footer-manage-btn-wrapper">
        <button class="footer-manage-btn" @click="goTo(ROUTES.MANAGEMENT.FAVORITE_MENU_MANAGEMENT)">
          <i class="ri-settings-3-line"></i>
          <span>{{ $t('common.sidebar.favorites.manage') }}</span>
        </button>
      </div>
      <div class="favorites-info">
        <i class="ri-information-line"></i>
        <div class="info-text">
          <div>{{ $t('common.sidebar.favorites.infoLine1') }}</div>
          <div>{{ $t('common.sidebar.favorites.infoLine2') }}</div>
        </div>
      </div>
    </div>
  </aside>

  <!-- 접기 버튼 - body에 직접 렌더링 -->
  <Teleport to="body">
    <button 
      class="sidebar-collapse-btn" 
      @click="toggleSidebar"
      :title="isCollapsed ? $t('common.sidebar.controls.expandSidebar') : $t('common.sidebar.controls.collapseSidebar')"
      :style="collapseButtonStyle"
    >
      <i :class="isCollapsed ? 'ri-arrow-right-s-line' : 'ri-arrow-left-s-line'"></i>
    </button>
  </Teleport>

  <!-- 즐겨찾기 제거 확인 모달 -->
  <AlertModal
    :isVisible="favoriteRemoveModalVisible"
    :disableBackgroundClose="true"
    :title="$t('common.sidebar.favorite.removeTitle')"
    :cancelText="$t('common.button.cancel')"
    :confirmText="$t('hrm.favoriteMenu.remove')"
    @close="cancelRemoveFavorite"
    @confirm="confirmRemoveFavorite"
  >
    <template #body>
      <strong>{{ $t(favoriteToRemove?.label) }}</strong>{{ $t('hrm.favoriteMenu.removeConfirm') }}
    </template>
  </AlertModal>

</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter, useRoute } from 'vue-router';
import { useSidebarStore } from '@/store/sidebar';
import { useAuthStore } from '@/store/auth';
import { useFavoritesStore } from '@/store/favorites';
import { getRolesFrom, canShow as checkPermission } from '@/utils/roleUtils';
import { WORKSPACE_METADATA, ROLE_GROUPS } from '@/config/roleConfig';
import { ROUTES } from '@/config/menuConfig';
import { 
  SCHEDULE_MENUS,
  INFO_MENUS,
  GUIDE_MENU,
  RECEIPT_REGISTRAR_MENUS, 
  RECEIPT_APPROVER_MENUS, 
  RECEIPT_PROXY_MENUS, 
  RECEIPT_INSPECTOR_MENUS, 
  RECEIPT_MANAGER_MENUS,
  HRM_USER_AUTH_MENUS,
  HRM_ORGANIZATION_MENUS,
  HRM_PERFORMANCE_MENUS,
  HRM_APPROVAL_MENUS,
  USER_INFO_MENUS,
  SYSTEM_ADMIN_MENUS,
  SYSTEM_USER_MENUS
} from '@/config/menuConfig';
import draggable from 'vuedraggable';
import AlertModal from '@/components/common/modal/AlertModal.vue';
import TimeCard from '@/components/sidebar/TimeCard.vue';
import UserInfoCard from '@/components/sidebar/UserInfoCard.vue';
import EmployeeSearchCard from '@/components/sidebar/EmployeeSearchCard.vue';
import RecentActivityCard from '@/components/sidebar/RecentActivityCard.vue';
import { toast } from 'vue3-toastify';

const router = useRouter();
const { t } = useI18n();
const route = useRoute();
const sidebarStore = useSidebarStore();
const authStore = useAuthStore();
const favoritesStore = useFavoritesStore();

/**
 * 초기 마운트 시 즐겨찾기 로드 및 body 클래스 초기화
 */
onMounted(() => {
  if (authStore.userId) {
    favoritesStore.loadFavorites();
  }
  
  // 초기 body 클래스 설정
  updateBodyClass();
  
  // 현재 경로에 맞는 카테고리 펼치기
  syncCategoryFromRoute();
});

/**
 * 컴포넌트 언마운트 시 정리
 */
onUnmounted(() => {
  document.body.classList.remove('sidebar-collapsed');
});

/**
 * 라우트 변경 감지하여 카테고리 동기화
 */
watch(() => route.path, () => {
  syncCategoryFromRoute();
});


/**
 * 사용자가 변경되면 즐겨찾기 다시 로드
 */
watch(
  () => authStore.userId,
  (newUserId) => {
    if (newUserId) {
      favoritesStore.loadFavorites();
    } else {
      favoritesStore.favoriteMenus = [];
    }
  }
);

/**
 * 사용자 권한
 */
const roles = computed(() => authStore.roles);

/**
 * 권한 체크 함수 (roleUtils.js의 canShow 사용)
 */
const canShow = (roleArr = []) => {
  return checkPermission(roles.value, roleArr);
};

/**
 * 현재 워크스페이스 정보 가져오기 (roleConfig.js의 WORKSPACE_METADATA 사용)
 */
const currentWorkspace = computed(() => {
  return WORKSPACE_METADATA[sidebarStore.selectedWorkspace] || WORKSPACE_METADATA.home;
});

/**
 * 헤더에 표시할 워크스페이스 라벨 (i18n)
 */
const headerWorkspaceLabel = computed(() => {
  return getWorkspaceLabel(sidebarStore.selectedWorkspace);
});

/**
 * 워크스페이스 키로 i18n 라벨 반환
 */
function getWorkspaceLabel(key) {
  switch (key) {
    case 'home': return t('common.sidebar.workspace.home');
    case 'info': return t('common.sidebar.workspace.info');
    case 'receipt': return t('common.sidebar.workspace.receipt');
    case 'management': return t('common.sidebar.workspace.management');
    case 'user': return t('common.sidebar.workspace.user');
    case 'system': return t('common.sidebar.workspace.system');
    case 'favorites': return t('common.sidebar.workspace.favorites');
    default: return key || '';
  }
}

/**
 * 확장된 카테고리 상태
 */
const expandedCategories = ref({});

/**
 * 하나라도 열려있는지 확인
 */
const hasAnyExpanded = computed(() => {
  return Object.values(expandedCategories.value).some(value => value === true);
});

/**
 * 카테고리 토글
 */
const toggleCategory = (key) => {
  expandedCategories.value[key] = !expandedCategories.value[key];
};

/**
 * 전체 카테고리 열기
 */
const expandAll = () => {
  const allCategories = [
    'home-menu',
    'info-schedule-management-menu', 'info-board-menu', 'info-guide-menu',
    'receipt-register', 'receipt-approver', 'receipt-proxy', 'receipt-inspector', 'receipt-admin',
    'hrm-user-auth', 'hrm-organization', 'hrm-performance', 'hrm-approval',
    'user-info',
    'system-admin'
  ];
  
  allCategories.forEach(key => {
    expandedCategories.value[key] = true;
  });
};

/**
 * 전체 카테고리 닫기
 */
const collapseAll = () => {
  expandedCategories.value = {};
};

/**
 * 페이지 이동
 */
const goTo = (path) => {
  // 경로 기반으로 워크스페이스 판단
  if (path.includes('/favorite-menu-management')) {
    sidebarStore.selectWorkspace('favorites');
  } else if (path.includes('/info/')) {
    sidebarStore.selectWorkspace('info');
  } else if (path.includes('/user/')) {
    sidebarStore.selectWorkspace('user');
  } else if (path.includes('/management/')) {
    sidebarStore.selectWorkspace('management');
  } else if (path.includes('/system/')) {
    sidebarStore.selectWorkspace('system');
  } else if (path.includes('/receipt/')) {
    sidebarStore.selectWorkspace('receipt');
  }
  
  router.push(path);
};

/**
 * 현재 경로에 맞는 카테고리를 자동으로 펼침
 */
const syncCategoryFromRoute = () => {
  const currentPath = route.path;
  console.log('[SecondSidebar] 경로 동기화:', currentPath);
  
  // 모든 카테고리 초기화 (선택적)
  // Object.keys(expandedCategories.value).forEach(key => {
  //   expandedCategories.value[key] = false;
  // });
  
  // 모든 메뉴 배열을 모아서 확인
  const allMenus = [
    { path: ROUTES.MAIN, categoryKey: 'info-board-menu' },
    ...SCHEDULE_MENUS.map(m => ({ ...m, categoryKey: 'info-schedule-management-menu' })),
    ...INFO_MENUS.map(m => ({ ...m, categoryKey: 'info-board-menu' })),
    { ...GUIDE_MENU, categoryKey: 'info-guide-menu' },
    ...RECEIPT_REGISTRAR_MENUS.map(m => ({ ...m, categoryKey: 'receipt-registrar' })),
    ...RECEIPT_APPROVER_MENUS.map(m => ({ ...m, categoryKey: 'receipt-approver' })),
    ...RECEIPT_PROXY_MENUS.map(m => ({ ...m, categoryKey: 'receipt-proxy' })),
    ...RECEIPT_INSPECTOR_MENUS.map(m => ({ ...m, categoryKey: 'receipt-inspector' })),
    ...RECEIPT_MANAGER_MENUS.map(m => ({ ...m, categoryKey: 'receipt-manager' })),
    ...HRM_USER_AUTH_MENUS.map(m => ({ ...m, categoryKey: 'hrm-user-auth' })),
    ...HRM_ORGANIZATION_MENUS.map(m => ({ ...m, categoryKey: 'hrm-organization' })),
    ...HRM_PERFORMANCE_MENUS.map(m => ({ ...m, categoryKey: 'hrm-performance' })),
    ...HRM_APPROVAL_MENUS.map(m => ({ ...m, categoryKey: 'hrm-approval' })),
    ...USER_INFO_MENUS.map(m => ({ ...m, categoryKey: 'user-info' })),
    ...SYSTEM_USER_MENUS.map(m => ({ ...m, categoryKey: 'system-user' })),
    ...SYSTEM_ADMIN_MENUS.map(m => ({ ...m, categoryKey: 'system-admin' }))
  ];
  
  // 현재 경로와 일치하는 메뉴 찾기
  const matchedMenu = allMenus.find(menu => menu.path === currentPath);
  
  console.log('[SecondSidebar] 일치하는 메뉴:', matchedMenu);
  
  if (matchedMenu && matchedMenu.categoryKey) {
    console.log('[SecondSidebar] 카테고리 펼치기:', matchedMenu.categoryKey);
    // 해당 카테고리 펼치기
    expandedCategories.value[matchedMenu.categoryKey] = true;
  } else {
    console.warn('[SecondSidebar] 일치하는 메뉴를 찾을 수 없음:', currentPath);
  }
};

/**
 * 즐겨찾기 토글
 */
const toggleFavorite = (menu, event) => {
  event.stopPropagation();
  
  // 이미 즐겨찾기인 경우 제거 확인 모달 표시
  if (checkIsFavorite(menu.path)) {
    favoriteToRemove.value = menu;
    favoriteRemoveModalVisible.value = true;
  } else {
    // 즐겨찾기 추가는 바로 처리
    favoritesStore.toggleFavorite(menu);
  }
};

/**
 * 즐겨찾기 제거 확인
 */
const confirmRemoveFavorite = () => {
  if (favoriteToRemove.value) {
    favoritesStore.toggleFavorite(favoriteToRemove.value);
    favoriteToRemove.value = null;
  }
  favoriteRemoveModalVisible.value = false;
};

/**
 * 즐겨찾기 제거 취소
 */
const cancelRemoveFavorite = () => {
  favoriteToRemove.value = null;
  favoriteRemoveModalVisible.value = false;
};

/**
 * 즐겨찾기 여부 확인
 */
const checkIsFavorite = (path) => {
  return favoritesStore.isFavorite(path);
};

/**
 * 스크롤바 표시 상태
 */
const showScrollbar = ref(false);

/**
 * 즐겨찾기 제거 확인 모달 상태
 */
const favoriteRemoveModalVisible = ref(false);
const favoriteToRemove = ref(null);

/**
 * 사이드바 접기 상태
 */
const isCollapsed = ref(false);

/**
 * 사이드바 접기/펴기 토글
 */
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
  updateBodyClass();
};

/**
 * body 클래스 업데이트
 */
const updateBodyClass = () => {
  if (isCollapsed.value) {
    document.body.classList.add('sidebar-collapsed');
  } else {
    document.body.classList.remove('sidebar-collapsed');
  }
};

/**
 * 접기 버튼 위치 스타일 계산
 */
const collapseButtonStyle = computed(() => {
  const sidebarLeft = 60; // 첫 번째 사이드바 폭
  const sidebarWidth = isCollapsed.value ? 60 : 260;
  const buttonLeft = sidebarLeft + sidebarWidth;
  
  return {
    position: 'fixed',
    left: `${buttonLeft}px`,
    top: '65px', // 헤더 시작 위치 (헤더 전체 높이와 버튼 높이가 같음)
    zIndex: 1010
  };
});

/**
 * 마우스 진입 시 스크롤바 표시
 */
const handleMouseEnter = () => {
  showScrollbar.value = true;
};

/**
 * 마우스 떠날 때 스크롤바 즉시 숨김
 */
const handleMouseLeave = () => {
  showScrollbar.value = false;
};

/**
 * 드래그 시작 전 원본 순서 저장
 */
const originalOrder = ref([]);

/**
 * 드래그 시작 시 원본 순서 기억
 */
const handleDragStart = () => {
  originalOrder.value = favoritesStore.favoriteMenus.map(item => item.id);
};

/**
 * 드래그 종료 시 순서 변경 확인 및 저장
 */
const handleDragEnd = () => {
  const currentOrder = favoritesStore.favoriteMenus.map(item => item.id);
  
  // 순서가 실제로 변경되었는지 확인
  const isOrderChanged = originalOrder.value.some((id, index) => id !== currentOrder[index]);
  
  if (isOrderChanged) {
    favoritesStore.reorderFavorites(favoritesStore.favoriteMenus);
    toast.success('즐겨찾기 순서가 변경되었습니다', {
      autoClose: 1500,
    });
  }
};
</script>

<style scoped>
/* ===== 두 번째 사이드바 ===== */
.second-sidebar {
  position: fixed;
  left: 60px; /* 첫 번째 사이드바 오른쪽 */
  top: 65px; /* 헤더 높이만큼 아래 */
  width: 260px;
  height: calc(100vh - 65px);
  background: var(--theme-sidebar-bg);
  border-right: 1px solid var(--theme-border);
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);
  z-index: 1009;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s ease, box-shadow 0.3s ease;
}

/* 접힌 상태 */
.second-sidebar.collapsed {
  width: 60px;
  box-shadow: 1px 0 3px rgba(0, 0, 0, 0.1);
}

/* 접힌 상태에서 텍스트 숨기기 - GPU 가속 사용 */
.second-sidebar.collapsed .sidebar-header span,
.second-sidebar.collapsed .header-controls {
  opacity: 0;
  visibility: hidden;
  transform: translate3d(-10px, 0, 0);
  transition: opacity 0.15s cubic-bezier(0.4, 0, 0.2, 1), 
              visibility 0s linear 0.15s,
              transform 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.second-sidebar.collapsed .sidebar-nav {
  opacity: 0;
  visibility: hidden;
  transform: translate3d(-10px, 0, 0);
  transition: opacity 0.15s cubic-bezier(0.4, 0, 0.2, 1), 
              visibility 0s linear 0.15s,
              transform 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 접힌 상태에서 개별 메뉴 아이템들도 사라짐 */
.second-sidebar.collapsed .category-header,
.second-sidebar.collapsed .sub-nav-link,
.second-sidebar.collapsed .favorites-info,
.second-sidebar.collapsed .sidebar-footer,
.second-sidebar.collapsed .footer-manage-btn-wrapper,
.second-sidebar.collapsed .home-widget-card,
.second-sidebar.collapsed .time-card,
.second-sidebar.collapsed .user-info-card,
.second-sidebar.collapsed .employee-search-card {
  opacity: 0;
  visibility: hidden;
  transform: translate3d(-10px, 0, 0);
  transition: opacity 0.15s cubic-bezier(0.4, 0, 0.2, 1), 
              visibility 0s linear 0.15s,
              transform 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 기본 상태에서의 transition - GPU 가속 및 easing 개선 */
.sidebar-header span,
.header-controls {
  will-change: opacity, transform;
  transform: translate3d(0, 0, 0);
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s, 
              visibility 0s linear,
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
}

.sidebar-nav {
  margin-bottom: 10px;
  height: 100%;
  will-change: opacity, transform;
  overscroll-behavior: contain;
  transform: translate3d(0, 0, 0);
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s, 
              visibility 0s linear,
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
}

/* 접힌 상태에서 패딩만 조정, 아이콘 위치는 그대로 */
.second-sidebar.collapsed .sidebar-header {
  padding: 0 20px; /* 원래 패딩 유지 */
}

/* 접힌 상태에서 아이콘 크기 조정 */
.second-sidebar.collapsed .sidebar-header > i {
  font-size: 24px;
}


/* ===== 사이드바 헤더 (대메뉴 표시) - 고정 ===== */
.sidebar-header {
  position: relative;
  height: 60px;
  background: linear-gradient(135deg, #777D8A 0%, #58606D 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 16px;
  font-weight: 700;
  gap: 12px;
  border-bottom: 1px solid #d1d5db;
  flex-shrink: 0;
  overflow: hidden; /* 텍스트가 잘리도록 */
  transition: padding 0.3s ease, justify-content 0.3s ease;
}

/* ===== 사이드바 푸터 (하단 고정) ===== */
.sidebar-footer {
  flex-shrink: 0;
  background: var(--theme-sidebar-bg);
  padding: 0;
  will-change: opacity, transform;
  transform: translate3d(0, 0, 0);
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s, 
              visibility 0s linear,
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
}

.sidebar-header > i {
  font-size: 22px;
}

.sidebar-header > span {
  flex: 1;
  white-space: nowrap; /* 텍스트 줄바꿈 방지 */
  transition: all 0.25s ease;
}

.header-controls {
  display: flex;
  gap: 2px;
  margin-left: auto;
}

/* 사이드바 접기 버튼 (body에 렌더링) */
.sidebar-collapse-btn {
  width: 22px;
  height: 60px; /* 헤더 높이와 동일 */
  background: linear-gradient(135deg, #c7c7c7 0%, #8b8b8b 100%);
  border: none;
  border-radius: 0 5px 5px 0;
  color: #ffffff;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 
    2px 0 8px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.sidebar-collapse-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.sidebar-collapse-btn:hover {
  background: linear-gradient(135deg, #adadad 0%, #cecece 100%);
  box-shadow: 
    3px 0 12px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transform: translateX(2px) scale(1.02);
}

.sidebar-collapse-btn:hover::before {
  opacity: 1;
}

.control-btn {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0;
}

.control-btn i {
  font-size: 14px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.control-btn:active {
  background: rgba(255, 255, 255, 0.15);
  transform: scale(0.8);
}

/* ===== 홈 워크스페이스 위젯 카드 ===== */
/* TimeCard, UserInfoCard, EmployeeSearchCard는 모두 자체 스타일 사용 */

/* 검색 카드 래퍼 - EmployeeSearchCard가 자체 스타일 가짐 */
.search-card {
  margin: 0 !important;
  padding: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
}

.search-card:hover {
  transform: none !important;
  box-shadow: none !important;
}

/* ===== 네비게이션 메뉴 - 스크롤 영역 ===== */
.sidebar-nav {
  overflow-y: auto;
  overflow-x: hidden;

  /* 항상 거터 예약 */
  scrollbar-gutter: stable;

  /* 거터를 먹어도 내용이 안 밀려 보이게 하는 트릭 */
  /* 스크롤바/거터 너비 한 군데로 관리 */
  --sbw: 12px;

  /* Firefox: 얇게 + 기본부터 보이게 */
  scrollbar-width: thin;
  scrollbar-color: rgba(160,174,192,.35) transparent;
}

/* WebKit: 항상 보이는 얇은 스크롤바 */
.sidebar-nav::-webkit-scrollbar {
  width: var(--sbw);
}
.sidebar-nav::-webkit-scrollbar-track {
  /* 필요하면 사이드바 배경색과 맞춰도 됨 */
  background: transparent;
}
.sidebar-nav::-webkit-scrollbar-thumb {
  /* 기본부터 보이게 */
  background: rgba(160,174,192,.35);
  border-radius: 6px;
}

/* ===== 카테고리 헤더 (역할 구분) ===== */
.nav-item {
  margin-bottom: 8px;
}

.category-header {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: var(--theme-text-primary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease 0.1s; /* 펼칠 때 지연 */
  position: relative;
  overflow: hidden; /* 텍스트가 잘리도록 */
  gap: 10px;
}

.category-header i:first-child {
  font-size: 18px;
  color: #718096;
}

.category-header span {
  flex: 1;
  white-space: nowrap; /* 텍스트 줄바꿈 방지 */
  overflow: hidden;
}

.category-header .toggle-icon {
  font-size: 16px;
  color: #a0aec0;
  margin-left: auto;
}

.category-header:hover {
  background: var(--theme-bg-hover);
  color: var(--theme-text-primary);
}

.category-header.has-children {
  cursor: pointer;
}

/* 자식 메뉴 (서브 메뉴) */
.sub-menu {
  background: var(--theme-submenu-bg);
  border-left: 2px solid #cbd5e0;
  margin-left: 20px;
}

.sub-nav-link {
  display: flex;
  align-items: center;
  padding: 10px 20px 10px 25px;
  color: var(--theme-text-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease 0.1s; /* 펼칠 때 지연 */
  overflow: hidden; /* 텍스트가 잘리도록 */
  gap: 8px;
  position: relative;
}

.sub-nav-link > i:first-child {
  font-size: 16px;
  color: #a0aec0;
  flex-shrink: 0;
}

.sub-nav-link > span {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap; /* 텍스트 줄바꿈 방지 */
}

.sub-nav-link:hover {
  background: var(--theme-bg-hover);
  color: var(--theme-text-primary);
}

.sub-nav-link.active {
  background: #d6ebff;
  color: #1a1a1a;
  font-weight: 600;
}

.sub-nav-link.active i {
  color: #1a1a1a;
}

/* 즐겨찾기 버튼 */
.favorite-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s ease;
  flex-shrink: 0;
  margin-left: auto;
}

.favorite-btn i {
  font-size: 14px;
  color: #a0aec0;
}

/* 호버 시 별 아이콘 표시 */
.sub-nav-link:hover .favorite-btn {
  opacity: 1;
}

/* 즐겨찾기된 항목은 항상 표시 */
.favorite-btn.active {
  opacity: 1;
}

.favorite-btn.active i {
  color: #f6ad55;
}

.favorite-btn:hover {
  background: rgba(246, 173, 85, 0.1);
  transform: scale(1.1);
}

.favorite-btn:hover i {
  color: #f6ad55;
}

.favorite-btn:active {
  transform: scale(0.95);
}

/* ===== 빈 상태 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  color: #a0aec0;
}

.empty-state > i:first-child {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 14px;
  font-weight: 600;
  color: #718096;
  margin-bottom: 8px;
  line-height: 1.5;
}

.empty-state small {
  font-size: 12px;
  color: #a0aec0;
  line-height: 1.5;
  margin-bottom: 0;
}

.empty-state .manage-btn {
  margin-top: 15px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}

.empty-state .manage-btn:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 4px 10px rgba(59, 130, 246, 0.4);
  transform: translateY(-1px);
}

.empty-state .manage-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.empty-state .manage-btn i {
  font-size: 15px;
}

/* 하단 관리 버튼 래퍼 */
.footer-manage-btn-wrapper {
  padding: 10px 15px 0 15px;
  will-change: opacity, transform;
  transform: translate3d(0, 0, 0);
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s, 
              visibility 0s linear,
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
}

.footer-manage-btn {
  width: 100%;
  padding: 10px 15px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.footer-manage-btn:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  border-color: #cbd5e1;
  color: #1e293b;
}

.footer-manage-btn:active {
  transform: scale(0.98);
}

.footer-manage-btn i {
  font-size: 14px;
}

/* 즐겨찾기 정보 안내 (하단 고정) */
.sidebar-footer .favorites-info {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 20px 10px 15px;
  margin: 10px 15px 10px 15px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  font-size: 0.65rem;
  color: #1890ff;
  line-height: 1.4;
  overflow: hidden; /* 텍스트가 잘리도록 */
  will-change: opacity, transform;
  transform: translate3d(0, 0, 0);
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s, 
              visibility 0s linear,
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1) 0.05s;
}

.sidebar-footer .favorites-info i {
  font-size: 14px;
  flex-shrink: 0;
  margin-top: 1px;
}

.sidebar-footer .favorites-info .info-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.5;
  overflow: hidden; /* 텍스트가 잘리도록 */
}

.sidebar-footer .favorites-info .info-text > div {
  white-space: nowrap; /* 텍스트 줄바꿈 방지 */
  overflow: hidden;
}

/* ===== 즐겨찾기 드래그 앤 드롭 ===== */
.favorites-list {
  padding: 10px 0;
}

.draggable-favorite {
  cursor: pointer; /* 클릭 가능한 커서로 변경 */
  padding: 8px 15px 10px 10px !important;
  flex-direction: column;
  align-items: flex-start;
  gap: 0px;
}

/* 첫 번째 줄: 드래그 핸들 + 워크스페이스 + 카테고리 + 별 */
.favorite-top-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.drag-handle {
  cursor: grab;
  color: #a0aec0;
  display: flex;
  align-items: center;
  opacity: 0.5;
  transition: opacity 0.2s ease;
  flex-shrink: 0;
}

.drag-handle:active {
  cursor: grabbing;
}

.draggable-favorite:hover .drag-handle {
  opacity: 1;
  color: #718096;
}

/* 드래그 핸들 호버 시에도 grab 커서 유지 */
.drag-handle:hover {
  cursor: grab;
}

.drag-handle:hover:active {
  cursor: grabbing;
}

.workspace-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  padding: 2px 6px;
  background: #edf2f7;
  color: #718096;
  border-radius: 3px;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease;
}

.workspace-badge:hover {
  background: #e2e8f0;
  color: #4a5568;
}

.workspace-badge i {
  font-size: 12px;
}

.category-text {
  flex: 1;
  color: #a0aec0;
  font-size: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s ease;
}

.category-text:hover {
  color: #718096;
}

/* 두 번째 줄: 아이콘 + 메뉴명 (드래그 핸들과 정렬) */
.favorite-bottom-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 15px 8px 0px;
  margin-right: 8px;
  margin-left: 22px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.favorite-bottom-row:hover {
  color: #2d3748;
}

.favorite-bottom-row:hover > i {
  color: #718096;
}

.drag-handle-spacer {
  /* width: 20px; */
  flex-shrink: 0;
}

.favorite-bottom-row > i {
  font-size: 16px;
  color: #a0aec0;
  flex-shrink: 0;
  transition: color 0.2s ease;
}

.draggable-favorite .menu-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  font-weight: 500;
}

/* 드래그 중인 아이템 */
.sortable-ghost {
  opacity: 0.5;
  background: #edf2f7;
}

.sortable-drag {
  opacity: 0.8;
}

/* ===== 애니메이션 ===== */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 500px;
  opacity: 1;
}

/* 모바일 대응 */
@media (max-width: 1300px) {
  .second-sidebar {
    display: none;
  }
  .sidebar-collapse-btn{
    display: none;
  }
}
</style>
