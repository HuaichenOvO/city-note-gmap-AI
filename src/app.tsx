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

import { TestNotes, Note } from './test_components/TestNotes';
import { MapComponent } from './components/MapComponent';

import { GMAP_API_KEY, GMAP_MAP_ID } from '../env';

const countyNotes: Note[] = [
    {
        noteId: 'nc294t59n',
        title: 'Beautiful place!',
        content: 'yeyeyey',
        pictureLinks: null,
        videoLink: null,
    },
    {
        noteId: 'n39vt8990',
        title: 'Aweful place!',
        content: 'hahhahaaga',
        pictureLinks: null,
        videoLink: null,
    },
];

const App: React.FC = () => {
    const [countyNameState, setCountyNameState] = useState<string | null>(null);

    return (
        <div className="static w-full h-full">
            <MapComponent
                GMAP_API_KEY={GMAP_API_KEY}
                GMAP_MAP_ID={GMAP_MAP_ID}
                onCountySelect={setCountyNameState}
            />
            <TestNotes countyName={countyNameState} notes={countyNotes} />
        </div>
    );
};

export default App;

const container = document.getElementById('app');
if (container) {
    const root = createRoot(container);
    root.render(<App />);
}
