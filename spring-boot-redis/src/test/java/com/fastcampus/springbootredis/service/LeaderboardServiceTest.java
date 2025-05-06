package com.fastcampus.springbootredis.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LeaderboardServiceTest {
    @Autowired
    private LeaderboardService leaderboardService;

    @Test
    void leaderboardOperationsTest() {
        // Given
        String userId = "user2";
        double score = 100.0;

        // When
        leaderboardService.addScore(userId, score);
        List<String> topPlayers = leaderboardService.getTopPlayers(1);  // 상위 한 명의 플레이어 가져오기. 현재는 user2만 있으므로, 무조건 user2가 나옴
        Long rank = leaderboardService.getUserRank(userId); // user2의 랭크 가져오기. 현재는 user2만 있으므로, 무조건 0이 나옴

        // Then
        assertFalse(topPlayers.isEmpty());
        assertEquals(userId, topPlayers.get(0));
        assertEquals(0L, rank);
    }
}