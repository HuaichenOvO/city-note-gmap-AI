import React, { useState } from 'react';
import { eventApi } from '../api/eventApi';
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
  const [eventType, setEventType] = useState<'TEXT' | 'IMAGE' | 'VIDEO'>('TEXT');
  const [pictureLinks, setPictureLinks] = useState<string[]>(['']);
  const [videoLink, setVideoLink] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isRecTextDisplayed, setIsRecTextDisplayed] = useState(false);
  const [recTitle, setRecTitle] = useState('');
  const [recContent, setRecContent] = useState('');

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
    <div className="fixed inset-0 bg-black bg-opacity-20 flex items-center justify-center z-50">
      <div className="absolute bg-white rounded-lg p-6 top-1/2 left-1/4 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold text-gray-800">
            Create New Event in {countyName}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-2xl"
          >
            ×
          </button>
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
                type="button"
                onClick={recommendText}
                className="flex-1 bg-purple-600 text-white py-2 px-4 rounded-md hover:bg-purple-800"
              > ✨ Recommend text ✨
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 bg-indigo-600 text-white py-2 px-4 rounded-md hover:bg-indigo-700 disabled:opacity-50"
            >
              {isSubmitting ? 'Creating...' : '✅ Create Event ✅'}
            </button>
            <button
              type="button"
              onClick={onClose}
              className="flex-1 bg-gray-600 text-white py-2 px-4 rounded-md hover:bg-gray-700"
            >
              ❌ Cancel ❌
            </button>
          </div>
        </form>
      </div>

      {isRecTextDisplayed && (
        <div className="absolute top-1/2 -translate-y-1/2 right-1/8 bg-white p-6 w-1/4 max-w-1/4 rounded shadow-lg text-black">
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
              w-full h-20 p-3
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
              w-full h-48 p-3
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
  );
}; 