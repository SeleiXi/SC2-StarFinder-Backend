package com.starfinder.controller;

import com.starfinder.dto.EventDTO;
import com.starfinder.dto.Result;
import com.starfinder.entity.Event;
import com.starfinder.security.AuthContext;
import com.starfinder.security.AuthPrincipal;
import com.starfinder.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    public Result<Event> createEvent(@RequestBody EventDTO dto,
            @RequestParam(required = false) Long userId) {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) return Result.BadRequest("需要登录");
        if (userId != null && !userId.equals(principal.userId())) {
            return Result.BadRequest("无权限");
        }
        return eventService.createEvent(dto, principal.userId());
    }

    @GetMapping("/list")
    public List<Event> getEvents() {
        return eventService.getApprovedEvents();
    }

    @GetMapping("/all")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @PutMapping("/{id}/approve")
    public Result<Event> approveEvent(@PathVariable Long id) {
        return eventService.approveEvent(id);
    }
}
