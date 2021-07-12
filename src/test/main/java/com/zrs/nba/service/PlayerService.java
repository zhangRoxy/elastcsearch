package com.zrs.nba.service;

import com.zrs.nba.pojo.Player;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface PlayerService {
    List<Player> getAllPlayers();

    Map<String, Object> getPLayer(String id) throws IOException;

    void addPlayer(Player player, String id) throws IOException;

    boolean updatePlayer(String player, String id) throws IOException;

    boolean deletePlayer(String id) throws IOException;

    boolean deleteAllPlayer() throws IOException;

    boolean importAll() throws IOException;

    List<Player> searchMatch(String key, String value) throws IOException;

    List<Player> searchTerm(String key, String value) throws IOException;

    List<Player> searchMatchPrefix(String key, String value) throws IOException;
}
