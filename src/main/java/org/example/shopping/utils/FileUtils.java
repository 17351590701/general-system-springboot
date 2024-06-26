package org.example.shopping.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author zyr
 * @date 2024/4/22 下午7:39
 * @Description
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/files")
public class FileUtils {

    // 存储文件的根路径
    public static final String rootFilePath = System.getProperty("user.dir") + "/file/";

    /**
     * 上传文件。
     *
     * @param file 用户上传的文件对象
     * @return 结果对象，包含文件上传成功后的唯一标识（UUID前缀）
     * @throws IOException 当文件读写过程中发生错误时抛出
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {
        synchronized (FileUtils.class){
        // 检查并创建文件存储目录，如果不存在
        if (!FileUtil.isDirectory(rootFilePath)) {
            FileUtil.mkdir(rootFilePath);
        }
        String originalFilename = file.getOriginalFilename(); // 获取文件原始名称
        // 使用util生成UUID作为文件名前缀，确保唯一性
        String uuidPrefix = IdUtil.fastSimpleUUID();

        // 将上传的文件保存到指定路径，文件名采用UUID前缀+原文件名的形式
        FileUtil.writeBytes(file.getBytes(), rootFilePath + uuidPrefix + "-" + originalFilename);

        // 返回UUID前缀，供后续下载文件时使用
        return Result.success(uuidPrefix);
        }
    }

    /**
     * 下载已上传的文件。
     *
     * @param flag 通过上传接口返回的UUID前缀，用于唯一标识待下载的文件
     * @param response HTTP响应对象，用于设置下载头信息及输出文件内容
     */
    @GetMapping("/{flag}")
    public void downloadFile(@PathVariable String flag, HttpServletResponse response) {

        OutputStream os;
        // 获取目录下所有文件名
        List<String> filenames = FileUtil.listFileNames(rootFilePath);
        // 根据UUID前缀查找对应的文件名
        String targetFilename = filenames.stream()
                .filter(name -> name.contains(flag))
                .findFirst() // 更改为findFirst，避免不必要的orElse调用
                .orElse(null);

        if (StringUtils.isNotEmpty(targetFilename)) {
            try {
                // 设置响应头，指示浏览器以附件形式下载文件
                response.addHeader("Content-Disposition", "attachment;filename=" + targetFilename);
                response.setContentType("application/octet-stream");

                // 读取文件内容并输出到HTTP响应中
                byte[] fileBytes = FileUtil.readBytes(rootFilePath + targetFilename);
                os = response.getOutputStream();
                os.write(fileBytes);
                os.flush();
                os.close();
            } catch (IOException e) {
                // 当文件读写或输出过程中发生错误时，包装为运行时异常抛出
                throw new RuntimeException("Failed to download file: " + targetFilename, e);
            }
        }
    }
}

