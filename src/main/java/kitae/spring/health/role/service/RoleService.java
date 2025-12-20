package kitae.spring.health.role.service;

import kitae.spring.health.exceptions.NotFoundException;
import kitae.spring.health.response.Response;
import kitae.spring.health.role.entity.Role;
import kitae.spring.health.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * 역할(Role) 생성
     *
     * @param role 생성할 역할(Role) 객체
     * @return 생성된 역할(Role) 정보가 포함된 Response 객체
     */
    public Response<Role> createRole(Role role) {

        Role savedRole = roleRepository.save(role);

        return Response.<Role>builder()
                .statusCode(HttpStatus.OK.value())
                .message("역할(Role)이 성공적으로 생성되었습니다.")
                .data(savedRole)
                .build();
    }

    /**
     * 역할(Role) 수정
     *
     * @param role 수정할 역할(Role) 객체
     * @return 수정된 역할(Role) 정보가 포함된 Response 객체
     */
    public Response<Role> updateRole(Role role) {

        Role savedRole = roleRepository.findById(role.getId())
                .orElseThrow(() -> new NotFoundException("역할(Role)을 찾을 수 없습니다. ID: " + role.getId()));

        savedRole.setName(role.getName());

        Role updatedRole = roleRepository.save(savedRole);

        return Response.<Role>builder()
                .statusCode(HttpStatus.OK.value())
                .message("역할(Role)이 성공적으로 수정되었습니다.")
                .data(updatedRole)
                .build();
    }

    /**
     * 모든 역할(Role) 조회
     *
     * @return 모든 역할(Role) 정보가 포함된 Response 객체
     */
    public Response<List<Role>> getAllRoles() {

        List<Role> savedRoles = roleRepository.findAll();
        return Response.<List<Role>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("모든 역할(Role) 정보를 성공적으로 조회하였습니다.")
                .data(savedRoles)
                .build();
    }

    /**
     * 역할(Role) 삭제
     *
     * @param roleId 삭제할 역할(Role)의 ID
     * @return 삭제 성공 메시지가 포함된 Response 객체
     */
    public Response<?> deleteRole(Long roleId) {
        if(!roleRepository.existsById(roleId)) {
            throw new NotFoundException("역할(Role)을 찾을 수 없습니다. ID: " + roleId);
        }

        roleRepository.deleteById(roleId);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("역할(Role)이 성공적으로 삭제되었습니다.")
                .data(null)
                .build();
    }
}
