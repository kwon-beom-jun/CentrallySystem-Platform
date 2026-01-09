/**
 * i18n 설정
 * 브라우저 언어 또는 localStorage에서 저장된 언어를 사용
 */
import { createI18n } from 'vue-i18n'
import ko from './ko'
import en from './en'

/**
 * 저장된 언어 또는 브라우저 언어 가져오기
 */
const getSavedLocale = () => {
  try {
    const savedLocale = localStorage.getItem('locale')
    if (savedLocale && ['ko', 'en'].includes(savedLocale)) {
      return savedLocale
    }
  } catch (e) {
    console.warn('localStorage 접근 실패:', e)
  }
  
  // 브라우저 언어 감지
  const browserLocale = navigator.language.split('-')[0]
  return ['ko', 'en'].includes(browserLocale) ? browserLocale : 'ko'
}

const i18n = createI18n({
  legacy: false, // Composition API 사용
  locale: getSavedLocale(),
  fallbackLocale: 'ko',
  messages: {
    ko,
    en,
  },
  // 번역 누락 시 경고 (개발 환경에서만)
  silentTranslationWarn: import.meta.env.PROD,
  silentFallbackWarn: import.meta.env.PROD,
})

/**
 * 언어 변경 함수
 * @param {string} locale - 변경할 언어 ('ko' | 'en')
 */
export const changeLocale = (locale) => {
  if (!['ko', 'en'].includes(locale)) {
    console.warn(`지원하지 않는 언어: ${locale}`)
    return
  }
  
  i18n.global.locale.value = locale
  
  try {
    localStorage.setItem('locale', locale)
  } catch (e) {
    console.warn('localStorage 저장 실패:', e)
  }
  
  // Axios 헤더 업데이트
  updateAxiosLocale(locale)
}

/**
 * Axios Accept-Language 헤더 업데이트
 * @param {string} locale - 설정할 언어
 */
const updateAxiosLocale = (locale) => {
  try {
    import('@/api/apiConfig').then(({ systemApi, authApi, hrmApi, receiptApi, infoApi }) => {
      const apis = [systemApi, authApi, hrmApi, receiptApi, infoApi]
      apis.forEach(api => {
        if (api && api.defaults && api.defaults.headers) {
          api.defaults.headers.common['Accept-Language'] = locale
        }
      })
    })
  } catch (e) {
    console.warn('Axios 헤더 업데이트 실패:', e)
  }
}

// 초기 Axios 헤더 설정
updateAxiosLocale(getSavedLocale())

export default i18n

