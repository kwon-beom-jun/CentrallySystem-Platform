// .storybook/preview.js
import { setup } from '@storybook/vue3-vite';
import BootstrapVue3 from 'bootstrap-vue-3';                // 이미 deps에 있음

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue-3/dist/bootstrap-vue-3.css';

import '../src/assets/styles/common.css'; // 공통 CSS 임포트
import '../src/assets/styles/card-ticket.css'; // 공통 CSS 임포트
import '../src/assets/styles/card-default.css'; // 공통 CSS 임포트
import '../src/assets/styles/preview-modal.css'; // 미리보기 CSS 임포트

// 필요하면 전역 공통 CSS도 같이 import
// import '../src/assets/global.css';

setup(app => {
  app.use(BootstrapVue3);
});
