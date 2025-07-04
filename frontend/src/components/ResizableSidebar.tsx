import React, { useState, useContext, useRef, useEffect } from 'react';
import { NoteType } from '../types/NoteType';
import { Note } from './Note';
import { LoadingNote } from './LoadingNote';
import { eventContext } from '../context/eventContext';

type ResizableSidebarProps = {
  handleNoteClick: (noteObj: NoteType) => void;
  onShowCreateEvent: () => void;
};

export const ResizableSidebar: React.FC<ResizableSidebarProps> = (props) => {
  const { data, handler } = useContext(eventContext);
  const [isResizing, setIsResizing] = useState(false);
  const [sidebarWidth, setSidebarWidth] = useState(400); // 默认宽度
  const [isVisible, setIsVisible] = useState(false);
  const sidebarRef = useRef<HTMLDivElement>(null);
  const resizeHandleRef = useRef<HTMLDivElement>(null);

  // 当选中county时显示侧边栏
  useEffect(() => {
    if (data.countyName) {
      setIsVisible(true);
    } else {
      setIsVisible(false);
    }
  }, [data.countyName]);

  const handleEventCreated = () => {
    if (data.countyId) {
      handler.refreshNotes();
    }
  };

  const handleMouseDown = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsResizing(true);
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isResizing) return;

      const newWidth = e.clientX;
      const minWidth = 300;
      const maxWidth = window.innerWidth * 0.6;

      if (newWidth >= minWidth && newWidth <= maxWidth) {
        setSidebarWidth(newWidth);
      }
    };

    const handleMouseUp = () => {
      setIsResizing(false);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };

    if (isResizing) {
      document.body.style.cursor = 'col-resize';
      document.body.style.userSelect = 'none';
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };
  }, [isResizing]);

  // 如果没有选中county，不显示侧边栏
  if (!isVisible) {
    return null;
  }

  return (
    <>
      <div
        ref={sidebarRef}
        className="relative flex-shrink-0 bg-gray-100 shadow-lg rounded-r-2xl overflow-hidden"
        style={{ width: `${sidebarWidth}px` }}
      >
        <div className="flex flex-col h-full p-3">
          <div className="flex justify-between items-center mb-2">
            <h2 className="text-2xl font-bold text-indigo-700 pl-3">
              County: {data.countyName}
            </h2>
            <button
              onClick={() => setIsVisible(false)}
              className="w-8 h-8 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-full transition-colors duration-200 flex items-center justify-center"
            >
              ×
            </button>
          </div>
          
          <div className="flex-grow overflow-y-auto pr-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {data.notes.map((note) => (
                <Note
                  key={note.noteId}
                  {...{ note: note, handleNoteClick: props.handleNoteClick }}
                />
              ))}
            </div>
          </div>
                </div>

        {/* 拖拽调整大小的手柄 */}
        <div
          ref={resizeHandleRef}
          className="absolute right-0 top-0 bottom-0 w-1 bg-gray-300 cursor-col-resize hover:bg-indigo-400 transition-colors duration-200"
          onMouseDown={handleMouseDown}
        >
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-2 h-8 bg-gray-400 rounded-full flex items-center justify-center">
            <div className="w-1 h-4 bg-white rounded-full"></div>
          </div>
        </div>

        {/* Create Event按钮固定在右下角 */}
        {data.countyName && (
          <button
            onClick={() => props.onShowCreateEvent()}
            className="absolute bottom-6 right-7 px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors duration-200 shadow-lg"
          >
            + Create Event
          </button>
        )}

 
      </div>
    </>
  );
}; 