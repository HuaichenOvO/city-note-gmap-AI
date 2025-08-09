import api from "./config";

interface UploadResponse {
    url: string;
    filename: string;
}

export const fileUploadApi = {
    uploadImage: async (file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post<UploadResponse>('/upload/image', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });

        return response.data.url;
    },
    deleteImage: async (fileLink: string): Promise<number> => {
        console.log(`[File api]: deleting this file: ${fileLink}`);

        const response = await api.delete<number>(`/upload/image`, {
            data: fileLink.substring(21),
            headers: {
                'Content-Type': 'text/plain'
            }
        });
        return response.data;
    }
}; 