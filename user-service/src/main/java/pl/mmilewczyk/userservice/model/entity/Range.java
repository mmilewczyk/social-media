package pl.mmilewczyk.userservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mmilewczyk.userservice.model.enums.RangeName;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Range {

    @Id
    @SequenceGenerator(name = "range_id_sequence", sequenceName = "range_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "range_id_sequence")
    private Long rangeId;
    @Enumerated(EnumType.STRING)
    private RangeName rangeName;
    private String icon;
}
