package kitae.spring.health.role.repository;

import kitae.spring.health.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByName(String name);

}
