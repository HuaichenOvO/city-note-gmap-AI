import React from 'react';

import { useMap } from '@vis.gl/react-google-maps';

export const PictureOverlay = () => {
    const map = useMap();

    const imageBounds = {
        north: 40.773941,
        south: 40.712216,
        east: -74.12544,
        west: -74.22655,
    };

    const historicalOverlay = new google.maps.GroundOverlay(
        'https://storage.googleapis.com/geo-devrel-public-buckets/newark_nj_1922-661x516.jpeg',
        imageBounds,
    );

    historicalOverlay.setMap(map);

    return <></>;
};
