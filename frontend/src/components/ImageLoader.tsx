import React, { useEffect, useState } from 'react';
import { fileApi } from '../api/fileApi';

interface ImageLoaderProps {
  src: string;
  className?: string;
  [key: string]: any;
}

export const ImageLoader: React.FC<ImageLoaderProps> = ({ src, ...props }) => {
  // const [img, setImg] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<boolean>(false);

  const loadSrc = error ? "error-404.png" : (loading ? "loading.png" : fileApi.getImageUrl(src))

  const handleLoad = () => {
    setLoading(false);
  }

  const handleError = () => {
    setLoading(false);
    setError(true);
  }

  // loading && !error : loading page
  // error: err page
  // !loading && !error successfully loaded
  return (
    <>
    <img 
      src={loadSrc}
      onLoad={handleLoad}
      onError={handleError}
      className={props.className}></img>
    </>
  );
};
