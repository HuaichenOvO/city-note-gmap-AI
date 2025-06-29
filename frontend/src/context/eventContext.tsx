import React, { createContext, useContext, useState, ReactNode, useEffect } from "react";

import { NoteType } from '../types/NoteType';
import { eventApi } from "../api/eventApi";

type eventContextType = {
  data: {
    countyId: string | null,
    countyName: string | null,
    notes: NoteType[]
  },
  handler: {
    setEventContextCounty_Id_Name:
    (countyId: string, countyName: string) => void
  }
}

export const eventContext = createContext<eventContextType>({
  data: {
    countyId: null,
    countyName: null,
    notes: []
  }, handler: {
    setEventContextCounty_Id_Name: (cName: string, cId: string) => { return }
  }
});

export const EventContextProvider:
  React.FC<{ children: ReactNode }>
  = ({ children }) => {

    // useState 选定 provider 内需要包含的数据/flags
    const [countyId, setCountyId] = useState<string | null>(null);
    const [countyName, setCountyName] = useState<string | null>(null);
    const [notes, setNotes] = useState<NoteType[]>([]);

    useEffect(
      () => {
        if (!countyId) {
          return;
        }
        else {
          eventApi.getEventsByCounty(countyId)
            .then(notes => setNotes(notes));
        }
      }, [countyId]
    );

    // 定义 contextType
    const eventContextValue: eventContextType = {
      data: {
        countyId: countyId,
        countyName: countyName,
        notes: notes
      },
      handler: {
        setEventContextCounty_Id_Name: (id: string, name: string) => {
          setCountyId(id);
          setCountyName(name);
        }
      }
    };


    // return
    return (
      <eventContext.Provider value={eventContextValue}>
        {children}
      </eventContext.Provider>
    )
  }