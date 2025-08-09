import React, { useEffect, useState } from 'react';

interface ImageLoaderProps {
  src: string;
  className?: string;
  [key: string]: any;
}

export const ImageLoader: React.FC<ImageLoaderProps> = ({ src, ...props }) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<boolean>(false);

  // Handle image URL, add server prefix for local uploaded images
  const getImageUrl = (imageSrc: string): string => {
    let result = "";
    if (imageSrc.startsWith('/api/upload/')) {
      result = `http://localhost:8080${imageSrc}`;
    }
    // If already a full URL, return as is
    if (imageSrc.startsWith('http://') || imageSrc.startsWith('https://')) {
      result = imageSrc;
    }
    // If relative path, add server prefix
    if (imageSrc.startsWith('/')) {
      result = `http://localhost:8080${imageSrc}`;
    }
    result = imageSrc;
    // console.log(`[Image Loader] processed image URL: ${result}`);
    return result;
  };

  useEffect(() => {
    setLoading(true);
    setError(false);

    const imgInstance = new Image();
    imgInstance.src = getImageUrl(src);

    imgInstance.onload = () => {
      setLoading(false);
      setError(true);
    };
  }, [src]);

  // loading && !error : loading page
  // error: err page
  // !loading && !error successfully loaded
  return (
    <>
      {loading && !error ? (
        <img
          src="loading.png"
          className={`flex-none animate-pulse bg-gray-200 ${props.className}`}
        ></img>
      ) : null}
      {error ? (
        <img src="error-404.png" className={props.className}></img>
      ) : null}
      {!loading && !error ? (
        <img src={getImageUrl(src)} className={props.className}></img>
      ) : null}
    </>
  );
};
