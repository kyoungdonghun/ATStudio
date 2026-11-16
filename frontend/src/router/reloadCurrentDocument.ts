// A full document navigation also resets failed dynamic-import module state.
// Keep this user-triggered: automatic reloads can discard in-progress work.
export function reloadCurrentDocument(): void {
  window.location.reload();
}
