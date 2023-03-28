<template>
  <lrud>
  <div>

    <div class="row justify-center">
      <div class="col text-h4 q-pa-xl text-center">NOTFLIX server configuration</div>
    </div>

    <div class="row justify-center">
      <div class="col-2" />
      <div class="col q-ma-md settings-list">
        <div class="row q-pa-sm">
          <q-item class="col-1" />
          <q-item class="col-4">Hostname</q-item>
          <q-item class="col-1">Default</q-item>
          <q-item class="col-1">SSL</q-item>
          <q-item class="col-1">Debug</q-item>
          <q-item class="col-1">Delete</q-item>
        </div>

        <template v-for="(server, index) in servers" :key="index">
          <div class="row q-pa-sm">
            <q-item class="col-1">{{ index + 1 }}</q-item>
            <q-input
              placeholder="Server hostname"
              hide-bottom-space
              dense
              dark
              color="white"
              label-color="white"
              :modelValue="server.hostname"
              @update:modelValue="(value) => $emit('update:hostname', {index, value})"
              data-server="1"
              class="col-4"
            />
            <q-checkbox
              class="col-1 q-pl-lg"
              :model-value="server.isDefault"
              @update:modelValue="setDefault(server)"
              tabindex="0"
            />
            <q-checkbox
              class="col-1 q-pl-lg"
              v-model="server.ssl"
              tabindex="0"
            />
            <q-checkbox
              class="col-1 q-pl-lg"
              v-model="server.debug"
              tabindex="0"
            />
            <q-btn
              icon="delete"
              class="col-1"
              tabindex="0"
              @click="doDelete(index)"
            />
          </div>
        </template>
      </div>
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
</style>

<script setup>
import {
  onMounted,
  reactive,
} from 'vue';

const servers = reactive([
  {
    hostname: 'nf.high5.nl',
    ssl: true,
    debug: false,
    isDefault: true,
  },
  {
    hostname: '192.168.178.24:8080',
    ssl: true,
    debug: true,
    isDefault: false,
  },
  {
    hostname: 'notflix.z42.nl',
    ssl: true,
    debug: false,
    isDefault: false,
  },
]);

function focus() {
  const elem = document.querySelector('[data-server]');
  // eslint-disable-next-line
  console.log('refocus ', elem);
  elem.focus();
}

function setDefault(server) {
  if (server.isDefault) {
    server.isDefault = false;
    return;
  }
  servers.forEach((s) => { s.isDefault = false; });
  server.isDefault = true;
}

function doDelete(index) {
  servers.splice(index, 1);
}

onMounted(() => {
  focus();
});

</script>
