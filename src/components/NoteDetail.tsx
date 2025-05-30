import React, {useState} from "react"
import { NoteType } from "./Note"

export const NoteDetail = (props: NoteType) => {
    

    return (
        <div
            key = {props.noteId}
            className="absolute left-1/2 top-1/2 overflow-visible
                        transform -translate-y-1/2 *:z-10
                        bg-gray-50">
            <button
                onClick={()=>true}
                className="
                    px-2 py-3
                    bg-indigo-400 text-white font-semibold
                    rounded-r-full shadow-lg
                    hover:bg-indigo-600
                    focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-75
                    transition-all duration-300 ease-in-out
                    whitespace-nowrap
                    pl-2 pr-2                     
                    "
            >
                <img src="arrow.png" className='h-5 w-5'/>
            </button>
            <div>{props.title}</div>
            <div>{props.content}</div>
            <div></div>
            <div></div>
        </div>
    )
}