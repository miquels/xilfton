<script>

/* eslint no-underscore-dangle: 0 */
/* eslint vue/multi-word-component-names: 0 */
/* eslint no-restricted-syntax: 0 */
/* eslint vue/no-setup-props-destructure: 0 */
/* eslint no-console: 0 */

import {
  withDirectives,
} from 'vue';
import { useQuasar } from 'quasar';

// If there is a label attached to the element, use that.
function labelFor(el) {
  let outerEl = el;
  if (el.id) {
    const { id } = el;
    const label = document.querySelector(`[for="${id}"]`);
    if (label) {
      outerEl = label;
    }
  }
  return outerEl;
}

function toNumber(val, dfl) {
  const r = parseFloat(val);
  return Number.isNaN(r) ? dfl : val;
}

// A FocussedElem is the element that is now focussed.
//
// We figure out the shape of the element, its center point,
// and the areas it can 'see' in all directions.
// Based on that, given a set of other elements, we can
// figure out which one is the closest by in a certain direction.
class FocussedElem {
  constructor(el) {
    this.el = el;

    // For calculating our starting position, we might need to look
    // at any parent elements that have center-x or center-y set.
    let rect = labelFor(el).getBoundingClientRect();
    for (let target = el; target; target = target.parentElement) {
      if (target.dataset.__centerX != null || target.dataset.__centerY != null) {
        el = target;
        rect = target.getBoundingClientRect();
        break;
      }
    }

    let { width } = rect;
    let { height } = rect;
    let { left } = rect;
    let { top } = rect;
    if (width < 16) {
      left -= (16 - width);
      width = 16;
    }
    if (height < 16) {
      top -= (16 - height);
      height = 16;
    }
    this.x = left + width * (toNumber(el.dataset.__centerX, 50) / 100);
    this.y = top + height * (toNumber(el.dataset.__centerY, 50) / 100);
    this.factor = height / width;
  }

  // Spational navigation.
  // Calculate the distance to 'rect' in a certain direction.
  distance(rect, dir) {
    // Start by finding out to which edge or corner of 'rect'
    // we need to measure the distance to.
    let x2 = this.x;
    let y2 = this.y;
    let bias = 1;

    if (dir === 'left' || dir === 'right') {
      if (dir === 'left') {
        if (rect.right > this.x) return null;
        x2 = rect.right;
      } else {
        if (rect.left < this.x) return null;
        x2 = rect.left;
      }
      if (rect.bottom < this.y) {
        y2 = rect.bottom;
      } else if (rect.top > this.y) {
        y2 = rect.top;
      }
      // Make sure that elemens that are further away in the
      // Y direction are made more "expensive" to reach.
      bias += Math.abs(this.y - y2) / 20;
    }

    if (dir === 'up' || dir === 'down') {
      if (dir === 'up') {
        if (rect.bottom > this.y) return null;
        y2 = rect.bottom;
      } else {
        if (rect.top < this.y) return null;
        y2 = rect.top;
      }
      if (rect.right < this.x) {
        x2 = rect.right;
      } else if (rect.left > this.x) {
        x2 = rect.left;
      }
      // Make sure that elemens that are further away in the
      // X direction are made more "expensive" to reach.
      bias += Math.abs(this.x - x2) / 200;
    }

    // Distance.
    const a = Math.abs(this.x - x2);
    const b = Math.abs(this.y - y2);
    return Math.sqrt(a ** 2 + b ** 2) * bias;
  }

  // See if 'el' is left / right / up / down relative to 'this'.
  // then update nearest if 'el' is the nearest element in that direction.
  update_nearest(dir, el) {
    const r2 = labelFor(el).getBoundingClientRect();

    const dist = this.distance(r2, dir);
    if (dist && (!this.dist || dist < this.dist)) {
      this.to = el;
      this.dist = dist;
    }
  }

  // Handle keyboard navigation from 'this' to another focusable element.
  navigate(ev, dir, elems) {
    // Loop over all focusable child elements and find the nearest.
    for (const e2 of elems) {
      if (this.el !== e2) {
        const tabindex = e2.getAttribute('tabindex');
        if (tabindex !== '-1' || e2.tagName !== 'INPUT') {
          this.update_nearest(dir, e2);
        }
      }
    }

    // Now we _have_ the nearest!
    // console.log('nearest', this.to);
    return this.to;
  }
}

const keymap = {
  ArrowLeft: ['L', 'left'],
  ArrowRight: ['R', 'right'],
  ArrowUp: ['U', 'up'],
  ArrowDown: ['D', 'down'],
  Enter: ['E', 'enter'],
};

let hasMenuOpen = false;

export default {
  name: 'Lrud',
  props: {
    'steal-keys-outside': Boolean,
    'no-nav-inside': Boolean,
    'no-scroll-into-view': Boolean,
    'attach-to-parent': Boolean,
    focus: Boolean,
    'filter-keys': String,
    tabindex: [String, Number],
    'center-x': [String, Number],
    'center-y': [String, Number],
  },
  setup(props, { slots }) {
    const { stealKeysOutside } = props;
    const { noNavInside } = props;
    const { noScrollIntoView } = props;
    const { attachToParent } = props;
    const focusElement = props.focus;
    const { filterKeys } = props;
    let { tabindex } = props;
    const { centerX } = props;
    const { centerY } = props;

    const keys = props.keys || 'LRUDE';
    let hasMenu = false;
    let handler;

    if (!tabindex && focusElement) {
      tabindex = '-1';
    }
    const quasar = useQuasar();

    // custom directive.
    const directive = {

      // Register keyboard handler.
      mounted(el) {
        handler = (ev) => {
          // console.log('keypress:', ev);

          // The 'back' button on android tv remotes has been mapped to
          // escape. So if we're sure that this is an actual keypress
          // and it's not being handled by anything else, go back.
          if (quasar.platform.is.tv
              && (ev.eventPhase === Event.AT_TARGET
               || ev.eventPhase === Event.BUBBLING_PHASE
               || hasMenu)
              && ev.key === 'Escape'
              && !hasMenuOpen
              && ev.isTrusted
              && !ev.defaultPrevented) {
            ev.stopPropagation();
            window.history.go(-1);
          }

          // If the 'filter-keys' prop was set, and this key matches that
          // key, stop the propagation and fire an alternative event.
          if (filterKeys && ev.trusted) {
            const m = keymap[ev.key];
            if (m && filterKeys.indexOf(m[0])) {
              const keydownLrud = new KeyboardEvent('keydown_lrud', ev);
              ev.stopPropagation();
              ev.target.dispatchEvent(keydownLrud, { bubbles: true });
              return;
            }
          }

          // for LRUD/Enter never choose the default action.
          const m = keymap[ev.key];
          if (m && !ev.defaultPrevented) {
            ev.preventDefault();
          }

          // Check if we should handle this key.
          if (!m || keys.indexOf(m[0]) < 0) {
            return;
          }
          const key = m[1];

          // Enter is click.
          if (key === 'enter') {
            if (!noNavInside) {
              // console.log('enter - click eventphase is', ev.eventPhase);
              // console.log('enter - event is', ev);
              ev.target.click();
              ev.stopPropagation();
            } else {
              // console.log('enter - ignoring');
            }
            return;
          }

          if (hasMenu && hasMenuOpen && (key === 'up' || key === 'down')) {
            // console.log('hasMenu up/down');
            return;
          }

          // If this is a "tabindex=-1" target, which means a clickable target
          // which was clicked, bubble down to find an actual focusable element.
          let { target } = ev;
          let improbableTarget;
          if (target.matches('[tabindex="-1"]')) {
            improbableTarget = target;
            target = target.querySelector(':scope :is([tabindex="0"], button, input, a[href])');
          }

          // If we didn't find that, try bubbling up.
          if (!target) {
            target = ev.target;
            while (target && !target.matches(':is([tabindex="0"], button, input, a[href])')) {
              target = target.parentElement;
            }
          }

          // Did we fail?
          if (!target && !improbableTarget) {
            console.log('lrud: target not found from', ev.target);
            return;
          }
          target ||= improbableTarget;

          // Find all focusable child elements in this scope.
          const elems = Array.from(
            el.querySelectorAll(':scope :is([tabindex="0"], button, input, a[href])'),
          );

          // Find the nearest in the direction of the arrowkey pressed.
          const fe = new FocussedElem(target);
          const dest = fe.navigate(ev, key, elems);

          // Found nearest, focus and stop.
          if (dest && !noNavInside) {
            ev.stopPropagation();
            // QSelect doesn't close the menu when it loses focus,
            // so fire an 'Escape' keyup event if the menu is open.
            if (hasMenuOpen) {
              const esc = new KeyboardEvent('keyup', {
                key: 'Escape',
                code: 'Escape',
                keyCode: 27,
              });
              ev.target.dispatchEvent(esc, { bubbles: true });
            }
            dest.focus({ preventScroll: true });
            if (!noScrollIntoView) {
              dest.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
            return;
          }

          // There's no next focussable element, and stealKeysOutside is set.
          //
          // Do not propagate the keyboard event to the inner elements
          // (we're in the capturing phase!) but dispatch a similar event from
          // the target, so that an outer <Lrud> can catch the event.
          if (stealKeysOutside && !dest) {
            const keydownLrud = new KeyboardEvent('keydown_lrud', ev);
            ev.stopPropagation();
            ev.target.dispatchEvent(keydownLrud, { bubbles: true });
          }

          // END OF HANDLER
        };

        if (attachToParent) {
          el = el.parentElement;
        }
        if (tabindex !== undefined) {
          el.setAttribute('tabindex', `${tabindex}`);
        }
        el.addEventListener('keydown', handler, stealKeysOutside);
        if (!stealKeysOutside) el.addEventListener('keydown_lrud', handler);

        if (focusElement) {
          el.focus();
        }

        if (centerX !== undefined) el.dataset.__centerX = centerX;
        if (centerY !== undefined) el.dataset.__centerY = centerY;
      },

      // Unregister keyboard handler.
      unmounted(el) {
        el.removeEventListener('keydown', handler, stealKeysOutside);
        el.removeEventListener('keydown_lrud', handler);
      },
    };

    // return the node in the first slot, with custom directive applied.
    return () => {
      let node = slots.default && slots.default()[0];
      if (node) {
        node = withDirectives(node, [
          [directive],
        ]);
        if (typeof node.type === 'object' && node.type.name === 'QSelect') {
          hasMenu = true;
          node.props.onPopupShow = () => { hasMenuOpen = true; };
          node.props.onPopupHide = () => { hasMenuOpen = false; };
        }
      }
      return node;
    };
  },
};
</script>
