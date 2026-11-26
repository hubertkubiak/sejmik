package pl.hubert.sejmik.api.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.hubert.sejmik.api.repository.entity.GameEntity;

/**
 * Spring Data JPA: an interface with no implementation. Spring generates one at startup, with
 * {@code save}, {@code findById}, {@code findAll}, {@code delete} and more, all from the generic types.
 */
interface GameEntityRepository extends JpaRepository<GameEntity, UUID> {
}
