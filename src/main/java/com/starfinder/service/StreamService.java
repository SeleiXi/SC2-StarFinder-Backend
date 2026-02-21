package com.starfinder.service;

import com.starfinder.dto.Result;
import com.starfinder.entity.Stream;
import java.util.List;

public interface StreamService {
    Result<Stream> addStream(Stream stream);
    Result<List<Stream>> getAllStreams();
    Result<Void> deleteStream(Long id, Long adminId);
}
