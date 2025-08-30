package seasonton.youthPolicy.domain.post.converter;

import seasonton.youthPolicy.domain.post.domain.entity.Posts;
import seasonton.youthPolicy.domain.post.dto.PostResponseDTO;
import seasonton.youthPolicy.domain.user.domain.entity.Region;

public class PostConverter {

    public static PostResponseDTO.PostCreateResponse toPostCreateResponse(
            Posts post,
            Region region
    ) {
        return PostResponseDTO.PostCreateResponse.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .is_anonymous(post.is_anonymous())
                .regionName(region.getRegionName())
                .build();
    }
}
