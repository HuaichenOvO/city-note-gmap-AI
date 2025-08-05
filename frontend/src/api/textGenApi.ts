import api from "./config";


export const textGenAptApi = {
    genText: async (title: string, content: string) => {
        const response = await api.post<any>(`text/recommend`, {title: title, currentText: content, URLs:[]});
        return response.data;
    }
        
        
}
