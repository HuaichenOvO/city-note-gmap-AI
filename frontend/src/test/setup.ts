import '@testing-library/jest-dom';
import { vi, expect } from 'vitest';

// Extend expect with jest-dom matchers
declare module 'vitest' {
  interface Assertion<T = any> {
    toBeInTheDocument(): T;
    toHaveClass(...className: string[]): T;
    toHaveTextContent(text: string | RegExp): T;
    toBeVisible(): T;
    toBeDisabled(): T;
    toBeEnabled(): T;
    toHaveValue(value: string | number | string[]): T;
    toHaveAttribute(attr: string, value?: string): T;
    toHaveStyle(css: string | object): T;
    toBeEmpty(): T;
    toBeEmptyDOMElement(): T;
    toHaveDisplayValue(value: string | string[]): T;
    toBePartiallyChecked(): T;
    toBeChecked(): T;
    toHaveDescription(text?: string | RegExp): T;
    toHaveAccessibleDescription(expectedAccessibleDescription?: string | RegExp): T;
    toHaveAccessibleName(expectedAccessibleName?: string | RegExp): T;
    toHaveErrorMessage(text?: string | RegExp): T;
    toHaveFormValues(expectedValues: Record<string, any>): T;
    toBeRequired(): T;
    toBeValid(): T;
    toBeInvalid(): T;
    toHaveFocus(): T;
  }
  
  interface AsymmetricMatchers {
    any(constructor: any): any;
  }
}

// Mock Google Maps API
global.google = {
  maps: {
    Map: vi.fn(),
    Marker: vi.fn(),
    InfoWindow: vi.fn(),
    LatLng: vi.fn(),
    places: {
      Autocomplete: vi.fn(),
    },
  },
} as any;

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(), // deprecated
    removeListener: vi.fn(), // deprecated
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
}); 