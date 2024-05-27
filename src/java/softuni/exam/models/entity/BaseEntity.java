package softuni.exam.models.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    protected BaseEntity(){}

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }
}
