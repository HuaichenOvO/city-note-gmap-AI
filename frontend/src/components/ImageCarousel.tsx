import React, { useState } from 'react';
import { ImageLoader } from './ImageLoader';

interface ImageCarouselProps {
  images: string[];
  className?: string;
}

export const ImageCarousel: React.FC<ImageCarouselProps> = ({ images, className = '' }) => {
  const [currentIndex, setCurrentIndex] = useState(0);

  if (!images || images.length === 0) {
    return null;
  }

  const goToPrevious = () => {
    setCurrentIndex((prevIndex) => 
      prevIndex === 0 ? images.length - 1 : prevIndex - 1
    );
  };

  const goToNext = () => {
    setCurrentIndex((prevIndex) => 
      prevIndex === images.length - 1 ? 0 : prevIndex + 1
    );
  };

  const goToImage = (index: number) => {
    setCurrentIndex(index);
  };

  return (
    <div className={`relative ${className}`}>
      {/* 主图片显示区域 */}
      <div className="relative overflow-hidden rounded-lg">
        <ImageLoader
          src={images[currentIndex]}
          className="w-full h-64 object-cover"
        />
        
        {/* 导航按钮 - 只在有多张图片时显示 */}
        {images.length > 1 && (
          <>
            {/* 左箭头 */}
            <button
              onClick={goToPrevious}
              className="absolute left-2 top-1/2 transform -translate-y-1/2 bg-black bg-opacity-50 text-white p-2 rounded-full hover:bg-opacity-75 transition-all duration-200"
              aria-label="Previous image"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            
            {/* 右箭头 */}
            <button
              onClick={goToNext}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-black bg-opacity-50 text-white p-2 rounded-full hover:bg-opacity-75 transition-all duration-200"
              aria-label="Next image"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </>
        )}
      </div>

      {/* 图片指示器 - 只在有多张图片时显示 */}
      {images.length > 1 && (
        <div className="flex justify-center mt-3 space-x-2">
          {images.map((_, index) => (
            <button
              key={index}
              onClick={() => goToImage(index)}
              className={`w-2 h-2 rounded-full transition-all duration-200 ${
                index === currentIndex 
                  ? 'bg-indigo-600' 
                  : 'bg-gray-300 hover:bg-gray-400'
              }`}
              aria-label={`Go to image ${index + 1}`}
            />
          ))}
        </div>
      )}

      {/* 图片计数器 - 只在有多张图片时显示 */}
      {images.length > 1 && (
        <div className="text-center mt-2 text-sm text-gray-600">
          {currentIndex + 1} / {images.length}
        </div>
      )}
    </div>
  );
}; 