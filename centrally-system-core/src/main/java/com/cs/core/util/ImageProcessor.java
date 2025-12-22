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
        // 포트폴리오용 - 구현 제거됨
        // 기능: 이미지 압축/리사이즈 및 저장 처리
        // - 디렉터리 생성
        // - 임시 파일로 원본 복사
        // - 이미지 포맷 검증 (ImageIO)
        // - 용량 체크 및 임계치 계산
        // - 용량 초과 시 Thumbnailator를 사용한 압축/리사이즈 처리
        // - EXIF 방향 정보 보존
        // - 최종 용량 재검증 및 예외 처리
        // - 임시 파일 정리
        throw new IOException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }

    public static class ImageTooLargeException extends Exception {
        public ImageTooLargeException(String msg) {
            super(msg);
        }
    }
} 