package seasonton.youthPolicy.domain.test.converter;

import seasonton.youthPolicy.domain.test.domain.entity.TestEntity;
import seasonton.youthPolicy.domain.test.dto.TestResponseDTO;

public class TestConverter {

    public static TestEntity toEntity(long fac) {
        return TestEntity.builder()
                .fac(fac)
                .build();
    }

    public static TestResponseDTO.TestResponse toTestResponse(long result) {
        return TestResponseDTO.TestResponse.builder()
                .result(result)
                .build();
    }
}
