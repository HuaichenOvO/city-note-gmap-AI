import React, { useContext, useState } from 'react';

import { eventContext } from './context/eventContext';

import { NavBar } from './components/NavBar';
import { NoteType } from './components/Note';
import { NoteContainer } from './components/NoteContainer';
import { MapComponent } from './components/MapComponent';
import { NoteDetail } from './components/NoteDetail';
import { GMAP_API_KEY, GMAP_MAP_ID } from '../env';
import { CITY_JSON } from '../pj_config';

const MainPage: React.FC = () => {
  const [noteDetailDataState, setNoteDetailDataState] = useState<NoteType | null>(null);
  const [noteDetailVisibleState, setNoteDetailVisibleState] = useState<boolean>(false);
  const [notePageVisibleState, setNotePageVisibleState] = useState<boolean>(true);

  const { data } = useContext(eventContext);

  const handleCountySelect = () => {
    setNoteDetailVisibleState(false);
    setNotePageVisibleState(true);
  };

  const handleMapDrag = () => {
    setNoteDetailVisibleState(false);
  };

  const handleDetailPageClose = () => {
    setNoteDetailVisibleState(false);
  };

  const handleSidePageClose = (toggleState: boolean) => {
    setNoteDetailVisibleState(false);
    setNotePageVisibleState(toggleState);
  };

  // TODO: set default county name as user's located county or New York
  const handleNoteClick = (note: NoteType) => {
    setNoteDetailDataState(note);
    setNoteDetailVisibleState(true);
  };

  return (
    <>
      <div className="h-1/20">
        <NavBar />
        {` | County name: ${data.countyName}, \nWhich note is displaying? ${noteDetailDataState?.title}\nIs the note visible now? ${noteDetailVisibleState}\nIs the side page on? ${notePageVisibleState}`}
      </div>

      <div className="relative flex flex-row w-full h-19/20 z-1">
        {noteDetailVisibleState ? (
          <div className="absolute left-1/2 top-1/2 overflow-visible w-1/4 transform -translate-y-1/2 z-10">
            <NoteDetail {...{
              note: noteDetailDataState,
              onClickClose: handleDetailPageClose,
            }} />
          </div>
        ) : null}
        <NoteContainer
          visible={notePageVisibleState}
          handleNoteClick={handleNoteClick}
          handlePageClose={handleSidePageClose}
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
    </>
  );
};

export default MainPage;