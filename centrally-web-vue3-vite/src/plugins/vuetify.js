// src/plugins/vuetify.js
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import '@mdi/font/css/materialdesignicons.css';
import { VTreeview }     from 'vuetify/labs/components';
import { md3 }            from 'vuetify/blueprints';

export default createVuetify({
  components: {
    ...components,        // 기본 컴포넌트
    VTreeview,            // 2-단 트리뷰
  },
  blueprint : md3,
  icons     : { 
    defaultSet: 'mdi' 
  },
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          // primary: '#1976D2',
          primary: '#004497',
          secondary: '#424242',
          accent: '#82B1FF',
          error: '#FF5252',
          info: '#2196F3',
          success: '#4CAF50',
          warning: '#FB8C00'
        },
      },
      
    },
  },
})

