package com.ias_software.test.domain.port;

import reactor.core.publisher.Mono;

public interface TransactionalExecutor {

	<T> Mono<T> execute(Mono<T> action);
}
