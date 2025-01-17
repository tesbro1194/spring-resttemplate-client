package com.sparta.springresttemplateclient.service;

import com.sparta.springresttemplateclient.dto.ItemDto;
import com.sparta.springresttemplateclient.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RestTemplateService {

    private final RestTemplate restTemplate;

    public RestTemplateService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public ItemDto getCallObject(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/get-call-obj")
                .queryParam("query", query)  // @RequestParam
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        // Get 방식으로 서버에 요청 진행. 해당 uri(첫 번째 인자)에서 받을 데이터를 받아줄 타입(두 번쨰 인자).
        ResponseEntity<ItemDto> responseEntity = restTemplate.getForEntity(uri, ItemDto.class);

        log.info("statusCode = " + responseEntity.getStatusCode());

        return responseEntity.getBody();
    }

    public List<ItemDto> getCallList() {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/get-call-list")
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        log.info("statusCode = " + responseEntity.getStatusCode());
        log.info("Body = " + responseEntity.getBody());

        return fromJSONtoItems(responseEntity.getBody());
    }
    public ItemDto postCall(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/post-call/{query}")  // @PathVariable 방식
                .encode()
                .build()
                .expand(query)  // 위 {} 에 들어갈 값을 넣어준다
                .toUri();
        log.info("uri = " + uri);

        User user = new User("Robbie", "1234");
        // Post 방식으로 서버에 요청 진행. 해당 uri(첫 번째 인자)에서 받을 데이터를 받아줄 타입(세 번쨰 인자).
        // server의 server로 보낼 Body (두 번째 인자, RestTemplate에 의해 Json 형태로 변환)
        ResponseEntity<ItemDto> responseEntity = restTemplate.postForEntity(uri, user, ItemDto.class);

        log.info("statusCode = " + responseEntity.getStatusCode());

        return responseEntity.getBody();
    }

    public List<ItemDto> exchangeCall(String token) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/exchange-call")
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);
        // Body 에 들어갈 데이터
        User user = new User("Robbie", "1234");

        RequestEntity<User> requestEntity = RequestEntity
                .post(uri)
                .header("X-Authorization", token)
                .body(user);
        // server의 server로 보낼 RequestEntity (1번 인자)와 server의 server로부터 받을 데이터 타입(2번 인자)
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        return fromJSONtoItems(responseEntity.getBody());
    }

/*
    {
        "items":[
        {"title":"Mac","price":3888000},
        {"title":"iPad","price":1230000},
        {"title":"iPhone","price":1550000},
        {"title":"Watch","price":450000},
        {"title":"AirPods","price":350000}
	]
    }*/
    public List<ItemDto> fromJSONtoItems(String responseEntity) {
        JSONObject jsonObject = new JSONObject(responseEntity);
        JSONArray items  = jsonObject.getJSONArray("items");
        List<ItemDto> itemDtoList = new ArrayList<>();

        for (Object item : items) {
            ItemDto itemDto = new ItemDto((JSONObject) item);
            itemDtoList.add(itemDto);
        }

        return itemDtoList;
    }
}