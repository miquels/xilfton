import { reactive } from 'vue';

const defaultState = () => ({
  servers: [
    {
      hostname: 'nf.high5.nl',
      ssl: true,
      debug: false,
      isDefault: true,
    },
  ],
  advanced: false,
});

const backingState = {};
const state = reactive(backingState);
let initialized = false;

function clearState() {
  // eslint-disable-next-line
  for (let key of Object.keys(state)) {
    delete state[key];
  }
}

function loadState() {
  let data = localStorage.getItem('settings');
  if (data) {
    data = JSON.parse(data);
  } else {
    data = defaultState();
  }
  Object.assign(state, data);
}

const store = {
  state,
  commit() {
    localStorage.setItem('settings', JSON.stringify(backingState));
  },
  rollback() {
    clearState();
    loadState();
  },
};

export function useStore() {
  if (!initialized) {
    loadState();
    initialized = true;
  }
  return store;
}
