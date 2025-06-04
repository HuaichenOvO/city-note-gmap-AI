import React, { useEffect, useRef } from 'react';
import { useMap, useMapsLibrary } from '@vis.gl/react-google-maps';

interface SearchBarProps {
  onPlaceSelected?: (place: google.maps.places.PlaceResult) => void;
}

const SearchBar: React.FC<SearchBarProps> = ({ onPlaceSelected }) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const map = useMap();
  const places = useMapsLibrary('places');

  useEffect(() => {
    if (!map || !inputRef.current || !places) return;

    const autocomplete = new places.Autocomplete(inputRef.current, {
      types: [
        'locality',
        'administrative_area_level_1',
        'administrative_area_level_2',
      ],
      fields: ['geometry', 'name', 'place_id'],
    });

    autocomplete.addListener('place_changed', () => {
      const place = autocomplete.getPlace();
      if (place.geometry?.location) {
        map.panTo(place.geometry.location);
        map.setZoom(9);
        onPlaceSelected?.(place);
      }
    });
  }, [map, places, onPlaceSelected]);

  return (
    <div className="absolute top-2.5 left-1/2 transform -translate-x-1/2 z-50 w-72">
      <input
        ref={inputRef}
        type="text"
        placeholder="Search for a city, state, or county..."
        className="w-full px-4 py-2.5 rounded-lg border border-gray-300 
                 bg-white shadow-md focus:outline-none focus:ring-2 
                 focus:ring-indigo-500 focus:border-transparent
                 placeholder-gray-400 text-gray-700"
      />
    </div>
  );
};

export default SearchBar;
