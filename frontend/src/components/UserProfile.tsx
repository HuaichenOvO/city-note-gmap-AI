import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { userApi, UserProfile } from '../api/userApi';
import { eventApi } from '../api/eventApi';
import { NoteType, CreateEventType } from '../types/NoteType';
import { EditEvent } from './EditEvent';
import { useAuth } from '../context/authContext';

export const UserProfilePage: React.FC = () => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [userEvents, setUserEvents] = useState<NoteType[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingEvent, setEditingEvent] = useState<NoteType | null>(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const navigate = useNavigate();
  const { logout } = useAuth();

  useEffect(() => {
    loadUserData();
  }, [currentPage]);

  const loadUserData = async () => {
    try {
      setLoading(true);
      // 获取用户信息
      const userData = await userApi.getCurrentUser();
      setUser(userData);

      // 获取用户发布的事件
      const eventsData = await userApi.getUserEvents(currentPage, 10);
      if (eventsData && !eventsData.empty) {
        const formattedEvents = eventsData.content.map((event: any) => ({
          noteId: event.id,
          title: event.title,
          content: event.content,
          pictureLinks: event.pictureLinks || [],
          videoLink: event.videoLink || '',
          date: new Date(event.createDate || event.date),
          county: event.county?.name || event.county,
          eventType: event.eventType,
          likes: event.likes || 0,
          authorUsername: event.userProfile?.username || "Unknown User",
          authorFirstName: event.userProfile?.firstName || "",
          authorLastName: event.userProfile?.lastName || ""
        }));
        setUserEvents(formattedEvents);
        setTotalPages(eventsData.totalPages || 0);
      } else {
        setUserEvents([]);
        setTotalPages(0);
      }
    } catch (error) {
      console.error('Error loading user data:', error);
      // 如果获取用户信息失败，可能是token过期，跳转到登录页
      logout();
      navigate('/login');
    } finally {
      setLoading(false);
    }
  };

  const handleEditEvent = (event: NoteType) => {
    setEditingEvent(event);
    setShowEditModal(true);
  };

  const handleDeleteEvent = async (eventId: string) => {
    if (window.confirm('Are you sure you want to delete this event?')) {
      try {
        await eventApi.deleteEvent(eventId);
        // 重新加载事件列表
        loadUserData();
      } catch (error) {
        console.error('Error deleting event:', error);
        alert('Failed to delete event');
      }
    }
  };

  const handleEditSuccess = () => {
    setShowEditModal(false);
    setEditingEvent(null);
    loadUserData();
  };

  const handleEditCancel = () => {
    setShowEditModal(false);
    setEditingEvent(null);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading profile...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600">User not found</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <button
              onClick={() => navigate('/')}
              className="text-gray-600 hover:text-gray-900"
            >
              ← Back to Map
            </button>
            <h1 className="text-xl font-semibold text-gray-900">My Profile</h1>
            <div></div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* User Info Section */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-8">
          <div className="flex items-center space-x-6">
            <div className="w-20 h-20 bg-blue-500 rounded-full flex items-center justify-center">
              <span className="text-white text-2xl font-bold">
                {user.username.charAt(0).toUpperCase()}
              </span>
            </div>
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-gray-900">{user.username}</h2>
              {user.firstName && user.lastName && (
                <p className="text-gray-600">{user.firstName} {user.lastName}</p>
              )}
              {user.email && (
                <p className="text-gray-500">{user.email}</p>
              )}
              {user.bio && (
                <p className="text-gray-700 mt-2">{user.bio}</p>
              )}
            </div>
          </div>
        </div>

        {/* User Events Section */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">My Events</h3>
          </div>

          {userEvents.length === 0 ? (
            <div className="px-6 py-12 text-center">
              <p className="text-gray-500">You haven't posted any events yet.</p>
              <button
                onClick={() => navigate('/create-event')}
                className="mt-4 bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600"
              >
                Create Your First Event
              </button>
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {userEvents.map((event) => (
                <div key={event.noteId} className="p-6">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h4 className="text-lg font-medium text-gray-900">{event.title}</h4>
                      <p className="text-gray-600 mt-1 line-clamp-2">{event.content}</p>
                      <div className="flex items-center space-x-4 mt-2 text-sm text-gray-500">
                        <span>{event.date.toLocaleDateString()}</span>
                        <span>•</span>
                        <span>{event.county}</span>
                        <span>•</span>
                        <span>{event.likes} likes</span>
                      </div>
                      {event.pictureLinks && event.pictureLinks.length > 0 && (
                        <div className="mt-3 flex space-x-2">
                          {event.pictureLinks.slice(0, 3).map((link, index) => (
                            <img
                              key={index}
                              src={link}
                              alt={`Event ${index + 1}`}
                              className="w-16 h-16 object-cover rounded-md"
                              onError={(e) => {
                                (e.target as HTMLImageElement).src = '/error-404.png';
                              }}
                            />
                          ))}
                          {event.pictureLinks.length > 3 && (
                            <div className="w-16 h-16 bg-gray-200 rounded-md flex items-center justify-center">
                              <span className="text-gray-500 text-sm">+{event.pictureLinks.length - 3}</span>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                    <div className="flex space-x-2 ml-4">
                      <button
                        onClick={() => handleEditEvent(event)}
                        className="bg-blue-500 text-white px-3 py-1 rounded-md text-sm hover:bg-blue-600"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDeleteEvent(event.noteId)}
                        className="bg-red-500 text-white px-3 py-1 rounded-md text-sm hover:bg-red-600"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="px-6 py-4 border-t border-gray-200">
              <div className="flex items-center justify-between">
                <button
                  onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                  className="px-3 py-1 text-sm bg-gray-200 text-gray-700 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
                >
                  Previous
                </button>
                <span className="text-sm text-gray-600">
                  Page {currentPage + 1} of {totalPages}
                </span>
                <button
                  onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                  disabled={currentPage === totalPages - 1}
                  className="px-3 py-1 text-sm bg-gray-200 text-gray-700 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Edit Event Modal */}
      {showEditModal && editingEvent && (
        <div className="fixed left-1/2 top-1/2 overflow-visible w-1/4 transform -translate-x-1/2 -translate-y-1/2 z-50">
          <EditEvent
            note={editingEvent}
            onClose={handleEditCancel}
            onEventUpdated={handleEditSuccess}
          />
        </div>
      )}
    </div>
  );
}; 