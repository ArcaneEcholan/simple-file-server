import '@/router/componentHooks'

import Vue from 'vue';

import Cookies from 'js-cookie';

import 'normalize.css/normalize.css'; // a modern alternative to CSS resets

import Element from 'element-ui';
import './styles/element-variables.scss';
import enLang from 'element-ui/lib/locale/lang/en'; // 如果使用中文语言包请默认支持，无需额外引入，请删除该依赖

import '@/styles/index.scss'; // global css

import App from "@/App.vue";
import store from './store';
import router from './router';

import './icons'; // icon
import './permission'; // permission control
import './utils/error-log'; // error log

import * as filters from './filters'; // global filters

Vue.use(Element, {
    size: Cookies.get('size') || 'medium', // set element-ui default size
    locale: enLang, // 如果使用中文，无需设置，请删除
});

// register global utility filters
Object.keys(filters).forEach((key) => {
    Vue.filter(key, filters[key]);
});

// //////////////////////////// font-awesome

/* import the fontawesome core */
import { library } from '@fortawesome/fontawesome-svg-core';

/* import font awesome icon component */
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

/* import specific icons */
import { faUserSecret } from '@fortawesome/free-solid-svg-icons';
import { faArrowUp } from '@fortawesome/free-solid-svg-icons';
import { faArrowDown } from '@fortawesome/free-solid-svg-icons';
import { faMinus } from '@fortawesome/free-solid-svg-icons';

/* add icons to the library */
library.add(faUserSecret, faArrowUp, faArrowDown, faMinus);

/* add font awesome icon component */
Vue.component('FontAwesomeIcon', FontAwesomeIcon);

// //////////////////////////// font-awesome

Vue.config.productionTip = false;

new Vue({
    el: '#app',
    router,
    store,
    render: (h) => h(App),
});
