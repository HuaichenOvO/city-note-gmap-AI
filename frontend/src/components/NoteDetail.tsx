import React, { useState, useContext, useEffect } from 'react';
import { NoteType } from '../types/NoteType';
import { ImageLoader } from './ImageLoader';
import { ImageCarousel } from './ImageCarousel';
import { eventApi } from '../api/eventApi';
import { eventContext } from '../context/eventContext';
import { EditEvent } from './EditEvent';

export const NoteDetail = (props: {
  note: NoteType | null;
  onClickClose: () => void;
}) => {
  const [like, setLike] = useState<number>(props.note?.likes || 0);
  const [canModify, setCanModify] = useState(false);
  const [showEditForm, setShowEditForm] = useState(false);
  const [isLikeLoading, setIsLikeLoading] = useState(false);
  const { handler } = useContext(eventContext);

  // Update local state when note data changes
  useEffect(() => {
    if (props.note) {
      setLike(props.note.likes || 0);
      checkUserPermissions();
    }
  }, [props.note]);

  // Check user permissions for modifying this event
  useEffect(() => {
    if (props.note) {
      checkUserPermissions();
    }
  }, [props.note]);

  const checkUserPermissions = async () => {
    if (!props.note) return;

    try {
      console.log('Checking permissions for event:', props.note.noteId);
      const hasPermission = await eventApi.canUserModifyEvent(props.note.noteId);
      console.log('Permission check result:', hasPermission);
      setCanModify(Boolean(hasPermission));
    } catch (error) {
      console.error('Failed to check user permissions:', error);
      setCanModify(false);
    }
  };

  const handleLike = async () => {
    if (props.note && !isLikeLoading) {
      setIsLikeLoading(true);
      try {
        console.log('Toggling like for note:', props.note.noteId);
        const isLiked = await eventApi.toggleEventLike(props.note.noteId);
        console.log('API response:', isLiked);

        // Update local state based on the response
        if (isLiked) {
          setLike(like + 1);
        } else {
          setLike(Math.max(0, like - 1));
        }

        handler.refreshNotes();
        console.log('Like toggled successfully');
      } catch (error) {
        console.error('Failed to toggle like:', error);
      } finally {
        setIsLikeLoading(false);
      }
    }
  };

  const handleEdit = () => {
    setShowEditForm(true);
  };

  const handleDelete = async () => {
    if (!props.note) return;

    if (!confirm('Are you sure you want to delete this event? This action cannot be undone.')) {
      return;
    }

    try {
      await eventApi.deleteEvent(props.note.noteId);
      // 刷新notes列表
      handler.refreshNotes();
      // 关闭详情页面
      props.onClickClose();
    } catch (error) {
      console.error('Failed to delete event:', error);
      alert('Failed to delete event. Please try again.');
    }
  };

  const handleEventUpdated = () => {
    handler.refreshNotes();
    setShowEditForm(false);
    // props.onClickClose(); // 不再关闭详情页
  };

  const getAuthorDisplayName = () => {
    if (!props.note) return "";
    if (props.note.authorFirstName && props.note.authorLastName) {
      return `${props.note.authorFirstName} ${props.note.authorLastName}`;
    } else if (props.note.authorFirstName) {
      return props.note.authorFirstName;
    } else {
      return props.note.authorUsername;
    }
  };

  return (props.note &&
    <div>
      {!showEditForm ? (
        <>
          <button
            onClick={props.onClickClose}
            className="
              absolute  z-1 right-0 top-0
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

          <div className='bg-gray-100 rounded-lg shadow-lg p-6'>

            <div className="text-2xl font-bold mb-2">{props.note.title}</div>

            <div className="text-sm text-gray-600 mb-3">
              <span className="font-medium">Posted by:</span> {getAuthorDisplayName()}
            </div>

            <hr className="mb-3 bg-gray-800 mr-5"></hr>

            <div className="flex overflow-x-auto max-h-96 mb-6">
              {props.note.content}
            </div>

            {props.note.pictureLinks && props.note.pictureLinks.length > 0 && (
              <div className="mb-6">
                <ImageCarousel
                  images={props.note.pictureLinks}
                  className="w-full"
                />
              </div>
            )}


            <div className='w-full mt-3 bg-gray-200 flex'>
              <button
                className={`flex items-center space-x-2 ${isLikeLoading ? 'opacity-50 cursor-not-allowed' : 'hover:bg-gray-300'}`}
                onClick={handleLike}
                disabled={isLikeLoading}
              >
                <img
                  src="/love.png"
                  alt="like"
                  className='bg-red-300 w-5 h-5 rounded-md hover:bg-red-500 transition-colors duration-200'
                  onError={(e) => {
                    console.error('Failed to load love.png');
                    e.currentTarget.style.display = 'none';
                  }}
                />
                <span className="text-gray-700 font-medium">: {like}</span>
                {isLikeLoading && <span className="text-xs text-gray-500">...</span>}
              </button>
              {canModify && (
                <div className="ml-auto flex space-x-2">
                  <button
                    onClick={handleEdit}
                    className="px-3 py-1 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors duration-200"
                  >
                    Edit
                  </button>
                  <button
                    onClick={handleDelete}
                    className="px-3 py-1 bg-red-500 text-white rounded-md hover:bg-red-600 transition-colors duration-200"
                  >
                    Delete
                  </button>
                </div>
              )}
            </div>
          </div>
        </>
      ) : (
        <EditEvent
          note={props.note}
          onClose={() => setShowEditForm(false)}
          onEventUpdated={handleEventUpdated}
        />
      )}
    </div>
  );
};
