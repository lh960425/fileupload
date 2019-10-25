package com.cool.study.common;

import com.alibaba.fastjson.JSON;
import com.cool.study.entity.vo.FileBean;
import com.cool.study.utils.ImageProperties;
import com.cool.study.utils.PubUtils;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName uploadfile
 * @Author liuhuan
 * @Description
 * @Date 2019/10/9 17:32
 */
@RestController
@Slf4j
@RequestMapping("upload")
public class Uploadfile {

    @Autowired
    private ImageProperties image;


    @PostMapping("fileupload")
    public List<FileBean> upload(Integer type,MultipartFile... file){
        String imageAccessUrl = "";
        String uploadDir = "";
        if (type==1){
            imageAccessUrl = "http://" + image.getDomain() + image.getAccessDir();
            uploadDir = image.getUploadDir();
        }else {
            imageAccessUrl = image.getDomain() + image.getVideoaccessDir();
            uploadDir = image.getVideouploadDir();
        }
        List<FileBean> fileBeans = new ArrayList<>();
        if(file == null) {
            return fileBeans;
        }
        Integer length = file.length;
        log.info("长度为",length);
        for(int i=0;i<file.length;i++) {
            if (!file[i].isEmpty()) {
                String fileName = file[i].getOriginalFilename();
                String suffixName = fileName.substring(fileName.lastIndexOf("."));
                // 使用UUID确保上传文件不重复
                String newFileName = PubUtils.uuid() + suffixName;
                log.info("文件名为：",newFileName);
                String realFileUri = uploadDir + newFileName;
                //检查文件目录是否存在
                File dest = new File(realFileUri);
                //检测是否存在目录
                if (!dest.getParentFile().exists()) {
                    //创建新目录
                    dest.getParentFile().mkdirs();
                }
                //上传文件
                try {
                    file[i].transferTo(dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //生成访问url
                String accessUri = "http://" + image.getDomain() +"/"+ newFileName;
                log.info("图片访问-路径：{}",accessUri);
                FileBean fileBean = FileBean.builder()
                        .originalName(fileName)
                        .currentName(newFileName)
                        .realAdr(realFileUri)
                        .accessAdr(accessUri)
                        .build();
                fileBeans.add(fileBean);
                log.info("第{}个文件上传成功",i+1);
            }
        }
        log.info("图片上传完成，返回结果集：{}", JSON.toJSONString(fileBeans));
        return fileBeans;
    }
}
