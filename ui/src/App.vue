<template>
  <router-view />
</template>

<script>
import { defineComponent } from 'vue';
import { useQuasar } from 'quasar';
import { fixQuasarPlatform, fixQuasarCss } from 'src/lib/android-tv.js';

export default defineComponent({
  name: 'App',
  setup() {
    const $q = useQuasar();
    $q.dark.set(true);
    fixQuasarPlatform();
  },
  mounted() {
    fixQuasarCss();
  },
});
</script>
