import React from 'react';

import {
    APIProvider,
    Map,
    MapCameraChangedEvent,
} from '@vis.gl/react-google-maps';

import Clickableboundry from './Clickableboundry';
import SearchBar from './SearchBar';

export type MapProps = {
    GMAP_API_KEY: string;
    GMAP_MAP_ID: string;
    GEO_JSON_URL: string;
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
                        geojsonUrl={props.GEO_JSON_URL}
                        countyNameProperty="NAME"
                        onCitySelect={props.onCountySelect} // Property in the GeoJSON features holding the county name
                    />
                </Map>
            </APIProvider>
        </div>
    );
};
