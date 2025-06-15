import React from 'react';

import { NoteType } from '../types/NoteType';

export function Note(props: {
  note: NoteType;
  handleNoteClick: (note: NoteType) => void;
}) {
  const handleClick = () => {
    props.handleNoteClick(props.note);
  };

  return (
    <div
      className="bg-gray-50 p-4 rounded-lg shadow-sm hover:shadow-md 
        transition-shadow duration-200 border border-gray-200"
    >
      <h3 className="text-lg font-semibold text-gray-700 mb-2">
        {props.note.title}
      </h3>
      <p className="text-sm text-gray-600 line-clamp-3 py-1">
        {props.note.content}
      </p>

      <button
        className="
            mt-3 px-3 py-1 bg-indigo-500 text-white text-sm rounded-md 
            hover:bg-indigo-700 transition-colors duration-200"
        onClick={handleClick}
      >
        View Details
      </button>
    </div>
  );
}
