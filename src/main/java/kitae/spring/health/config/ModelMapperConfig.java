package kitae.spring.health.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

  @Bean
  public ModelMapper modelMapper() {
    // ModelMapper 객체 생성
    ModelMapper modelMapper = new ModelMapper();

    // ModelMapper의 설정(Configuration)에 접근하여 상세 옵션 지정
    modelMapper.getConfiguration()

        // 1. 필드 매칭 활성화 (기본값은 비활성화)
        //    : true로 설정 시, ModelMapper가 getter/setter 메서드뿐만 아니라
        //      필드(멤버 변수) 이름을 기준으로도 매칭을 시도합니다.
        .setFieldMatchingEnabled(true)

        // 2. 필드 접근 레벨 설정 (PRIVATE)
        //    : ModelMapper가 접근할 수 있는 필드의 최소 접근 제어자 레벨을 설정합니다.
        //      AccessLevel.PRIVATE으로 설정하면, private으로 선언된 필드에도
        //      리플렉션(Reflection)을 통해 직접 접근하여 값을 읽고 쓸 수 있게 됩니다.
        //      (대부분의 Entity나 DTO의 필드는 private이므로 권장되는 설정입니다.)
        .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)

        // 3. 매칭 전략 설정 (STANDARD)
        //    : MatchingStrategies.STANDARD (표준 전략)을 사용합니다.
        //      이 전략은 목적지(Destination) 객체의 모든 프로퍼티(필드)가
        //      원본(Source) 객체에서 매칭되는 필드를 가져야 함을 의미합니다.
        //      만약 하나라도 매칭되지 않으면 예외(ConfigurationException)가 발생합니다.
        //      (참고: LOOSE 전략은 더 유연하게 매칭을 시도합니다.)
        .setMatchingStrategy(MatchingStrategies.STANDARD);

    // 설정이 완료된 ModelMapper 객체를 반환
    return modelMapper;
  }
}