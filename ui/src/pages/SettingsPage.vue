<template>
  <lrud>
  <div @focusout="focus()" ref="el">

    <div class="row justify-center">
      <div class="col text-h4 q-pa-xl text-center">NOTFLIX server configuration</div>
    </div>

    <div class="row justify-center q-pa-sm">
      <q-item class="col-1" />
      <q-item class="col-4">Hostname</q-item>
      <q-item class="col-1">Default</q-item>
      <q-item v-if="store.state.advanced" class="col-1 q-pl-lg">SSL</q-item>
      <q-item v-if="store.state.advanced" class="col-1">Debug</q-item>
      <q-item class="col-1">Delete</q-item>
    </div>

    <template v-for="(server, index) in store.state.servers" :key="index">
      <div class="row justify-center align-center q-pa-sm">
        <q-item class="col-1" />
        <q-item class="col-4">
        <lrud keys="">
        <q-tv-input
          placeholder="Server hostname"
          hide-bottom-space
          dense
          dark
          color="white"
          label-color="white"
          :modelValue="server.hostname"
          @update:modelValue="(value) => server.hostname = value"
          :data-server="index"
          style="width: 100%"
          @focusin="currentServer = index"
        />
        </lrud>
        </q-item>
        <q-item class="col-1">
        <q-checkbox
          :model-value="server.isDefault"
          @update:modelValue="setDefault(server)"
          tabindex="0"
          dark
          class="focus-helper"
        />
        </q-item>
        <q-item class="col-1" v-if="store.state.advanced">
        <q-checkbox
          v-model="server.ssl"
          tabindex="0"
          class="focus-helper"
        />
        </q-item>
        <q-item class="col-1" v-if="store.state.advanced">
        <q-checkbox
          v-model="server.debug"
          tabindex="0"
          class="focus-helper"
        />
        </q-item>
        <q-item class="col-1">
        <q-btn
          :style="(store.state.servers.length > 1 ? {} : { opacity: 0.2 })"
          icon="delete"
          tabindex="0"
          @click="deleteServer(index)"
          class="focus-helper"
        />
        </q-item>
      </div>
    </template>

    <div class="row justify-center align-center q-pa-sm">
      <lrud keys="">
      <q-item class="col-1">
        <q-btn
          v-if="store.state.servers.length < 6"
          icon="add"
          class="focus-helper"
          tabindex="0"
          @click="addServer"
          rounded
        />
      </q-item>
      </lrud>
      <q-item class="col-2">
        <q-btn
          tabindex="0"
          class="submit-focus-helper"
          label="Submit"
          :to="{ name: 'index' }"
         />
      </q-item>
      <q-item class="col-3" />
      <q-item v-if="store.state.advanced" class="col-2" />
      <q-item class="col-1">
        <q-btn
          icon="settings_suggest"
          class="focus-helper"
          tabindex="0"
          @click="store.state.advanced = !store.state.advanced"
        />
      </q-item>
    </div>

  </div>
  </lrud>
</template>

<style lang="scss">
@import '../css/app.scss';
.settings-list {
  font: roboto;
  font-size: 1.5em;
}
.my-menu-link:focus {
  background-color: white;
  color: black;
}
.focus-helper {
  background: $blue-grey-10;
}
.focus-helper:focus, .focus-helper:hover {
  background: $blue-grey-6;
}
.submit-focus-helper {
  background: #125692;
}
.submit-focus-helper:focus, .submit-focus-helper:hover {
  background: $primary;
}
</style>

<script setup>
import {
  nextTick,
  onUnmounted,
  onMounted,
  ref,
} from 'vue';
import QTvInput from 'components/QTvInput.vue';
import { useStore } from '../store/index.js';

const store = useStore();
const currentServer = ref(0);
const el = ref(null);

function setDefault(server) {
  if (server.isDefault) {
    server.isDefault = false;
    return;
  }
  store.state.servers.forEach((s) => { s.isDefault = false; });
  server.isDefault = true;
}

function deleteServer(index) {
  if (store.state.servers.length > 1) {
    store.state.servers.splice(index, 1);
  }
}

function addServer() {
  store.state.servers.push({
    hostname: '',
    ssl: true,
    debug: false,
    isDefault: store.state.servers.length === 0,
  });
}

function focus() {
  nextTick(() => {
    if (el.value && !el.value.querySelector(':scope *:focus')) {
      // console.log('refocus!');
      let elem = document.querySelector(`[data-server="${currentServer.value}"]`);
      if (!elem) {
        elem = document.querySelector('[data-server]');
      }
      elem.focus();
    }
  });
}
onMounted(() => {
  focus();
});

onUnmounted(() => {
  // Remove empty slots before saving.
  let i = 0;
  while (i < store.state.servers.length) {
    if (i > 0 && store.state.servers[i].hostname === '') {
      store.state.servers.splice(i, 1);
    } else {
      i += 1;
    }
  }
  store.save();

  // If we have the androidApp interface, update the default server.
  if (window.androidApp) {
    const server = store.state.servers.find((s) => s.isDefault);
    if (server) {
      const proto = server.ssl ? 'https' : 'http';
      const url = `${proto}://${server.hostname}/`;
      window.androidApp.setDefaultPwaUrl(url);
      window.androidApp.setDevelEnabled(server.debug);
    } else {
      window.androidApp.setDefaultPwaUrl('');
      window.androidApp.setDevelEnabled(false);
    }
  }
});
</script>
