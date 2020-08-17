package com.nikolaynikolov.primenumberapi.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "permission", schema = "public")
public class Permission implements Serializable {

  public static final String ACCESS_PRIME_API = "ACCESS_PRIME_API";

  @Id
  private String name;

  @ManyToMany(mappedBy = "permissions")
  private Set<User> users;

  public Permission() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || (getClass() != o.getClass() && !o.getClass().equals(String.class))) return false;
    if (o instanceof Permission) {
      Permission that = (Permission) o;
      return Objects.equals(name, that.name);
    } else {
      String permission = (String) o;
      return Objects.equals(permission, this.getName());
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
