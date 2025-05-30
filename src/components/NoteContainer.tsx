import React, { useState} from 'react';

// popWindow 只负责渲染，不负责取数据
// 外部传入数据、点击新地点时，上级加载新数据
// 折叠：用一个 state 控制折叠，不折叠时放出所有 note 栏
//      折叠时收起，变成一个小矩形，但是仍然可以继续放出

// 数据来自 这个 county 的 名字： county+州双字母缩写（但不是这个部件管的内容）
import {Note, NoteType} from "./Note";
import { LoadingNote } from './LoadingNote';

type NoteContainerProps = {
    countyName: string | null;
    notes: Array<NoteType>; // 不同的城市得到不同的array
    handleNoteClick: (noteObj: NoteType | null) => void;
};

export const NoteContainer = (props: NoteContainerProps) => {
    const [fold, setFold] = useState<boolean>(false);

    const loadingNotes: number[] = new Array(12).fill(0);

    if (fold)
        return (
            <div
                className="absolute left-0 top-1/2 overflow-visible
                            transform -translate-y-1/2 *:z-10">
                <button
                    onClick={()=>setFold(false)}
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
                    <img src="arrow.png" className='h-3 w-3'/>
                </button>
            </div>
        );
    else {
        return (
            <div className="relative flex h-screen w-3/10 overflow-hidden">
                <div className="relative flex flex-col flex-grow bg-gray-400 shadow-lg p-3 pr-10">
                    <h2 className="text-2xl font-bold mb-4 text-indigo-700 pl-3">
                        County:{' '}
                        {props.countyName ? props.countyName : 'Not selected'}
                    </h2>

                    
                    <div className="flex-grow overflow-y-auto pr-4">
                        
                        {props.countyName ? 

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {props.notes.map((note) => (
                                    <Note {...{note: note, handleNoteClick: props.handleNoteClick}} />
                                ))}
                            </div>

                        :   
                            <div className="grid grid-cols-2 md:grid-cols-2 gap-7">
                                {loadingNotes.map(e => <LoadingNote/>)}
                            </div>
                        }

                        <div
                            className="absolute right-0 top-1/2 overflow-visible
                                        transform -translate-y-1/2 *:z-10">
                            <button
                                onClick={()=>setFold(true)}
                                className="
                                    px-2 py-3
                                    bg-indigo-400 text-white font-semibold
                                    rounded-l-full shadow-lg
                                    hover:bg-indigo-600
                                    focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-75
                                    transition-all duration-300 ease-in-out
                                    whitespace-nowrap
                                    pl-2 pr-2                     
                                    "
                            >
                                <img src="arrow.png" className='h-3 w-3 rotate-180'/>
                            </button>
                        </div>

                    </div>
                </div>
            </div>
        );
    }
};
