package kitae.spring.health.users.service;

import kitae.spring.health.exceptions.BadRequestException;
import kitae.spring.health.exceptions.NotFoundException;
import kitae.spring.health.notification.dto.NotificationDTO;
import kitae.spring.health.notification.entity.Notification;
import kitae.spring.health.notification.service.NotificationService;
import kitae.spring.health.response.Response;
import kitae.spring.health.users.dto.UpdatePasswordRequest;
import kitae.spring.health.users.dto.UserDTO;
import kitae.spring.health.users.entity.User;
import kitae.spring.health.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.bytecode.internal.bytebuddy.PassThroughInterceptor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    // 파일 업로드 위치 설정
    private final String uploadDir = "uploads/profile-pictures/"; // 백엔드 이미지 저장 위치
//    private final String uploadDir = "/Users/mac/phegonDev/dat-react/public/profile-picture/"; // 프론트엔드 이미지 저장 위치

    /**
     * 현재 인증된 사용자 정보 가져오기
     * @return
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null){
            throw new NotFoundException("현재 인증된 사용자가 없습니다.");
        }
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * 내 정보 가져오기
     * @return
     */
    public Response<UserDTO> getMyUserDetails() {

        User user = getCurrentUser(); // 현재 인증된 사용자 정보 가져오기
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
            .statusCode(200)
            .message("내 정보 조회 성공")
            .data(userDTO)
            .build();
    }

    /**
     * 사용자 ID로 사용자 조회
     * @param userId
     * @return
     */
    public Response<UserDTO> getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(200)
                .message("사용자 조회 성공")
                .data(userDTO)
                .build();
    }

    /**
     * 모든 사용자 조회
     * @return
     */
    public Response<List<UserDTO>> getAllUsers() {

        List<UserDTO> userDTOS = userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();

        return Response.<List<UserDTO>>builder()
                .statusCode(200)
                .message("모든 사용자 조회 성공")
                .data(userDTOS)
                .build();
    }

    /**
     * 비밀번호 변경
     * @param updatePasswordRequest
     * @return
     */
    public Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {

        User user = getCurrentUser();

        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();

        if(oldPassword == null || newPassword == null) {
            throw new BadRequestException("이전 비밀번호와 새 비밀번호를 모두 입력해야 합니다.");
        }

        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("이전 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));  // 새 비밀번호 인코딩 후 설정
        userRepository.save(user);

        // 비밀번호 변경 알림 전송 (예: 이메일)
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("귀하의 계정 비밀번호가 성공적으로 변경되었습니다.")
                .templateName("password-change")
                .templateVariables(Map.of(
                        "name", user.getName()
                ))
                .build();

        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(200)
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .build();
    }

    /**
     * 프로필 사진 업로드
     * @param file
     * @return
     */
    public Response<?> uploadProfilePicture(MultipartFile file) {

        User user = getCurrentUser();

        try {
            Path uploadPath = Paths.get(uploadDir); // 업로드 디렉토리 경로 설정

            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);    // 디렉토리 없으면 생성
            }

            if(user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                // 기존 프로필 사진 파일 삭제
                Path oldFile = Paths.get(user.getProfilePictureUrl());
                if(Files.exists(oldFile)) {
                    Files.delete(oldFile);  // 기존 파일 삭제
                }
            }

            // 충돌 방지를 위해 유일한 파일명으로 저장
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if(originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));  // 파일 확장자 추출
            }

            String newFileName = UUID.randomUUID()+ fileExtension; // 새로운 파일명 생성
            Path filePath = uploadPath.resolve(newFileName);    // 업로드 경로에 새로운 파일명 결합

            Files.copy(file.getInputStream(), filePath); // 파일 저장

            String fileUrl = uploadDir + newFileName;
//            String fileUrl = "/profile-picture/" + newFileName;

            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            return Response.builder()
                    .statusCode(200)
                    .message("프로필 사진 업로드 성공")
                    .data(fileUrl)
                    .build();

        } catch(IOException e) {
            throw new RuntimeException("프로필 사진 업로드에 실패했습니다.\n" + e.getMessage());
        }
    }

    /**
     * S3에 프로필 사진 업로드
     * @param file
     * @return
     */
    public Response<?> uploadProfilePictureToS3(MultipartFile file) {
        return null;
    }
}
