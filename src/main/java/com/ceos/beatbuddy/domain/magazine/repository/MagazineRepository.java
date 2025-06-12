package com.ceos.beatbuddy.domain.magazine.repository;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MagazineRepository extends JpaRepository<Magazine, Long> {
    List<Magazine> findMagazinesByIsVisibleTrue();

    Optional<Magazine> findByIdAndIsVisibleTrue(Long id);
}
