package org.x.backend.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

@Service
public class CosService{

    @Autowired
    private COSClient cosClient;

    @Value("${tencent.cos.bucket-name}")
    private String bucketName;

    @Value("${tencent.cos.base-url}")
    private String baseUrl;

    public String fileUpload(MultipartFile file, Map<String, String> tags) {
        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());

            // 关键：通过 HTTP Header 设置标签
            if (tags != null && !tags.isEmpty()) {
                StringBuilder tagBuilder = new StringBuilder();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    if (tagBuilder.length() > 0) {
                        tagBuilder.append("&");
                    }
                    // URL 编码标签键值（避免特殊字符问题）
                    tagBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    tagBuilder.append("=");
                    tagBuilder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                // 设置 x-cos-tagging HTTP Header
                objectMetadata.addUserMetadata("x-cos-tagging", tagBuilder.toString());
            }

            // 构建上传请求并执行
            /* PutObjectRequest：封装上传操作的必要参数
             * bucketName：目标存储桶名称。
             * newFileName：在 COS 中的存储路径（键）。
             * inputStream：文件输入流。
             * objectMetadata：文件元数据。
             * cosClient.putObject：执行上传操作，成功后返回 PutObjectResult（包含 ETag 等信息）
             * */
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, newFileName, inputStream, objectMetadata);

            cosClient.putObject(putObjectRequest);

            return baseUrl + "/" + newFileName;

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        } catch (CosClientException e) {
            throw new RuntimeException("COS客户端异常", e);
        }
    }
}
