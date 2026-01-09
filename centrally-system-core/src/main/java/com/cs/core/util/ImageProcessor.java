package com.cs.core.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import com.cs.core.handler.GlobalExceptionHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 공통 이미지 압축/리사이즈 유틸리티.<br/>
 * <ul>
 *   <li>maxSizeMb, maxDimension(px)를 초과하는 이미지는 Thumbnailator로 리사이즈 및 품질 조정</li>
 *   <li>처리 후에도 용량 제한을 초과하면 {@link ImageTooLargeException} 예외 발생</li>
 * </ul>
 */
public final class ImageProcessor {

    private ImageProcessor() {}

    /**
     * MultipartFile 을 지정 디렉터리에 저장하면서 조건을 초과할 경우 자동으로 압축/리사이즈합니다.
     *
     * @param multipart     원본 MultipartFile
     * @param destDir       저장 디렉터리 (없으면 생성)
     * @param safeFileName  저장할 파일명 (경로 제외)
     * @param maxSizeMb     허용 최대 용량(MB)
     * @param maxDimension  허용 최대 너비/높이(px)
     * @return 저장된 File 객체
     */
    public static File processAndSave(MultipartFile multipart,
                                      File destDir,
                                      String safeFileName,
                                      int maxSizeMb,
                                      int maxDimension) throws IOException, ImageTooLargeException {
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new IOException(GlobalExceptionHandler.CC + "Fail to create directory " + destDir);
        }
        File destFile = new File(destDir, safeFileName);

        Path temp = Files.createTempFile("receipt_img", ".tmp");
        try {
            // 원본을 임시 파일에 복사
            try (InputStream in = multipart.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            // 2) 이미지 읽기
            BufferedImage srcImg;
            try (InputStream tIn = Files.newInputStream(temp)) {
                srcImg = ImageIO.read(tIn);
            }
            if (srcImg == null) {
                throw new IOException(GlobalExceptionHandler.CC + "지원하지 않는 이미지 포맷입니다.");
            }

            // 임계치(Byte) 계산
            long maxBytes = (long) maxSizeMb * 1024 * 1024;
            long originalSize = Files.size(temp);

            // 3) 용량이 임계치 이하이면 그대로 저장 후 종료
            if (originalSize <= maxBytes) {
                Files.move(temp, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return destFile;
            }

            // 4) 임계치 초과 → 한 번만 압축/리사이즈 시도
            Thumbnails.of(temp.toFile())
                      .size(maxDimension, maxDimension)
                      .useExifOrientation(true)
                      .outputQuality(0.9f)
                      .toFile(destFile);

            // 5) 최종 용량 체크: 여전히 초과하면 예외
            if (destFile.length() > maxBytes) {
                destFile.delete();
                throw new ImageTooLargeException(
                    GlobalExceptionHandler.CC + "이미지 용량이 제한(" + maxSizeMb + "MB)을 초과했습니다."
                );
            }

            return destFile;
        } finally {
            // 임시 파일 정리 (이미 move 되었거나 존재하지 않으면 무시)
            Files.deleteIfExists(temp);
        }
    }

    public static class ImageTooLargeException extends Exception {
        public ImageTooLargeException(String msg) {
            super(msg);
        }
    }
} 