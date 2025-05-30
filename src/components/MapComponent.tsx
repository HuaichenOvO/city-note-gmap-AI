import React from 'react';

import {
    APIProvider,
    Map,
    MapCameraChangedEvent,
} from '@vis.gl/react-google-maps';

import Clickableboundry from './Clickableboundry';
import SearchBar from './SearchBar';

import { PoiMarkers, Poi } from './PoiMarkers';
import { CITY_JSON } from '../../pj_config';

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

export type MapProps = {
    GMAP_API_KEY: string;
    GMAP_MAP_ID: string;
    onCountySelect: (county: string) => void;
};

export const MapComponent: React.FC<MapProps> = (props: MapProps) => {
    if (!props.GMAP_API_KEY) {
        console.error('Google Maps API Key is not defined.');
        return <div>Error: Google Maps API Key missing.</div>;
    }

    // TODO: 增加“放大/缩小时不允许渲染 geoJson”
    return (
        <div className="absolute w-full h-full">
            <APIProvider
                apiKey={props.GMAP_API_KEY}
                onLoad={() => console.log('Maps API has loaded.')}
                libraries={['places', 'drawing']}
            >
                <Map
                    defaultZoom={7}
                    defaultCenter={{ lat: 37.33548, lng: -121.893028 }}
                    onCameraChanged={(ev: MapCameraChangedEvent) =>
                        // google map JS API 提供的 "地图被拖动" 及 "地图缩放" 后的回调函数接口
                        console.log(
                            'camera changed:',
                            ev.detail.center,
                            'zoom:',
                            ev.detail.zoom,
                        )
                    }
                    mapId={props.GMAP_MAP_ID}
                    gestureHandling="greedy"
                    draggableCursor="default"
                >
                    <SearchBar />

                    <Clickableboundry
                        geojsonUrl={CITY_JSON}
                        countyNameProperty="NAME"
                        onCitySelect={props.onCountySelect} // Property in the GeoJSON features holding the county name
                    />
                </Map>
            </APIProvider>
        </div>
    );
};
