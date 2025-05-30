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

import { NoteType } from './components/Note';
import { NoteContainer } from './components/NoteContainer';
import { MapComponent } from './components/MapComponent';
import { NoteDetail } from './components/NoteDetail';

import { GMAP_API_KEY, GMAP_MAP_ID } from '../env';
import { CITY_JSON } from '../pj_config';

const App: React.FC = () => {
    const [countyNameState, setCountyNameState] = useState<string | null>(null);
    const [noteDetailState, setNoteDetailState] = useState<NoteType | null>(null);
    const [noteVisibleState, setNoteVisibleState] = useState<boolean>(false);

    // TODO: set default county name as user's located county or New York
    const handleNoteClick = (note: NoteType) => {
        setNoteVisibleState(true);
        setNoteDetailState(note);
    }

    const notes: NoteType[] = Array.from({ length: 20 }, (_, i) => ({
        noteId: `${i}`,
        title: `Event ${i + 1} in ${countyNameState}`,
        content: `This is a detailed description for card number ${
            i + 1
        }. It can be quite long to test scrolling behavior.`,
        pictureLinks: null, videoLink: null
    }));

    return (
        <div className="static w-full h-full">
            <MapComponent
                GMAP_API_KEY={GMAP_API_KEY}
                GMAP_MAP_ID={GMAP_MAP_ID}
                GEO_JSON_URL={CITY_JSON}
                onCountySelect={setCountyNameState}
            />
            {noteVisibleState && noteDetailState ? 
                <NoteDetail {...{note: noteDetailState, 
                    onClickClose: () => setNoteVisibleState(false)}}/>
                : null
            }
            <NoteContainer countyName={countyNameState} notes={(notes ? notes : [])} 
                handleNoteClick={handleNoteClick} />
        </div>
    );
};

export default App;

const container = document.getElementById('app');
if (container) {
    const root = createRoot(container);
    root.render(<App />);
}
