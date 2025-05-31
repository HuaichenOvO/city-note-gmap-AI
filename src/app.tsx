/**
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// import React, {useEffect, useState, useRef, useCallback} from 'react';
import React, { useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';

import { NavBar } from './components/NavBar';
import { NoteType } from './components/Note';
import { NoteContainer } from './components/NoteContainer';
import { MapComponent } from './components/MapComponent';
import { NoteDetail } from './components/NoteDetail';

import { GMAP_API_KEY, GMAP_MAP_ID } from '../env';
import { CITY_JSON } from '../pj_config';

const App: React.FC = () => {
  const [countyNameState, setCountyNameState] = useState<string | null>(null);
  const [noteDetailDataState, setNoteDetailDataState] =
    useState<NoteType | null>(null);
  const [noteDetailVisibleState, setNoteDetailVisibleState] =
    useState<boolean>(false);
  const [notePageVisibleState, setNotePageVisibleState] =
    useState<boolean>(true);

  const handleCountySelect = (county: string | null) => {
    if (county != countyNameState) {
      setNoteDetailDataState(null);
    }
    setCountyNameState(county);
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
    setNoteDetailVisibleState(true);
    setNoteDetailDataState(note);
  };

  const notes: NoteType[] = Array.from({ length: 20 }, (_, i) => ({
    noteId: `${i}`,
    title: `Event ${i + 1} in ${countyNameState}`,
    content:
      i > 3
        ? `This is a detailed description for card number ${i + 1}. 
        It can be quite long to test scrolling behavior.`
        : 'Kangaroos are famous for their forward-opening pouchwhere the \
        joey (baby kangaroo) develops and suckles. A female kangaroo is \
        known as a flyer or a doe and a male kangaroo a buck or a boomer \
        (hence the nickname of the  \
        Australian mens basketball team, the Boomers). They live in social \
        groups called mobs. Can kangaroos swim? Yes! Kangaroos are reasonably \
        strong swimmers. Though they re not exactly built for swimming, they \
        can comfortably swim short distances if motivated enough to access \
        new pastures or to avoid predators Kangaroos are famous for their forward \
        -opening pouchwhere the joey (baby kangaroo) develops and suckles. A \
        female kangaroo is known as a flyer or a doe and a male kangaroo a buck or a boomer \
        (hence the nickname of the Australian mens basketball team, the Boomers). \
        They live in social groups called mobs. Can kangaroos swim? Yes! Kangaroos \
        are reasonably strong swimmers. Though they re not exactly built for swimming, \
        they can comfortably swim short distances if motivated enough to access \
        new pastures or to avoid predators',
    pictureLinks:
      i < 5
        ? [
            'https://nationalzoo.si.edu/sites/default/files/styles/wide/public/2024-11/20241024-817A7713-16RP-bao-li.jpg?h=6acbff97&itok=orzUN1IX',
            'localhost:11999',
            'https://www.bushheritage.org.au/cdn-cgi/image/quality=90,fit=scale-down/https://www.bushheritage.org.au/uploads/main/Images/Places/Qld/pilungah/RS32891-kanga-pilung-peter-wallis.jpg',
          ]
        : [],
    videoLink: null,
  }));

  return (
    <>
      <div className="h-1/20">
        <NavBar/>
        {` County name: ${countyNameState}, 
        Which note is displaying? ${noteDetailDataState?.title}
        Is the note visible now? ${noteDetailVisibleState}
        Is the side page on? ${notePageVisibleState}`}
      </div>

      <div className="relative flex flex-row w-full h-19/20 z-1">

          {noteDetailVisibleState && noteDetailDataState 
          ? (
            <div className="absolute left-1/2 top-1/2 overflow-visible w-1/4
                            transform -translate-y-1/2 z-10">
              <NoteDetail {...{
                    note: noteDetailDataState,
                    onClickClose: handleDetailPageClose,
                }}/>
            </div>
          ) : null}
        
        <NoteContainer
          countyName={countyNameState}
          notes={notes ? notes : []}
          visible={notePageVisibleState}
          handleNoteClick={handleNoteClick}
          handlePageClose={handleSidePageClose}
        />

        <div className='relative z-2 basis-full right-0 
                        rounded-2xl bg-gray-100 p-2'>
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

export default App;

const container = document.getElementById('app');
if (container) {
  const root = createRoot(container);
  root.render(<App />);
}
