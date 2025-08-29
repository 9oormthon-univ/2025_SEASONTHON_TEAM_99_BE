package seasonton.youthPolicy.domain.test.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seasonton.youthPolicy.domain.test.converter.TestConverter;
import seasonton.youthPolicy.domain.test.domain.entity.TestEntity;
import seasonton.youthPolicy.domain.test.domain.repository.TestRepository;
import seasonton.youthPolicy.domain.test.dto.TestRequestDTO;
import seasonton.youthPolicy.domain.test.dto.TestResponseDTO;
import seasonton.youthPolicy.domain.test.exception.TestException;
import seasonton.youthPolicy.global.error.code.status.ErrorStatus;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;

    @Transactional
    public TestResponseDTO.TestResponse test(TestRequestDTO.TestRequest testRequest) {

        long value = testRequest.getNumber();

        long result = factorial(value);

        TestEntity entity = TestConverter.toEntity(result);
        testRepository.save(entity);

        return TestConverter.toTestResponse(result);
    }

    private long factorial(long number) {

        if (number < 0) {
            throw new TestException(ErrorStatus.ILLEGAL_ARGUMENT_INPUT);
        }

        if (number == 0 || number == 1) {
            return 1;
        }

        return number * factorial(number - 1);
    }
}
