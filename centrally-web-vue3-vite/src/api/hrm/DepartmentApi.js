import { hrmApi } from '@/api/apiConfig';
import { FUNCTIONS } from '@/config/activityConfig';

const receiptFlag = () =>
  import.meta.env.VITE_FEATURE_RECEIPT_ENABLED === 'true' ? '?syncReceipt=true' : '';

export default {
  // [GET] 전체 부서 목록 조회
  async getDepartments() {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.DEPT_LIST) + "; path=/";
    return await hrmApi.get('/departments');
  },
  // [GET] 팀 ID로 부서 조회
  async getDepartmentByTeamId(teamId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.DEPT_BY_TEAM) + "; path=/";
    return await hrmApi.get(`/departments/${teamId}`);
  },
  // [POST] 부서 생성
  async createDepartment(departmentName) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.DEPT_CREATE) + "; path=/";
    return hrmApi.post('/departments', { departmentName });
  },

  // [PATCH] 부서 수정
  async updateDepartment(departmentId, departmentName) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.DEPT_UPDATE) + "; path=/";
    return hrmApi.patch(`/departments/${departmentId}${receiptFlag()}`, { departmentName: departmentName });
  },

  // [DELETE] 부서 삭제
  async deleteDepartment(departmentId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.DEPT_DELETE) + "; path=/";
    return hrmApi.delete(`/departments/${departmentId}${receiptFlag()}`);
  },

  // [POST] 팀 생성
  async createTeam(departmentId, teamName) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.TEAM_CREATE) + "; path=/";
    return hrmApi.post(`/departments/${departmentId}/teams`, { teamName });
  },

  // [PATCH] 팀 수정
  async updateTeam(departmentId, teamId, teamName) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.TEAM_UPDATE) + "; path=/";
    return hrmApi.patch(`/departments/${departmentId}/teams/${teamId}${receiptFlag()}`,{ teamName });
  },

  // [DELETE] 팀 삭제
  async deleteTeam(departmentId, teamId) {
    document.cookie = "X-Func-Vue=" + encodeURIComponent(FUNCTIONS.HRM.TEAM_DELETE) + "; path=/";
    return hrmApi.delete(`/departments/${departmentId}/teams/${teamId}${receiptFlag()}`);
  }
};
