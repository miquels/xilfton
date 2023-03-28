import { boot } from 'quasar/wrappers';
import Lrud from '../components/Lrud.vue';
import { Autofocus, AutofocusInit } from '../directives/Autofocus.js';

export default boot(async ({ app }) => {
  // eslint-disable-next-line
  app.component('lrud', Lrud);
  app.directive('autofocus', Autofocus);
  AutofocusInit();
});
