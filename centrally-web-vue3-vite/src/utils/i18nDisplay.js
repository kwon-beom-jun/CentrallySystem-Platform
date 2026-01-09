import i18n from '@/locales'

/**
 * 메뉴 i18n 키를 표시 문자열로 변환합니다.
 *
 * @param {string} menuKey - 메뉴 i18n 키 혹은 일반 문자열
 * @param {(key: string) => string} [translator] - vue-i18n 의 t 함수 (선택)
 * @returns {string} 변환된 메뉴명
 */
export function translateMenuKey(menuKey, translator) {
  if (!menuKey) {
    return ''
  }

  const t = translator || i18n.global.t
  if (typeof menuKey === 'string' && menuKey.startsWith('menu.')) {
    const translated = t(menuKey)
    return translated && translated !== menuKey ? translated : menuKey
  }

  return menuKey
}

/**
 * 기능 i18n 키를 표시 문자열로 변환합니다.
 *
 * @param {string} functionKey - 기능 i18n 키 혹은 일반 문자열
 * @param {(key: string) => string} [translator] - vue-i18n 의 t 함수 (선택)
 * @returns {string} 변환된 기능명
 */
export function translateFunctionKey(functionKey, translator) {
  if (!functionKey) {
    return ''
  }

  const t = translator || i18n.global.t
  if (typeof functionKey === 'string' && functionKey.startsWith('activity.')) {
    const translated = t(functionKey)
    return translated && translated !== functionKey ? translated : functionKey
  }

  return functionKey
}


