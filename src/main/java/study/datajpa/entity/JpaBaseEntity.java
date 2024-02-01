package study.datajpa.entity;

import static java.time.LocalDateTime.now;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;

// 속성만 상속받는다.
@MappedSuperclass
@Getter
public class JpaBaseEntity {

	@Column(updatable = false)
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;

	/**
	 * Persist전에 event 발생
	 */
	@PrePersist
	public void prePersist() {
		LocalDateTime now = now();
		createdDate = now;
		updatedDate = now;
	}

	@PreUpdate
	public void preUpdate() {
		updatedDate = now();
	}
}
