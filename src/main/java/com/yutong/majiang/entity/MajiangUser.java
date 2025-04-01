package com.yutong.majiang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "MAJIANG_USER")
@Getter
@Setter
public class MajiangUser {

    @Column(name = "user_id")
    @Id
    private String userId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "create_date")
    private Timestamp createDate;
}
