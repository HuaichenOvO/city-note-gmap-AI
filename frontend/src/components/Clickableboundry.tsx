// src/ClickableCountyBoundary.tsx
import React, { useEffect, useRef, useState } from 'react';
import { useMap } from '@vis.gl/react-google-maps';

interface ClickableCountyBoundaryProps {
  geojsonUrl: string;
  countyNameProperty: string; // The property in GeoJSON features that holds the county name
  countyIdProperty: string;
  onCountySelect: (cId: any, cName: any) => void;
}

const Clickableboundry: React.FC<ClickableCountyBoundaryProps> = ({
  geojsonUrl,
  countyNameProperty,
  countyIdProperty,
  onCountySelect,
}) => {
  const map = useMap();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  // Use a ref to manage the data layer features and listeners
  const listenersRef = useRef<google.maps.MapsEventListener[]>([]);
  const dataLayerRef = useRef<google.maps.Data | null>(null);

  useEffect(() => {
    if (!map) {
      return;
    }

    // Initialize or ensure a single data layer instance associated with the map
    if (!dataLayerRef.current) {
      // Create a new Data layer and add it to the map
      dataLayerRef.current = new google.maps.Data({ map });
    } else {
      // If a data layer already exists (e.g., from a previous render or HMR), clear its features
      dataLayerRef.current.forEach((feature) => {
        dataLayerRef.current?.remove(feature);
      });
      // Ensure it's on the current map instance
      if (dataLayerRef.current.getMap() !== map) {
        dataLayerRef.current.setMap(map);
      }
    }

    const dataLayer = dataLayerRef.current;

    // Style for the county boundaries
    dataLayer.setStyle({
      fillColor: 'transparent', // Or a light, semi-transparent fill
      strokeColor: 'transparent',
      strokeWeight: 0.5,
      clickable: true,
    });

    // Style for when a county boundary is hovered over
    const mouseOverStyle: google.maps.Data.StyleOptions = {
      strokeColor: 'indigo',
      strokeWeight: 1,
      fillColor: 'oklch(67.3% 0.182 276.935)', // Light red fill on hover
    };

    // Fetch and load GeoJSON data
    fetch(geojsonUrl)
      .then((response) => {
        if (!response.ok) {
          throw new Error(
            `Failed to fetch GeoJSON: ${response.status} ${response.statusText}`,
          );
        }
        return response.json();
      })
      .then((geoJsonData) => {
        dataLayer.addGeoJson(geoJsonData);
        setErrorMessage(null);
      })
      .catch((error) => {
        console.error('Error loading GeoJSON data:', error);
        setErrorMessage(`Error loading county boundaries: ${error.message}`);
      });

    const onBoundryClick = (event: google.maps.Data.MouseEvent) => {
      if (event.feature) {
        const countyName = event.feature.getProperty(countyNameProperty);
        const countyId = event.feature.getProperty(countyIdProperty);
        if (countyId !== null && countyName !== null
          && typeof countyId !== "undefined"
          && typeof countyName !== "undefined") {
          const countyIdStr = new String(countyId);
          const countyNameStr = new String(countyName);
          let mapClickMsg = '';
          mapClickMsg += `[CountyBoundry] Clicked on: ${countyIdStr} - ${countyNameStr}`;
          onCountySelect(countyIdStr, countyNameStr);
          // if (event.latLng) {
          //   mapClickMsg += ` LatLng: ${event.latLng.lat()}, ${event.latLng.lng()}`;
          //   map.panTo(event.latLng);
          //   map.setZoom(9);
          // }
          console.log(mapClickMsg);
          // onCountyClick(String(countyName), event.latLng || null);
        } else {
          // console.warn(`Property "${countyIdProperty}" and "${countyNameProperty}" not found on clicked feature.`);
          // onCountyClick('Unknown County (property missing)', event.latLng || null);
          console.log(`[CountyBoundry] Clicked on: Unknown County (property missing): ${countyId}, ${countyName}`);
          if (event.latLng) {
            console.log(
              'Clicked at LatLng:',
              event.latLng.lat(),
              event.latLng.lng(),
            );
          }
        }
      }
    };

    // Clear previous listeners before adding new ones
    listenersRef.current.forEach((listener) => listener.remove());
    listenersRef.current = [];

    // Add click listener
    listenersRef.current.push(dataLayer.addListener('click', onBoundryClick));

    // Add mouseover listener for hover effect
    listenersRef.current.push(
      dataLayer.addListener(
        'mouseover',
        (event: google.maps.Data.MouseEvent) => {
          dataLayer.overrideStyle(event.feature, mouseOverStyle);
        },
      ),
    );

    // Add mouseout listener to revert hover effect
    listenersRef.current.push(
      dataLayer.addListener(
        'mouseout',
        (event: google.maps.Data.MouseEvent) => {
          dataLayer.revertStyle();
        },
      ),
    );

    // Cleanup function when component unmounts or dependencies change
    return () => {
      listenersRef.current.forEach((listener) => listener.remove());
      listenersRef.current = [];
      // Clear features from the data layer if this component is responsible for them
      // and they shouldn't persist after the component unmounts.
      if (dataLayer) {
        dataLayer.forEach((feature) => {
          dataLayer.remove(feature);
        });
      }
    };
  }, [map, geojsonUrl, countyNameProperty]); // Effect dependencies

  if (errorMessage) {
    // Display an error message if GeoJSON loading fails
    return (
      <div
        style={{
          position: 'absolute',
          top: '10px',
          left: '10px',
          zIndex: 1000,
          padding: '10px',
          backgroundColor: 'lightyellow',
          border: '1px solid red',
          color: 'red',
        }}
      >
        {errorMessage}
      </div>
    );
  }

  return null; // This component only manipulates the map, it doesn't render any direct DOM
};

export default Clickableboundry;
