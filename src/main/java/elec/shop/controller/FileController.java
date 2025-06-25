package elec.shop.controller;

import elec.shop.pojo.sys.dto.UpdateProfileDTO;
import elec.shop.service.sys.SysUserService;
import elec.shop.utils.AllContextUtils;
import elec.shop.utils.MinioUtil;
import elec.shop.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioUtil minioUtil;
    private final SysUserService userService;

    /**
     * 上传文件
     * @param file 文件
     * @param dir 目录（可选）
     * @return 文件信息
     */
    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public Result uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dir", defaultValue = "") String dir) {
        try {
            // 上传文件到MinIO
            String fileName = minioUtil.upload(file, dir);

            // 构建返回结果
            Map<String, String> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("originalFileName", file.getOriginalFilename());
            result.put("previewUrl", minioUtil.getPreviewUrl(fileName));
            result.put("downloadUrl", minioUtil.getDownloadUrl(fileName));

            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail().message("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 更新当前用户头像
     * @param file 头像文件
     * @return 更新结果
     */
    @ApiOperation("更新当前用户头像")
    @PostMapping("/avatar")
    public Result updateAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.fail().message("只能上传图片文件");
            }

            // 获取当前登录用户
            Long userId = AllContextUtils.getLoginSysUser().getUserId();
            if (userService.getById(userId).getAvatar() != null) {
                boolean objectExist = minioUtil.isObjectExist("elec-shop", userService.getById(userId).getAvatar());
                if (objectExist)
                    // 删除旧头像
                    minioUtil.remove(userService.getById(userId).getAvatar());
            }
            // 上传头像到MinIO
            String fileName = minioUtil.upload(file, "avatar/");
            String previewUrl = minioUtil.getPreviewUrl(fileName);

            UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
            updateProfileDTO.setAvatar(fileName);
            // 更新用户头像
            userService.updateProfile(userId, updateProfileDTO);

            Map<String, String> result = new HashMap<>();
            result.put("avatar", previewUrl);

            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail().message("更新头像失败：" + e.getMessage());
        }
    }

    /**
     * 预览文件
     * @param fileName 文件名
     * @param response HTTP响应
     */
    @ApiOperation("预览文件")
    @GetMapping("/preview/{fileName}")
    public void previewFile(
            @PathVariable String fileName,
            HttpServletResponse response) {
        minioUtil.getFile(fileName, response);
    }

    /**
     * 下载文件
     * @param fileName 文件名
     * @param delete 下载后是否删除（可选）
     * @param response HTTP响应
     */
    @ApiOperation("下载文件")
    @GetMapping("/download/{fileName}")
    public void downloadFile(
            @PathVariable String fileName,
            @RequestParam(value = "delete", defaultValue = "false") Boolean delete,
            HttpServletResponse response) {
        minioUtil.fileDownload(fileName, delete, response);
    }

    /**
     * 删除文件
     * @param fileName 文件名
     * @return 操作结果
     */
    @ApiOperation("删除文件")
    @DeleteMapping("/{fileName}")
    public Result deleteFile(@PathVariable String fileName) {
        try {
            minioUtil.remove(fileName);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail().message("文件删除失败：" + e.getMessage());
        }
    }
}
