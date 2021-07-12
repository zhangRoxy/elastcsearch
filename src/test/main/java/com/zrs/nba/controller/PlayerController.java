package com.zrs.nba.controller;

import com.zrs.nba.pojo.Player;
import com.zrs.nba.service.PlayerService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
public class PlayerController {
    @Resource
    PlayerService playerService;

    @PutMapping("/updatePlayer/{id}")
    public boolean updatePlayer(@RequestBody String player, @PathVariable String id) throws IOException {
        System.out.println(player);
        return playerService.updatePlayer(player, id);
    }

    @PostMapping("/searchMatch/{key}")
    public List<Player> searchMatch(@PathVariable String key) throws IOException {
        return playerService.searchMatch("displayNameEn", key);

    }

    @PostMapping("/searchTerm")
    public List<Player> searchTerm(@RequestParam(value = "country", required = false) String country,
                                   @RequestParam(value = "teamName", required = false) String teamName) throws IOException {
        if (StringUtils.hasLength(country))
            return playerService.searchMatch("country", country);
        else if (StringUtils.hasLength(teamName))
            return playerService.searchMatch("teamName", teamName);
        return null;
    }
    @PostMapping("/searchPrefix/{key}")
    public List<Player> searchPrefix(@PathVariable String key) throws IOException {
        return playerService.searchMatchPrefix("displayNameEn", key);
    }
}
