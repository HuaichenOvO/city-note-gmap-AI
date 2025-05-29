import React, {useState, useEffect, useRef} from "react";

// popWindow 只负责渲染，不负责取数据
// 外部传入数据、点击新地点时，上级加载新数据
// 折叠：用一个 state 控制折叠，不折叠时放出所有 note 栏
//      折叠时收起，变成一个小矩形，但是仍然可以继续放出

// 数据来自 这个 county 的 名字： county+州双字母缩写（但不是这个部件管的内容）

export type Note = {
    noteId: string,
    title: string,
    content: string,
    // 下面两个只能有一个（需要限制）
    pictureLinks: string[] | null,
    videoLink: string | null,
};

type TestNoteProps = {
    countyName: string | null,
    notes: Array<Note>, // 不同的城市得到不同的array
};

export const TestNotes = (props: TestNoteProps) => {
    const [fold, setFold] = useState<boolean>(false);
    if (fold) 
        return (
        <div > 
            <p className="text-blue-600 underline">小盒子</p>
            <button onClick={() => setFold(false)}>Unfold me</button>
        </div>);
    else {
        {/* // return <div key={e.dataID}>{e.content}</div>; */}
        return (
            <div >
                <p className="text-blue-600">Title: {props.countyName ? props.countyName : "Not selected"}</p>
                <button className="text-emerald-600"
                    onClick={() => setFold(true)}>Fold me
                </button>
                {props.notes.map(
                    (e: Note) => {return <div key={e.noteId}>
                        <div className="underline hover:text-blue-600 dark:hover:text-blue-400">
                            {e.title}
                        </div>
                        <div>{e.content}</div>
                        {(e.pictureLinks) ? <div>{e.pictureLinks}</div> : <></>}
                    </div>}
                )}
            </div>
        );
    }
}


