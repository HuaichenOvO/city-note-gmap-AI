package com.citynote.mapper;

import com.citynote.entity.EventEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EventMapper {
    EventEntity selectEventById(Long id);
    List<EventEntity> getEventsByCounty(String countyId);
    List<EventEntity> getEventsByAuthor(String userId);
    int insertEvent(EventEntity event);
    int updateEvent(EventEntity event);
    int deleteEventById(Long id);
}
