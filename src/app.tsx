/**
 * Copyright 2024 Google LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

// import React, {useEffect, useState, useRef, useCallback} from 'react';
import React, {useRef, useState} from 'react';
import {createRoot} from 'react-dom/client';

import {
  APIProvider,
  Map,
  MapCameraChangedEvent,
} from '@vis.gl/react-google-maps';

import {PoiMarkers, Poi} from "./components/PoiMarkers"
import Clickableboundry from './components/Clickableboundry';
import {TestNotes, Note} from './test_components/TestNotes';
import SearchBar from './components/SearchBar';

import {GMAP_API_KEY, GMAP_MAP_ID} from "../env";
import {CITY_JSON} from "../pj_config";

// const locations: Poi[] = [
//   {key: 'NEU@SV', location: { lat: 37.38265628429269, lng: -121.89585431612574  }}, 
//   {key: 'Airport@SJ', location: { lat: 37.36546903641177, lng: -121.92895513462652 }},
//   {key: 'IntelMusuem', location: { lat: 37.39061977133763, lng: -121.96350658655435 }},
//   {key: 'GreatAmerica',  location: { lat: 37.40235389447383, lng: -121.97682604026035 }},
//   {key: 'theRocks',   location: { lat: -33.8587568, lng: 151.2058246 }},
//   {key: 'circularQuay', location: { lat: -33.858761, lng: 151.2055688 }},
//   {key: 'harbourBridge', location: { lat: -33.852228, lng: 151.2038374 }},
//   {key: 'kingsCross', location: { lat: -33.8737375, lng: 151.222569 }},
//   {key: 'botanicGardens', location: { lat: -33.864167, lng: 151.216387 }},
//   {key: 'museumOfSydney', location: { lat: -33.8636005, lng: 151.2092542 }},
//   {key: 'maritimeMuseum', location: { lat: -33.869395, lng: 151.198648 }},
//   {key: 'kingStreetWharf', location: { lat: -33.8665445, lng: 151.1989808 }},
//   {key: 'aquarium', location: { lat: -33.869627, lng: 151.202146 }},
//   {key: 'darlingHarbour', location: { lat: -33.87488, lng: 151.1987113 }},
//   {key: 'barangaroo', location: { lat: - 33.8605523, lng: 151.1972205 }},
// ];

const countyNotes: Note[] = [
  {noteId: "nc294t59n", title: "Beautiful place!", content: "yeyeyey", pictureLinks: null, videoLink: null},
  {noteId: "n39vt8990", title: "Aweful place!", content: "hahhahaaga", pictureLinks: null, videoLink: null}
];

// GeoJSON URL for US Counties
// This file uses 'NAME' as the property for the county name.

const App: React.FC = () => {

  if (!GMAP_API_KEY) {
    console.error("Google Maps API Key is not defined.");
    return <div>Error: Google Maps API Key missing.</div>;
  }

  const [countyNameState, setCountyNameState] = useState<string | null>(null);

  const onCountySelect = (city) => {
    setCountyNameState(city);
  }

  // TODO: 增加“放大/缩小时不允许渲染 geoJson”
  return (
    <>
    {/* <TestNotes countyName={countyNameState} notes={countyNotes}/> */}
    <APIProvider apiKey={GMAP_API_KEY} onLoad={() => console.log('Maps API has loaded.')} libraries={['places', 'drawing']}>
      <Map
        defaultZoom={7}
        defaultCenter={{ lat: 37.335480, lng: -121.893028 }}
        onCameraChanged={ (ev: MapCameraChangedEvent) =>
          // google map JS API 提供的 "地图被拖动" 及 "地图缩放" 后的回调函数接口
          console.log('camera changed:', ev.detail.center, 'zoom:', ev.detail.zoom)
        }
        mapId={GMAP_MAP_ID}
        gestureHandling="greedy"
        draggableCursor="default"
        >
        <SearchBar />
        {/* <PoiMarkers pois={locations} /> */}
        
        <Clickableboundry
          geojsonUrl={CITY_JSON}
          countyNameProperty="NAME"
          onCitySelect={onCountySelect} // Property in the GeoJSON features holding the county name
        />

      </Map>
    </APIProvider>
    </>
  );
}

export default App;

const container = document.getElementById('app');
if (container) {
  const root = createRoot(container);
  root.render(<App />);
}

