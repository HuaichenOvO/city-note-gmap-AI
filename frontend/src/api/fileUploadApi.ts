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
    }
}; 