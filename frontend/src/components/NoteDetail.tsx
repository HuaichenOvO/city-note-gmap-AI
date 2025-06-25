import React, { useState } from 'react';
import { NoteType } from '../types/NoteType';
import { ImageLoader } from './ImageLoader';

export const NoteDetail = (props: {
  note: NoteType | null;
  onClickClose: () => void;
}) => {
  const [like, setLike] = useState<number>(50);

  return (props.note &&
    <div className='relative bg-gray-200 rounded-lg shadow-lg 
                    px-4 py-6 z-0'
    >
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

        <hr className="mb-3 bg-gray-800 mr-5"></hr>

        <div className="flex overflow-x-auto max-h-96 mb-6">
          {props.note.content}
        </div>

        {props.note.pictureLinks ? (
          <div className="flex overflow-x-auto space-x-4 pb-2">
            {props.note.pictureLinks
              ? props.note.pictureLinks.map((link: string, idx: number) => (
                <ImageLoader
                  key={
                    link.length > 5
                      ? link.slice(link.length - 5, link.length)
                      : `key-${idx}`
                  }
                  src={link}
                  className="w-32 h-32 object-cover rounded-md"
                />
              ))
              : null}
          </div>
        ) : null}
        {props.note.videoLink ? <div></div> : null}


        <div className='w-full mt-3 bg-gray-200 flex'>
          <button className='flex'>
            <img src="love.png" onClick={() => setLike(like + 1)}
              className='bg-red-300 w-5 h-5 rounded-md
                            hover:bg-red-500' />
            : {like}
          </button>
        </div>
      </div>

    </div>
  );
};
