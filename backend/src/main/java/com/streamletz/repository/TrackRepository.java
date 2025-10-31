package com.streamletz.repository;

import com.streamletz.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    List<Track> findByTitleContainingIgnoreCase(String title);

    List<Track> findByArtistContainingIgnoreCase(String artist);

    List<Track> findByAlbumContainingIgnoreCase(String album);

    List<Track> findByUploadedById(Long userId);

    Optional<Track> findByFilePath(String filePath);
}