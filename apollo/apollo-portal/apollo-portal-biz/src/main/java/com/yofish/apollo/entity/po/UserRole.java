package com.yofish.apollo.entity.po;

import com.yofish.gary.entity.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@Entity
@Table(name = "UserRole")
@SQLDelete(sql = "Update UserRole set isDeleted = 1 where id = ?")
@Where(clause = "isDeleted = 0")
public class UserRole extends BaseEntity {
  @Column(name = "UserId", nullable = false)
  private String userId;

  @Column(name = "RoleId", nullable = false)
  private long roleId;

}