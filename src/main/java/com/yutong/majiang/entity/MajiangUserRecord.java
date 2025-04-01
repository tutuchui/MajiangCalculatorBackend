package com.yutong.majiang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MAJIANG_USER_RECORD")
@Getter
@Setter
public class MajiangUserRecord {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "win_matches")
    private Integer winMatches;

    @Column(name = "lose_matches")
    private Integer loseMatches;

    @Column(name = "total_score")
    private Integer totalScore;
}
