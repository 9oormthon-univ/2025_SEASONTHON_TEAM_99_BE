package seasonton.youthPolicy.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seasonton.youthPolicy.domain.post.converter.PostConverter;
import seasonton.youthPolicy.domain.post.domain.entity.PostImage;
import seasonton.youthPolicy.domain.post.domain.entity.Posts;
import seasonton.youthPolicy.domain.post.domain.repository.PostImageRepository;
import seasonton.youthPolicy.domain.post.domain.repository.PostRepository;
import seasonton.youthPolicy.domain.post.dto.PostRequestDTO;
import seasonton.youthPolicy.domain.post.dto.PostResponseDTO;
import seasonton.youthPolicy.domain.user.domain.entity.Region;
import seasonton.youthPolicy.domain.user.domain.entity.User;
import seasonton.youthPolicy.domain.user.domain.repository.UserRepository;
import seasonton.youthPolicy.domain.user.exception.UserException;
import seasonton.youthPolicy.global.dto.S3DTO;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;
import seasonton.youthPolicy.global.service.S3Service;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponseDTO.PostCreateResponse createPost(Long userId, String title, String content, Long regionId, boolean isAnonymous, List<MultipartFile> images) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.USER_NOT_FIND));

        Region region = user.getRegion();

        Posts post = Posts.builder()
                .title(title)
                .content(content)
                .is_anonymous(isAnonymous)
                .region(region)
                .user(user)
                .build();

        postRepository.save(post);

        // 이미지 업로드
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                S3DTO.UploadResult result = s3Service.uploadFile(image);

                PostImage postImage = PostImage.builder()
                        .originalName(result.getOriginalName())
                        .storedName(result.getStoredName())
                        .imageUrl(result.getUrl())
                        .post(post)
                        .build();

                postImageRepository.save(postImage);
            }
        }


        return PostConverter.toPostCreateResponse(post, region);
    }


}