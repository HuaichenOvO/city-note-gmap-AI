import api from "./config";

interface UploadResponse {
    url: string;
    filename: string;
}

export const fileApi = {
    getImageUrl: (filename: string) => {
        return `${api.defaults.baseURL}/upload/image/${filename}`;
    },
    uploadImage: async (file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post<UploadResponse>('/upload/image', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        // console.log(`[fileUploadApi - uploadImage] received filename: ${response.data.filename}`);
        return response.data.filename;
    },
    deleteImage: async (fileLink: string): Promise<number> => {
        console.log(`[File api]: deleting this file: ${fileLink}`);

        const response = await api.delete<number>(`/upload/image`, {
            data: fileLink,
            headers: {
                'Content-Type': 'text/plain'
            }
        });
        return response.data;
    }
}; 