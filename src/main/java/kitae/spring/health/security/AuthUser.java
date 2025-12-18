package kitae.spring.health.security;

import kitae.spring.health.users.entity.User;
import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Builder
@Data
public class AuthUser implements UserDetails {

    private User user;

    /**
     * 현재 사용자에 부여된 권한 목록을 반환합니다.
     * User#getRole().name() 은 `ROLE_ADMIN`, `ROLE_USER` 처럼 스프링 시큐리티에서 사용하는 권한 이름입니다.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    /**
     * 사용자의 암호를 반환합니다. (암호화된 상태여야 합니다)
     */
    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자의 로그인 식별자(username)를 반환합니다. 여기서는 이메일을 사용합니다.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * 계정 만료 여부를 반환합니다. true면 만료되지 않음을 의미합니다.
     * 현재는 단순화를 위해 항상 true를 반환합니다.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부를 반환합니다. true면 잠금되지 않음을 의미합니다.
     * 현재는 단순화를 위해 항상 true를 반환합니다.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격증명(비밀번호) 만료 여부를 반환합니다. true면 만료되지 않음을 의미합니다.
     * 현재는 단순화를 위해 항상 true를 반환합니다.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화(enabled) 여부를 반환합니다. true면 활성 상태입니다.
     * 현재는 단순화를 위해 항상 true를 반환합니다.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
