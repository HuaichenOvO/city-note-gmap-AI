import api from "./config";

import { NoteType } from '../types/NoteType';

export const eventApi = {
    createEvent: async (note: NoteType) => {
        const response = await api.post(`event`, note);
        return response.data;
    },
    getEventsByCounty: async (countyId: string) => {
        const response = await api.get<any>(`event/county/${countyId}?page=0&size=10`);
        console.log("[eventApi.getEventsByCounty]", response.data);

        if (response.data && !response.data.empty) {
            return response.data.content.map(
                (i: any) => ({
                    noteId: i.id,
                    title: i.title,
                    content: i.content,
                    pictureLinks: i.pictureLinks,
                    videoLink: i.videoLink,
                    date: new Date(i.date),
                    county: i.county,
                    eventType: i.eventType
                })
            );
        }
        else {
            console.log("empty");
            console.log(response.data);
            return [];
        }
    },
    getEventsByUser: async (userId: string) => {
        const response = await api.get<NoteType[]>(`event/county/${userId}`);
        return response.data;
    },
    updateEvent: async (noteId: string, note: NoteType) => {
        const response = await api.put(`event/${noteId}`, note);
        return response.data;
    },
    deleteEvent: async (noteId: string) => {
        const response = await api.delete(`event/${noteId}`);
        return response.data;
    }
}

