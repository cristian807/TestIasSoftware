package com.ias_software.test.infrastructure.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.ias_software.test.domain.port.TransactionalExecutor;

import reactor.core.publisher.Mono;

@Component
public class ReactiveTransactionalExecutor implements TransactionalExecutor {

	private final TransactionalOperator operator;

	public ReactiveTransactionalExecutor(ReactiveTransactionManager transactionManager) {
		this.operator = TransactionalOperator.create(transactionManager);
	}

	@Override
	public <T> Mono<T> execute(Mono<T> action) {

        return operator.transactional(action);
	}
}
