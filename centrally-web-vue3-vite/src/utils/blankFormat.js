// Utility functions to map department/team blank <-> display "미지정"

export const toDeptTeamDisplay = v => {
  return v === null || v === undefined || v === '' ? '미지정' : v;
};

export const toDeptTeamPayload = v => {
  return (v === null || v === undefined || v === '' || v === '미지정') ? null : v;
}; 