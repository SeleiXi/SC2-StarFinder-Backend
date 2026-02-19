package com.starfinder.service;

import com.starfinder.dto.EventDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Event;

import java.util.List;

public interface EventService {
    Result<Event> createEvent(EventDTO dto, Long submittedBy);

    List<Event> getApprovedEvents();

    List<Event> getAllEvents();

    Result<Event> approveEvent(Long id);
}
