package com.starfinder.service.impl;

import com.starfinder.dto.EventDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Event;
import com.starfinder.mapper.EventMapper;
import com.starfinder.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventMapper eventMapper;

    @Override
    public Result<Event> createEvent(EventDTO dto, Long submittedBy) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setRules(dto.getRules());
        event.setRewards(dto.getRewards());
        event.setContactLink(dto.getContactLink());
        event.setGroupLink(dto.getGroupLink());
        event.setSubmittedBy(submittedBy);
        event.setStatus("pending");
        event.setRegion(dto.getRegion());
        event.setStartTime(dto.getStartTime());
        eventMapper.insert(event);
        return Result.success(event);
    }

    @Override
    public List<Event> getApprovedEvents() {
        return eventMapper.findAllApproved();
    }

    @Override
    public List<Event> getAllEvents() {
        return eventMapper.findAll();
    }

    @Override
    public Result<Event> approveEvent(Long id) {
        Event event = eventMapper.findById(id);
        if (event == null) {
            return Result.BadRequest("赛事不存在");
        }
        eventMapper.updateStatus(id, "approved");
        event.setStatus("approved");
        return Result.success(event);
    }
}
