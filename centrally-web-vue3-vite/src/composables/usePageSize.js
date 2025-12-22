// 반응형 페이지 사이즈 계산용 컴포저블
// 데스크탑(>=651px) : 10, 모바일 : 4 / 5 / 6 / 7 / 8 / 9 반환 헬퍼 제공

import { computed } from 'vue'
import useBreakPoint from '@/composables/useBreakPoint'

/**
 * 데스크탑이면 8, 모바일이면 4 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize8or4 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 4 : 8))
}

/**
 * 데스크탑이면 8, 모바일이면 5 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize8or5 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 5 : 8))
} 

/**
 * 데스크탑이면 8, 모바일이면 6 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize8or6 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 6 : 8))
} 

/**
 * 데스크탑이면 8, 모바일이면 7 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize8or7 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 7 : 8))
} 

/** --------------------------------------------------- */

/**
 * 데스크탑이면 10, 모바일이면 4 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize10or4 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 4 : 10))
}

/**
 * 데스크탑이면 10, 모바일이면 5 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize10or5 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 5 : 10))
} 

/**
 * 데스크탑이면 10, 모바일이면 6 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize10or6 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 6 : 10))
} 

/**
 * 데스크탑이면 10, 모바일이면 7 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize10or7 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 7 : 10))
} 

/**
 * 데스크탑이면 10, 모바일이면 8 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize10or8 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 8 : 10))
} 

/**
 * 데스크탑이면 10, 모바일이면 9 반환
 * 
 * @returns {import('vue').ComputedRef<number>}
 */
export function usePageSize10or9 () {
  const { isMobile } = useBreakPoint()
  return computed(() => (isMobile.value ? 9 : 10))
} 