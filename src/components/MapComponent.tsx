import React, { useContext } from 'react';

import {
  APIProvider,
  Map,
  MapCameraChangedEvent,
} from '@vis.gl/react-google-maps';

import { eventContext } from '../context/eventContext';

import Clickableboundry from './Clickableboundry';
import SearchBar from './SearchBar';

export type MapProps = {
  GMAP_API_KEY: string;
  GMAP_MAP_ID: string;
  GEO_JSON_URL: string;
  onCountySelect: (county: string) => void;
  onBoundaryDrag: () => void;
};

export const MapComponent: React.FC<MapProps> = (props: MapProps) => {

  const { handler } = useContext(eventContext);

  if (!props.GMAP_API_KEY) {
    console.error('Google Maps API Key is not defined.');
    return <div>Error: Google Maps API Key missing.</div>;
  }

  const handleCountySelect = (county: string) => {
    props.onCountySelect(county);
    handler.onCountyClick(county);
  }

  // TODO: 增加“放大/缩小时不允许渲染 geoJson”
  return (
    <APIProvider
      apiKey={props.GMAP_API_KEY}
      onLoad={() => console.log('Maps API has loaded.')}
      libraries={['places', 'drawing']}
    >
      <Map
        defaultZoom={7}
        defaultCenter={{ lat: 37.33548, lng: -121.893028 }}
        onCameraChanged={(ev: MapCameraChangedEvent) => {
          console.log(
            `camera changed: ${ev.detail.center} zoom: ${ev.detail.zoom}`,
          );
          props.onBoundaryDrag();
        }}
        mapId={props.GMAP_MAP_ID}
        gestureHandling="greedy"
        draggableCursor="default"
      >
        <SearchBar />

        <Clickableboundry
          geojsonUrl={props.GEO_JSON_URL}
          countyNameProperty="NAME"
          onCitySelect={handleCountySelect} // Property in the GeoJSON features holding the county name
        />
      </Map>
    </APIProvider>
  );
};
