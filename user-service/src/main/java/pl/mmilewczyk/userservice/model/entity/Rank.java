package pl.mmilewczyk.userservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.mmilewczyk.userservice.model.enums.RankName;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Rank {

    @Id
    @SequenceGenerator(name = "rank_id_sequence", sequenceName = "rank_id_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rank_id_sequence")
    private Long rankId;
    @Enumerated(EnumType.STRING)
    private RankName rankName;
    private String icon;
}
