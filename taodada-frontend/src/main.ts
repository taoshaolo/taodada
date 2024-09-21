import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import ArcoVue from "@arco-design/web-vue";
import "@arco-design/web-vue/dist/arco.css";
// 引入全局权限校验拦截器
import "@/access";

const pinia = createPinia();

createApp(App).use(pinia).use(ArcoVue).use(router).mount("#app");
