export type NoteType = {
    noteId: string;
    title: string;
    content: string;
    pictureLinks?: string[] | null;
    date: Date;
    county: string;
    countyId?: number;
    eventType: string;
    likes: number;
    authorUsername: string;
    authorFirstName: string;
    authorLastName: string;
};

export type CreateEventType = {
    title: string;
    content: string;
    pictureLinks?: string[] | null;
    eventType: 'TEXT' | 'IMAGE';
    countyId: number;
};