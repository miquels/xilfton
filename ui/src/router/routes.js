// When run from android filesystem.
const baseUrl = '';
/*
if (window.location.href.startsWith('file:///android_asset/ui/')) {
  baseUrl = 'file:///android_asset/ui';
}
*/

const routes = [
  {
    name: 'index',
    path: `${baseUrl}/`,
    component: () => import('pages/IndexPage.vue'),
  },
  {
    name: 'settings',
    path: `${baseUrl}/settings`,
    component: () => import('pages/SettingsPage.vue'),
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
