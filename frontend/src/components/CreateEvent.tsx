import React, { useState } from 'react';
import { eventApi } from '../api/eventApi';
import { CreateEventType } from '../types/NoteType';

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
  const [eventType, setEventType] = useState<'TEXT' | 'IMAGE' | 'VIDEO'>('TEXT');
  const [pictureLinks, setPictureLinks] = useState<string[]>(['']);
  const [videoLink, setVideoLink] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 如果countyId或countyName为空，不渲染组件
  if (!countyId || !countyName) {
    return null;
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
      const eventData: CreateEventType = {
        title: title.trim(),
        content: content.trim(),
        eventType,
        pictureLinks: eventType === 'IMAGE' ? pictureLinks.filter(link => link.trim()) : [],
        videoLink: eventType === 'VIDEO' ? videoLink.trim() : null,
        countyId: parseInt(countyId)  
      };

      await eventApi.createEvent(eventData);
      
      // 重置表单
      setTitle('');
      setContent('');
      setEventType('TEXT');
      setPictureLinks(['']);
      setVideoLink('');
      
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

  const addPictureLink = () => {
    setPictureLinks([...pictureLinks, '']);
  };

  const removePictureLink = (index: number) => {
    setPictureLinks(pictureLinks.filter((_, i) => i !== index));
  };

  const updatePictureLink = (index: number, value: string) => {
    const newLinks = [...pictureLinks];
    newLinks[index] = value;
    setPictureLinks(newLinks);
  };

  return (
    <div className="fixed left-1/2 top-1/2 overflow-visible w-1/4 transform -translate-x-1/2 -translate-y-1/2 z-50">
      <div className="relative bg-gray-200 rounded-lg shadow-lg px-4 py-6 z-0 inline-block max-w-2xl">
      <button
        onClick={onClose}
        className="
          absolute z-1 right-0 top-0
          mx-3 my-3 px-2 py-2
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

      <div className="bg-gray-100 rounded-lg shadow-lg p-6 max-h-[80vh] overflow-y-auto max-w-2xl">
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
              onChange={(e) => setEventType(e.target.value as 'TEXT' | 'IMAGE' | 'VIDEO')}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="TEXT">Text Only</option>
              <option value="IMAGE">With Images</option>
              <option value="VIDEO">With Video</option>
            </select>
          </div>

          {eventType === 'IMAGE' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Image Links
              </label>
              {pictureLinks.map((link, index) => (
                <div key={index} className="flex gap-2 mb-2">
                  <input
                    type="url"
                    value={link}
                    onChange={(e) => updatePictureLink(index, e.target.value)}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    placeholder="Enter image URL"
                  />
                  {pictureLinks.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removePictureLink(index)}
                      className="px-3 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
                    >
                      Remove
                    </button>
                  )}
                </div>
              ))}
              <button
                type="button"
                onClick={addPictureLink}
                className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600"
              >
                Add Another Image
              </button>
            </div>
          )}

          {eventType === 'VIDEO' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Video Link
              </label>
              <input
                type="url"
                value={videoLink}
                onChange={(e) => setVideoLink(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Enter video URL"
              />
            </div>
          )}

          <div className="flex gap-4 pt-4">
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 bg-indigo-600 text-white py-2 px-4 rounded-md hover:bg-indigo-700 disabled:opacity-50"
            >
              {isSubmitting ? 'Creating...' : 'Create Event'}
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
    </div>
      </div>
  );
}; 