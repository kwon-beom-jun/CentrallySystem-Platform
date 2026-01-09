import { defineConfig } from 'vite';
import vue      from '@vitejs/plugin-vue';
import vuetify  from 'vite-plugin-vuetify';
// 롤업 번들 시각화 플러그인 (stats.html 생성)
import { visualizer } from 'rollup-plugin-visualizer';
// gzip/brotli 파일을 추가 생성하여 배포 트래픽 절감
import compression from 'vite-plugin-compression';

/**
  CORS 문제를 개발 단계에서 “미리” 없애기 위해
  Vue CLI 시절에도 devServer.proxy 로 백엔드(8080) 요청을 우회하는 것이 일반적이지만,
  원본 vue.config.js 코드가 없어서 예시용으로 기본 프록시 규칙을 넣어 둔 것
  백엔드와 포트가 다르면 브라우저 CORS 에러가 나므로 ↓ 와 같은 프록시가 필요

  백엔드가 CORS 헤더를 제대로 내보내면 Vite의 server.proxy 를 써도 되고, 안 써도 됨
  단, 쿠키(JWT · 세션)처럼 withCredentials를 쓰는 경우와 로컬 테스트용 도메인(예: localhost:3000)이 
  CORS 화이트리스트에 포함되지 않은 경우는 프록시를 두는 편이 훨씬 간단
 */
export default defineConfig(({ command }) => ({
  // plugins 배열 – 개발 서버와 배포 빌드에서의 차이를 명시적으로 구분
  plugins: [
    vue(),                           // 기본 Vue 플러그인
    vuetify({ autoImport: true }),   // Vuetify 자동 임포트

    // 아래 두 플러그인은 *빌드(`vite build`) 시에만* 동작하도록 조건부로 포함
    //    dev 서버에선 필요 없고, 빌드 시간만 증가시키므로 제외하는 편이 좋음
    command === 'build' && visualizer({ gzipSize: true, brotliSize: true, open: false }), // 번들 분석
    command === 'build' && compression(),                                                // gzip & brotli 압축 파일 생성
  ].filter(Boolean), // `false` 값은 제거하여 Vite에 전달되지 않도록 함
  resolve: { alias: { '@': '/src' } },
  server : {
    port : 3000,
    proxy: {
      '/api'     : 'http://localhost:8080',
      '/auth'    : 'http://localhost:8080',
      '/hrm'     : 'http://localhost:8080',
      '/receipt' : 'http://localhost:8080',
      '/info'    : 'http://localhost:8080'
    }
  },
  build: { outDir: 'dist' }
}));
