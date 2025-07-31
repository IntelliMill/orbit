package io.intellimill.orbit.agent.context;

import io.intellimill.orbit.agent.event.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * @author Sun Yuhan
 */
public class EventEmitter {

	private final Sinks.Many<String> sink;

	private final Flux<String> flux;

	public EventEmitter() {
		this.sink = Sinks.many().multicast().onBackpressureBuffer();
		this.flux = sink.asFlux();
	}

	public void emit(Event event) {
		sink.tryEmitNext(event.getContent());
	}

	public Flux<String> getFlux() {
		return flux;
	}

}
