package com.draper.dao.util;

import com.draper.entity.Music;
import org.apache.commons.io.IOUtils;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

public class Mp3Util {

    private Mp3Util() {
    }

    /**
     * 获取MP3歌曲名、歌手、时长等具体的信息
     *
     * @param mp3File
     * @return
     */
    public static Music getMP3Info(File mp3File) {
        Music music = new Music();
        try {
            MP3File file = new MP3File(mp3File);
            AbstractID3v2Tag v2tag = file.getID3v2Tag();

            // FIXME: 2018/9/20 虽然会牺牲一些性能，但是代码便于阅读和维护
            String name = v2tag.getFirst(ID3v24Frames.FRAME_ID_TITLE);
            Blob musicData = new SerialBlob(IOUtils.toByteArray(new FileInputStream(mp3File)));
            String singer = v2tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST);
            Integer duration = file.getAudioHeader().getTrackLength();
            Blob imageData = new SerialBlob(getMP3Image(mp3File));

            music.setName(name);
            music.setMusicData(musicData);
            music.setSinger(singer);
            music.setDuration(duration);
            music.setImageData(imageData);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TagException e) {
            e.printStackTrace();
        } catch (ReadOnlyFileException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        } catch (SerialException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return music;
    }

    /**
     * 获取MP3封面图片
     *
     * @param mp3File
     * @return
     */
    public static byte[] getMP3Image(File mp3File) {
        byte[] imageData = null;
        try {
            MP3File mp3file = new MP3File(mp3File);
            AbstractID3v2Tag tag = mp3file.getID3v2Tag();
            AbstractID3v2Frame frame = (AbstractID3v2Frame) tag.getFrame("APIC");
            FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();

            imageData = body.getImageData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageData;
    }


    /**
     * 获取mp3图片并将其保存至指定路径下
     *
     * @param mp3File          mp3文件对象
     * @param mp3ImageSavePath mp3图片保存位置（默认mp3ImageSavePath +"\" mp3File文件名 +".jpg" ）
     * @param cover            是否覆盖已有图片
     * @return 生成图片全路径
     */
    public static String saveMP3Image(File mp3File, String mp3ImageSavePath, boolean cover) {
        //生成mp3图片路径
        String mp3FileLabel = getFileLabel(mp3File.getName());
        String mp3ImageFullPath = mp3ImageSavePath + (mp3FileLabel + ".jpg");

        //若为非覆盖模式，图片存在则直接返回（不再创建）
        if (!cover) {
            File tempFile = new File(mp3ImageFullPath);
            if (tempFile.exists()) {
                return mp3ImageFullPath;
            }
        }

        //生成mp3存放目录
        File saveDirectory = new File(mp3ImageSavePath);
        saveDirectory.mkdirs();

        //获取mp3图片
        byte imageData[] = getMP3Image(mp3File);
        //若图片不存在，则直接返回null
        if (null == imageData || imageData.length == 0) {
            return null;
        }
        //保存mp3图片文件
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mp3ImageFullPath);
            fos.write(imageData);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mp3ImageFullPath;
    }


    /**
     * 仅返回文件名（不包含.类型）
     *
     * @param fileName
     * @return
     */
    private static String getFileLabel(String fileName) {
        int indexOfDot = fileName.lastIndexOf(".");
        fileName = fileName.substring(0, (indexOfDot == -1 ? fileName.length() : indexOfDot));
        return fileName;
    }

    private static String toGB2312(String s) {
        try {
            return new String(s.getBytes("ISO-8859-1"), "gb2312");
        } catch (Exception e) {
            return s;
        }
    }
}
