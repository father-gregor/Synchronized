package com.benlinus92.synchronize.event;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class VideolistReceivedEventHandler implements ApplicationListener<SessionSubscribeEvent> {

	@Override
	public void onApplicationEvent(SessionSubscribeEvent event) {
		System.out.println("SUBSCRIBE: " + event.getUser().getName() + "  ; PLACE: " + event.getMessage().toString());
		System.out.println("ID: " + event.getMessage().getHeaders().ID);
		for(String header: event.getMessage().getHeaders().keySet()) {
			System.out.println("HEADER: " + header);
		}
		System.out.println("\n\n");
	}

}
