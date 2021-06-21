package fr.feepin.go4lunch.data.models.mappers;

public interface Mapper<Dto, Entity> {

    Entity toEntity(Dto dto);

}
