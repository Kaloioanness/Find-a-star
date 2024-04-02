package softuni.exam.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import softuni.exam.models.entity.Star;

import java.util.Optional;
import java.util.Set;

@Repository
public interface StarRepository extends JpaRepository<Star, Long> {

    Optional<Star> findByName(String name);

    //Filter only stars who are Red Giants and have never been observed and order them by the light years in ascending order.

    @Query(value = "SELECT s FROM Star s WHERE s.starType = 'RED_GIANT' and SIZE(s.astronomers) = 0 ORDER BY s.lightYears")
    Set<Star> getStarByStarTypeOrderedByLightYears();
}
