package jdm.model;

public record PublicRelationType(
        Integer id,
        String name,
        String gpname,
        Double quot,
        Double quotmin,
        Double quotmax,
        Integer price,
        String help,
        Integer playable,
        Integer oppos,
        String posyes,
        String posno,
        Integer constraint_ent,
        String constraints_start,
        String constraints_end,
        String carac
) {}
