import React, { useContext, useState, useEffect } from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';

import { eventContext } from './context/eventContext';

import { NavBar } from './components/NavBar';
import { NoteType } from './types/NoteType';
import { ResizableSidebar } from './components/ResizableSidebar';
import { MapComponent } from './components/MapComponent';
import { NoteDetail } from './components/NoteDetail';
import { CreateEvent } from './components/CreateEvent';
import { UserProfilePage } from './components/UserProfile';
import { GMAP_API_KEY, GMAP_MAP_ID } from '../env';
import { CITY_JSON } from '../pj_config';
import { eventApi } from './api/eventApi';

const MainPage: React.FC = () => {
  const [noteDetailDataState, setNoteDetailDataState] = useState<NoteType | null>(null);
  const [noteDetailVisibleState, setNoteDetailVisibleState] = useState<boolean>(false);
  const [showCreateEvent, setShowCreateEvent] = useState<boolean>(false);

  const { data, handler } = useContext(eventContext);
  const location = useLocation();

  // Check if location.state requires showing Create Event modal
  useEffect(() => {
    if (location.state && location.state.showCreateEvent) {
      setShowCreateEvent(true);
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  // Update current event detail when event list is refreshed
  useEffect(() => {
    if (noteDetailDataState && noteDetailVisibleState) {
      // Find the corresponding event from the updated event list
      const updatedNote = data.notes.find(note => note.noteId === noteDetailDataState.noteId);
      if (updatedNote) {
        setNoteDetailDataState(updatedNote);
      }
    }
  }, [data.notes, noteDetailDataState, noteDetailVisibleState]);

  const handleCountySelect = () => {
    setNoteDetailVisibleState(false);
    setShowCreateEvent(false);
  };

  const handleMapDrag = () => {
    setNoteDetailVisibleState(false);
    setShowCreateEvent(false);
  };

  const handleDetailPageClose = () => {
    setNoteDetailVisibleState(false);
  };

  const handleCreateEventClose = () => {
    setShowCreateEvent(false);
  };

  const handleEventCreated = () => {
    if (data.countyId) {
      handler.refreshNotes();
    }
    setShowCreateEvent(false);
  };

  const handleShowCreateEvent = () => {
    setNoteDetailVisibleState(false);
    setShowCreateEvent(true);
  };

  // TODO: set default county name as user's located county or New York
  const handleNoteClick = (note: NoteType) => {
    setShowCreateEvent(false);
    setNoteDetailDataState(note);
    setNoteDetailVisibleState(true);
  };



  // Check if we're on the profile page
  const isProfilePage = location.pathname === '/profile';

  return (
    <Routes>
      <Route path="/profile" element={<UserProfilePage />} />
      <Route path="/*" element={
        <div className="flex flex-col h-screen">
          <div className="flex-shrink-0">
            <NavBar />
          </div>

          {noteDetailVisibleState ? (
            <div className="fixed left-1/2 top-1/2 overflow-visible w-1/4 transform -translate-x-1/2 -translate-y-1/2 z-50 bg-transparent border-none">
              <NoteDetail {...{
                note: noteDetailDataState,
                onClickClose: handleDetailPageClose,
              }} />
            </div>
          ) : null}
          
          {showCreateEvent && data.countyId && data.countyName ? (
            <CreateEvent
              countyId={data.countyId}
              countyName={data.countyName}
              onClose={handleCreateEventClose}
              onEventCreated={handleEventCreated}
            />
          ) : null}

          <div className="relative flex flex-row w-full flex-1 z-1">
            <ResizableSidebar
              handleNoteClick={handleNoteClick}
              onShowCreateEvent={handleShowCreateEvent}
            />
            <div className='relative z-2 basis-full right-0 rounded-2xl bg-gray-100 p-2'>
              <MapComponent
                GMAP_API_KEY={GMAP_API_KEY}
                GMAP_MAP_ID={GMAP_MAP_ID}
                GEO_JSON_URL={CITY_JSON}
                onCountySelect={handleCountySelect}
                onBoundaryDrag={handleMapDrag}
              />
            </div>
          </div>
        </div>
      } />
    </Routes>
  );
};

export default MainPage;