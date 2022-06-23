package de.ma.app.utils

import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import javax.enterprise.inject.Stereotype
import javax.transaction.Transactional

@QuarkusTest
@Stereotype
@TestTransaction
@Retention
@Target(AnnotationTarget.CLASS)
annotation class TransactionalQuarkusTest