import React, { useState } from 'react';
import { eventApi } from '../api/eventApi';
import { fileUploadApi } from '../api/fileUploadApi';
import { textGenAptApi } from '../api/textGenApi';
import { CreateEventType } from '../types/NoteType';
import { textGenAptApi } from '../api/textGenApi';

interface CreateEventProps {
  countyId: string | null;
  countyName: string | null;
  onClose: () => void;
  onEventCreated: () => void;
}

export const CreateEvent: React.FC<CreateEventProps> = ({
  countyId,
  countyName,
  onClose,
  onEventCreated
}) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [eventType, setEventType] = useState<'TEXT' | 'IMAGE'>('TEXT');
  const [pictureFiles, setPictureFiles] = useState<File[]>([]);
  const [uploadedImageUrls, setUploadedImageUrls] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [isRecTextDisplayed, setIsRecTextDisplayed] = useState(false);
  const [recTitle, setRecTitle] = useState('');
  const [recContent, setRecContent] = useState('');

  // If countyId or countyName is empty, do not render the component
  if (!countyId || !countyName) {
    return null;
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    setPictureFiles(prev => [...prev, ...files]);
  };

  const removeFile = (index: number) => {
    setPictureFiles(prev => prev.filter((_, i) => i !== index));
    setUploadedImageUrls(prev => prev.filter((_, i) => i !== index));
  };

  const uploadImages = async (files: File[]): Promise<string[]> => {
    const uploadPromises = files.map(async (file) => {
      // Check if the file is HEIC/HEIF
      if (file.name.toLowerCase().endsWith('.heic') || file.name.toLowerCase().endsWith('.heif')) {
        alert('HEIC/HEIF images may not display correctly in browsers. Please convert to JPEG and re-upload.');
        // You can choose to continue or block upload
        // return null; // Block upload
      }

      try {
        const imageUrl = await fileUploadApi.uploadImage(file);
        return imageUrl;
      } catch (error) {
        console.error('Failed to upload image:', error);
        throw error;
      }
    });

    const results = await Promise.all(uploadPromises);
    return results.filter(url => url !== null);
  };

  const recommendText = () => {
    setRecTitle('');
    setRecContent('');
    setIsRecTextDisplayed(true);
    textGenAptApi.genText(title, content)
      .then(response => {
        setRecTitle(response.newTitle);
        setRecContent(response.newContent);
      })
      .catch(error => {
        console.error("Error generating text:", error);
        alert("Failed to generate text. Please try again.");
      });
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 检查用户是否已登录
    const token = localStorage.getItem('token');
    if (!token) {
      alert('Please login first to create an event.');
      return;
    }

    if (!title.trim() || !content.trim()) {
      alert('Please fill in title and content');
      return;
    }

    setIsSubmitting(true);

    try {
      let finalImageUrls: string[] = [];

      // 如果有图片文件，先上传
      if (eventType === 'IMAGE' && pictureFiles.length > 0) {
        setIsUploading(true);
        finalImageUrls = await uploadImages(pictureFiles);
        setIsUploading(false);
      }

      const eventData: CreateEventType = {
        title: title.trim(),
        content: content.trim(),
        eventType,
        pictureLinks: eventType === 'IMAGE' ? finalImageUrls : [],
        countyId: parseInt(countyId)
      };

      await eventApi.createEvent(eventData);

      // 重置表单
      setTitle('');
      setContent('');
      setEventType('TEXT');
      setPictureFiles([]);
      setUploadedImageUrls([]);
      // 通知父组件刷新数据
      onEventCreated();
      onClose();

    } catch (error) {
      console.error('Failed to create event:', error);

      // 显示更详细的错误信息
      let errorMessage = 'Failed to create event. Please try again.';

      if (error.response?.data) {
        // 如果是服务器返回的错误
        errorMessage = error.response.data || errorMessage;
      } else if (error.message) {
        // 如果是其他错误
        errorMessage = error.message;
      }

      alert(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={`fixed top-1/2 transform -translate-y-1/2 z-50 ${isRecTextDisplayed ? 'left-1/2 -translate-x-1/2 w-auto max-w-6xl' : 'left-1/2 -translate-x-1/2 w-auto max-w-md'}`}>
      <div className={`relative z-0 ${isRecTextDisplayed ? 'flex gap-4' : 'inline-block'}`}>
        <div className={`bg-gray-100 rounded-lg shadow-lg max-h-[80vh] overflow-y-auto relative ${isRecTextDisplayed ? 'flex-1 max-w-md p-4' : 'p-6'}`}>
          <button
            onClick={onClose}
            className="
              absolute z-10 right-2 top-2
              px-2 py-2
              bg-indigo-400 text-white font-semibold
              rounded-l-full rounded-r-full shadow-lg
              hover:bg-indigo-600
              focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-75
              transition-all duration-300 ease-in-out
              whitespace-nowrap              
            "
          >
            <img src="close.png" className="h-5 w-5" />
          </button>
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">
              Create New Event in {countyName}
            </h2>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Title *
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Enter event title"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Content *
              </label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={4}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Enter event content"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Event Type
              </label>
              <select
                value={eventType}
                onChange={(e) => setEventType(e.target.value as 'TEXT' | 'IMAGE')}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="TEXT">Text Only</option>
                <option value="IMAGE">With Images</option>
              </select>
            </div>

            {eventType === 'IMAGE' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Upload Images
                </label>
                {/* 文件上传区域 */}
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 mb-4">
                  <input
                    type="file"
                    multiple
                    accept="image/*"
                    onChange={handleFileChange}
                    className="w-full"
                  />
                  <p className="text-sm text-gray-500 mt-2">
                    Select one or more image files (max 10MB each)
                  </p>
                </div>

                {/* 已选择的文件列表 */}
                {pictureFiles.length > 0 && (
                  <div className="space-y-2">
                    <h4 className="text-sm font-medium text-gray-700">Selected Files:</h4>
                    {pictureFiles.map((file, index) => (
                      <div key={index} className="flex items-center justify-between bg-gray-50 p-2 rounded">
                        <span className="text-sm text-gray-600 truncate">{file.name}</span>
                        <button
                          type="button"
                          onClick={() => removeFile(index)}
                          className="text-red-500 hover:text-red-700 text-sm"
                        >
                          Remove
                        </button>
                      </div>
                    ))}
                  </div>
                )}

                {/* 上传预览 */}
                {uploadedImageUrls.length > 0 && (
                  <div className="mt-4">
                    <h4 className="text-sm font-medium text-gray-700 mb-2">Uploaded Images:</h4>
                    <div className="flex flex-wrap gap-2">
                      {uploadedImageUrls.map((url, index) => (
                        <img
                          key={index}
                          src={url.startsWith('http') ? url : `http://localhost:8080${url}`}
                          alt={`Preview ${index + 1}`}
                          className="w-16 h-16 object-cover rounded border"
                        />
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}

            <div className="flex gap-4 pt-4">
              <button
                type="button"
                onClick={recommendText}
                className="flex-1 bg-purple-600 text-white py-2 px-4 rounded-md hover:bg-purple-800"
              >
                ✨ AI Recommend ✨
              </button>
              <button
                type="submit"
                disabled={isSubmitting || isUploading}
                className="flex-1 bg-indigo-600 text-white py-2 px-4 rounded-md hover:bg-indigo-700 disabled:opacity-50"
              >
                {isSubmitting ? 'Creating...' : isUploading ? 'Uploading...' : 'Create Event'}
              </button>
              <button
                type="button"
                onClick={onClose}
                className="flex-1 bg-gray-500 text-white py-2 px-4 rounded-md hover:bg-gray-600"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>

        {isRecTextDisplayed && (
          <div className="bg-white rounded-lg shadow-lg p-4 flex-1 max-w-md">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold text-gray-800 flex items-center">
                {recTitle == "" ?
                  <> Loading Data
                    <div className="w-5 h-5 border-4 ml-3 border-purple-700 border-t-transparent rounded-full animate-spin"></div>
                  </>
                  : "Recommended post draft"}</h2>
              <button
                onClick={() => setIsRecTextDisplayed(false)}
                className="text-gray-500 hover:text-gray-700 text-2xl"
              >
                ×
              </button>
            </div>

            <div className="block text-sm font-mono font-medium text-gray-700 mb-3">
              Title
            </div>
            <div
              className="
                w-full h-24 p-3
                border rounded-md border-gray-300 
                bg-gray-50 text-gray-500 font-mono text-sm
                overflow-y-auto resize-none
                whitespace-pre-wrap
              "
            >
              {recTitle == "" ? "Awaiting generation..." : recTitle}
            </div>

            <div className="block text-sm font-mono font-medium text-gray-700 my-3">
              Content
            </div>
            <div
              className="
                w-full h-64 p-3
                border rounded-md border-gray-300 
                bg-gray-50 text-gray-500 font-mono text-sm
                overflow-y-auto resize-none
                whitespace-pre-wrap
              "
            >
              {recContent == "" ? "Awaiting generation..." : recContent}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}; 