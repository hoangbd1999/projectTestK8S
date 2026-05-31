package com.elcom.adminconsolebackend.entity.management;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@Accessors(chain = true)
@IdClass(WarningMessageReadEntity.PK.class)
@Table(name = "warning_message_read", schema = "admin_console")
public class WarningMessageReadEntity implements Serializable {

    @Id
    private String userId;

    @Id
    private String warningId;;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @Embeddable
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class PK implements Serializable {

        private String userId;

        private String warningId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PK pk = (PK) o;
            return Objects.equals(warningId, pk.warningId) &&
                    Objects.equals(userId, pk.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(warningId, userId);
        }
    }

}
