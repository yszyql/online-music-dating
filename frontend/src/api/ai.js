// 导入request.js
import request from '@/utils/request.js'

// 与AI服务进行交互的函数
export const talkWithAIService = (prompt) => {
    return request.post('/ai/generate', prompt, {
        headers: {
            'Content-Type': 'text/plain' // 明确指定文本类型，避免编码
        }
    });
};