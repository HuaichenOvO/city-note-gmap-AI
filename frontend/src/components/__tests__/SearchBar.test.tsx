import React from 'react';
import { render, screen } from '@testing-library/react';
import { vi, describe, test, expect, beforeEach } from 'vitest';

// Mock the Google Maps libraries
const mockMap = {
  panTo: vi.fn(),
  setZoom: vi.fn(),
};

const mockAutocomplete = {
  addListener: vi.fn(),
  getPlace: vi.fn(),
};

const mockPlaces = {
  Autocomplete: vi.fn(() => mockAutocomplete),
};

vi.mock('@vis.gl/react-google-maps', () => ({
  useMap: () => mockMap,
  useMapsLibrary: () => mockPlaces,
}));

import SearchBar from '../SearchBar';

describe('SearchBar Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('renders search input', () => {
    render(<SearchBar />);
    
    const searchInput = screen.getByPlaceholderText('Search for a city, state, or county...');
    expect(searchInput).toBeInTheDocument();
  });

  test('renders with correct styling classes', () => {
    render(<SearchBar />);
    
    const searchInput = screen.getByPlaceholderText('Search for a city, state, or county...');
    expect(searchInput).toHaveClass('w-full', 'px-4', 'py-2.5', 'rounded-lg');
  });

  test('renders in correct position', () => {
    render(<SearchBar />);
    
    const container = screen.getByPlaceholderText('Search for a city, state, or county...').closest('div');
    expect(container).toHaveClass('absolute', 'top-2.5', 'left-1/2');
  });

  test('initializes autocomplete when map and places are available', () => {
    render(<SearchBar />);
    
    expect(mockPlaces.Autocomplete).toHaveBeenCalledWith(
      expect.any(HTMLInputElement),
      {
        types: [
          'locality',
          'administrative_area_level_1',
          'administrative_area_level_2',
        ],
        fields: ['geometry', 'name', 'place_id'],
      }
    );
  });

  test('adds place_changed listener to autocomplete', () => {
    render(<SearchBar />);
    
    expect(mockAutocomplete.addListener).toHaveBeenCalledWith('place_changed', expect.any(Function));
  });
}); 