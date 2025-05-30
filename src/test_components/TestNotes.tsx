import React, { useState, useEffect, useRef } from 'react';

// popWindow 只负责渲染，不负责取数据
// 外部传入数据、点击新地点时，上级加载新数据
// 折叠：用一个 state 控制折叠，不折叠时放出所有 note 栏
//      折叠时收起，变成一个小矩形，但是仍然可以继续放出

// 数据来自 这个 county 的 名字： county+州双字母缩写（但不是这个部件管的内容）

export type Note = {
    noteId: string;
    title: string;
    content: string;
    // 下面两个只能有一个（需要限制）
    pictureLinks: string[] | null;
    videoLink: string | null;
};

type TestNoteProps = {
    countyName: string | null;
    notes: Array<Note>; // 不同的城市得到不同的array
};

export const TestNotes = (props: TestNoteProps) => {
    const [fold, setFold] = useState<boolean>(false);

    // Dummy data for cards
    const cards = Array.from({ length: 20 }, (_, i) => ({
        id: i,
        title: `Event ${i + 1} in ${props.countyName}`,
        description: `This is a detailed description for card number ${
            i + 1
        }. It can be quite long to test scrolling behavior.`,
    }));

    const handleButtonClick = () => {
        alert('Button Clicked!');
        setFold(true);
        // Add your button logic here
    };

    if (fold)
        return (
            // <div style={{
            //     position: 'absolute',
            //     top: '50%',
            //     left: 0,
            //     width: '200px',
            //     height: '10%',
            //     backgroundColor: 'rgba(255, 255, 255, 0.9)',
            //     boxShadow: '2px 0 5px rgba(0,0,0,0.2)',
            //     overflowY: 'auto',
            //     zIndex: 100 // 确保它在地图上方
            //   }}>
            <div className="relative top-1/2 left-0 w-1/6 h-1/5 overflow-visible \
            bg-amber-50">
                <p className="text-blue-600 underline">小盒子</p>
                <button
                    className="hover:text-lime-600"
                    onClick={() => setFold(false)}
                >
                    Unfold me
                </button>
            </div>
        );
    else {
        return (
            // <div style={{
            //           backgroundColor: 'rgba(255, 255, 255, 0.9)',
            //           boxShadow: '2px 0 5px rgba(0,0,0,0.2)'
            //         }}
            //     className=
            //     "relative top-0 left-0 w-1/6 h-full overflow-visible">
            //     <p className="text-blue-600">County: {props.countyName ? props.countyName : "Not selected"}</p>
            //     <button className="relative
            //         text-lime-400 hover:text-emerald-800"
            //         onClick={() => setFold(true)}>Fold me
            //     </button>
            //     {props.notes.map(
            //         (e: Note) => {
            //             return <div key={e.noteId}>
            //                         <div className="underline hover:text-blue-600 dark:hover:text-blue-400">
            //                             {e.title}
            //                         </div>
            //                         <div>{e.content}</div>
            //                         {(e.pictureLinks) ? <div>{e.pictureLinks}</div> : <></>}
            //                     </div>}
            //     )}
            // </div>
            <div className="relative flex h-screen w-1/4 overflow-hidden bg-gray-100">
                <div className="relative flex flex-col flex-grow bg-white shadow-lg p-6">
                    <h2 className="text-2xl font-bold mb-4 text-gray-600">
                        County:{' '}
                        {props.countyName ? props.countyName : 'Not selected'}
                    </h2>

                    {/* Scrollable Card Container */}
                    <div className="flex-grow overflow-y-auto pr-4">
                        {/* Add pr-4 for scrollbar padding */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {cards.map((card) => (
                                <div
                                    key={card.id}
                                    className="bg-gray-50 p-4 rounded-lg shadow-sm hover:shadow-md 
                                    transition-shadow duration-200 border border-gray-200"
                                >
                                    <h3 className="text-lg font-semibold text-gray-700 mb-2">
                                        {card.title}
                                    </h3>
                                    <p className="text-sm text-gray-600">
                                        {card.description}
                                    </p>
                                    {/* Example of card content */}
                                    <button className="
                                        mt-3 px-3 py-1 bg-blue-500 text-white text-sm rounded-md 
                                        hover:bg-blue-600 transition-colors duration-200">
                                        View Details
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Floating Button Container */}
                    {/* We use 'absolute' positioning within the relative 'flex-grow' parent
            for the scrollable content. If you want it truly fixed to the viewport,
            change the outer 'SidePage' div to 'relative' and this button's parent
            to 'fixed' relative to the viewport.
            
            Using 'absolute' here makes it relative to the 'flex-grow' scrollable container.
            
            Let's reconsider this. For a "middle-right of the sidepage" button that stays
            even when scrolling, it's better to make it `fixed` relative to the *viewport*,
            or `sticky` relative to a scrollable container that *is* the side page itself.

            Let's refine the structure to make the main content area scrollable,
            and the button sticky within it.
        */}
                </div>

                {/* Button placed outside the main scrollable content, but within the side page container */}
                {/* This button will be fixed relative to the entire side page. */}
                <div className="absolute right-6
                                top-1/2 transform -translate-y-1/2
                                z-10">
                    <button
                        onClick={handleButtonClick}
                        className="px-6 py-3
                            bg-indigo-600 text-white font-semibold
                            rounded-full shadow-lg
                            hover:bg-indigo-700
                            focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-opacity-75
                            transition-all duration-300 ease-in-out
                            whitespace-nowrap">
                        Action Button
                    </button>
                </div>
            </div>
        );
    }
};
