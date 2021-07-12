package com.zrs.nba;

import com.zrs.nba.mapper.PlayerMapper;
import com.zrs.nba.pojo.Player;
import com.zrs.nba.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
class NbaApplicationTests {

    @Test
    void contextLoads() {
    }

    @Resource
    PlayerMapper playerMapper;
    @Resource
    PlayerService playerService;

    @Test
    public void test1(){
       List<Player> list= playerMapper.getAllPlayers();
       for(Player player:list){
           System.out.println(player.toString());
       }
    }
    @Test
    public void  getPLayer() throws IOException {
     Map<String,Object> map= playerService.getPLayer("999");
     System.out.println(map);
    }

    @Test
    public void addPlayer() throws IOException {
        Player player = new Player();
        player.setId(999);
        player.setDisplayName("杨超越");
        playerService.addPlayer(player,"999");
    }
}
