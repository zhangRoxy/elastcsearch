package com.zrs.nba.mapper;

import com.zrs.nba.pojo.Player;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlayerMapper {

   //@Select("select * from test1.nba_player")
   List<Player> getAllPlayers();
}
