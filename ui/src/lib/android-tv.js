//
// Hot-patch some stuff in quasar to work with Android TV.
//
import { useQuasar } from 'quasar';

// For Android TV, remove touch support, unmark platform as mobile,
// mark platform as desktop again, and also set a 'tv' platform property.
//
// This should be called as early as possible. Unfortunately we can't
// call this from a boot file, since at that point the quasar object
// is not yet available.
export function fixQuasarPlatform() {
  const quasar = useQuasar();
  if (quasar.platform.userAgent.match(/AOSP TV|Chromecast|Android TV/)
      || (quasar.platform.is.android && !quasar.platform.has.touch)) {
    // eslint-disable-next-line
    console.log('Android TV detected, patching quasar.platform');
    quasar.platform.has.touch = false;
    delete quasar.platform.is.mobile;
    quasar.platform.is.desktop = true;
    quasar.platform.is.tv = true;
  }
}

// On Android TV, Quasar has initialized the CSS as if this was
// a mobile device. Fix that. Call this when the first component
// gets mounted.
export function fixQuasarCss() {
  const quasar = useQuasar();
  const pf = quasar.platform;
  const cl = document.body.classList;
  // remove mobile and touch classes.
  if (cl.contains('mobile') && !pf.is.mobile) {
    cl.remove('mobile');
  }
  if (cl.contains('touch') && !pf.has.touch) {
    cl.remove('touch');
  }
  // add desktop class.
  if (!cl.contains('desktop') && pf.is.desktop) {
    cl.add('desktop');
  }
}
