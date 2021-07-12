package com.zrs.nba.service.impl;

import com.google.gson.Gson;
import com.zrs.nba.mapper.PlayerMapper;
import com.zrs.nba.pojo.Player;
import com.zrs.nba.service.PlayerService;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.seqno.RetentionLeaseActions;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.query.EarlyTerminatingCollector;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.PushBuilder;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final String SEARCH_INDEX = "nba";

    private static final int START_OFFSET = 0;

    private static final int MAX_COUNT = 1000;

    @Resource
    PlayerMapper playerMapper;

    @Resource
    RestHighLevelClient restHighLevelClient;

    @Override
    public List<Player> getAllPlayers() {
        return playerMapper.getAllPlayers();
    }
    /**
    * @Param [player, id]
    * @description  添加一个doc
    * @return void
    * @throws
    **/

    @Override
    public void addPlayer(Player player, String id) throws IOException {
        Gson gson = new Gson();
        String jsonString = gson.toJson(player);
        GsonJsonParser gsonJsonParser = new GsonJsonParser();
        Map<String, Object> map = gsonJsonParser.parseMap(jsonString);
        IndexRequest indexRequest = new IndexRequest(SEARCH_INDEX).id(id).source(map);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse.status());
    }

    /**
    * @Param [player, id]
    * @description  更新一个 doc
    * @return boolean
    * @throws
    **/
    @Override
    public boolean updatePlayer(String player, String id) throws IOException {
        GsonJsonParser gsonJsonParser = new GsonJsonParser();
        Map<String, Object> map = gsonJsonParser.parseMap(player);
        UpdateRequest updateRequest = new UpdateRequest(SEARCH_INDEX, id).doc(map);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse);
        return true;
    }

    /**
    * @Param [id]
    * @description  删除一个 doc
    * @return boolean
    * @throws
    **/
    @Override
    public boolean deletePlayer(String id) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(SEARCH_INDEX, id);
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteResponse);
        return true;
    }

    /**
    * @Param []
    * @description  删除所有 doc
    * @return boolean
    * @throws
    **/
    @Override
    public boolean deleteAllPlayer() throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(SEARCH_INDEX);
        BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        System.out.println(bulkByScrollResponse);
        return true;
    }

    /**
    * @Param []
    * @description  导入数据库的所有数据到ES
    * @return boolean
    * @throws
    **/
    @Override
    public boolean importAll() throws IOException {
        List<Player> playerList = playerMapper.getAllPlayers();
        for (Player player : playerList) {
            addPlayer(player, String.valueOf(player.getId()));
        }
        return true;
    }

    /**
    * @Param [key, value]
    * @description  通过名字全文匹配查询
    * @return java.util.List<com.zrs.nba.pojo.Player>
    * @throws
    **/
    @Override
    public List<Player> searchMatch(String key, String value) throws IOException {
        return search(key, value, QueryBuilders.matchQuery(key, value));

    }

    /**
    * @Param [key, value]
    * @description  通过 国家 和 队名 查询
    * @return java.util.List<com.zrs.nba.pojo.Player>
    * @throws
    **/
    @Override
    public List<Player> searchTerm(String key, String value) throws IOException {
        return search(key, value, QueryBuilders.termQuery(key, value));
    }

    /**
    * @Param [key, value]
    * @description  通过首字母查询
    * @return java.util.List<com.zrs.nba.pojo.Player>
    * @throws
    **/
    @Override
    public List<Player> searchMatchPrefix(String key, String value) throws IOException {
        return search(key, value, QueryBuilders.prefixQuery(key, value));
    }


    @Override
    public Map<String, Object> getPLayer(String id) throws IOException {
        GetRequest getRequest = new GetRequest(SEARCH_INDEX, id);
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        return getResponse.getSourceAsMap();
    }

    public List<Player> search(String key, String value, AbstractQueryBuilder builder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(SEARCH_INDEX);
        //SearchSourceBuilder 构建搜索源search source
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索类型
        searchSourceBuilder.query(builder);
        //最多显示
        searchSourceBuilder.from(START_OFFSET);
        searchSourceBuilder.size(MAX_COUNT);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮的字段
        highlightBuilder.field(key);
        //是否多个字段都高亮
        highlightBuilder.requireFieldMatch(false);
        //前缀后缀
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //设置搜索 request的搜索源
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //System.out.println(searchResponse);
        SearchHit[] hits = searchResponse.getHits().getHits();
        List<Player> list = new LinkedList<>();
        for (SearchHit searchHit : hits) {
            //System.out.println(searchHit);
            //System.out.println(searchHit.getSourceAsMap());
            //String stringHit = searchHit.getSourceAsString();
            Map<String,Object> searchHitMap=searchHit.getSourceAsMap();
            //解析高亮的字段,获取高亮字段
            Map<String, HighlightField> highlightFields=searchHit.getHighlightFields();
            HighlightField highlightField=highlightFields.get(key);
            //将原来的字段替换为高亮字段即可
            if(highlightField!=null){
                Text[] fragments = highlightField.fragments();
                StringBuilder newTitle = new StringBuilder();
                for (Text text : fragments) {
                    newTitle.append(text);
                }
                //替换掉原来的内容
                searchHitMap.put(key, newTitle.toString());
            }
            //System.out.println(stringHit);
            Gson gson = new Gson();
            Player player = gson.fromJson(gson.toJson(searchHitMap), Player.class);
            list.add(player);
        }

        return list;
    }
}
