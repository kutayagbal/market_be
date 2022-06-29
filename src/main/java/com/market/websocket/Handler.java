package com.market.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.market.entity.Stock;
import com.market.model.Quote;
import com.market.model.QuotePriceHistory;

@Component
public class Handler extends TextWebSocketHandler {
    private static Log logger = LogFactory.getLog(Handler.class);
    private ConcurrentHashMap<String, WebSocketSession> socketSessions;
    private final ObjectMapper jsonMapper;

    public Handler() {
        super();
        socketSessions = new ConcurrentHashMap<>();

        jsonMapper = new ObjectMapper();
        jsonMapper.registerModule(new JavaTimeModule());
        // jsonMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"));

        logger.info("#### web socket handler was created");
    }

    public void onTrade(Stock stock) {
        Quote quote = mapToQuote(stock);
        String quoteJSON;
        try {
            quoteJSON = jsonMapper.writeValueAsString(quote);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.info("#### JSON conversion error for quote: " + quote.toString());
            return;
        }

        socketSessions.entrySet().stream().forEach((Map.Entry<String, WebSocketSession> socketSession) -> {
            try {
                socketSession.getValue().sendMessage(new TextMessage(quoteJSON));
                logger.info("#### message sent to session: " + socketSession.toString());
            } catch (IOException e) {
                e.printStackTrace();
                logger.info("#### message can not be sent to session: " + socketSession.toString());
            }
        });
    }

    private Quote mapToQuote(Stock stock) {
        List<QuotePriceHistory> quotePriceHistory = stock.getPriceHistory().stream()
                .map(priceHistory -> new QuotePriceHistory(priceHistory.getTime(), priceHistory.getPrice()))
                .collect(Collectors.toList());
        return new Quote(stock.getName(), stock.getPrice(), stock.getBestBid(), stock.getBestAsk(),
                stock.getRate(), quotePriceHistory);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        if (session.getUri() != null && StringUtils.hasText(session.getUri().getQuery())) {
            socketSessions.put(session.getUri().getQuery(), session);
            logger.info("#### web socket connection established with session: " + session.toString());
        } else {
            session.close();
            logger.info("#### web socket session closed: " + session.toString());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        if (session.getUri() != null && StringUtils.hasText(session.getUri().getQuery())) {
            socketSessions.remove(session.getUri().getQuery());
            logger.info("#### web socket session removed: " + session.toString());
        }
    }

    // @Override
    // protected void handleTextMessage(WebSocketSession session, TextMessage
    // message) throws Exception {
    // // TODO Auto-generated method stub
    // super.handleTextMessage(session, message);

    // logger.info("#### handling websocket message: " + message.getPayload());
    // String payload = message.getPayload();
    // ObjectMapper mapper = new ObjectMapper();
    // JsonNode node = mapper.readTree(payload);
    // // JSONObject jsonObject = new JSONObject(payload);
    // // session.sendMessage(new TextMessage("Hi " + jsonObject.get("user") + " how
    // // may we help you?"));
    // session.sendMessage(new TextMessage("Hi " + node.get("username")));
    // }

}
