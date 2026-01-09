<template>
  <div v-if="isReceiptEnabled">
    <hr class="hr-line-receipt" />

    <!-- 데스크톱: 대리결재자 지정 + 우측 히스토리 -->
    <div v-if="!mobile">
      <div class="delegate-two-col">
        <div v-if="canManageDelegate" class="delegate-section">
          <!-- 지정 전 카드 -->
          <div v-if="!activeDelegate" class="delegate-assign-card">
            <div class="delegate-assign-card__header">
              <i class="ri-user-add-line"></i>
              <span>{{ $t('receipt.delegateApproval.title') }}</span>
            </div>
            <div
              class="delegate-assign-card__body"
              @keydown.enter.prevent.stop="onDelegateEnter"
            >
              <DefaultFormRow class="delegate-controls">
                <UserSearchDropdown
                  inputId="delegateSearch"
                  :placeholder="$t('receipt.delegateApproval.delegatee') + ' ' + $t('common.label.search')"
                  :includeCurrentUser="false"
                  :filterBy="{ service: 'receipt', roleDetails: RECEIPT_DELEGATE_ROLES }"
                  :disabled="!!activeDelegate"
                  @userSelected="onDelegateSelected"
                />
                <DefaultButton
                  size="small"
                  @click="saveDelegate"
                  :disabled="!!activeDelegate || !selectedDelegate"
                  >{{ $t('receipt.delegateApproval.assign') }}</DefaultButton
                >
              </DefaultFormRow>

              <div v-if="selectedDelegate" class="selected-chip">
                <i class="ri-user-line"></i>
                <span>{{ selectedDelegate.userName }}</span>
                <span class="email">({{ selectedDelegate.email }})</span>
                <div class="delegate-candidate">{{ $t('receipt.delegateApproval.delegatee') }}</div>
                <button type="button" class="chip-close" @click="selectedDelegate = null">
                  ×
                </button>
              </div>
              <div v-else class="selected-chip">
                <i class="ri-information-line"></i>
                <span>{{ $t('receipt.delegateApproval.noRequests') }}</span>
              </div>

              <!-- 대리결재 안내 문구 -->
              <div class="delegate-guide">
                <div class="guide-content">
                  <i class="ri-lightbulb-line"></i>
                  <div class="guide-text">
                    <h4 v-if="selectedDelegate">{{ $t('receipt.delegateApproval.assignConfirm') }}</h4>
                    <h4 v-else>{{ $t('receipt.delegateApproval.title') }}</h4>
                    <p v-if="selectedDelegate">
                      {{ $t('receipt.delegateApproval.assignGuideWithName', { name: selectedDelegate.userName }) }}
                    </p>
                    <p v-else>
                      {{ $t('receipt.delegateApproval.assignGuide') }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 지정 후 카드 -->
          <div v-if="activeDelegate" class="delegate-summary box-shadow-blue">
            <div class="delegate-card">
              <div class="delegate-card__header">
                <i class="ri-shield-user-line"></i>
                <span>{{ $t('receipt.delegateApproval.currentDelegate') }}</span>
                <button type="button" class="badge-red" @click="removeDelegate">
                  {{ $t('receipt.delegateApproval.remove') }}
                </button>
              </div>
              <div class="delegate-card__body">
                <div class="info-chip">
                  <i class="ri-user-line"></i>
                  <DefaultLabel :text="$t('receipt.delegateApproval.delegatee')" alignment="left" size="small" />
                  <DefaultLabel
                    :text="activeDelegate.delegateName || activeDelegate.userName"
                    alignment="left"
                    size="small"
                  />
                </div>
                <div class="info-chip">
                  <i class="ri-mail-line"></i>
                  <DefaultLabel :text="$t('common.label.email')" alignment="left" size="small" />
                  <DefaultLabel
                    :text="activeDelegate.delegateEmail || '-'"
                    alignment="left"
                    size="small"
                  />
                </div>
                <div class="info-chip">
                  <i class="ri-building-line"></i>
                  <DefaultLabel :text="$t('common.label.department')" alignment="left" size="small" />
                  <DefaultLabel
                    :text="activeDelegate.delegateDepartment || '-'"
                    alignment="left"
                    size="small"
                  />
                </div>
                <div class="info-chip">
                  <i class="ri-group-line"></i>
                  <DefaultLabel :text="$t('common.label.team')" alignment="left" size="small" />
                  <DefaultLabel
                    :text="activeDelegate.delegateTeam || '-'"
                    alignment="left"
                    size="small"
                  />
                </div>

                <!-- 지정된 대리결재자 안내 -->
                <div class="delegate-active-guide">
                  <div class="active-guide-content">
                    <i class="ri-check-line"></i>
                    <div class="active-guide-text">
                      <span class="active-guide-message">
                        <strong>{{ activeDelegate.delegateName || activeDelegate.userName }}</strong>님이 내 결재 차례일 때 
                        대리결재를 진행할 수 있습니다.
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 나를 대리자로 지정한 원결재자 목록 -->
          <div v-if="canManageDelegate && principalsWhoDesignatedMe.length" class="designated-me-box">
            <div class="designated-me-title">
              <i class="ri-user-follow-line"></i>
              <span>{{ $t('receipt.delegateApproval.delegator') }}</span>
            </div>
            <ul class="designated-me-list">
              <li v-for="p in principalsWhoDesignatedMe" :key="p.id">
                <span class="name">{{ formatPrincipalDisplay(p.principalUserId) }}</span>
                <span class="sep">·</span>
                <span class="org">{{
                  (p.delegateDepartment || '-') + '/' + (p.delegateTeam || '-')
                }}</span>
              </li>
            </ul>
          </div>
        </div>
        <!-- 우측 히스토리 문구 -->
        <div class="history-inline-text">
          <span class="history-title">
            <i class="ri-time-line"></i>
            {{ $t('receipt.common.historyTitle') }}
          </span>
          <div class="history-body">
            <div class="history-period" v-if="periodStartDate && periodEndDate">
              <span class="period-text"
                >{{ $t('receipt.common.dateRange') }} {{ periodStartDate }} ~ {{ periodEndDate }} {{ $t('receipt.common.dateRangeEnd') }}</span
              >
            </div>
            <div class="history-summary">
              <span>{{ $t('receipt.common.thisMonth') }}</span>&nbsp;
              <strong>{{ thisMonthSubmitCount || 0 }}{{ $t('receipt.common.count') }}</strong>
              · <strong>{{ formatAmountKRW(thisMonthTotalAmount) }}</strong>
              <template v-if="myPendingCount && myPendingCount > 0">
                · <span class="pending">{{ $t('receipt.common.myTurn') }} {{ myPendingCount }}{{ $t('receipt.common.count') }}</span>
              </template>
            </div>
            <!-- 데스크톱 승인율: 2줄 구성 (라벨/퍼센트바+수치) -->
            <div class="rate-box-desktop" v-if="!mobile" :aria-label="$t('receipt.common.approvalRate')">
              <div class="rate-label">
                <i class="ri-percent-line"></i>
                <span>{{ $t('receipt.common.approvalRate') }}</span>
                <span class="hint">({{ $t('receipt.common.approvalRateHint') }})</span>
                <i
                  class="ri-information-line info-tip"
                  :v-tooltip="$t('receipt.common.approvalRateTooltip')"
                  :aria-label="$t('receipt.common.approvalRateTooltip')"
                ></i>
              </div>
              <div class="rate-bar">
                <span class="pct-rail">
                  <span
                    class="pct-fill"
                    :style="{
                      width: `${Math.min(Math.max(thisMonthApprovalRate || 0, 0), 100)}%`,
                    }"
                  ></span>
                </span>
                <span class="rate-value">{{ thisMonthApprovalRate || 0 }}%</span>
              </div>
            </div>
            <div class="history-actions">
              <DefaultFormRow align="between">
                <button
                  type="button"
                  class="link-button"
                  @click="goThisMonthPersonalHistory"
                >
                  {{ $t('receipt.personal.submitted') }}
                </button>
                <button type="button" class="link-button" @click="goMyPending">
                  {{ $t('receipt.approvalRequest.title') }}
                </button>
              </DefaultFormRow>
            </div>
          </div>
        </div>
      </div>

      <!-- 공통 히스토리 리스트 (데스크톱/모바일) -->
      <div v-if="canViewHistory" class="history-box">
        <div class="history-box-title" @click="goHistoryPageForPeriod">
          <i class="ri-history-line"></i>
          <span>{{ $t('receipt.common.historyTitle') }}</span>
        </div>

        <!-- 데스크톱 헤더 -->
        <div
          class="history-feed-header"
          v-if="!mobile && !isHistoryLoading && displayedHistory.length"
        >
          <span class="h-icon"></span>
          <span class="h-time">{{ $t('common.label.date') }}</span>
          <span class="h-status">{{ $t('common.label.status') }}</span>
          <span class="h-applicant">{{ $t('receipt.submission.create') }}</span>
          <span class="h-category">{{ $t('receipt.submission.category') }}</span>
          <span class="h-amount">{{ $t('receipt.common.amount') }}</span>
          <span class="h-text">{{ $t('receipt.common.reason') }}</span>
        </div>

        <!-- 모바일 헤더 -->
        <div
          class="mobile-feed-header"
          v-if="mobile && !isHistoryLoading && displayedHistory.length"
        >
          <span>{{ $t('common.label.date') }}</span>
          <span>{{ $t('common.label.status') }}</span>
          <span>{{ $t('receipt.submission.create') }}</span>
        </div>

        <ul class="history-feed" @click="openReceiptDetailFromActivity($event)">
          <li v-if="isHistoryLoading" class="loading-row">
            <i class="ri-loader-4-line spin"></i>
            <span>{{ $t('common.message.loading') }}</span>
          </li>
          <li v-else-if="!displayedHistory.length" class="empty-row">
            <span class="selected-chip empty-chip">
              <i class="ri-time-line"></i>
              {{ $t('receipt.common.noRecentActivity') }}
            </span>
          </li>
          <li
            v-for="(h, idx) in displayedHistory"
            :key="h.key || idx"
            :data-receipt-id="h.receiptId || null"
            :data-receipt-code="h.receiptCode || null"
          >
            <i :class="h.iconClass" class="feed-icon h-icon"></i>
            <span class="feed-time h-time">{{ h.ts }}</span>
            <span class="h-status">
              <IconBadge :color="statusBadgeColor(h.label)">{{ h.label }}</IconBadge>
            </span>
            <span
              class="feed-applicant h-applicant"
              v-if="h.applicant?.userName"
              :title="h.applicant.userEmail || ''"
            >
              <span class="app-name">{{ h.applicant.userName }}</span>
              <span v-if="mobile" class="app-sub">
                <span class="app-pos" v-if="h.applicant.positionName">{{ h.applicant.positionName }}</span>
                <span class="app-email" v-if="h.applicant.userEmail">{{ h.applicant.userEmail }}</span>
              </span>
            </span>
            <span class="feed-category h-category" v-if="!mobile && h.categoryName">{{
              h.categoryName
            }}</span>
            <span class="feed-amount h-amount" v-if="!mobile && h.amount != null">{{
              formatAmountKRW(h.amount)
            }}</span>
            <span class="feed-text h-text" :title="h.text" v-if="!mobile">{{
              h.text
            }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 모바일: 대리결재/나를 지정한 원결재자/히스토리 요약 -->
    <div v-else class="delegate-section">
      <!-- 지정 전: 박스 형태 카드 (모바일) -->
      <div v-if="canManageDelegate && !activeDelegate" class="delegate-assign-card">
        <div class="delegate-assign-card__header">
          <i class="ri-user-add-line"></i>
          <span>{{ $t('receipt.delegateApproval.title') }}</span>
        </div>
        <div
          class="delegate-assign-card__body"
          @keydown.enter.prevent.stop="onDelegateEnter"
        >
          <DefaultFormRow class="delegate-controls">
            <UserSearchDropdown
              inputId="delegateSearch"
              :placeholder="$t('receipt.delegateApproval.delegatee') + ' ' + $t('common.label.search')"
              :includeCurrentUser="false"
              :filterBy="{ service: 'receipt', roleDetails: requiredDelegateRoles }"
              :disabled="!!activeDelegate"
              @userSelected="onDelegateSelected"
            />
            <DefaultButton
              size="small"
              @click="saveDelegate"
              :disabled="!!activeDelegate || !selectedDelegate"
              >{{ $t('receipt.delegateApproval.assign') }}</DefaultButton
            >
          </DefaultFormRow>

          <div v-if="selectedDelegate" class="selected-chip">
            <i class="ri-user-line"></i>
            <span>{{ selectedDelegate.userName }}</span>
            <span class="email">({{ selectedDelegate.email }})</span>
            <div class="delegate-candidate">{{ $t('receipt.delegateApproval.delegatee') }}</div>
            <button type="button" class="chip-close" @click="selectedDelegate = null">
              ×
            </button>
          </div>
          <div v-else class="selected-chip">
            <i class="ri-information-line"></i>
            <span>{{ $t('receipt.delegateApproval.noRequests') }}</span>
          </div>

          <!-- 대리결재 안내 문구 -->
          <div class="delegate-guide mobile-delegate-guide">
            <div class="guide-content">
              <i class="ri-lightbulb-line"></i>
              <div class="guide-text">
                <h4 v-if="selectedDelegate">{{ $t('receipt.delegateApproval.assignConfirm') }}</h4>
                <h4 v-else>{{ $t('receipt.delegateApproval.title') }}</h4>
                <p v-if="selectedDelegate">
                  {{ $t('receipt.delegateApproval.assignGuideWithName', { name: selectedDelegate.userName }) }}
                </p>
                <p v-else>
                  {{ $t('receipt.delegateApproval.assignGuide') }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 지정 후 카드 (모바일) -->
      <div v-if="canManageDelegate && activeDelegate" class="delegate-summary box-shadow-blue">
        <div class="delegate-card">
          <div class="delegate-card__header">
            <i class="ri-shield-user-line"></i>
            <span>{{ $t('receipt.delegateApproval.currentDelegate') }}</span>
            <button type="button" class="badge-red" @click="removeDelegate">{{ $t('receipt.delegateApproval.remove') }}</button>
          </div>
          <div class="delegate-card__body">
            <div class="info-chip">
              <i class="ri-user-line"></i>
              <DefaultLabel :text="$t('receipt.delegateApproval.delegatee')" alignment="left" size="small" />
              <DefaultLabel
                :text="activeDelegate.delegateName || activeDelegate.userName"
                alignment="left"
                size="small"
              />
            </div>
            <div class="info-chip">
              <i class="ri-mail-line"></i>
              <DefaultLabel :text="$t('common.label.email')" alignment="left" size="small" />
              <DefaultLabel
                :text="activeDelegate.delegateEmail || '-'"
                alignment="left"
                size="small"
              />
            </div>
            <div class="info-chip">
              <i class="ri-building-line"></i>
              <DefaultLabel :text="$t('common.label.department')" alignment="left" size="small" />
              <DefaultLabel
                :text="activeDelegate.delegateDepartment || '-'"
                alignment="left"
                size="small"
              />
            </div>
            <div class="info-chip">
              <i class="ri-group-line"></i>
              <DefaultLabel :text="$t('common.label.team')" alignment="left" size="small" />
              <DefaultLabel
                :text="activeDelegate.delegateTeam || '-'"
                alignment="left"
                size="small"
              />
            </div>

            <!-- 지정된 대리결재자 안내 (모바일) -->
            <div class="delegate-active-guide mobile-active-guide">
              <div class="active-guide-content">
                <i class="ri-check-line"></i>
                <div class="active-guide-text">
                  <span class="active-guide-message">
                    <strong>{{ activeDelegate.delegateName || activeDelegate.userName }}</strong>님이 내 결재 차례일 때 
                    대리결재를 진행할 수 있습니다.
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 나를 대리자로 지정한 원결재자 목록 (모바일) -->
      <div v-if="canManageDelegate && principalsWhoDesignatedMe.length" class="designated-me-box">
          <div class="designated-me-title">
            <i class="ri-user-follow-line"></i>
            <span>{{ $t('receipt.delegateApproval.delegator') }}</span>
          </div>
        <ul class="designated-me-list">
          <li v-for="p in principalsWhoDesignatedMe" :key="p.id">
            <span class="name">{{ formatPrincipalDisplay(p.principalUserId) }}</span>
            <span class="sep">·</span>
            <span class="org">{{
              (p.delegateDepartment || '-') + '/' + (p.delegateTeam || '-')
            }}</span>
          </li>
        </ul>
      </div>

      <!-- 모바일 히스토리 요약 영역 -->
      <div class="history-inline-text history-inline-mobile">
        <div class="history-inline-mobile__header">
          <i class="ri-time-line"></i>
          <span>{{ $t('receipt.common.historyTitle') }}</span>
        </div>
        <div class="summary-row" v-if="periodStartDate && periodEndDate">
          <span class="chip chip-period" :title="`${periodStartDate} ~ ${periodEndDate}`">
            <i class="ri-calendar-2-line"></i>
            <span class="label">{{ $t('receipt.common.dateRange') }}</span>
            <span class="value">{{ periodStartDate }} ~ {{ periodEndDate }}</span>
          </span>
          <span class="chip" :title="$t('receipt.common.totalReceiptCount')">
            <i class="ri-list-check"></i>
            <span class="label">{{ $t('receipt.common.count') }}</span>
            <span class="value">{{ thisMonthSubmitCount || 0 }}{{ $t('receipt.common.count') }}</span>
          </span>
          <span class="chip chip-amount" :title="$t('receipt.common.totalAmount')">
            <i class="ri-coins-line"></i>
            <span class="label">{{ $t('receipt.common.totalAmount') }}</span>
            <span class="value">{{ formatAmountKRW(thisMonthTotalAmount) }}</span>
          </span>
          <div class="rate-box" :aria-label="$t('receipt.common.approvalRate')">
            <div class="rate-label">
              <i class="ri-percent-line"></i>
              <span>{{ $t('receipt.common.approvalRate') }}</span>
              <span class="hint">({{ $t('receipt.common.approvalRateHint') }})</span>
              <i
                class="ri-information-line info-tip"
                :v-tooltip="$t('receipt.common.approvalRateTooltip')"
                :aria-label="$t('receipt.common.approvalRateTooltip')"
              ></i>
            </div>
            <div class="rate-bar">
              <span class="pct-mini">
                <span
                  class="pct-fill"
                  :style="{
                    width: `${Math.min(Math.max(thisMonthApprovalRate || 0, 0), 100)}%`,
                  }"
                ></span>
              </span>
              <span class="rate-value">{{ thisMonthApprovalRate || 0 }}%</span>
            </div>
          </div>
          <span
            v-if="myPendingCount && myPendingCount > 0"
            class="chip chip-pending"
            :title="$t('receipt.common.myTurn')"
          >
            <i class="ri-time-line"></i>
            <span class="label">{{ $t('receipt.common.myTurn') }}</span>
            <span class="value">{{ myPendingCount }}{{ $t('receipt.common.count') }}</span>
          </span>
        </div>

        <div class="history-actions">
          <DefaultFormRow align="between" marginLeft="3px">
            <button type="button" class="link-button" @click="goThisMonthPersonalHistory">
              {{ $t('receipt.personal.submitted') }}
            </button>
            <button type="button" class="link-button" @click="goMyPending">
              {{ $t('receipt.approvalRequest.title') }}
            </button>
          </DefaultFormRow>
        </div>
      </div>

      <!-- 모바일 내 결재선 활동 내역 영역 -->
      <div v-if="canViewHistory" class="history-box history-box-mobile">
        <div class="history-box-title" @click="goHistoryPageForPeriod">
          <i class="ri-history-line"></i>
          <span>{{ $t('receipt.common.historyTitle') }}</span>
        </div>

        <!-- 데스크톱 헤더 -->
        <div
          class="history-feed-header"
          v-if="!mobile && !isHistoryLoading && displayedHistory.length"
        >
          <span class="h-icon"></span>
          <span class="h-time">{{ $t('common.label.date') }}</span>
          <span class="h-status">{{ $t('common.label.status') }}</span>
          <span class="h-applicant">{{ $t('receipt.submission.create') }}</span>
          <span class="h-category">{{ $t('receipt.submission.category') }}</span>
          <span class="h-amount">{{ $t('receipt.common.amount') }}</span>
          <span class="h-text">{{ $t('receipt.common.reason') }}</span>
        </div>

        <!-- 모바일 헤더 -->
        <div
          class="mobile-feed-header"
          v-if="mobile && !isHistoryLoading && displayedHistory.length"
        >
          <span>{{ $t('common.label.date') }}</span>
          <span>{{ $t('common.label.status') }}</span>
          <span>{{ $t('receipt.submission.create') }}</span>
        </div>

        <ul class="history-feed" @click="openReceiptDetailFromActivity($event)">
          <li v-if="isHistoryLoading" class="loading-row">
            <i class="ri-loader-4-line spin"></i>
            <span>{{ $t('receipt.common.loading') }}</span>
          </li>
          <li v-else-if="!displayedHistory.length" class="empty-row">
            <span class="selected-chip empty-chip">
              <i class="ri-time-line"></i>
              {{ $t('receipt.common.noRecentActivity') }}
            </span>
          </li>
          <li
            v-for="(h, idx) in displayedHistory"
            :key="h.key || idx"
            :data-receipt-id="h.receiptId || null"
            :data-receipt-code="h.receiptCode || null"
          >
            <i :class="h.iconClass" class="feed-icon h-icon"></i>
            <span class="feed-time h-time">{{ h.ts }}</span>
            <span class="h-status">
              <IconBadge :color="statusBadgeColor(h.label)">{{ h.label }}</IconBadge>
            </span>
            <span
              class="feed-applicant h-applicant"
              v-if="h.applicant?.userName"
              :title="h.applicant.userEmail || ''"
            >
              <span class="app-name">{{ h.applicant.userName }}</span>
              <span v-if="mobile" class="app-sub">
                <span class="app-pos" v-if="h.applicant.positionName">{{ h.applicant.positionName }}</span>
                <span class="app-email" v-if="h.applicant.userEmail">{{ h.applicant.userEmail }}</span>
              </span>
            </span>
            <span class="feed-category h-category" v-if="!mobile && h.categoryName">{{
              h.categoryName
            }}</span>
            <span class="feed-amount h-amount" v-if="!mobile && h.amount != null">{{
              formatAmountKRW(h.amount)
            }}</span>
            <span class="feed-text h-text" :title="h.text" v-if="!mobile">{{
              h.text
            }}</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 영수증 상세 모달 -->
    <ReceiptDetailViewModal
      :isVisible="detailModalVisible"
      :receiptId="detailReceiptId"
      @close="detailModalVisible = false"
    ></ReceiptDetailViewModal>

    <!-- 대리결재자 해제 확인 모달 -->
    <AlertModal
      :isVisible="removeDelegateModalVisible"
      :title="$t('receipt.delegateApproval.removeConfirm')"
      :confirmText="$t('receipt.delegateApproval.remove')"
      :cancelText="$t('common.button.cancel')"
      @confirm="confirmRemoveDelegate"
      @close="removeDelegateModalVisible = false"
    >
      <template #body>
        <div style="margin-bottom: 10px;">{{ $t('receipt.delegateApproval.removeConfirm') }}</div>
        <div style="color: #6b7280;">
          {{ $t('receipt.delegateApproval.removeGuide') }}
        </div>
      </template>
    </AlertModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ROUTES } from '@/config/menuConfig';
import { toast } from 'vue3-toastify';
import { useAuthStore } from '@/store/auth';

import DefaultButton from '@/components/common/button/DefaultButton.vue';
import DefaultFormRow from '@/components/common/DefaultFormRow.vue';
import DefaultLabel from '@/components/common/label/DefaultLabel.vue';
import UserSearchDropdown from '@/components/auth/UserSearchDropdown.vue';
import ReceiptDetailViewModal from '@/components/receipt/ReceiptDetailViewModal.vue';
import AlertModal from '@/components/common/modal/AlertModal.vue';

import ReceiptsDelegateApi from '@/api/receipt/ReceiptsDelegateApi.js';
import ReceiptsSearchApi from '@/api/receipt/ReceiptsSearchApi.js';
import ReceiptsApi from '@/api/receipt/ReceiptsApi.js';
import IconBadge from '@/components/common/badge/IconBadge.vue';
import HrmUserApi from '@/api/hrm/UsersApi';
import { RECEIPT_DELEGATE_ROLES, RECEIPT_APPROVAL_ROLES, SYSTEM_ROLES } from '@/config/roleConfig';

const props = defineProps({
  mobile: { type: Boolean, default: false },
});

const router = useRouter();
const authStore = useAuthStore();
const { t } = useI18n();

const isReceiptEnabled = import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true';

// 권한/역할
const rolesRaw = computed(() => authStore.getRoles || []);
/**
 * 역할명 배열을 계산한다
 */
const roleNames = computed(() => {
  const src = rolesRaw.value || [];
  return src
    .map((r) => (typeof r === 'string' ? r : r.roleNameDetail || r.roleName))
    .filter(Boolean);
});
/**
 * 대리결재자 지정 가능 여부
 */
const canManageDelegate = computed(() => {
  const names = roleNames.value;
  return (
    names.some((n) => RECEIPT_DELEGATE_ROLES.includes(n)) ||
    names.includes(SYSTEM_ROLES.ADMIN)
  );
});

/**
 * 내 결재선 활동 내역 조회 가능 여부
 */
const canViewHistory = computed(() => {
  const names = roleNames.value;
  return (
    names.some((n) => RECEIPT_APPROVAL_ROLES.includes(n)) ||
    names.includes(SYSTEM_ROLES.ADMIN)
  );
});

// 대리결재 상태
const selectedDelegate = ref(null);
const activeDelegate = ref(null);
const principalsWhoDesignatedMe = ref([]);
const principalUserMap = ref(new Map());

// 히스토리/요약
const isHistoryLoading = ref(false);
const historyItems = ref([]);
const thisMonthSubmitCount = ref(0);
const thisMonthTotalAmount = ref(0);
const thisMonthApprovalRate = ref(0);
const myPendingCount = ref(0);
const periodStartDate = ref('');
const periodEndDate = ref('');

// 상세 모달
const detailModalVisible = ref(false);
const detailReceiptId = ref(null);

/**
 * 대리결재자 해제 확인 모달
 */
const removeDelegateModalVisible = ref(false);

/**
 * 활동 이력 아이템을 라벨/아이콘/문구로 가공한다
 */
function mapActivityItem(raw) {
  const rawLabel = (raw?.label || '').toString().trim();
  const statusMap = {
    REQUEST: '신청',
    REJECTED: '반려',
    APPROVED: '승인',
    WAITING: '대기',
    CLOSED: '마감',
  };
  const upper = rawLabel.toUpperCase();
  const label = statusMap[upper] || rawLabel || '활동';
  const text = (raw?.text || '').toString().trim();
  const iconClass = 'ri-time-line';
  return { iconClass, label, text };
}

/**
 * 상태 라벨 → 배지 클래스 매핑
 */
function statusBadgeClass(label) {
  if (label === '승인') return 'badge-success';
  if (label === '반려') return 'badge-danger';
  if (label === '신청') return 'badge-primary';
  if (label === '대기') return 'badge-secondary';
  if (label === '마감') return 'badge-dark';
  return 'badge-secondary';
}

/**
 * 활동 상태 → IconBadge 컬러
 */
function statusBadgeColor(label) {
  if (label === '승인') return 'success';
  if (label === '반려') return 'danger';
  if (label === '신청') return 'primary';
  if (label === '대기') return 'secondary';
  if (label === '마감') return 'dark';
  return 'secondary';
}

/**
 * 활동 상태 → IconBadge 아이콘
 */
function statusBadgeIcon(label) {
  if (label === '승인') return 'ri-check-line';
  if (label === '반려') return 'ri-close-line';
  if (label === '신청') return 'ri-send-plane-line';
  if (label === '대기') return 'ri-time-line';
  if (label === '마감') return 'ri-lock-line';
  return 'ri-information-line';
}

/**
 * 의미 있는 활동 항목인지 판정한다
 */
function isMeaningfulActivity(raw) {
  const hasText = !!(raw?.text && String(raw.text).trim().length);
  const hasTime = !!raw?.timestamp;
  return hasTime && hasText;
}

/**
 * 렌더링할 히스토리(중복제거/최대 8개)
 */
const displayedHistory = computed(() => {
  const seen = new Set();
  const list = (historyItems.value || [])
    .filter(isMeaningfulActivity)
    .map((raw) => {
      const { iconClass, label, text } = mapActivityItem(raw);
      const ts = (raw?.timestamp || '').toString().slice(0, 16).replace('T', ' ');
      const key = `${ts}|${label}|${text}`;
      const receiptId = raw.receiptId || raw.id || null;
      const receiptCode = raw.receiptCode || raw.code || null;
      const applicant = {
        userId: raw.applicantUserId || null,
        userName: raw.applicantUserName || null,
        userEmail: raw.applicantUserEmail || null,
      };
      const categoryName = raw.categoryName || null;
      const amount = raw.amount != null ? Number(raw.amount) : null;
      return {
        ts,
        iconClass,
        label,
        text,
        key,
        receiptId,
        receiptCode,
        applicant,
        categoryName,
        amount,
      };
    })
    .filter((item) => {
      if (seen.has(item.key)) return false;
      seen.add(item.key);
      return true;
    })
    .filter((item) => item && item.text && item.text !== '기록');
  return list.slice(0, 5);
});

/**
 * 이번 달의 시작/끝 날짜(yyyy-MM-dd)를 반환한다
 */
function getThisMonthRange() {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), 1);
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
  const toStr = (d) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
      d.getDate(),
    ).padStart(2, '0')}`;
  return { startDate: toStr(start), endDate: toStr(end) };
}

/**
 * 숫자 금액을 3자리 콤마 문자열로 포맷한다
 */
function formatAmountKRW(amount) {
  if (!amount || isNaN(amount)) return '0원';
  return `${Number(amount).toLocaleString('ko-KR')}원`;
}

/**
 * 활동 항목 클릭 → 영수증 상세로 이동 (가능한 경우)
 */
function openReceiptDetailFromActivity(event) {
  const target = event.target;
  if (!target) return;
  const li = target.closest('li');
  if (!li) return;
  const dataId = li.getAttribute('data-receipt-id');
  if (dataId) {
    const rid = Number(dataId);
    if (props.mobile) {
      try {
        sessionStorage.setItem('receiptDetail', JSON.stringify({ id: rid }));
      } catch (e) {}
      router.push(ROUTES.RECEIPT.DETAIL_MOBILE);
    } else {
      detailReceiptId.value = rid;
      detailModalVisible.value = true;
    }
    return;
  }
  const dataCode = li.getAttribute('data-receipt-code');
  if (dataCode) {
    fetchReceiptIdByCode(dataCode).then((rid) => {
      if (!rid) return toast.info(t('common.message.noData'));
      if (props.mobile) {
        try {
          sessionStorage.setItem('receiptDetail', JSON.stringify({ id: rid }));
        } catch (e) {}
        router.push(ROUTES.RECEIPT.DETAIL_MOBILE);
      } else {
        detailReceiptId.value = rid;
        detailModalVisible.value = true;
      }
    });
    return;
  }
  const text = li.textContent || '';
  const codeMatch = text.match(/\b\d{4}-\d+\b/);
  if (codeMatch) {
    fetchReceiptIdByCode(codeMatch[0]).then((rid) => {
      if (!rid) return toast.info(t('common.message.noData'));
      if (props.mobile) {
        try {
          sessionStorage.setItem('receiptDetail', JSON.stringify({ id: rid }));
        } catch (e) {}
        router.push(ROUTES.RECEIPT.DETAIL_MOBILE);
      } else {
        detailReceiptId.value = rid;
        detailModalVisible.value = true;
      }
    });
    return;
  }
  const idMatch = text.match(/receiptId\s*[:=#-]?\s*(\d+)/i);
  if (idMatch && idMatch[1]) {
    const rid = Number(idMatch[1]);
    if (props.mobile) {
      try {
        sessionStorage.setItem('receiptDetail', JSON.stringify({ id: rid }));
      } catch (e) {}
      router.push(ROUTES.RECEIPT.DETAIL_MOBILE);
    } else {
      detailReceiptId.value = rid;
      detailModalVisible.value = true;
    }
  }
}

/**
 * 코드만 있는 경우, 기간/유저 범위 내에서 ID를 찾는다
 */
async function fetchReceiptIdByCode(code) {
  try {
    const params = {
      userId: authStore.getUserId,
      startDate: periodStartDate.value,
      endDate: periodEndDate.value,
      page: 0,
      size: 50,
    };
    const res = await ReceiptsApi.getReceiptsBySearchUserDateRange(params);
    const content = res?.data?.content || [];
    const found = content.find((r) => String(r.receiptCode) === String(code));
    return found?.receiptId ?? null;
  } catch {
    return null;
  }
}

/**
 * 활동 이력(최근 5~10개)과 월간 요약을 조회한다
 */
function loadHistoryAndSummary() {
  if (!isReceiptEnabled || !authStore?.getUserId) return;
  const { startDate, endDate } = getThisMonthRange();
  periodStartDate.value = startDate;
  periodEndDate.value = endDate;
  isHistoryLoading.value = true;
  const approverId = authStore.getUserId;
  ReceiptsSearchApi.getHistoryOverview(approverId, { startDate, endDate, limit: 8 })
    .then((res) => {
      const data = res?.data || {};
      const ms = data?.monthlySummary || {};
      thisMonthSubmitCount.value = Number(ms.count || 0);
      thisMonthTotalAmount.value = Number(ms.total || 0);
      thisMonthApprovalRate.value = Number(ms.approvalRate || 0);
      myPendingCount.value = Number(data?.myPendingCount || 0);
      // 활동 내역은 권한이 있을 때만 표시
      if (canViewHistory.value) {
        historyItems.value = Array.isArray(data?.recentActivities)
          ? data.recentActivities
          : [];
      }
    })
    .finally(() => {
      isHistoryLoading.value = false;
    });
}

/**
 * 이번 달 개인 제출 내역 화면으로 이동한다
 */
function goThisMonthPersonalHistory() {
  const { startDate, endDate } = getThisMonthRange();
  router.push({
    path: '/receipt/personal-receipt-history',
    query: { startDate, endDate },
  });
}

/**
 * 히스토리 타이틀 클릭 → 해당 월 범위로 ReceiptHistory 이동
 */
function goHistoryPageForPeriod() {
  const start = periodStartDate.value;
  const end = periodEndDate.value;
  if (!start || !end) return;
  router.push({
    path: '/receipt/receipt-history',
    query: { startDate: start, endDate: end },
  });
}

/**
 * 대리결재 지정 UI에서 Enter 키를 눌렀을 때 저장 실행
 */
function onDelegateEnter() {
  if (selectedDelegate.value && !activeDelegate.value) saveDelegate();
}

/**
 * 내 차례(요청 대기) 결재 현황 화면으로 이동한다
 */
function goMyPending() {
  router.push({ path: '/receipt/receipt-approval-request-overview' });
}

/**
 * 현재 활성 대리결재자/원결재자 목록을 조회한다
 */
async function loadActiveDelegate() {
  const principalId = authStore.getUserId;
  if (!principalId) return;
  try {
    const { data, status } = await ReceiptsDelegateApi.getActive(principalId);
    activeDelegate.value =
      status === 200 && data && (data.delegateUserId || data.id) ? data : null;
    const mine = await ReceiptsDelegateApi.getPrincipalsByDelegate(authStore.getUserId);
    principalsWhoDesignatedMe.value = Array.isArray(mine?.data) ? mine.data : [];
  } catch {
    activeDelegate.value = null;
    principalsWhoDesignatedMe.value = [];
  }
}

/**
 * 검색에서 선택한 사용자로 후보를 설정한다
 */
function onDelegateSelected(user) {
  if (!user) {
    selectedDelegate.value = null;
    return;
  }
  selectedDelegate.value = {
    delegateUserId: user.userId,
    userName: user.name,
    email: user.email || user.userEmail || user.loginEmail || null,
    department: user.department || user.departmentName || null,
    team: user.team || user.teamName || null,
    departmentName: user.departmentName || user.department || null,
    teamName: user.teamName || user.team || null,
  };
}

/**
 * 대리결재자를 저장한다
 */
async function saveDelegate() {
  const principalId = authStore.getUserId;
  if (!selectedDelegate.value || !principalId) return;
  if (
    activeDelegate.value &&
    activeDelegate.value.delegateUserId === selectedDelegate.value.delegateUserId
  ) {
    toast.info(t('receipt.delegateApproval.title'));
    return;
  }
  await ReceiptsDelegateApi.saveMapping({
    principalUserId: principalId,
    delegateUserId: selectedDelegate.value.delegateUserId,
    delegateName: selectedDelegate.value.userName,
    delegateEmail: selectedDelegate.value.email || selectedDelegate.value.userEmail,
    delegateDepartment:
      selectedDelegate.value.department || selectedDelegate.value.departmentName,
    delegateTeam: selectedDelegate.value.team || selectedDelegate.value.teamName,
  });
  await loadActiveDelegate();
  toast.success(t('receipt.delegateApproval.approveSuccess'));
}

/**
 * 대리결재자 해제 확인 모달 표시
 */
function removeDelegate() {
  if (!activeDelegate.value) return;
  removeDelegateModalVisible.value = true;
}

/**
 * 대리결재자를 실제로 해제한다
 */
async function confirmRemoveDelegate() {
  if (!activeDelegate.value) return;
  
  try {
    await ReceiptsDelegateApi.deleteMapping(activeDelegate.value.id);
    activeDelegate.value = null;
    removeDelegateModalVisible.value = false;
    toast.success(t('receipt.delegateApproval.rejectSuccess'));
  } catch (error) {
    console.error('대리결재자 해제 실패:', error);
    toast.error(t('common.message.serverError'));
  }
}

/**
 * '원결재자' 표시용 문자열을 반환한다
 */
function formatPrincipalDisplay(principalId) {
  const u = principalUserMap.value.get(principalId);
  const name = u?.name || '-';
  return `${name}(${principalId})`;
}

onMounted(() => {
  if (isReceiptEnabled) loadActiveDelegate();
  if (isReceiptEnabled) loadHistoryAndSummary();
});

watch(roleNames, () => {
  if (isReceiptEnabled && canManageDelegate.value) loadActiveDelegate();
  if (isReceiptEnabled) loadHistoryAndSummary();
});

watch(
  principalsWhoDesignatedMe,
  async (list) => {
    try {
      const ids = (list || [])
        .map((p) => p?.principalUserId)
        .filter(
          (v) => typeof v === 'number' || (typeof v === 'string' && v.trim() !== ''),
        );
      if (!ids.length) {
        principalUserMap.value = new Map();
        return;
      }
      const uniq = Array.from(new Set(ids.map((v) => Number(v))));
      const res = await HrmUserApi.getUsersByIds(uniq);
      const map = new Map();
      (Array.isArray(res) ? res : []).forEach((u) => {
        if (u && u.userId != null)
          map.set(Number(u.userId), {
            name: u.name,
            email: u.email,
            positionName: u.position?.positionName || '',
          });
      });
      principalUserMap.value = map;
    } catch {
      principalUserMap.value = new Map();
    }
  },
  { immediate: true },
);
</script>

<style scoped>
/* 데스크톱: 시간/사유 폰트/말줄임/폭을 UserInformation.vue와 동일하게 맞춤 */
.history-feed .feed-time {
  color: #6b7a90;
  min-width: 110px;
  font-size: 0.82rem;
}
.history-feed .feed-text {
  display: inline-block;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 0.82rem;
}
.history-feed li {
  color: #111827;
}
.history-feed .feed-icon {
  font-size: 1.1rem;
  color: #0b6bcb;
}
.hr-line-receipt {
  border: 0;
  border-top: 1px solid #000000;
  margin: 40px 0;
  position: relative;
  border-color: #666666 !important;
}
.hr-line-receipt::after {
  content: '영수증';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: var(--theme-bg-main);
  padding: 0 8px;
  color: var(--theme-text-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.delegate-two-col {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 20px;
  align-items: start;
  min-height: 185px;
}
.delegate-two-col .history-inline-text {
  transform: translateY(0%);
}
.delegate-candidate {
  color: #c62828;
  font-weight: 900;
  font-size: 0.7rem;
  margin-left: 4px;
}

.history-inline-text {
  min-height: 282px;
  font-size: 0.85rem;
  color: var(--theme-text-secondary);
  line-height: 1.9;
  border: 1px solid var(--theme-border-blue);
  border-radius: 12px;
  background: var(--theme-card-bg);
  padding: 18px 20px;
  box-shadow: 0 2px 8px rgba(0, 140, 255, 0.06);
}
.history-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 800;
  color: #0b6bcb;
  margin-right: 8px;
  font-size: 0.95rem;
}
.history-title i {
  font-size: 1.1rem;
}
.history-desc {
  display: inline;
}
.history-body {
  margin-left: 27px;
  margin-top: 10px;
}
.history-summary {
  margin: 8px 0 4px 0;
  font-size: 0.78rem;
}
.history-summary strong {
  color: #0b162f;
  font-size: 0.82rem;
}
.history-summary .pending {
  color: #c05621;
  font-weight: 700;
}
/* 데스크톱용 승인율 2줄 박스 */
.rate-box-desktop {
  /* 모바일 박스와 유사한 카드형 컨테이너 */
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 6px;
  padding: 10px 12px;
  border: 1px solid #e6f2ff;
  border-radius: 10px;
  background: #f7fbff;
  margin-top: 8px;
  margin-bottom: 8px;
}
.rate-box-desktop .rate-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.78rem;
  color: #6b7a90;
}
.rate-box-desktop .rate-label .hint {
  font-size: 0.72rem;
  color: #9aa5b1;
}
.rate-box-desktop .rate-label .info-tip {
  color: #0b6bcb;
  font-size: 1rem;
  cursor: help;
}
.rate-box-desktop .rate-bar {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.rate-box-desktop .pct-rail {
  position: relative;
  width: 80%;
  height: 8px;
  background: #eef2f7;
  border-radius: 999px;
  overflow: hidden;
}
.rate-box-desktop .pct-rail .pct-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  background: linear-gradient(90deg, #26a3ff 0%, #0077e6 100%);
}
.rate-box-desktop .rate-value {
  font-size: 0.8rem;
  font-weight: 800;
  color: #0b162f;
  min-width: 40px;
  text-align: right;
}
.history-period {
  margin-top: 6px;
}
.history-period .period-text {
  color: #6b7a90;
  font-size: 0.78rem;
}

.history-feed {
  list-style: none;
  padding: 0;
  margin: 8px 0 12px 0;
  width: 100%;
}
.loading-row {
  display: grid !important;
  grid-template-columns:
    var(--col-icon) minmax(var(--min-time), 0.14fr) minmax(var(--min-status), 0.12fr)
    minmax(var(--min-applicant), 0.18fr) minmax(var(--min-category), 0.18fr) minmax(
      var(--min-amount),
      0.14fr
    )
    1fr;
  align-items: center;
  column-gap: 12px;
  padding: 6px 0;
  color: #6b7a90;
  font-size: 0.82rem;
}
.empty-row {
  display: flex !important;
  justify-content: left;
  align-items: left;
  padding: 10px 0;
}
.empty-chip {
  background: #f7fbff;
  border: 1px solid #e6f2ff;
  color: #6b7a90;
  margin: 0 !important;
  font-size: 0.82rem;
}
.loading-row i.spin {
  animation: spin 1s linear infinite;
  color: #6b7a90;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
/* 모바일: 신청자 표시 개선 (직책/이름/이메일 스택) */
.app-name {
  font-size: 0.75rem;
}
.history-feed .h-applicant .app-name {
  display: block;
  font-weight: 800;
}
.history-feed .h-applicant .app-sub {
  display: block;
  color: #6b7a90;
  font-size: 0.7rem;
}
.history-feed .h-applicant .app-sub .app-pos::after {
  content: ' · ';
  opacity: 0.7;
}
.history-feed .badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 0.60rem;
  line-height: 1;
}
.history-feed .feed-icon {
  color: #6b7a90;
  margin-right: 6px;
}
.history-feed .feed-time {
  color: #6b7a90;
  min-width: 80px;
}
.history-feed .feed-sep {
  color: #a0a7b4;
  margin: 0 6px;
}
.history-caption {
  color: #6b7a90;
  font-size: 0.7rem;
  margin: 6px 0 4px 0;
}
.history-box {
  width: 100%;
  min-height: 265px;
  padding: 18px 20px;
  border-radius: 12px;
  background: var(--theme-card-bg);
  margin-bottom: 10px;
  margin-top: 20px;
  line-height: 1.9;
  border: 1px solid var(--theme-border-blue);
  box-shadow: 0 2px 8px rgba(0, 140, 255, 0.06);
}
.history-box-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
  font-weight: 800;
  color: #0b6bcb;
  margin-bottom: 16px;
  cursor: pointer;
  text-decoration: underline;
}

.history-box-title i {
  font-size: 1.1rem;
}
.history-box {
  --col-icon: 20px;
  --min-time: 90px;
  --min-status: 50px;
  --min-applicant: 70px;
  --min-category: 140px;
  --min-amount: 90px;
}
.history-feed-header {
  width: 100%;
  display: grid;
  grid-template-columns:
    var(--col-icon) minmax(var(--min-time), 0.14fr) minmax(var(--min-status), 0.12fr)
    minmax(var(--min-applicant), 0.18fr) minmax(var(--min-category), 0.18fr) minmax(
      var(--min-amount),
      0.14fr
    )
    1fr;
  column-gap: 12px;
  padding: 8px 10px 10px 10px;
  margin-bottom: 8px;
  color: #6b7a90;
  font-size: 0.8rem;
  font-weight: 700;
  position: relative;
}

.history-feed-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 10px;
  right: 10px;
  height: 2px;
  background: #e9eef3;
}
.history-feed li {
  display: grid;
  grid-template-columns:
    var(--col-icon) minmax(var(--min-time), 0.14fr) minmax(var(--min-status), 0.12fr)
    minmax(var(--min-applicant), 0.18fr) minmax(var(--min-category), 0.18fr) minmax(
      var(--min-amount),
      0.14fr
    )
    1fr;
  align-items: center;
  column-gap: 12px;
  padding: 8px 10px;
  color: var(--theme-text-primary);
  transition: background-color 0.2s;
  font-size: 0.82rem;
  position: relative;
}

.history-feed li::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 10px;
  right: 10px;
  height: 1px;
  background: #f3f4f6;
}
.history-feed li:hover {
  cursor: pointer;
  background-color: var(--theme-bg-hover);
  text-decoration: none;
}

.history-feed {
  list-style: none;
  margin: 0;
  padding: 0;
  width: 100%;
}

.history-feed .feed-icon {
  margin: 0;
}
.history-feed .feed-time {
  min-width: auto;
}
.history-feed .feed-sep {
  display: none;
}
.history-feed .feed-applicant,
.history-feed .feed-category,
.history-feed .feed-amount {
  color: #2b2f36;
  font-size: 0.82rem;
}
.link-button {
  all: unset;
  color: #0b6bcb;
  cursor: pointer;
  margin-right: 14px;
  text-decoration: underline;
  font-size: 0.8rem;
  font-weight: 600;
}
.link-button:hover {
  text-decoration: underline;
  color: #084a94;
}

.history-actions {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.delegate-card {
  padding: 16px 18px;
  max-width: 100%;
  min-height: 282px;
}
.delegate-controls {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 8px;
}
.delegate-assign-card {
  border: 1px dashed var(--theme-border-blue);
  border-radius: 12px;
  background: var(--theme-bg-secondary);
  padding: 18px 20px;
  min-height: 265px;
  line-height: 1.9;
}
.delegate-assign-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
  color: #0b6bcb;
  margin-bottom: 12px;
  font-size: 0.95rem;
}
.delegate-assign-card__header i {
  font-size: 1rem;
}
.delegate-assign-card__body {
  padding-top: 8px;
}
.selected-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--theme-border);
  background: var(--theme-bg-secondary);
  border-radius: 999px;
  padding: 8px 12px;
  margin-top: 8px;
  font-size: 0.78rem;
}
.selected-chip .email {
  color: var(--theme-text-secondary);
  font-size: 0.75rem;
}

/* 대리결재 안내 문구 */
.delegate-guide {
  margin-top: 10px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
  border-radius: 10px;
}

.guide-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.guide-content > i {
  font-size: 1.2rem;
  color: #0ea5e9;
  flex-shrink: 0;
  margin-top: 2px;
}

.guide-text {
  flex: 1;
}

.guide-text h4 {
  margin: 0 0 6px 0;
  font-size: 0.85rem;
  font-weight: 600;
  color: #0c4a6e !important;
}

.guide-text p {
  margin: 0;
  font-size: 0.75rem;
  color: #0e7490;
  line-height: 1.5;
}

/* 지정된 대리결재자 안내 */
.delegate-active-guide {
  grid-column: 1 / -1; /* 전체 그리드 컬럼 차지 */
  margin-top: 12px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border: 1px solid #bbf7d0;
  border-radius: 8px;
}

.active-guide-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.active-guide-content > i {
  font-size: 1rem;
  color: #16a34a;
  flex-shrink: 0;
}

.active-guide-text {
  flex: 1;
}

.active-guide-message {
  font-size: 0.8rem;
  color: #166534;
  line-height: 1.5;
}

.active-guide-message strong {
  color: #15803d;
  font-weight: 600;
}
.designated-me-box {
  margin-top: 12px;
  border: 1px dashed var(--theme-border-blue);
  border-radius: 10px;
  padding: 10px 12px;
  background: var(--theme-bg-secondary);
  max-width: 100%;
}
.designated-me-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 800;
  color: #0b6bcb;
  margin-bottom: 10px;
  font-size: 0.82rem;
}
.designated-me-title i {
  font-size: 1rem;
  color: #0b6bcb;
}
.designated-me-list {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 0.75rem;
  color: #2b2f36;
  margin-left: 20px;
}
.designated-me-list li {
  margin: 0;
  padding: 2px 0;
  line-height: 1.5;
  display: grid;
  grid-template-columns: minmax(80px, auto) 12px 1fr;
  align-items: center;
  column-gap: 6px;
}
.designated-me-list .sep {
  color: #a0a7b4;
  margin: 0 4px;
  text-align: center;
}
.designated-me-list .name,
.designated-me-list .org {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.designated-me-list .name {
  max-width: 80px;
}
.designated-me-list .org {
  color: #6b7280;
}
.chip-close {
  border: none;
  background: transparent;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
  color: #6b7a90;
}
.delegate-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
  color: #0b6bcb;
  margin-bottom: 20px;
  margin-top: 10px;
  font-size: 0.95rem;
}
.delegate-card__header i {
  font-size: 1.1rem;
}
.delegate-card__body {
  display: grid;
  grid-template-columns: 3.2fr 6.8fr;
  gap: 10px 14px;
  font-size: 0.78rem;
}
.info-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--theme-bg-secondary);
  border: 1px solid var(--theme-border);
  border-radius: 8px;
  padding: 8px 10px;
}
.info-chip label {
  font-size: 0.75rem !important;
}
.info-chip i {
  color: #0b6bcb;
  font-size: 1.05rem;
}
.delegate-card__body i {
  font-size: 1.05rem;
}
.badge-red {
  margin-left: auto;
}

/* 최근 활동이 없습니다 호버 효과 제거 - 데스크탑/모바일 공통 */
.empty-row {
  pointer-events: none !important;
  cursor: default !important;
}
.empty-row .selected-chip {
  pointer-events: none !important;
  cursor: default !important;
}

@media (max-width: 650px) {
  .mobile-feed-header {
    display: none !important;
  }
  .history-feed-header {
    display: none !important;
  }
  .history-feed li {
    display: grid;
    grid-template-columns: 70px 40px 1fr; /* time | status | applicant */
    column-gap: 8px;
    align-items: center;
    position: relative;
    background: var(--theme-card-bg);
    border: 1px solid var(--theme-border);
    border-radius: 12px;
    padding: 10px 12px;
    margin-bottom: 10px;
    box-shadow: 0 2px 8px rgba(0, 140, 255, 0.08);
  }
  .history-feed li::after {
    content: '›';
    position: absolute;
    right: 10px;
    left: auto;
    top: 50%;
    transform: translateY(-50%);
    color: #94a3b8;
    font-size: 18px;
    background: none;
    height: auto;
    width: auto;
  }
  .history-feed .h-time {
    color: #6b7a90;
    font-size: 0.68rem;
  }
  .history-box {
    padding: 12px 14px;
    margin-top: 12px;
    border: 1px solid #e6f2ff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 140, 255, 0.06);
  }
  /* 모바일 요약 한 줄 + 퍼센트바 */
  .summary-row {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    align-items: center;
    margin: 6px 0 10px 0;
  }
  .chip {
    background: #eaf4ff;
    border: 1px solid #cfe6ff;
    color: #0b6bcb;
    border-radius: 999px;
    padding: 4px 8px;
    font-size: 0.65rem;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
  .chip .label {
    color: #0b6bcb;
  }
  .chip .value {
    font-weight: 700;
    color: #0b162f;
  }
  .chip-amount {
    color: #0b6bcb;
    font-weight: 800;
  }
  .chip-period {
    color: #0b6bcb;
  }
  .chip-pending {
    color: #c05621;
    font-weight: 700;
  }
  .rate-box {
    display: flex;
    flex-direction: column;
    width: 100%;
    gap: 6px;
    padding: 8px 10px;
    border: 1px solid var(--theme-border);
    border-radius: 12px;
    background: var(--theme-card-bg);
    box-shadow: 0 2px 8px rgba(0, 140, 255, 0.06);
  }
  .rate-label {
    display: inline-flex;
    gap: 4px;
    align-items: center;
    font-size: 0.68rem;
    color: #6b7280;
  }
  .rate-label .hint {
    font-size: 0.62rem;
    color: #9aa5b1;
  }
  .rate-label .info-tip {
    color: #9aa5b1;
    font-size: 0.9rem;
    cursor: help;
  }
  .rate-bar {
    display: inline-flex;
    align-items: center;
    gap: 8px;
  }
  .rate-value {
    font-size: 0.7rem;
    font-weight: 700;
    color: #0b162f;
    min-width: 36px;
    text-align: center;
  }
  .pct-mini {
    width: 90%;
    height: 6px;
    background: #eef2f7;
    border-radius: 999px;
    overflow: hidden;
    position: relative;
  }
  .pct-mini .pct-fill {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    background: linear-gradient(90deg, #26a3ff 0%, #0077e6 100%);
  }
  /* 모바일에서는 아이콘/카테고리/금액/사유 컬럼 숨김 */
  .history-feed .h-icon,
  .history-feed .h-category,
  .history-feed .h-amount,
  .history-feed .h-text {
    display: none !important;
  }
  .mobile-feed-header {
    display: grid;
    grid-template-columns: 80px 40px 1fr;
    column-gap: 8px;
    padding: 4px 10px 6px 12px;
    margin-bottom: 6px;
    border-bottom: 1px solid #e9eef3;
    color: #6b7a90;
    font-size: 0.68rem;
  }
  hr {
    margin: 15px 0px 15px 0px !important;
  }
  .content {
    width: 100%;
    padding: 80px 20px 100px; /* 모바일 유지 */
  }
  .delegate-two-col {
    display: block;
  }
  .history-inline-text {
    display: none;
    padding: 14px 12px !important;
    min-height: 0px !important;
  }
  .history-inline-mobile {
    display: block;
    margin-top: 12px;
    padding-left: 2px;
  }
  /* 폼 그룹 여백 살짝 줄이기 */
  .form-group,
  .form-group-name-phone {
    margin-bottom: 8px !important;
  }
  .form-group-password {
    margin-top: 22px;
  }
  /* 기본 글자 사이즈 살짝 축소 */
  label,
  .date-input,
  .edit-btn {
    font-size: 0.75rem !important;
  }
  .mobile-icon {
    font-size: 1rem;
    margin-right: 5px;
    /* color: #999; */
  }
  /* ─── 모바일 카드 스타일 개선 ─── */
  .card {
    width: 100%;
    border: 1px solid #888888;
    padding: 10px;
    background-color: #ffffff; /* 완전 화이트 */
    border-radius: 12px; /* 둥근 모서리 */
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); /* 부드러운 그림자 */
    margin: 12px 0 10px 0 !important; /* 카드 간격 */
    overflow: hidden; /* 둥근 모서리에 맞춰 내부 잘림 */
  }
  .card-header {
    /* background: linear-gradient(135deg, #c4e0ff 0%, #96c8ff 100%); */
    /* background: linear-gradient(135deg, #c2daff 0%, #5aaeff 100%);  */
    background: linear-gradient(135deg, rgba(0, 88, 221, 0.719) 0%, #004497 100%);
    padding: 10px 14px !important;
    margin-bottom: 0px;
    color: #002a5c;
  }
  .card-title {
    font-size: 0.85rem !important;
    font-weight: 900;
    color: #ffffff;
    margin-bottom: 0px;
  }
  .card-body {
    padding: 12px 14px 5px 14px;
  }
  /* 카드 본문 글씨 간격·크기 최적화 */
  .card-body .card-text {
    font-size: 0.65rem !important;
    line-height: 1.4;
    color: #000000;
  }
  /* ─── input-group 세로 정렬 & 버튼 풀-와이드 ─── */
  .input-group {
    display: flex;
    align-items: center; /* 수직 가운데 */
  }
  .input-group .input-group-append {
    margin: 0; /* 세로 여백 제거 */
  }
  .input-group-append > * {
    width: auto; /* 버튼을 내용 크기로 */
    flex-shrink: 0; /* 버튼 줄바꿈 방지 */
  }
  .edit-btn {
    border: none;
    background: transparent;
    font-size: 0.8rem;
    margin-left: 3px;
    cursor: pointer;
  }
  .date-input {
    font-size: 0.65rem !important;
    padding: 0px !important;
    width: 93px !important;
  }
  /* 모바일 이미지 크기 조정 */
  .profile-image-wrapper {
    width: 120px !important;
    height: 120px !important;
  }
  .delegate-assign-card {
    border: 1px dashed var(--theme-border-blue);
    border-radius: 12px;
    background: var(--theme-bg-secondary);
    padding: 14px 12px 14px 12px;
    min-height: 130px;
  }
  .delegate-assign-card__header {
    font-size: 0.85rem !important;
  }
  .delegate-assign-card__header i {
    font-size: 0.9rem !important;
  }
  .delegate-assign-card__body {
    padding-top: 0px;
  }
  .delegate-card {
    max-width: 100%;
  }
  .delegate-section {
    max-width: 100%;
  }
  .delegate-card__body {
    grid-template-columns: 1fr;
  }
  .hr-line-receipt {
    margin: 30px 0px 30px 0px !important;
  }
  .selected-chip {
    font-size: 0.65rem;
  }
  .selected-chip .email {
    font-size: 0.63rem;
  }
  .summary-row {
    margin-top: 6px !important;
  }
  .history-box {
    border: 1px solid var(--theme-border-blue);
    border-radius: 12px;
    background: var(--theme-card-bg);
    padding: 14px 12px;
    margin-bottom: 12px;
    margin-top: 20px;
    line-height: 1.9;
    min-height: 0px;
    box-shadow: 0 2px 8px rgba(0, 140, 255, 0.06);
  }
  .history-box-title {
    font-size: 0.85rem;
    font-weight: 800;
    color: #0b6bcb;
    margin-bottom: 15px;
  }
  .history-box-title i {
    font-size: 0.9rem;
  }
  .history-inline-mobile__header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 800;
    color: #0b6bcb;
    margin-bottom: 12px;
    font-size: 0.85rem;
  }
  .history-inline-mobile__header i {
    font-size: 0.9rem;
  }
  .history-feed li {
    font-size: 0.68rem;
  }
  .history-summary {
    font-size: 0.7rem;
  }
  .history-summary .pending {
    font-size: 0.7rem;
  }

  /* 모바일 대리결재 안내 문구 */
  .mobile-delegate-guide {
    margin-top: 12px;
    padding: 10px 12px;
  }

  .mobile-delegate-guide .guide-text h4 {
    font-size: 0.75rem;
  }

  .mobile-delegate-guide .guide-text p {
    font-size: 0.7rem;
  }

  .mobile-active-guide {
    grid-column: 1 / -1; /* 모바일에서도 전체 너비 차지 */
    margin-top: 10px;
    padding: 8px 10px;
  }

  .mobile-active-guide .active-guide-message {
    font-size: 0.72rem;
  }
  .designated-me-box {
    min-width: 100%;
  }
  .history-inline-mobile {
    margin-top: 20px;
  }

  /* 모바일에서 배지 색상을 블루 톤으로 보정 */
  .history-feed .badge-success {
    background-color: #eaf4ff;
    color: #0b6bcb;
  }
  .history-feed .badge-primary {
    background-color: #e6f4ff;
    color: #0b6bcb;
  }
  .history-feed .badge-secondary {
    background-color: #f1f5f9;
    color: #475569;
  }
  .history-feed .badge-danger {
    background-color: #ffefef;
    color: #c62828;
  }
}

/* ═══════════════════════════════════════════════════════════════
 * 다크모드 스타일 통합
 * ═══════════════════════════════════════════════════════════════ */

/* 히스토리 타이틀 */
body[data-theme="dark"] .history-title {
  color: #d3d3d3 !important;
}

/* 히스토리 박스 */
body[data-theme="dark"] .history-box {
  border: 1px solid var(--theme-border);
}

body[data-theme="dark"] .history-box-title {
  color: #d3d3d3 !important;
}

/* 대리결재자 지정 타이틀 */
body[data-theme="dark"] .delegate-assign-card__header {
  color: #d3d3d3 !important;
}

/* 현재 대리결재자 타이틀 */
body[data-theme="dark"] .delegate-card__header {
  color: #d3d3d3 !important;
}

/* 히스토리 인라인 타이틀 (모바일) */
body[data-theme="dark"] .history-inline-mobile__header {
  color: #d3d3d3 !important;
}

/* 다크모드: 대리결재 안내(delegate-guide) */
body[data-theme="dark"] .delegate-guide {
  background: linear-gradient(135deg, #2f3640 0%, #252a31 100%) !important;
  border: 1px solid var(--theme-border) !important;
}
body[data-theme="dark"] .delegate-guide i {
  color: #60a5fa !important;
}
body[data-theme="dark"] .delegate-guide,
body[data-theme="dark"] .delegate-guide * {
  color: #d1d5db !important;
}
</style>
