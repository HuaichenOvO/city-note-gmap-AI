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
    (countyId: string, countyName: string) => void,
    refreshNotes: () => void
  }
}

export const eventContext = createContext<eventContextType>({
  data: {
    countyId: null,
    countyName: null,
    notes: []
  }, handler: {
    setEventContextCounty_Id_Name: (cName: string, cId: string) => { return },
    refreshNotes: () => { return }
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
          console.log('No countyId set, skipping notes fetch');
          return;
        }
        else {
          console.log('Fetching notes for county:', countyId);
          eventApi.getEventsByCounty(countyId)
            .then(notes => {
              console.log('Fetched notes:', notes);
              setNotes(notes);
            })
            .catch(error => {
              console.error('Failed to fetch notes:', error);
            });
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
        },
        refreshNotes: () => {
          if (!countyId) {
            console.warn('Cannot refresh notes: countyId is null');
            return;
          }
          console.log('Refreshing notes for county:', countyId);
          eventApi.getEventsByCounty(countyId)
            .then(notes => {
              console.log('Refreshed notes:', notes);
              setNotes(notes);
            })
            .catch(error => {
              console.error('Failed to refresh notes:', error);
            });
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