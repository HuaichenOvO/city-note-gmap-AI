export type NoteType = {
    noteId: string;
    title: string;
    content: string;
    // 下面两个只能有一个（需要限制）
    pictureLinks?: string[] | null;
    videoLink?: string | null;
    date: Date;
    county: string;
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
    videoLink?: string | null;
    eventType: 'TEXT' | 'IMAGE' | 'VIDEO';
    countyId: number;
};