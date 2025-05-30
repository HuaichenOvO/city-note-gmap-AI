import React, {useState} from "react"
import { NoteType } from "./Note"
import { ImageLoader } from "./ImageLoader"

export const NoteDetail = (props: {note: NoteType, onClickClose: () => void}) => {
    

    return (
        <div
            key = {props.note.noteId}
            className="absolute left-1/2 top-1/2 overflow-visible
                        w-1/4
                        bg-gray-50 rounded-lg shadow-lg p-6
                        transform -translate-y-1/2 *:z-10">
            <button
                onClick={props.onClickClose}
                className="
                    absolute right-0 top-0
                    mx-4 my-4 px-2 py-2
                    bg-indigo-400 text-white font-semibold
                    rounded-l-full rounded-r-full shadow-lg
                    hover:bg-indigo-600
                    focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-75
                    transition-all duration-300 ease-in-out
                    whitespace-nowrap              
                    "
            >
                <img src="close.png" className='h-5 w-5'/>
            </button>
            <div className="text-2xl font-bold mb-4">{props.note.title}</div>
            <div className="flex overflow-x-auto max-h-96 mb-6">{props.note.content}</div>

            <div className="h-10px"></div>

            {true ? (
            <div className="flex overflow-x-auto space-x-4 pb-2">
                {props.note.pictureLinks ? props.note.pictureLinks.map((link: string, idx: number) => (
                    <ImageLoader 
                        key={link.length > 5 ? link.slice(link.length-5, link.length): `key-${idx}`}
                        src="https://www.bushheritage.org.au/cdn-cgi/image/quality=90,fit=scale-down/https://www.bushheritage.org.au/uploads/main/Images/Places/Qld/pilungah/RS32891-kanga-pilung-peter-wallis.jpg"
                        className="w-32 h-32 object-cover rounded-md"
                />
                )) : null}
               
               <ImageLoader 
                    src="localhost:11999"
                    className="w-32 h-32 object-cover rounded-md"
                />
                <ImageLoader 
                    src="https://nationalzoo.si.edu/sites/default/files/styles/wide/public/2024-11/20241024-817A7713-16RP-bao-li.jpg?h=6acbff97&itok=orzUN1IX"
                    className="w-32 h-32 object-cover rounded-md"
                />
                <ImageLoader 
                    src="https://www.bushheritage.org.au/cdn-cgi/image/quality=90,fit=scale-down/https://www.bushheritage.org.au/uploads/main/Images/Places/Qld/pilungah/RS32891-kanga-pilung-peter-wallis.jpg"
                    className="w-32 h-32 object-cover rounded-md"
                />
            </div>
            ) : null}
            {props.note.videoLink ? <div></div> : null}
        </div>
    )
}