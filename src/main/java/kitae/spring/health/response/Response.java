package kitae.spring.health.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private int statusCode; // 상태 코드

    private String message; // 응답 메시지

    private T data; // 응답 데이터
}
