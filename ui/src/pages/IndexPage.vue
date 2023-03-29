<template>
  <lrud>
  <div>

    <div class="row q-pa-xl" />
    <div class="row justify-center">
      <div class="col-3" />
      <div class="col-6 text-h4 text-center">NOTFLIX server selection</div>
      <q-item class="col-2" />
      <q-item class="col-1">
        <q-btn round color="primary" icon="settings" :to="{ name: 'settings' }" />
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
            />
          </q-item>
        </template>
      </div>
    </div>

  </div>
  </lrud>
</template>

<style lang="scss">
@import '../css/app.scss';
.my-card {
  min-width: 250px;
  font: roboto;
  font-size: 1.4em;
}
</style>

<script setup>
import { useQuasar } from 'quasar';
import {
  onMounted,
  ref,
} from 'vue';
import { useStore } from '../store/index.js';

const store = useStore();
const $q = useQuasar();
const currentServer = ref(0);

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

onMounted(() => {
  focus();
});

</script>
