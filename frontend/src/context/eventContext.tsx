import React, { createContext, useContext, useState, ReactNode, useEffect } from "react";

import { NoteType } from "../components/Note";

type eventContextType = {
  data: {
    countyName: string | null,
    notes: NoteType[]
  },
  handler: {
    onCountyClick: (county: string) => void
  }
}

export const eventContext = createContext<eventContextType>({
  data: {
    countyName: null,
    notes: []
  }, handler: {
    onCountyClick: (cName: string) => { return }
  }
});

export const EventContextProvider:
  React.FC<{ children: ReactNode }>
  = ({ children }) => {

    // useState 选定 provider 内需要包含的数据/flags
    const [countyName, setCountyName] = useState<string | null>(null);
    const [notes, setNotes] = useState<NoteType[]>([]);

    useEffect(
      () => {
        // load setNotes
        setNotes(
          Array.from({ length: 20 }, (_, i) => ({
            noteId: `${i}`,
            title: `Event ${i + 1} in ${countyName}`,
            content:
              i > 3
                ? `This is a detailed description for card number ${i + 1}. \nIt can be quite long to test scrolling behavior.`
                : 'Kangaroos are famous for their forward-opening pouchwhere the joey (baby kangaroo) develops and suckles. A female kangaroo is known as a flyer or a doe and a male kangaroo a buck or a boomer (hence the nickname of the  Australian mens basketball team, the Boomers). They live in social groups called mobs. Can kangaroos swim? Yes! Kangaroos are reasonably strong swimmers. Though they re not exactly built for swimming, they can comfortably swim short distances if motivated enough to access new pastures or to avoid predators Kangaroos are famous for their forward -opening pouchwhere the joey (baby kangaroo) develops and suckles. A female kangaroo is known as a flyer or a doe and a male kangaroo a buck or a boomer (hence the nickname of the Australian mens basketball team, the Boomers). They live in social groups called mobs. Can kangaroos swim? Yes! Kangaroos are reasonably strong swimmers. Though they re not exactly built for swimming, they can comfortably swim short distances if motivated enough to access new pastures or to avoid predators',
            pictureLinks:
              i < 5
                ? [
                  'https://nationalzoo.si.edu/sites/default/files/styles/wide/public/2024-11/20241024-817A7713-16RP-bao-li.jpg?h=6acbff97&itok=orzUN1IX',
                  'localhost:11999',
                  'https://www.bushheritage.org.au/cdn-cgi/image/quality=90,fit=scale-down/https://www.bushheritage.org.au/uploads/main/Images/Places/Qld/pilungah/RS32891-kanga-pilung-peter-wallis.jpg',
                ]
                : [],
            videoLink: null,
          }))
        );
      }, [countyName]
    );

    // 定义 contextType
    const eventContextValue: eventContextType = {
      data: {
        countyName: countyName,
        notes: notes
      },
      handler: {
        onCountyClick: (county: string) => setCountyName(county)
      }
    };


    // return
    return (
      <eventContext.Provider value={eventContextValue}>
        {children}
      </eventContext.Provider>
    )
  }