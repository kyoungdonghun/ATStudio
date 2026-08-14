import { afterEach, describe, expect, it } from 'vitest';

import {
  consumeNavigationDestinationFocus,
  focusNavigationDestination,
  requestNavigationDestinationFocus,
} from './navigationFocus';

afterEach(() => {
  document.body.replaceChildren();
});

describe('focusNavigationDestination', () => {
  it('focuses an ARIA level-1 heading inside main without leaving a tabindex', () => {
    const main = document.createElement('div');
    main.setAttribute('role', 'main');
    const heading = document.createElement('div');
    heading.setAttribute('role', 'heading');
    heading.setAttribute('aria-level', '1');
    main.append(heading);
    document.body.append(main);

    expect(focusNavigationDestination()).toBe(true);
    expect(heading).toHaveFocus();
    expect(heading).not.toHaveAttribute('tabindex');
  });

  it('falls back to main and leaves existing focus alone when no destination exists', () => {
    const main = document.createElement('main');
    document.body.append(main);

    expect(focusNavigationDestination()).toBe(true);
    expect(main).toHaveFocus();
    expect(main).not.toHaveAttribute('tabindex');

    const existingTarget = document.createElement('button');
    document.body.replaceChildren(existingTarget);
    existingTarget.focus();

    expect(focusNavigationDestination()).toBe(false);
    expect(existingTarget).toHaveFocus();
  });

  it('consumes a missing destination once without replacing existing focus', () => {
    const existingTarget = document.createElement('button');
    document.body.append(existingTarget);
    existingTarget.focus();

    requestNavigationDestinationFocus();
    consumeNavigationDestinationFocus();

    expect(existingTarget).toHaveFocus();

    const laterMain = document.createElement('main');
    document.body.append(laterMain);
    consumeNavigationDestinationFocus();

    expect(existingTarget).toHaveFocus();
    expect(laterMain).not.toHaveAttribute('tabindex');
  });
});
