package com.draper.dao;

import com.draper.entity.Music;
import org.apache.ibatis.annotations.Param;

import java.io.File;
import java.sql.Blob;
import java.util.List;

public interface MusicDao {

    void add(File file);

    void insertMusic(@Param("name") String name, @Param("musicData") Blob musicData, @Param("singer") String singer, @Param("duration") int duration, @Param("imageData") Blob imageData);

    Music queryByName(@Param("name") String name);

    String findImage(String name);

    List<Music> findMusicInfoList();

    String findMusicData(String name);

    int getMusicNum();

    List<String> findMusicNameList();
}
