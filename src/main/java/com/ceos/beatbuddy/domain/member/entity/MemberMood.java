package com.ceos.beatbuddy.domain.member.entity;

import com.ceos.beatbuddy.domain.archive.entity.Archive;
import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMood extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;

    @Transient
    private Vector moodVector;

    @Lob
    private String moodVectorString;

    @OneToMany(mappedBy = "memberMood", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Archive> archives;

    public void setMoodVector(Vector vector) {
        this.moodVector = vector;
        this.moodVectorString = vector.getElements().toString();
    }

    public Vector getMoodVector() {
        if (moodVector == null && moodVectorString != null) {
            List<Double> elements = Stream.of(moodVectorString.replace("[", "").replace("]", "").split(",")).map(String::trim).map(Double::parseDouble).collect(Collectors.toList());
            moodVector = new Vector(elements);
        }
        return moodVector;
    }
}