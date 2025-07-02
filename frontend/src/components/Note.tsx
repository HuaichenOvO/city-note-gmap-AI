import React from 'react';

import { NoteType } from '../types/NoteType';

export function Note(props: {
  note: NoteType;
  handleNoteClick: (note: NoteType) => void;
}) {
  const handleClick = () => {
    props.handleNoteClick(props.note);
  };

  const getAuthorDisplayName = () => {
    if (props.note.authorFirstName && props.note.authorLastName) {
      return `${props.note.authorFirstName} ${props.note.authorLastName}`;
    } else if (props.note.authorFirstName) {
      return props.note.authorFirstName;
    } else {
      return props.note.authorUsername;
    }
  };

  return (
    <div
      className="bg-gray-50 p-4 rounded-lg shadow-sm hover:shadow-md 
        transition-shadow duration-200 border border-gray-200"
    >
      <div className="flex justify-between items-start mb-2">
        <h3 className="text-lg font-semibold text-gray-700">
          {props.note.title}
        </h3>
        <div className="text-xs text-gray-500 text-right">
          <div className="font-medium">By {getAuthorDisplayName()}</div>
        </div>
      </div>
      
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
