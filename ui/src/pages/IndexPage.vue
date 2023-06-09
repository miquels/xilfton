<template>
  <lrud>
  <div class="index-page" @keydown="onKeyDown($event)">

    <div class="row q-pa-xl" />
    <div class="row justify-center">
      <div class="col-3" />
      <div class="col-6 text-h4 text-center">NOTFLIX server selection</div>
      <q-item class="col-2" />
      <q-item class="col-1">
        <q-btn
          round
          color="primary"
          icon="settings"
          @click="router.replace({ name: 'settings' })" />
      </q-item>
    </div>

    <div class="row justify-center">
      <div class="col-6 q-ma-md">
        <template v-for="(server, index) in store.state.servers" :key="index">
          <q-item>
            <q-btn
              clickable
              v-ripple
              dark
              @click="selectedServer(index)"
              class="my-menu-link"
              :data-server="index"
              tabindex="0"
              :label="server.hostname"
              color="primary"
              rounded
              style="width: 100%"
              v-autofocus="{vIf: index === 0}"
            />
          </q-item>
        </template>
      </div>
    </div>

    <div class="row index-bottom">
      <div class="col text-center">
        You can always get back to this menu by pressing the back button 7 times quickly.
      </div>
    </div>

  </div>
  </lrud>
</template>

<style lang="scss">
@import '../css/app.scss';
.index-page {
  position: relative;
  height: 100%;
  width: 100%;
}
.index-bottom {
  position: absolute;
  left: 0px;
  bottom: 40px;
  right: 0px;
}
</style>

<script setup>
import { useQuasar } from 'quasar';
import { useRouter } from 'vue-router';
import {
  onMounted,
  ref,
} from 'vue';
import { useStore } from '../store/index.js';

const store = useStore();
const $q = useQuasar();
const router = useRouter();
const currentServer = ref(0);
const bootTime = Date.now();

function focus() {
  let elem = document.querySelector(`[data-server="${currentServer.value}"]`);
  if (!elem) {
    elem = document.querySelector('[data-server]');
  }
  elem.focus();
}

function selectedServer(index) {
  const server = store.state.servers[index];
  const proto = server.ssl ? 'https' : 'http';
  const url = `${proto}://${server.hostname}/`;
  if (window.androidApp) {
    window.androidApp.setDevelEnabledOnce(server.debug);
  }
  if ($q.platform.is.tv) {
    window.location.replace(url);
  } else {
    window.location.href = url;
  }
}

function onKeyDown(ev) {
  if (ev.key === 'Escape' && window.androidApp) {
    const now = Date.now();
    if (now > bootTime + 2000) {
      window.androidApp.exitApplication();
    }
  }
}

onMounted(() => {
  focus();
});
</script>
