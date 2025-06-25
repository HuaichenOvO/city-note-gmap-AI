import api from "./config";

import { NoteType } from '../types/NoteType';

export const eventApi = {
    createEvent: async (note: NoteType) => {
        const response = await api.post(`event`, note);
        return response.data;
    },
    getEventsByCounty: async (countyName: string) => {
        const response = await api.get<NoteType[]>(`event/county/${countyName}`);
        return response.data;
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