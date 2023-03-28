<template>
  <lrud>
  <div>

    <div class="row justify-center">
      <div class="col text-h4 q-pa-xl text-center">NOTFLIX server selection</div>
    </div>

    <div class="row justify-center">
      <q-card dark bordered class="col-6 q-ma-md bg-grey-10 my-card">
        <q-list bordered padding class="rounded-borders text-primary">
          <template v-for="(server, index) in servers" :key="index">
            <q-separator v-if="index > 0" dark/>
            <q-item
              clickable
              v-ripple
              dark
              @click="selectedServer(index)"
              class="my-menu-link"
              :data-server="index"
              tabindex="0"
            >
              <q-item-section class="text-center">{{ server }}</q-item-section>
            </q-item>
          </template>
        </q-list>
      </q-card>
    </div>

  </div>
  </lrud>
</template>

<style lang="scss">
@import '../css/app.scss';
.my-card {
  min-width: 250px;
  font: roboto;
  font-size: 1.3em;
}
.my-menu-link:focus {
  background-color: white;
  color: black;
}
</style>

<script setup>
import {
  onMounted,
  ref,
} from 'vue';

const servers = ['nf.high5.nl', '192.168.178.24:8080', 'notflix.z42.nl'];
const currentServer = ref(0);

function focus() {
  let elem = document.querySelector(`[data-server="${currentServer.value}"]`);
  if (!elem) {
    elem = document.querySelector('[data-server]');
  }
  // eslint-disable-next-line
  console.log('refocus ', elem);
  elem.focus();
}

function selectedServer(index) {
  const isTv = navigator.userAgent.match(/Android/) && !('ontouchstart' in window);
  const url = `https://${servers[index]}`;
  if (isTv) {
    window.location.replace(url);
  } else {
    window.location.href = url;
  }
}

onMounted(() => {
  focus();
});

</script>
