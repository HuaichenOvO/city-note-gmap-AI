import React, { useState, useEffect } from 'react';
import { eventApi } from '../api/eventApi';
import { fileUploadApi } from '../api/fileUploadApi';
import { NoteType, CreateEventType } from '../types/NoteType';

interface EditEventProps {
  note: NoteType;
  onClose: () => void;
  onEventUpdated: () => void;
}

export const EditEvent: React.FC<EditEventProps> = ({
  note,
  onClose,
  onEventUpdated
}) => {
  const [title, setTitle] = useState(note.title);
  const [content, setContent] = useState(note.content);
  const [eventType, setEventType] = useState<'TEXT' | 'IMAGE'>(note.eventType as 'TEXT' | 'IMAGE');
  const [existingPictureLinks, setExistingPictureLinks] = useState<string[]>(note.pictureLinks || []);
  const [pictureFiles, setPictureFiles] = useState<File[]>([]);
  const [uploadedImageUrls, setUploadedImageUrls] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    setPictureFiles(prev => [...prev, ...files]);
  };

  const removeFile = (index: number) => {
    setPictureFiles(prev => prev.filter((_, i) => i !== index));
    setUploadedImageUrls(prev => prev.filter((_, i) => i !== index));
  };

  const removeExistingLink = (index: number) => {
    setExistingPictureLinks(prev => prev.filter((_, i) => i !== index));
  };

  const updateExistingLink = (index: number, value: string) => {
    const newLinks = [...existingPictureLinks];
    newLinks[index] = value;
    setExistingPictureLinks(newLinks);
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
      alert('Please login first to edit an event.');
      return;
    }
    
    if (!title.trim() || !content.trim()) {
      alert('Please fill in title and content');
      return;
    }

    setIsSubmitting(true);

    try {
      let finalImageUrls: string[] = [...existingPictureLinks];
      
      // 如果有新上传的图片文件，先上传
      if (eventType === 'IMAGE' && pictureFiles.length > 0) {
        const newUploadedUrls = await uploadImages(pictureFiles);
        finalImageUrls = [...finalImageUrls, ...newUploadedUrls];
      }

      const eventData: CreateEventType = {
        title: title.trim(),
        content: content.trim(),
        eventType,
        pictureLinks: eventType === 'IMAGE' ? finalImageUrls : [],
        countyId: note.countyId || 0  // Use the original event's countyId if available
      };

      await eventApi.updateEvent(note.noteId, eventData);
      
      // Notify parent component to refresh data
      onEventUpdated();
      onClose();
      
    } catch (error) {
      console.error('Failed to update event:', error);
      
      // Show more detailed error message
      let errorMessage = 'Failed to update event. Please try again.';
      
      if (error.response?.data) {
        errorMessage = error.response.data || errorMessage;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      alert(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-gray-100 rounded-lg shadow-lg p-6 max-h-[80vh] overflow-y-auto max-w-2xl relative">
      <button
        onClick={onClose}
        className="
          absolute z-10 right-4 top-4
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
          Edit Event in {note.county}
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
                Images
              </label>
              
              {/* 现有图片链接 */}
              {existingPictureLinks.length > 0 && (
                <div className="mb-4">
                  <h4 className="text-sm font-medium text-gray-700 mb-2">Existing Images:</h4>
                  {existingPictureLinks.map((link, index) => (
                    <div key={`existing-${index}`} className="flex gap-2 mb-2">
                      <input
                        type="url"
                        value={link}
                        onChange={(e) => updateExistingLink(index, e.target.value)}
                        className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                        placeholder="Enter image URL"
                      />
                      <button
                        type="button"
                        onClick={() => removeExistingLink(index)}
                        className="px-3 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}

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
                  Select one or more image files to add (max 10MB each)
                </p>
              </div>

              {/* 已选择的新文件列表 */}
              {pictureFiles.length > 0 && (
                <div className="space-y-2 mb-4">
                  <h4 className="text-sm font-medium text-gray-700">New Files to Upload:</h4>
                  {pictureFiles.map((file, index) => (
                    <div key={`new-${index}`} className="flex items-center justify-between bg-gray-50 p-2 rounded">
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
                  <h4 className="text-sm font-medium text-gray-700 mb-2">Newly Uploaded Images:</h4>
                  <div className="flex flex-wrap gap-2">
                    {uploadedImageUrls.map((url, index) => (
                      <img
                        key={`preview-${index}`}
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
              type="submit"
              disabled={isSubmitting || isUploading}
              className="flex-1 bg-indigo-600 text-white py-2 px-4 rounded-md hover:bg-indigo-700 disabled:opacity-50"
            >
              {isSubmitting ? 'Updating...' : isUploading ? 'Uploading...' : 'Update Event'}
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
  );
}; 