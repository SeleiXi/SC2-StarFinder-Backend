package com.starfinder.controller;

import com.starfinder.dto.Result;
import com.starfinder.entity.Stream;
import com.starfinder.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stream")
public class StreamController {

    @Autowired
    private StreamService streamService;

    @PostMapping
    public Result<Stream> addStream(@RequestBody Stream stream) {
        return streamService.addStream(stream);
    }

    @GetMapping("/list")
    public Result<List<Stream>> listStreams() {
        return streamService.getAllStreams();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteStream(@PathVariable Long id, @RequestParam Long adminId) {
        return streamService.deleteStream(id, adminId);
    }
}
