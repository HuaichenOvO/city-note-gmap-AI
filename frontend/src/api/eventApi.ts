import api from "./config";

import { NoteType, CreateEventType } from '../types/NoteType';

export const eventApi = {
    createEvent: async (note: CreateEventType) => {
        const response = await api.post(`event`, note);
        return response.data;
    },
    getEventsByCounty: async (countyId: string) => {
        // TODO: add page flipping
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
                    eventType: i.eventType,
                    likes: i.likes || 0,
                    authorUsername: i.authorUsername || "Unknown User",
                    authorFirstName: i.authorFirstName || "",
                    authorLastName: i.authorLastName || ""
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
    updateEvent: async (noteId: string, note: CreateEventType) => {
        const response = await api.put(`event/${noteId}`, note);
        return response.data;
    },
    deleteEvent: async (noteId: string) => {
        const response = await api.delete(`event/${noteId}`);
        return response.data;
    },
    updateEventLikes: async (noteId: string) => {
        const response = await api.put(`event/${noteId}/like`);
        return response.data;
    },
    canUserModifyEvent: async (noteId: string) => {
        const response = await api.get(`event/${noteId}/can-modify`);
        return response.data;
    },
    getEventById: async (noteId: string) => {
        const response = await api.get(`event/${noteId}`);
        return response.data;
    }
}

