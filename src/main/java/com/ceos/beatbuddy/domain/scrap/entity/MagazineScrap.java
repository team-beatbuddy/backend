package com.ceos.beatbuddy.domain.scrap.entity;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MagazineScrap extends AbstractScrap{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magazineId")
    private Magazine magazine;
}
