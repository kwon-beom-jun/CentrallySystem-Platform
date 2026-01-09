/**
 * 일정 관련 유틸리티 함수
 */

import { NOTIFICATION_TIMING_UNIT } from '@/constants/infoConstants';

/**
 * Hex 색상을 rgba로 변환 (투명도 적용)
 * @param {string} hex - Hex 색상 코드 (# 포함 또는 미포함)
 * @param {number} opacity - 투명도 (0~1, 기본값 0.7)
 * @returns {string|null} rgba 색상 문자열 또는 null
 */
export function hexToRgba(hex, opacity = 0.7) {
  if (!hex) return null;
  
  // # 제거
  const cleanHex = hex.replace('#', '');
  
  // 3자리 hex를 6자리로 변환
  const fullHex = cleanHex.length === 3 
    ? cleanHex.split('').map(char => char + char).join('')
    : cleanHex;
  
  // RGB 값 추출
  const r = parseInt(fullHex.substring(0, 2), 16);
  const g = parseInt(fullHex.substring(2, 4), 16);
  const b = parseInt(fullHex.substring(4, 6), 16);
  
  return `rgba(${r}, ${g}, ${b}, ${opacity})`;
}

/**
 * 알림 시점 값 검증 및 자동 보정
 * @param {string} timingType - 알림 시점 단위 ('none', 'DAY', 'HOUR', 'MINUTE')
 * @param {Object} timingValue - 알림 시점 값 객체 { date, hour, minute }
 * @returns {Object} 보정된 알림 시점 값 객체
 */
export function validateTimingValue(timingType, timingValue) {
  const result = { ...timingValue };
  
  if (timingType === NOTIFICATION_TIMING_UNIT.DAY) {
    // 일: 일자, 시(0-23), 분(0-59)
    if (result.hour !== null && result.hour !== undefined) {
      result.hour = Math.max(0, Math.min(23, result.hour || 0));
    }
    if (result.minute !== null && result.minute !== undefined) {
      result.minute = Math.max(0, Math.min(59, result.minute || 0));
    }
  } else if (timingType === NOTIFICATION_TIMING_UNIT.HOUR) {
    // 시간: 시(0-23), 분(0-59)
    if (result.hour !== null && result.hour !== undefined) {
      result.hour = Math.max(0, Math.min(23, result.hour || 0));
    }
    if (result.minute !== null && result.minute !== undefined) {
      result.minute = Math.max(0, Math.min(59, result.minute || 0));
    }
  } else if (timingType === NOTIFICATION_TIMING_UNIT.MINUTE) {
    // 분: 분(3-59)
    if (result.minute !== null && result.minute !== undefined) {
      result.minute = Math.max(3, Math.min(59, result.minute || 3));
    }
  }
  
  return result;
}

/**
 * 계산된 알림 시간 (실시간 표시용)
 * @param {string} timingType - 알림 시점 단위 ('none', 'DAY', 'HOUR', 'MINUTE')
 * @param {Object} timingValue - 알림 시점 값 객체 { date, hour, minute }
 * @param {string} startDate - 일정 시작일 (YYYY-MM-DD)
 * @param {string|null} startTime - 일정 시작 시간 (HH:mm) 또는 null
 * @returns {string|null} 계산된 알림 시간 문자열 (예: "2025년 12월 18일 15시 30분 00초") 또는 에러 메시지 또는 null
 */
export function calculateNotificationTime(timingType, timingValue, startDate, startTime = null) {
  if (timingType === 'none') {
    return null;
  }
  
  if (!startDate) {
    return '일정 시작일을 입력해 주세요';
  }
  
  try {
    const timing = timingValue;
    let notificationDateTime = null;
    
    if (timingType === NOTIFICATION_TIMING_UNIT.DAY) {
      // 일: 직접 지정된 일자 + 시 + 분
      if (!timing.date) {
        return '알림 일자를 입력해 주세요';
      }
      
      // 일자만 입력되어도 시작일과 비교 검증
      const notificationDate = new Date(timing.date);
      const startDateObj = new Date(startDate);
      if (notificationDate > startDateObj) {
        return '알림 일자는 시작일보다 클 수 없습니다';
      }
      if (notificationDate.getTime() === startDateObj.getTime()) {
        // 같은 날짜인 경우 시간 비교 필요
        if (startTime) {
          const [startHour, startMinute] = startTime.split(':').map(Number);
          if (timing.hour !== null && timing.hour !== undefined && timing.minute !== null && timing.minute !== undefined) {
            const notificationHour = timing.hour || 0;
            const notificationMinute = timing.minute || 0;
            if (notificationHour > startHour || (notificationHour === startHour && notificationMinute > startMinute)) {
              return '알림 시간은 시작 시간보다 클 수 없습니다';
            }
          }
        }
      }
      
      if (timing.hour === null || timing.hour === undefined) {
        return '알림 시간을 입력해 주세요';
      }
      if (timing.minute === null || timing.minute === undefined) {
        return '알림 분을 입력해 주세요';
      }
      const dateStr = timing.date;
      const hour = timing.hour || 0;
      const minute = timing.minute || 0;
      notificationDateTime = new Date(`${dateStr}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:00`);
    } else if (timingType === NOTIFICATION_TIMING_UNIT.HOUR) {
      // 시간: 일정 시작 시간에서 시, 분 빼기
      if (timing.hour === null || timing.hour === undefined) {
        return '알림 시간을 입력해 주세요';
      }
      if (timing.minute === null || timing.minute === undefined) {
        return '알림 분을 입력해 주세요';
      }
      const startDateObj = new Date(startDate);
      if (startTime) {
        const [startHour, startMinute] = startTime.split(':').map(Number);
        startDateObj.setHours(startHour || 0, startMinute || 0, 0, 0);
      } else {
        startDateObj.setHours(0, 0, 0, 0);
      }
      
      notificationDateTime = new Date(startDateObj);
      notificationDateTime.setHours(
        notificationDateTime.getHours() - (timing.hour || 0),
        notificationDateTime.getMinutes() - (timing.minute || 0)
      );
    } else if (timingType === NOTIFICATION_TIMING_UNIT.MINUTE) {
      // 분: 일정 시작 시간에서 분 빼기
      if (timing.minute === null || timing.minute === undefined) {
        return '알림 분을 입력해 주세요';
      }
      const startDateObj = new Date(startDate);
      if (startTime) {
        const [startHour, startMinute] = startTime.split(':').map(Number);
        startDateObj.setHours(startHour || 0, startMinute || 0, 0, 0);
      } else {
        startDateObj.setHours(0, 0, 0, 0);
      }
      
      notificationDateTime = new Date(startDateObj);
      notificationDateTime.setMinutes(notificationDateTime.getMinutes() - (timing.minute || 0));
    }
    
    if (notificationDateTime && !isNaN(notificationDateTime.getTime())) {
      // 시작 시간보다 알림 시간이 크면 안 됨
      const startDateObj = new Date(startDate);
      if (startTime) {
        const [startHour, startMinute] = startTime.split(':').map(Number);
        startDateObj.setHours(startHour || 0, startMinute || 0, 0, 0);
      } else {
        startDateObj.setHours(0, 0, 0, 0);
      }
      
      if (notificationDateTime > startDateObj) {
        return '알림 시간은 시작 시간보다 클 수 없습니다';
      }
      
      // 모든 단위에서 최소 3분 전 검증
      const diffMs = startDateObj.getTime() - notificationDateTime.getTime();
      const diffMinutes = diffMs / (1000 * 60);
      if (diffMinutes < 3) {
        return '알림 시간은 시작 시간보다 최소 3분 전으로 설정해야 합니다';
      }
      
      const year = notificationDateTime.getFullYear();
      const month = String(notificationDateTime.getMonth() + 1).padStart(2, '0');
      const day = String(notificationDateTime.getDate()).padStart(2, '0');
      const hour = String(notificationDateTime.getHours()).padStart(2, '0');
      const minute = String(notificationDateTime.getMinutes()).padStart(2, '0');
      const second = String(notificationDateTime.getSeconds()).padStart(2, '0');
      return `${year}년 ${month}월 ${day}일 ${hour}시 ${minute}분 ${second}초`;
    }
  } catch (e) {
    console.warn('알림 시간 계산 실패:', e);
    return '알림 시간 계산 중 오류가 발생했습니다';
  }
  
  return '알림을 정확히 입력해 주세요';
}

/**
 * 알림 시점 설정 검증 (저장 전 검증용)
 * @param {string} timingType - 알림 시점 단위 ('none', 'DAY', 'HOUR', 'MINUTE')
 * @param {Object} timingValue - 알림 시점 값 객체 { date, hour, minute }
 * @param {string} startDate - 일정 시작일 (YYYY-MM-DD)
 * @param {string|null} startTime - 일정 시작 시간 (HH:mm) 또는 null
 * @returns {string|null} 에러 메시지 또는 null (유효한 경우)
 */
export function validateNotificationTiming(timingType, timingValue, startDate, startTime = null) {
  if (timingType === 'none') {
    return null;
  }
  
  const notificationTime = calculateNotificationTime(timingType, timingValue, startDate, startTime);
  if (!notificationTime || !notificationTime.includes('년')) {
    return notificationTime || '알림 시점을 올바르게 설정해 주세요';
  }
  
  return null;
}

