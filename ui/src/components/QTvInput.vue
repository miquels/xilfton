// This component makes a QInput component "edit clickable" on TVs.
//
// Normally when an input element gets the focus, devices with a virtual
// keyboard will pop up the keyboard right  away. That is bad for
// navigation on TVs. This component will prevent that, you'll
// have to "click" or "enter" first.

<template>
  <q-input
      ref="input"
      @click="onClick"
      @focusout="onFocusOut"
      @keydown="onKeyDown"
  />
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useQuasar } from 'quasar';

const $q = useQuasar();
const isTv = $q.platform.is.tv;

const input = ref(null);
let inputElem = null;
let readOnly = false;

onMounted(() => {
  inputElem = input.value.$el.querySelector(':scope INPUT');
  inputElem.setAttribute('autocapitalize', 'off');
  inputElem.setAttribute('autocomplete', 'off');
  if (isTv) {
    inputElem.setAttribute('readonly', 'true');
    readOnly = true;
  }
});

function onClick() {
  if (isTv) {
    if (readOnly) {
      inputElem.removeAttribute('readonly');
      inputElem.focus();
    } else {
      // Need this, otherwise there is a race condition where
      // the new value is not $emit'ted.
      setTimeout(() => {
        inputElem.setAttribute('readonly', 'true');
      }, 0);
    }
    readOnly = !readOnly;
  }
}

function onFocusOut() {
  if (isTv) {
    inputElem.setAttribute('readonly', 'true');
    readOnly = true;
  }
}

function onKeyDown(ev) {
  if (!readOnly && (ev.key === 'ArrowLeft' || ev.key === 'ArrowRight')) {
    ev.stopPropagation();
  }
}
</script>
