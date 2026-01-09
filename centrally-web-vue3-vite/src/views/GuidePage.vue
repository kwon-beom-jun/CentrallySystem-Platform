<template>
  <div class="guide-page-container" :class="{ 'logged-in': authStore.isLoggedIn }">
    <!-- 프론트 헤더 (비로그인 상태일 때만) -->
    <FrontHeader v-if="!authStore.isLoggedIn" />
    <div v-if="!authStore.isLoggedIn" class="padding-h-30" />
    
    <!-- 메인 콘텐츠 영역 -->
    <div class="content content-wrapper">
      <PageTitle
        :title="$t('guide.page.title')"
        :subtitle="$t('guide.page.subtitle')"
        icon="ri-guide-line"
        :show-back-button="!authStore.isLoggedIn"
      />

      <!-- 빠른 이동 내비게이션 (해시 대신 스크롤 함수) -->
      <nav class="guide-nav">
        <button v-for="s in sections" :key="s.id" @click="scrollTo(s.id)">
          {{ $t(`guide.sections.${s.id}`) }}
        </button>
      </nav>
      <div class="guide-nav-actions">
        <button @click="toggleAllDetails" class="toggle-details-btn">
          {{ allDetailsExpanded ? $t('guide.page.collapseAll') : $t('guide.page.expandAll') }}
        </button>
      </div>
      <hr class="nav-divider" />

      <!-- 세부 가이드 영역 (새 순서) -->

      <!-- 회원가입 -->
      <section id="signup" class="guide-section">
        <h3 @click="toggleSection('signup')" class="section-title-toggle">
          {{ $t('guide.sections.signup') }}
          <i :class="sectionsExpanded.signup ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        
        <div v-show="sectionsExpanded.signup" class="section-content">
        <!-- ───────────── 회원가입 프로세스 (접이식) ───────────── -->
        <details ref="signupDetails" class="signup-process">
          <summary>{{ signupContent.process.summary }}</summary>
          <div class="process-content">
            <h4 class="process-content-h4">{{ signupContent.process.title }}</h4>
            <ol>
              <li v-for="(item, idx) in signupContent.process.steps" :key="`signup-process-step-${idx}`" v-html="item"></li>
            </ol>

                        <!-- 회원가입 흐름도 (CSS로 구현) -->
            <div class="signup-flow">
              <template v-for="(step, idx) in signupContent.process.flow" :key="`signup-flow-${idx}`">
                <div class="flow-step" :class="step.isAdmin ? 'admin' : 'step'">
                  <div class="step-number">{{ step.icon }}</div>
                  <div class="step-text" v-html="step.text"></div>
                </div>
                <div v-if="idx < signupContent.process.flow.length - 1" class="flow-arrow">→</div>
              </template>
            </div>

            <hr class="process-content-hr" />

            <h4 class="process-content-h4">{{ signupContent.process.table.title }}</h4>
            <table class="input-table">
              <thead>
                <tr>
                  <th v-for="(head, idx) in signupContent.process.table.head" :key="`signup-table-head-${idx}`">
                    {{ head }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, idx) in signupContent.process.table.rows" :key="`signup-table-row-${idx}`">
                  <td v-html="row.label"></td>
                  <td class="table-td-center" v-html="row.required"></td>
                  <td v-html="row.example"></td>
                </tr>
              </tbody>
            </table>
            <div class="process-footer">
              <button class="process-close-btn" @click="closeSignupProcess">
                닫기
              </button>
            </div>
          </div>
        </details>

        <div class="section-grid">
          <div>
            <ol>
              <li v-for="(item, idx) in signupContent.checklist" :key="`signup-checklist-${idx}`" v-html="item"></li>
            </ol>
          </div>
          <template v-if="!isMobile">
            <img
              class="section-img zoomable"
              loading="lazy"
              decoding="async"
              src="/guide/회원%20가입.png"
              :alt="signupContent.images.desktopAlt || ''"
              @click="openZoom($event.target.src)"
            />
          </template>
          <template v-else>
            <img
              class="section-img zoomable"
              loading="lazy"
              decoding="async"
              src="/guide/회원%20가입%20모바일.png"
              :alt="signupContent.images.mobileAlt || ''"
              @click="openZoom($event.target.src)"
            />
          </template>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <!-- 비밀번호 찾기 -->
      <section id="password" class="guide-section">
        <h3 @click="toggleSection('password')" class="section-title-toggle">
          {{ $t('guide.sections.password') }}
          <i :class="sectionsExpanded.password ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        <div v-show="sectionsExpanded.password" class="section-content">
        <div class="section-grid">
          <div>
            <ol>
              <li
                v-for="(item, idx) in passwordContent.steps"
                :key="`password-step-${idx}`"
                v-html="item"
              ></li>
            </ol>
          </div>
          <template v-if="!isMobile">
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in passwordContent.images.desktopAlts"
                :key="`password-desktop-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                :src="idx === 0 ? '/guide/비밀번호%20찾기.png' : '/guide/비밀번호%20찾기2.png'"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
          <template v-else>
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in passwordContent.images.mobileAlts"
                :key="`password-mobile-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                :src="idx === 0 ? '/guide/비밀번호%20찾기%20모바일.png' : '/guide/비밀번호%20찾기%20모바일2.png'"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <!-- 소셜 로그인 -->
      <section id="social" class="guide-section">
        <h3 @click="toggleSection('social')" class="section-title-toggle">
          {{ $t('guide.sections.social') }}
          <i :class="sectionsExpanded.social ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        <div v-show="sectionsExpanded.social" class="section-content">
        <div class="section-grid">
          <div>
            <ol>
              <li
                v-for="(item, idx) in socialContent.steps"
                :key="`social-step-${idx}`"
                v-html="item"
              ></li>
            </ol>
          </div>
          <template v-if="!isMobile">
            <img
              class="section-img zoomable"
              loading="lazy"
              decoding="async"
              src="/guide/소셜%20로그인.png"
              :alt="socialContent.images.desktopAlt || ''"
              @click="openZoom($event.target.src)"
            />
          </template>
          <template v-else>
            <img
              class="section-img zoomable"
              loading="lazy"
              decoding="async"
              src="/guide/소셜%20로그인%20모바일.png"
              :alt="socialContent.images.mobileAlt || ''"
              @click="openZoom($event.target.src)"
            />
          </template>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <!-- 메인 페이지 -->
      <section v-if="shouldShowSection('main')" id="main" class="guide-section">
        <h3 @click="toggleSection('main')" class="section-title-toggle">
          {{ $t('guide.sections.main') }}
          <i :class="sectionsExpanded.main ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        <div v-show="sectionsExpanded.main" class="section-content">
        <div class="section-grid">
          <div>
            <h4>{{ mainContent.permissionsTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in mainContent.permissions" :key="`main-permission-${idx}`" v-html="item"></li>
            </ul>
            <br/>
            <h4>{{ mainContent.featuresTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in mainContent.features" :key="`main-feature-${idx}`" v-html="item"></li>
            </ul>
          </div>
          <template v-if="!isMobile">
            <img
              class="section-img zoomable"
              loading="lazy"
              decoding="async"
              src="/guide/메인 페이지.png"
              :alt="mainContent.images.desktopAlt || ''"
              @click="openZoom($event.target.src)"
            />
          </template>
          <template v-else>
            <img
              class="section-img zoomable"
              loading="lazy"
              decoding="async"
              src="/guide/메인 페이지 모바일.png"
              :alt="mainContent.images.mobileAlt || ''"
              @click="openZoom($event.target.src)"
            />
          </template>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <!-- 공지사항 -->
      <section v-if="shouldShowSection('notice')" id="notice" class="guide-section">
        <h3 @click="toggleSection('notice')" class="section-title-toggle">
          {{ $t('guide.sections.notice') }}
          <i :class="sectionsExpanded.notice ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        <div v-show="sectionsExpanded.notice" class="section-content">
        <div class="section-grid">
          <div>
            <h4>{{ noticeContent.permissionsTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in noticeContent.permissions" :key="`notice-permission-${idx}`" v-html="item"></li>
            </ul>
            <br/>
            <h4>{{ noticeContent.featuresTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in noticeContent.features" :key="`notice-feature-${idx}`" v-html="item"></li>
            </ul>
          </div>
          <template v-if="!isMobile">
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in (noticeContent.images.desktopAlts || [])"
                :key="`notice-desktop-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                :src="idx === 0 ? '/guide/공지%20사항.png' : '/guide/공지%20사항2.png'"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
          <template v-else>
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in (noticeContent.images.mobileAlts || [])"
                :key="`notice-mobile-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                :src="idx === 0 ? '/guide/공지%20사항%20모바일.png' : '/guide/공지%20사항%20모바일2.png'"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <!-- 영수증 -->
      <section v-if="shouldShowSection('receipt')" id="receipt" class="guide-section">
        <h3 @click="toggleSection('receipt')" class="section-title-toggle">
          {{ $t('guide.sections.receipt') }}
          <i :class="sectionsExpanded.receipt ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        <div v-show="sectionsExpanded.receipt" class="section-content">
        <!-- ───────────── 영수증 처리 프로세스 (접이식) ───────────── -->
        <details ref="receiptDetails" class="receipt-process">
          <summary>{{ receiptContent.process.summary }}</summary>
          <div class="process-content">
            <h4 class="process-content-h4">{{ receiptContent.process.title }}</h4>
            
            <!-- 영수증 처리 흐름도 (CSS로 구현) -->
            <div class="receipt-flow">
              <template v-for="(step, idx) in receiptContent.process.flow" :key="`receipt-flow-${idx}`">
                <div class="flow-step step">
                  <div class="step-number">{{ step.icon }}</div>
                  <div class="step-text">{{ step.text }}</div>
                </div>
                <div
                  v-if="idx < receiptContent.process.flow.length - 1 || receiptContent.process.exceptionStep"
                  class="flow-arrow"
                >
                  →
                </div>
              </template>
              <div v-if="receiptContent.process.exceptionStep" class="flow-step exception">
                <div class="step-number">❗</div>
                <div class="step-text">{{ receiptContent.process.exceptionStep }}</div>
              </div>
              
              <!-- 반려 화살표들 -->
              <div v-if="receiptContent.process.rejectFlow.length" class="reject-flow">
                <div
                  v-for="(item, idx) in receiptContent.process.rejectFlow"
                  :key="`receipt-reject-${idx}`"
                  class="reject-arrow"
                  v-html="item"
                ></div>
              </div>
            </div>

            <hr class="process-content-hr" />

            <div
              v-for="(sectionData, idx) in receiptContent.process.sections"
              :key="`receipt-section-${idx}`"
            >
              <h4 class="process-content-h4">{{ sectionData.title }}</h4>
              <ul>
                <li
                  v-for="(item, itemIdx) in (sectionData.items || [])"
                  :key="`receipt-section-${idx}-item-${itemIdx}`"
                  v-html="item"
                ></li>
              </ul>
              <template v-if="sectionData.table">
                <h5>{{ sectionData.table.title || '' }}</h5>
                <table class="input-table">
                  <thead>
                    <tr>
                      <th
                        v-for="(head, headIdx) in sectionData.table.head"
                        :key="`receipt-section-${idx}-table-head-${headIdx}`"
                      >
                        {{ head }}
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="(row, rowIdx) in (sectionData.table.rows || [])"
                      :key="`receipt-section-${idx}-table-row-${rowIdx}`"
                    >
                      <td v-html="row.label"></td>
                      <td v-html="row.description"></td>
                    </tr>
                  </tbody>
                </table>
              </template>
              <hr
                v-if="idx < receiptContent.process.sections.length - 1"
                class="process-content-hr"
              />
            </div>
            
            <h4 v-if="receiptContent.process.exception.title" class="process-content-h4">
              {{ receiptContent.process.exception.title }}
            </h4>
            <div v-if="receiptContent.process.exception.cases.length" class="exception-box">
              <div
                v-for="(exceptionCase, idx) in receiptContent.process.exception.cases"
                :key="`receipt-exception-case-${idx}`"
              >
                <h5 v-html="exceptionCase.title"></h5>
                <ul>
                  <li
                    v-for="(item, itemIdx) in (exceptionCase.items || [])"
                    :key="`receipt-exception-case-${idx}-item-${itemIdx}`"
                    v-html="item"
                  ></li>
                </ul>
              </div>
            </div>
            
            <hr class="process-content-hr" />
            
            <h4 class="process-content-h4">{{ receiptContent.process.reference.title }}</h4>
            <div class="info-box">
              <h5>{{ receiptContent.process.reference.boxTitle }}</h5>
              <ul>
                <li
                  v-for="(item, idx) in receiptContent.process.reference.items"
                  :key="`receipt-reference-${idx}`"
                  v-html="item"
                ></li>
              </ul>
            </div>
            
            <hr class="process-content-hr" />
            
            <h4 class="process-content-h4">{{ receiptContent.process.summaryTable.title }}</h4>
            <table class="input-table">
              <thead>
                <tr>
                  <th
                    v-for="(head, idx) in receiptContent.process.summaryTable.head"
                    :key="`receipt-summary-head-${idx}`"
                  >
                    {{ head }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(row, idx) in receiptContent.process.summaryTable.rows"
                  :key="`receipt-summary-row-${idx}`"
                >
                  <td v-html="row.label"></td>
                  <td v-html="row.description"></td>
                </tr>
              </tbody>
            </table>
            <div class="process-footer">
              <button class="process-close-btn" @click="closeReceiptProcess">
                닫기
              </button>
            </div>
          </div>
        </details>

        <div class="section-grid">
          <div>
            <h4 class="process-content-h4">{{ receiptContent.permissionsTitle }}</h4>
            <ul>
              <li
                v-for="(item, idx) in receiptContent.permissions"
                :key="`receipt-permission-${idx}`"
                v-html="item"
              ></li>
            </ul>
            <br/>
            <h4 class="process-content-h4">{{ receiptContent.hierarchyTitle }}</h4>
            <h5>{{ receiptContent.hierarchySubtitle }}</h5>
            <ul>
              <li
                v-for="(item, idx) in receiptContent.hierarchy"
                :key="`receipt-hierarchy-${idx}`"
                v-html="item"
              ></li>
            </ul>
            <br/>
            <h4 class="process-content-h4">{{ receiptContent.processListTitle }}</h4>
            <ol>
              <li
                v-for="(item, idx) in receiptContent.processList"
                :key="`receipt-process-${idx}`"
                v-html="item"
              ></li>
            </ol>
          </div>
          <!-- 데스크탑 이미지 3장 -->
          <div v-if="!isMobile" class="image-grid-three">
            <img
              v-for="(alt, idx) in (receiptContent.images.desktopAlts || [])"
              :key="`receipt-desktop-img-${idx}`"
              class="section-img zoomable"
              loading="lazy"
              decoding="async"
              :src="[
                '/guide/영수증%20등록.png',
                '/guide/영수증%20등록%20모달.png',
                '/guide/영수증%20수정.png',
              ][idx]"
              :alt="alt || ''"
              @click="openZoom($event.target.src)"
            />
          </div>
          <!-- 모바일 이미지: 등록 1개 한 줄, 수정/상세 2개 한 줄 -->
          <div v-else class="image-grid-receipt">
            <div class="image-row-single">
              <img
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                src="/guide/영수증%20등록%20모바일.png"
                :alt="(receiptContent.images.mobileAlts || [])[0] || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
            <div class="image-row-two">
              <img
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                src="/guide/영수증%20등록%20모달%20모바일.png"
                :alt="(receiptContent.images.mobileAlts || [])[1] || ''"
                @click="openZoom($event.target.src)"
              />
              <img
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                src="/guide/영수증%20수정%20모바일.png"
                :alt="(receiptContent.images.mobileAlts || [])[2] || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </div>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <!-- 즐겨찾기 -->
      <section v-if="shouldShowSection('favorite')" id="favorite" class="guide-section">
        <h3 @click="toggleSection('favorite')" class="section-title-toggle">
          {{ $t('guide.sections.favorite') }}
          <i :class="sectionsExpanded.favorite ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        <div v-show="sectionsExpanded.favorite" class="section-content">
        <div class="section-grid">
          <div>
            <h4>{{ favoriteContent.permissionsTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in favoriteContent.permissions" :key="`favorite-permission-${idx}`" v-html="item"></li>
            </ul>
            <br/>
            <h4>{{ favoriteContent.featuresTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in favoriteContent.features" :key="`favorite-feature-${idx}`" v-html="item"></li>
            </ul>
          </div>
          <template v-if="!isMobile">
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in (favoriteContent.images.desktopAlts || [])"
                :key="`favorite-desktop-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                :src="idx === 0 ? '/guide/즐겨찾기.png' : '/guide/즐겨찾기 카드 디자인 선택 모달.png'"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
          <template v-else>
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in (favoriteContent.images.mobileAlts || [])"
                :key="`favorite-mobile-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                :src="idx === 0 ? '/guide/즐겨찾기 모바일.png' : '/guide/즐겨찾기 카드 디자인 선택 모달 모바일.png'"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <!-- 사원 관리 -->
      <section v-if="shouldShowSection('employee')" id="employee" class="guide-section">
        <h3 @click="toggleSection('employee')" class="section-title-toggle">
          {{ $t('guide.sections.employee') }}
          <i :class="sectionsExpanded.employee ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        <div v-show="sectionsExpanded.employee" class="section-content">
        <div class="section-grid">
          <div>
            <h4>{{ employeeContent.permissionsTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in employeeContent.permissions" :key="`employee-permission-${idx}`" v-html="item"></li>
            </ul>
            <br/>
            <h4>{{ employeeContent.featuresTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in employeeContent.features" :key="`employee-feature-${idx}`" v-html="item"></li>
            </ul>
          </div>
          <template v-if="!isMobile">
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in (employeeContent.images.desktopAlts || [])"
                :key="`employee-desktop-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                :src="idx === 0 ? '/guide/권한%20부여.png' : '/guide/부서(팀)%20관리.png'"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
          <template v-else>
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in (employeeContent.images.mobileAlts || [])"
                :key="`employee-mobile-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                :src="idx === 0 ? '/guide/권한%20부여%20모바일.png' : '/guide/부서(팀)%20관리%20모바일.png'"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <!-- 시스템 -->
      <section v-if="shouldShowSection('system')" id="system" class="guide-section">
        <h3 @click="toggleSection('system')" class="section-title-toggle">
          {{ $t('guide.sections.system') }}
          <i :class="sectionsExpanded.system ? 'ri-arrow-up-s-line' : 'ri-arrow-down-s-line'"></i>
        </h3>
        <div v-show="sectionsExpanded.system" class="section-content">
        <div class="section-grid">
          <div>
            <h4>{{ systemContent.permissionsTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in systemContent.permissions" :key="`system-permission-${idx}`" v-html="item"></li>
            </ul>
            <br/>
            <h4>{{ systemContent.featuresTitle }}</h4>
            <ul>
              <li v-for="(item, idx) in systemContent.features" :key="`system-feature-${idx}`" v-html="item"></li>
            </ul>
          </div>
          <template v-if="!isMobile">
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in (systemContent.images.desktopAlts || [])"
                :key="`system-desktop-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                src="/guide/대시%20보드.png"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
          <template v-else>
            <div class="image-grid-two">
              <img
                v-for="(alt, idx) in (systemContent.images.mobileAlts || [])"
                :key="`system-mobile-img-${idx}`"
                class="section-img zoomable"
                loading="lazy"
                decoding="async"
                src="/guide/대시%20보드%20모바일.png"
                :alt="alt || ''"
                @click="openZoom($event.target.src)"
              />
            </div>
          </template>
        </div>
        </div>
        <hr class="section-divider" />
      </section>

      <hr />

      <!-- ───────────── Contact │ 관리자 정보 ───────────── -->
      <footer class="guide-footer">
        <div class="contact-section">
          <div class="contact-title">{{ $t('guide.contact.title') }}</div>
          <div class="contact-description">{{ $t('guide.contact.description') }}</div>
          
          <div class="contact-wrapper">
            <div class="contact-column">
              <div class="contact-category">{{ $t('guide.contact.systemCategory') }}</div>
              <div class="contact-row">
                <span class="contact-field">{{ $t('guide.contact.manager') }}</span>
                <span class="contact-data">권범준</span>
              </div>
              <div class="contact-row">
                <span class="contact-field">{{ $t('guide.contact.email') }}</span>
                <span class="contact-data">qjawns0619@gmail.com</span>
              </div>
            </div>
            
            <div class="contact-divider"></div>
            
            <div class="contact-column">
              <div class="contact-category">{{ $t('guide.contact.managementCategory') }}</div>
              <div class="contact-row">
                <span class="contact-field">{{ $t('guide.contact.phone') }}</span>
                <span class="contact-data">042-123-4567</span>
              </div>
              <div class="contact-row">
                <span class="contact-field">{{ $t('guide.contact.email') }}</span>
                <span class="contact-data">management@gmail.com</span>
              </div>
            </div>
          </div>
        </div>
      </footer>
    </div>

    <!-- 이미지 확대 오버레이 -->
    <div v-if="zoomOpen" class="img-overlay" @click="closeZoom">
      <img :src="zoomSrc" alt="확대 이미지" />
    </div>

    <!-- 맨 위로 가기 버튼 -->
    <transition name="fade">
      <button 
        v-if="showScrollTop"
        class="scroll-to-top"
        @click="scrollToTop"
        title="맨 위로"
      >
        <i class="ri-arrow-up-line"></i>
      </button>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from "vue";
import PageTitle from '@/components/common/title/PageTitle.vue'
import FrontHeader from '@/components/auth/FrontHeader.vue'
import { useAuthStore } from '@/store/auth.js'
import useBreakPoint from '@/composables/useBreakPoint'
import { refreshSession } from '@/utils/session'
import { 
  hasPermission, 
  ROLE_GATE_SYSTEM, 
  ROLE_HRM_ASSISTANT_MANAGER, 
  ROLE_HRM_MANAGER, 
  ROLE_RECEIPT_REGISTRAR,
  ROLE_RECEIPT_APPROVER,
  ROLE_RECEIPT_INSPECTOR,
  ROLE_RECEIPT_FINALIZER,
  ROLE_RECEIPT_MANAGER
} from '@/utils/roleUtils'
import { useI18n } from 'vue-i18n'

const authStore = useAuthStore();
const { t, tm, locale, messages } = useI18n();

const SIGNUP_FLOW_ICONS = ['1️⃣', '2️⃣', '3️⃣', '4️⃣', '5️⃣'];
const RECEIPT_FLOW_ICONS = ['1️⃣', '2️⃣', '3️⃣', '4️⃣'];

/**
 * 다국어 메시지 트리를 탐색하여 값을 반환한다.
 * @param {string} key 조회할 메시지 키
 * @returns {unknown} 조회된 메시지 값
 */
function getMessageValue(key) {
  if (typeof tm === 'function') {
    const value = tm(key);
    if (value !== undefined && value !== null) {
      return value;
    }
  }

  const paths = key.split('.');
  let current = messages.value?.[locale.value];

  for (const segment of paths) {
    if (current === undefined || current === null) {
      return undefined;
    }
    current = current[segment];
  }

  return current;
}

/**
 * 다국어 객체 메시지를 반환한다.
 * @param {string} key 조회할 메시지 키
 * @returns {Record<string, unknown>} 객체 형태 메시지
 */
function translateObjectMessage(key) {
  const value = getMessageValue(key);
  return value && typeof value === 'object' && !Array.isArray(value) ? value : {};
}

/**
 * 다국어 배열 메시지를 반환한다.
 * @param {string} key 조회할 메시지 키
 * @returns {Array<unknown>} 배열 형태 메시지
 */
function translateArrayMessage(key) {
  const value = getMessageValue(key);
  return Array.isArray(value) ? value : [];
}

const signupContent = computed(() => {
  const process = translateObjectMessage('guide.signup.process');
  const flowTexts = translateArrayMessage('guide.signup.process.flow');
  const table = translateObjectMessage('guide.signup.process.table');
  return {
    process: {
      summary: process.summary ?? '',
      title: process.title ?? '',
      steps: translateArrayMessage('guide.signup.process.steps'),
      flow: flowTexts.map((text, index) => ({
        icon: SIGNUP_FLOW_ICONS[index] ?? '',
        text,
        isAdmin: index === flowTexts.length - 1,
      })),
      table: {
        title: process.tableTitle ?? table.title ?? '',
        head: Array.isArray(table.head) ? table.head : [],
        rows: Array.isArray(table.rows) ? table.rows : [],
      },
    },
    checklist: translateArrayMessage('guide.signup.checklist'),
    images: translateObjectMessage('guide.signup.images'),
  };
});

const passwordContent = computed(() => ({
  steps: translateArrayMessage('guide.password.steps'),
  images: {
    desktopAlts: translateArrayMessage('guide.password.images.desktopAlts'),
    mobileAlts: translateArrayMessage('guide.password.images.mobileAlts'),
  },
}));

const socialContent = computed(() => ({
  steps: translateArrayMessage('guide.social.steps'),
  images: translateObjectMessage('guide.social.images'),
}));

const mainContent = computed(() => {
  const section = translateObjectMessage('guide.main');
  return {
    permissionsTitle: section.permissionsTitle ?? '',
    permissions: translateArrayMessage('guide.main.permissions'),
    featuresTitle: section.featuresTitle ?? '',
    features: translateArrayMessage('guide.main.features'),
    images: section.images ?? {},
  };
});

const noticeContent = computed(() => {
  const section = translateObjectMessage('guide.notice');
  return {
    permissionsTitle: section.permissionsTitle ?? '',
    permissions: translateArrayMessage('guide.notice.permissions'),
    featuresTitle: section.featuresTitle ?? '',
    features: translateArrayMessage('guide.notice.features'),
    images: section.images ?? {},
  };
});

const receiptContent = computed(() => {
  const section = translateObjectMessage('guide.receipt');
  const process = translateObjectMessage('guide.receipt.process');
  const sections = translateArrayMessage('guide.receipt.process.sections').map((item) => {
    const table = item.table ?? null;
    const normalizedTable = table
      ? {
          title: table.title ?? '',
          head: Array.isArray(table.head) ? table.head : [],
          rows: Array.isArray(table.rows) ? table.rows : [],
        }
      : null;
    return {
      title: item.title ?? '',
      items: Array.isArray(item.items) ? item.items : [],
      table: normalizedTable,
    };
  });
  const exception = translateObjectMessage('guide.receipt.process.exception');
  const reference = translateObjectMessage('guide.receipt.process.reference');
  const summary = translateObjectMessage('guide.receipt.process.summaryTable');
  const summaryTable = summary.table ?? {};

  return {
    process: {
      summary: process.summary ?? '',
      title: process.title ?? '',
      flow: translateArrayMessage('guide.receipt.process.flowSteps').map((text, index) => ({
        icon: RECEIPT_FLOW_ICONS[index] ?? '',
        text,
      })),
      exceptionStep: process.exceptionStep ?? '',
      rejectFlow: translateArrayMessage('guide.receipt.process.rejectFlow'),
      sections,
      exception: {
        title: exception.title ?? '',
        cases: Array.isArray(exception.cases) ? exception.cases : [],
      },
      reference: {
        title: reference.title ?? '',
        boxTitle: reference.boxTitle ?? '',
        items: Array.isArray(reference.items) ? reference.items : [],
      },
      summaryTable: {
        title: summary.title ?? '',
        head: Array.isArray(summaryTable.head) ? summaryTable.head : [],
        rows: Array.isArray(summaryTable.rows) ? summaryTable.rows : [],
      },
    },
    permissionsTitle: section.permissionsTitle ?? '',
    permissions: translateArrayMessage('guide.receipt.permissions'),
    hierarchyTitle: section.hierarchyTitle ?? '',
    hierarchySubtitle: section.hierarchySubtitle ?? '',
    hierarchy: translateArrayMessage('guide.receipt.hierarchy'),
    processListTitle: section.processListTitle ?? '',
    processList: translateArrayMessage('guide.receipt.processList'),
    images: {
      desktopAlts: translateArrayMessage('guide.receipt.images.desktopAlts'),
      mobileAlts: translateArrayMessage('guide.receipt.images.mobileAlts'),
    },
  };
});

const favoriteContent = computed(() => {
  const section = translateObjectMessage('guide.favorite');
  return {
    permissionsTitle: section.permissionsTitle ?? '',
    permissions: translateArrayMessage('guide.favorite.permissions'),
    featuresTitle: section.featuresTitle ?? '',
    features: translateArrayMessage('guide.favorite.features'),
    images: section.images ?? {},
  };
});

const employeeContent = computed(() => {
  const section = translateObjectMessage('guide.employee');
  return {
    permissionsTitle: section.permissionsTitle ?? '',
    permissions: translateArrayMessage('guide.employee.permissions'),
    featuresTitle: section.featuresTitle ?? '',
    features: translateArrayMessage('guide.employee.features'),
    images: section.images ?? {},
  };
});

const systemContent = computed(() => {
  const section = translateObjectMessage('guide.system');
  return {
    permissionsTitle: section.permissionsTitle ?? '',
    permissions: translateArrayMessage('guide.system.permissions'),
    featuresTitle: section.featuresTitle ?? '',
    features: translateArrayMessage('guide.system.features'),
    images: section.images ?? {},
  };
});

const allSections = [
  { id: "signup" },
  { id: "password" },
  { id: "social" },
  { id: "main" },
  { id: "notice" },
  { id: "receipt" },
  { id: "favorite" },
  { id: "employee" },
  { id: "system" },
];

/**
 * 특정 섹션이 현재 사용자에게 표시되어야 하는지 확인
 */
function shouldShowSection(sectionId) {
  if (!authStore.isLoggedIn) {
    // 비로그인 상태에서는 회원가입, 비밀번호 찾기, 소셜로그인만
    return ['signup', 'password', 'social'].includes(sectionId);
  }
  
  const userRoles = authStore.roles || [];
  
  switch (sectionId) {
    case 'signup':
    case 'password':
    case 'social':
    case 'main':
    case 'notice':
    case 'favorite':
      // 모든 로그인 사용자 접근 가능
      return true;
    case 'receipt':
      // 영수증 권한이 있는 사용자만
      return hasPermission(userRoles, [
        ROLE_RECEIPT_REGISTRAR,
        ROLE_RECEIPT_APPROVER,
        ROLE_RECEIPT_INSPECTOR,
        ROLE_RECEIPT_FINALIZER,
        ROLE_RECEIPT_MANAGER
      ]);
    case 'employee':
      // 중간관리자 이상만
      return hasPermission(userRoles, [
        ROLE_HRM_ASSISTANT_MANAGER,
        ROLE_HRM_MANAGER
      ]);
    case 'system':
      // 시스템 관리자만
      return hasPermission(userRoles, [ROLE_GATE_SYSTEM]);
    default:
      return true;
  }
}

// 로그인 상태와 권한에 따라 보여줄 섹션들을 필터링
const sections = computed(() => {
  return allSections.filter(section => shouldShowSection(section.id));
});

// 반응형: 전역 브레이크포인트 사용
const { isMobile } = useBreakPoint()

// 섹션별 접기/펼치기 상태 (초기: 모두 접힘)
const sectionsExpanded = ref({
  signup: false,
  password: false,
  social: false,
  main: false,
  notice: false,
  receipt: false,
  favorite: false,
  employee: false,
  system: false
});

// 개별 섹션 토글
function toggleSection(sectionId) {
  sectionsExpanded.value[sectionId] = !sectionsExpanded.value[sectionId];
  updateAllDetailsState();
}

// details 요소 제어
const allDetailsExpanded = ref(false);

function toggleAllDetails() {
  allDetailsExpanded.value = !allDetailsExpanded.value;
  // sectionsExpanded도 동기화
  Object.keys(sectionsExpanded.value).forEach(key => {
    sectionsExpanded.value[key] = allDetailsExpanded.value;
  });
  // details 요소도 동기화
  /* const detailsElements = document.querySelectorAll('details');
  detailsElements.forEach(detail => {
    detail.open = allDetailsExpanded.value;
  }); */
}

// sectionsExpanded 상태가 변경되면 allDetailsExpanded 업데이트
function updateAllDetailsState() {
  const allExpanded = Object.values(sectionsExpanded.value).every(v => v === true);
  allDetailsExpanded.value = allExpanded;
}

function scrollTo(id) {
  const el = document.getElementById(id);
  if (el) {
    // 모바일/데스크탑에 따라 다른 오프셋 적용
    const offset = isMobile.value ? 180 : 150;
    const y = el.getBoundingClientRect().top + window.pageYOffset - offset;
    window.scrollTo({ top: y, behavior: "smooth" });
  }
}

// 이미지 확대 (Lightbox)
const zoomOpen = ref(false);
const zoomSrc = ref("");

function openZoom(src) {
  zoomSrc.value = src;
  zoomOpen.value = true;
}

function closeZoom() {
  zoomOpen.value = false;
}

/**
 * 맨 위로 가기 버튼
 */
const showScrollTop = ref(false);

/**
 * 스크롤 이벤트 핸들러
 */
const handleScroll = () => {
  showScrollTop.value = window.scrollY > 300;
};

/**
 * 맨 위로 스크롤
 */
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
};

/**
 * 회원가입 프로세스 details ref
 */
const signupDetails = ref(null);

/**
 * 영수증 프로세스 details ref
 */
const receiptDetails = ref(null);

/**
 * 회원가입 프로세스 닫기
 */
function closeSignupProcess() {
  if (signupDetails.value) {
    signupDetails.value.open = false;
    // 닫기와 동시에 회원가입 섹션 타이틀로 스크롤
    scrollTo('signup');
  }
}

/**
 * 영수증 프로세스 닫기
 */
function closeReceiptProcess() {
  if (receiptDetails.value) {
    receiptDetails.value.open = false;
    // 닫기와 동시에 영수증 섹션 타이틀로 스크롤
    scrollTo('receipt');
  }
}

/**
 * 스크롤 리스너 등록/해제 및 로그인 재검증
 */
onMounted(async () => {
  window.addEventListener('scroll', handleScroll);
  
  // 로그인되어 있을 때 /auth/me로 재검증
  if (authStore.isLoggedIn) {
    try {
      await refreshSession();
    } catch (error) {
      // refreshSession 실패 시 에러는 공통 인터셉터에서 처리됨
      console.error('[GuidePage] 세션 갱신 실패:', error);
    }
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
h3 {
  font-size: 1.3rem;
}
h4 {
  font-size: 1rem;
}
h5, ul, li {
  font-size: 0.875rem;
}
:deep(strong),
strong {
  font-weight: 900 !important;   /* 기본 Bold(≈700) 강제 */
}
/**
 * 기본 콘텐츠 스타일
 */
.content {
  padding-bottom: 20px;
}

/**
 * 로그인하지 않은 상태에서 콘텐츠 넓이 제한 및 중앙 정렬
 */
.guide-page-container:not(.logged-in) .content {
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 20px;
  padding-right: 20px;
  padding-top: 100px;
}
.guide-nav {
  background: none !important;
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin: 1.5rem 0;
}
.guide-nav button {
  padding: 0.5rem 0.9rem;
  background: #f0f3f8;
  border-radius: 20px;
  font-size: 0.85rem;
  color: #555;
  border: none;
  cursor: pointer;
}
.guide-nav button:hover {
  background: #004497;
  color: #fff;
}

.guide-nav-actions {
  display: flex;
  justify-content: flex-end;
  margin: 1rem 0 1rem 0;
}

.toggle-details-btn {
  padding: 0.5rem 1rem;
  background: #004497;
  border-radius: 20px;
  font-size: 0.9rem;
  color: #fff;
  border: none;
  cursor: pointer;
  font-weight: 600;
}

.toggle-details-btn:hover {
  background: #003376;
}

/* 모바일에서 버튼 크기 작게 */
@media (max-width: 650px) {
  .toggle-details-btn {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
  }
}

.guide-section {
  margin-top: 3rem;
  scroll-margin-top: 120px;
}
.guide-section h3 {
  font-size: 1.3rem;
  font-weight: 800;
  margin-bottom: 1.2rem;
  color: #004497;
  padding-bottom: 0.4rem;
  border-bottom: 1px solid #e0e0e0;
}

.section-title-toggle {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: color 0.2s ease;
}

.section-title-toggle:hover {
  color: #003376;
}

.section-title-toggle i {
  font-size: 1.2rem;
  margin-left: auto;
}
.section-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.2rem;
  align-items: center;
}
.section-grid img {
  width: 100%;
  border-radius: 0px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}
.flow {
  font-weight: 600;
  margin: 0.4rem 0 0.6rem;
}

.section-divider {
  border: none;
  border-bottom: 1px solid #f0f0f0;
  margin: 3rem 0 0;
}

.image-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.image-grid img {
  width: 100%;
  border-radius: 0px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.section-img {
  width: 100%;
  border-radius: 4px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  margin-top: 1rem;
  cursor: pointer;
  background-color: #f2f3f5;
  padding: 0px;
  box-sizing: border-box;
}

.nav-divider {
  border: none;
  border-bottom: 1px solid #e0e0e0;
  margin: 1rem 0;
}

.image-grid-two {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.image-grid-three {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 영수증 모바일 레이아웃: 등록 1개 한 줄, 수정/상세 2개 한 줄 */
.image-grid-receipt {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.image-row-single {
  width: 100%;
}
.image-row-two {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
}

/* 모바일에서 이미지가 2장 이상일 때 2열 그리드로 표시 */
@media (max-width: 650px) {
  .image-grid-two,
  .image-grid-three {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.5rem;
  }
}

ul li,
ol li {
  margin-bottom: 0.4rem;
}

.img-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  cursor: zoom-out;
}

.img-overlay img {
  max-width: 75vw;
  max-height: 90vh;
  border-radius: 5px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
}

@media (max-width: 650px) {
  .img-overlay img {
    max-width: 95vw;
    max-height: 95vh;
  }
}

/* ───────────── Contact │ Footer ───────────── */
.guide-footer {
  margin: 5rem 0 3rem;
  border-top: 1px solid #e8e8e8;
  padding-top: 3rem;
}

.contact-section {
  width: 100%;
  background: #fafafa;
  padding: 2.5rem 0;
}

.contact-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 400;
  letter-spacing: 8px;
  color: #111;
  margin-bottom: 1rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.contact-description {
  text-align: center;
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 3rem;
  font-weight: 500;
}

.contact-wrapper {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 3rem;
  max-width: 800px;
  margin: 0 auto;
  align-items: start;
}

.contact-column {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.contact-category {
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 2px;
  color: #333;
  margin-bottom: 1rem;
  text-align: center;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #ddd;
}

.contact-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.contact-row:last-child {
  border-bottom: none;
}

.contact-field {
  font-size: 0.7rem;
  color: #888;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
  min-width: 60px;
}

.contact-data {
  font-size: 0.8rem;
  /* color: #111; */
  color: #575757;
  font-weight: 500;
  text-align: right;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
}

.contact-divider {
  width: 1px;
  height: 100px;
  background: linear-gradient(to bottom, transparent, #ddd 20%, #ddd 80%, transparent);
  align-self: center;
}


/* 데스크탑 줄간격 확대 */
@media (min-width: 651px) {
  ul li,
  ol li {
    margin-bottom: 0.6rem;
    line-height: 1.75;
  }
}

/* 모바일 */
@media (max-width: 650px) {
  /**
   * 모바일에서 로그인하지 않은 상태 콘텐츠 중앙 정렬
   */
  .guide-page-container:not(.logged-in) .content {
    max-width: 100%;
    padding-left: 16px;
    padding-right: 16px;
    padding-top: 20px;
  }
  
  hr {
    margin-bottom: 0px !important;
  }
  h3 {
    font-size: 1rem !important;
    font-weight: 800;
    margin-bottom: 30px !important;
  }
  h4 {
    font-size: 0.85rem;
    margin-bottom: 15px !important;
  }
  h5, ul, li {
    font-size: 0.82rem;
    margin-bottom: 10px !important;
  }
  .content-sub-title {
    margin-bottom: 50px !important;
  }
  .section-grid {
    grid-template-columns: 1fr;
    gap: 0.9rem;
  }
  .guide-nav {
    overflow-x: auto;
    padding-bottom: 4px;
    -ms-overflow-style: none;
    scrollbar-width: none;
  }
  .guide-nav::-webkit-scrollbar {
    display: none;
  }
  .guide-nav button {
    padding: 0.4rem 0.8rem;
    background: #f0f3f8;
    border-radius: 20px;
    font-size: 0.75rem;
    color: #555;
    border: none;
    cursor: pointer;
  }
  .section-img {
    max-height: 55vh;
    object-fit: contain;
    margin-top: 0.5rem;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  }
  .image-grid-two,
  .image-grid-three {
    gap: 0.8rem;
  }
  /* 모바일 전용 여백 & 폰트 */
  .section-divider {
    margin-bottom: 1.5rem; /* ≈ 24px (15px 추가) */
  }

  .guide-content h3 {
    font-size: 0.875rem;
  }

  ul li,
  ol li {
    font-size: 0.8rem;
  }

  .guide-footer {
    margin: 3rem 0 2rem;
    padding-top: 2rem;
  }

  .contact-section {
    padding: 1rem 1rem;
  }

  .contact-title {
    font-size: 1.2rem;
    letter-spacing: 4px;
    margin-bottom: 0.5rem;
  }

  .contact-description {
    font-size: 0.7rem;
    margin-bottom: 2rem;
  }

  .contact-wrapper {
    grid-template-columns: 1fr;
    gap: 2rem;
    max-width: none;
  }

  .contact-divider {
    display: none;
  }

  .contact-column {
    display: flex;
    flex-direction: column;
    gap: 0rem;
  }

  .contact-category {
    font-size: 0.7rem;
    letter-spacing: 1.5px;
    margin-bottom: 0.5rem;
  }

  .contact-row {
    padding: 0.6rem 0;
  }

  .contact-field {
    font-size: 0.65rem;
    min-width: 50px;
  }

  .contact-data {
    font-size: 0.75rem;
  }

  /* 입력 테이블 폰트 크기 (모바일) */
  .input-table,
  .input-table th,
  .input-table td {
    font-size: 0.65rem;
  }
  .process-content {
    padding: 0.8rem 0.9rem;
  }
  .signup-flow,
  .receipt-flow {
    padding: 0.8rem;
    gap: 0.6rem;
  }
}

/* ───────── 이너 테이블 헤더 중앙 정렬 ───────── */
.process-content .input-table thead th {
  text-align: center;
  vertical-align: middle;
}

/* ───────── '필수 여부(○)' 셀 중앙 정렬 ───────── */
.input-table td.table-td-center {
  text-align: center;
  vertical-align: middle;
}

/* ───────────── 회원가입 프로세스 ───────────── */
.process-content-h4 {
  margin-top: 15px;
}

.signup-process summary {
  cursor: pointer;
  font-weight: 700;
  color: #004497;
  font-size: 1rem;
  margin-bottom: 15px;
}
/* 
.signup-process[open] summary {
  margin-bottom: 15px;
} */

.process-content {
  background: #fafbff;
  padding: 1rem 1.2rem;
  border: 1px solid #e6e9f2;
  border-radius: 6px;
  margin-top: 0.5rem;
  margin-bottom: 15px;
}

/* 프로세스 푸터 */
.process-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e6e9f2;
}

.process-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.4rem 0.8rem;
  background: #dc3545;
  border: 1px solid #dc3545;
  border-radius: 4px;
  color: #fff;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.process-close-btn:hover {
  background: #c82333;
  border-color: #bd2130;
  color: #fff;
}

.input-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 0.8rem;
  font-size: 0.85rem;
}

.input-table th,
.input-table td {
  border: 1px solid #ddd;
  padding: 0.4rem 0.6rem;
  text-align: left;
}

.input-table th {
  background: #f1f3f8;
  font-weight: 600;
}

/* ───────────── 회원가입 흐름도 스타일 ───────────── */
.signup-flow {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.8rem;
  margin: 1.2rem 0;
  padding: 1rem;
  background: linear-gradient(135deg, #f8f9ff 0%, #e8f0ff 100%);
  border-radius: 8px;
  border: 1px solid #dde6ff;
}

.flow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.8rem 1rem;
  border-radius: 8px;
  min-width: 100px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.flow-step:hover {
  transform: translateY(-2px);
}

.flow-step.step {
  background: #fafaff;
  border: 2px solid #4f80ff;
  color: #0a0a23;
}

.flow-step.admin {
  background: #e0f7fa;
  border: 2px solid #00acc1;
  color: #004d40;
}

.step-number {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 0.3rem;
}

.step-text {
  font-size: 0.75rem;
  font-weight: 600;
  line-height: 1.2;
}

.flow-arrow {
  font-size: 1.2rem;
  color: #666;
  font-weight: bold;
}

/* 모바일에서 세로 배치 */
@media (max-width: 650px) {
  .signup-flow {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .flow-step {
    width: 100%;
    min-width: auto;
    padding: 0.6rem;
  }
  
  .flow-arrow {
    transform: rotate(90deg);
    font-size: 1rem;
  }
  
  .step-text {
    font-size: 0.7rem;
  }

  .signup-process summary {
    font-size: 0.875rem;
  }

  .process-close-btn {
    padding: 0.35rem 0.7rem;
    font-size: 0.75rem;
  }
}

/* ───────────── 영수증 프로세스 스타일 (수정) ───────────── */
.receipt-process summary {
  cursor: pointer;
  font-weight: 700;
  color: #004497;
  font-size: 1rem;
  margin-bottom: 15px;
}

/* .receipt-process[open] summary {
  margin-bottom: 0.8rem;
} */

.receipt-flow {
  display: flex;
  flex-wrap: wrap;
  /* align-items: flex-start;  ← (삭제) */
  align-items: center;            /* ← (추가) 화살표/박스 세로 중앙 정렬 */
  gap: 0.8rem;
  margin: 1.2rem 0;
  padding: 1rem;
  background: linear-gradient(135deg, #fff8f0 0%, #fef3e7 100%);
  border-radius: 8px;
  border: 1px solid #ffe4c4;
  position: relative;
}

/* 화살표를 라인(행) 높이에 맞게 늘리고 내부에서 중앙 정렬 */
.receipt-flow .flow-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: stretch;   /* ← (추가) 가장 큰 이웃 높이에 맞춤 */
  min-width: 24px;
  padding: 0 0.3rem;
  font-size: 1.2rem;
  color: #666;
  font-weight: bold;
}

/* 예외 처리 박스를 라인 안으로 배치 (오른쪽 고정 제거) */
.receipt-flow .flow-step.exception {
  /* position: absolute; top: 1rem; right: 1rem; ← (삭제) */
  position: static;          /* ← (추가) 일반 플렉스 아이템으로 */
  margin-left: 0.8rem;       /* ← (추가) 마지막 스텝과 적당한 간격 */
  order: 2;                  /* ← (추가) 상단 라인 내에서 스텝 뒤에 오도록 */
  min-width: 100px;          /* (선택) 표시 안정화 */
}

/* 반려 라인(텍스트) 묶음을 한 줄 아래로 확실히 내림 */
.reject-flow {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  margin-top: 0.6rem;
  width: 100%;
  order: 3;                  /* ← (추가) 예외 처리 박스(2) 뒤에 배치 */
}

.reject-arrow {
  font-size: 0.7rem;
  color: #ff5a5f;
  font-weight: 600;
  text-align: center;
  padding: 0.2rem;
  background: rgba(255, 90, 95, 0.1);
  border-radius: 4px;
}

/* 모바일에서의 배치(기존 의도 유지) */
@media (max-width: 650px) {
  .receipt-flow {
    flex-direction: column;
    gap: 0.5rem;
  }

  .receipt-flow .flow-step.exception {
    position: static;
    width: 100%;
    margin-top: 0.5rem;
    order: 2;               /* 모바일에서도 예외 → 반려 순서 유지 */
  }

  .reject-arrow {
    font-size: 0.65rem;
  }

  .receipt-process summary {
    font-size: 0.875rem;
  }

  .process-close-btn {
    padding: 0.35rem 0.7rem;
    font-size: 0.75rem;
  }

  .guide-footer {
    padding-bottom: 50px;
  }
}

/* 맨 위로 가기 버튼 */
.scroll-to-top {
  position: fixed;
  right: 80px;
  bottom: 100px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #3b82f6 0%, #2564ebbd 100%);
  border: none;
  border-radius: 50%;
  color: #ffffff;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
  z-index: 1000;
}

.scroll-to-top:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.5);
}

.scroll-to-top:active {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(59, 130, 246, 0.4);
}

.scroll-to-top i {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 페이드 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 모바일에서 버튼 크기 조정 */
@media (max-width: 650px) {
  .scroll-to-top {
    right: 30px;
    bottom: 90px;
    width: 45px;
    height: 45px;
    font-size: 20px;
  }
}
</style>
