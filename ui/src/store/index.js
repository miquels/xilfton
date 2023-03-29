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

let backingState;

const store = {
  save() {
    localStorage.setItem('settings', JSON.stringify(backingState));
  },
};

export function useStore() {
  if (!backingState) {
    const state = localStorage.getItem('settings');
    if (state) {
      backingState = JSON.parse(state);
    } else {
      backingState = defaultState();
    }
    store.state = reactive(backingState);
  }
  return store;
}
