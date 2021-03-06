/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oozie.util.db;

import org.apache.oozie.ErrorCode;
import org.apache.oozie.XException;
import org.apache.oozie.executor.jpa.JPAExecutorException;
import org.junit.Test;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.QueryTimeoutException;
import javax.persistence.RollbackException;
import javax.persistence.TransactionRequiredException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestPersistenceExceptionSubclassFilterRetryPredicate {
    private final PersistenceExceptionSubclassFilterRetryPredicate predicate =
            new PersistenceExceptionSubclassFilterRetryPredicate();

    @Test
    public void testFilteredJPAExceptions() {
        assertFalse(predicate.test(new EntityExistsException()));
        assertFalse(predicate.test(new EntityNotFoundException()));
        assertFalse(predicate.test(new LockTimeoutException()));
        assertFalse(predicate.test(new NoResultException()));
        assertFalse(predicate.test(new NonUniqueResultException()));
        assertFalse(predicate.test(new OptimisticLockException()));
        assertFalse(predicate.test(new PessimisticLockException()));
        assertFalse(predicate.test(new QueryTimeoutException()));
        assertFalse(predicate.test(new TransactionRequiredException()));
    }

    @Test
    public void testNotFilteredJPAExceptions() {
        assertTrue(predicate.test(new RollbackException()));
        assertTrue(predicate.test(new PersistenceException()));
    }

    @Test
    public void testNonJPAExceptions() {
        assertTrue(predicate.test(new IllegalStateException()));
        assertTrue(predicate.test(new Exception()));
    }

    @Test
    public void testNestedFilteredJPAExceptions() {
        assertFalse(predicate.test(wrapCause(new EntityExistsException())));
        assertFalse(predicate.test(wrapCause(new EntityNotFoundException())));
        assertFalse(predicate.test(wrapCause(new LockTimeoutException())));
        assertFalse(predicate.test(wrapCause(new NoResultException())));
        assertFalse(predicate.test(wrapCause(new NonUniqueResultException())));
        assertFalse(predicate.test(wrapCause(new OptimisticLockException())));
        assertFalse(predicate.test(wrapCause(new PessimisticLockException())));
        assertFalse(predicate.test(wrapCause(new QueryTimeoutException())));
        assertFalse(predicate.test(wrapCause(new TransactionRequiredException())));
    }

    @Test
    public void testNestedNotFilteredJPAExceptions() {
        assertTrue(predicate.test(wrapCause(new RollbackException())));
        assertTrue(predicate.test(wrapCause(new PersistenceException())));
    }

    @Test
    public void testNestedNonJPAExceptions() {
        assertTrue(predicate.test(wrapCause(new RuntimeException())));
        assertTrue(predicate.test(wrapCause(new Exception())));
    }

    @Test
    public void testPlainJPAExecutorExceptionWithMessage() {
        assertFalse(predicate.test(wrapMessage("No WorkflowJobBean found in database")));
        assertFalse(predicate.test(wrapMessage("Some other message")));

        assertFalse(predicate.test(wrapMessageRuntime("Some runtime problem")));
    }

    private JPAExecutorException wrapCause(final Throwable cause) {
        return new JPAExecutorException(new XException(ErrorCode.E0603, new PersistenceException(cause)));
    }

    private JPAExecutorException wrapMessage(final String message) {
        return new JPAExecutorException(ErrorCode.E0603, message);
    }

    private RuntimeException wrapMessageRuntime(final String message) {
        return new RuntimeException(message);
    }
}