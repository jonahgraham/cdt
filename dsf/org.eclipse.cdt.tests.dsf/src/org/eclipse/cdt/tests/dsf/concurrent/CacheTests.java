/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestCache;
import org.eclipse.cdt.tests.dsf.TestDsfExecutor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that exercise the DataCache object.
 */
public class CacheTests {

    TestDsfExecutor fExecutor;
    TestCache fTestCache;
    DataRequestMonitor<Integer> fRetrieveRm;
    
    class TestCache extends RequestCache<Integer> {
        
        public TestCache() {
            super(new ImmediateInDsfExecutor(fExecutor));
        }

        @Override
        protected void retrieve(DataRequestMonitor<Integer> rm) {
            synchronized(CacheTests.this) {
                fRetrieveRm = rm;
                CacheTests.this.notifyAll();
            }
        }
        
        @Override
        protected void reset() {
            super.reset();
        }
        
        @Override
        public void reset(Integer data, IStatus status) {
            super.reset(data, status);
        }
        
        @Override
        public void set(Integer data, IStatus status) {
            super.set(data, status);
        }
        
    }

    /**
     * There's no rule on how quickly the cache has to start data retrieval
     * after it has been requested.  It could do it immediately, or it could
     * wait a dispatch cycle, etc..
     */
    private void waitForRetrieveRm() {
        synchronized(this) {
            while (fRetrieveRm == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
    
    @Before 
    public void startExecutor() throws ExecutionException, InterruptedException {
        fExecutor = new TestDsfExecutor();
        fTestCache = new TestCache();
    }   
    
    @After 
    public void shutdownExecutor() throws ExecutionException, InterruptedException {
        fExecutor.submit(new DsfRunnable() { public void run() {
            fExecutor.shutdown();
        }}).get();
        if (fExecutor.exceptionsCaught()) {
            Throwable[] exceptions = fExecutor.getExceptions();
            throw new ExecutionException(exceptions[0]);
        }
        fRetrieveRm = null;
        fTestCache = null;
        fExecutor = null;
    }

    private void assertCacheValidWithData(Object data) {
        Assert.assertTrue(fTestCache.isValid());
        Assert.assertEquals(data, fTestCache.getData());
        Assert.assertTrue(fTestCache.getStatus().isOK());
    }

    private void assertCacheResetWithoutData() {
        Assert.assertFalse(fTestCache.isValid());
        Assert.assertEquals(null, fTestCache.getData());
        Assert.assertFalse(fTestCache.getStatus().isOK());
        Assert.assertEquals(fTestCache.getStatus().getCode(), IDsfStatusConstants.INVALID_STATE);
    }

    private void assertCacheWaiting() {
        Assert.assertFalse(fTestCache.isValid());
        Assert.assertEquals(null, fTestCache.getData());
        Assert.assertFalse(fTestCache.getStatus().isOK());
        Assert.assertEquals(fTestCache.getStatus().getCode(), IDsfStatusConstants.INVALID_STATE);
        Assert.assertFalse(fRetrieveRm.isCanceled());
    }

    private void assertCacheCanceled() {
        Assert.assertFalse(fTestCache.isValid());
        Assert.assertEquals(null, fTestCache.getData());
        Assert.assertFalse(fTestCache.getStatus().isOK());
        Assert.assertEquals(fTestCache.getStatus().getCode(), IDsfStatusConstants.INVALID_STATE);
        Assert.assertTrue(fRetrieveRm.isCanceled());
    }

    @Test 
    public void getWithCompletionInDsfThreadTest() throws InterruptedException, ExecutionException {
        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        // Check initial state
        Assert.assertFalse(fTestCache.isValid());
        Assert.assertFalse(fTestCache.getStatus().isOK());
        Assert.assertEquals(fTestCache.getStatus().getCode(), IDsfStatusConstants.INVALID_STATE);
        
        fExecutor.execute(q);
        
        // Wait until the cache requests the data.
        waitForRetrieveRm();
        
        // Check state while waiting for data
        Assert.assertFalse(fTestCache.isValid());

        // Complete the cache's retrieve data request.
        fExecutor.submit(new Callable<Object>() { public Object call() {
            fRetrieveRm.setData(1);
            fRetrieveRm.done();

            // Check that the data is available in the cache immediately
            // (in the same dispatch cycle).
            Assert.assertEquals(1, (int)fTestCache.getData());
            Assert.assertTrue(fTestCache.isValid());
            
            return null;
        }}).get();
        
        Assert.assertEquals(1, (int)q.get());
        
        // Re-check final state
        assertCacheValidWithData(1);
    }

    @Test 
    public void getTest() throws InterruptedException, ExecutionException {
        // Check initial state
        Assert.assertFalse(fTestCache.isValid());
        
        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q);
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Check state while waiting for data
        Assert.assertFalse(fTestCache.isValid());

        // Set the data without using an executor.  
        fRetrieveRm.setData(1);
        fRetrieveRm.done();
        
        Assert.assertEquals(1, (int)q.get());

        // Check final state
        assertCacheValidWithData(1);
    }

    @Test 
    public void getTestWithTwoClients() throws InterruptedException, ExecutionException {
        // Check initial state
        Assert.assertFalse(fTestCache.isValid());
        
        // Request data from cache
        Query<Integer> q1 = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q1);

        // Request data from cache again
        Query<Integer> q2 = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q2);
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Check state while waiting for data
        Assert.assertFalse(fTestCache.isValid());

        // Set the data without using an executor.  
        fRetrieveRm.setData(1);
        fRetrieveRm.done();
        
        Assert.assertEquals(1, (int)q1.get());
        Assert.assertEquals(1, (int)q2.get());

        // Check final state
        assertCacheValidWithData(1);
    }

    @Test 
    public void getTestWithManyClients() throws InterruptedException, ExecutionException {
        // Check initial state
        Assert.assertFalse(fTestCache.isValid());
        
        // Request data from cache
        List<Query<Integer>> qList = new ArrayList<Query<Integer>>(); 
        for (int i = 0; i < 10; i++) {
            Query<Integer> q = new Query<Integer>() { 
                @Override
                protected void execute(DataRequestMonitor<Integer> rm) {
                    fTestCache.update(rm);
                }
            };
            fExecutor.execute(q);
            qList.add(q);
        }
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Check state while waiting for data
        Assert.assertFalse(fTestCache.isValid());

        // Set the data without using an executor.  
        fRetrieveRm.setData(1);
        fRetrieveRm.done();

        for (Query<Integer> q : qList) {
            Assert.assertEquals(1, (int)q.get());            
        }
        
        // Check final state
        assertCacheValidWithData(1);
    }
    
    @Test 
    public void disableWithValueTest() throws InterruptedException, ExecutionException {
        // Disable the cache with a given value 
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.set(2, Status.OK_STATUS);
            }
        }).get();
        
        // Validate that cache is disabled without data.
        assertCacheValidWithData(2);
    }
    

    @Test 
    public void resetBeforeRequestTest() throws InterruptedException, ExecutionException {
        // Disable the cache with a given value 
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.reset();
            }
        }).get();
        
        assertCacheResetWithoutData();
        
        // Try to request data from cache (check that cache still works normally)
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q);
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();
        
        // Complete the request
        fRetrieveRm.setData(1);
        fRetrieveRm.done();

        // Check result
        Assert.assertEquals(1, (int)q.get());

        assertCacheValidWithData(1);
    }
    
    @Test 
    public void resetWhilePendingTest() throws InterruptedException, ExecutionException {
        // Request data from cache 
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q);

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();
        
        // Disable the cache with a given value 
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.reset();
            }
        }).get();
        
        assertCacheResetWithoutData();
        
        // Completed the retrieve RM
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        }).get();
        
        // Validate that cache is still disabled without data.
        assertCacheResetWithoutData();
    }

    @Test 
    public void cancelWhilePendingTest() throws InterruptedException, ExecutionException {
        // Request data from cache 
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q);

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel the client request
        q.cancel(true);
        try {
            q.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;
        
        assertCacheCanceled();

        // Completed the retrieve RM
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        }).get();

        // Validate that cache accepts the canceled request data
        assertCacheValidWithData(1);
    }

    @Test 
    public void cancelWhilePendingWithoutClientNotificationTest() throws InterruptedException, ExecutionException {
        // Request data from cache 
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(new DataRequestMonitor<Integer>(ImmediateExecutor.getInstance(), rm) {
                    @Override
                    public synchronized void addCancelListener(ICanceledListener listener) {
                        // Do not add the cancel listener so that the cancel request is not
                        // propagated to the cache.
                    }
                });
            }
        };
        fExecutor.execute(q);

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel the client request
        q.cancel(true);
        
        assertCacheCanceled();

        try {
            q.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;

        // Completed the retrieve RM
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        }).get();

        // Validate that cache accepts the canceled request data
        assertCacheValidWithData(1);
    }
    
    @Test 
    public void cancelWhilePendingWithTwoClientsTest() throws InterruptedException, ExecutionException {
        // Request data from cache 
        Query<Integer> q1 = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q1);

        // Request data from cache again
        Query<Integer> q2 = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q2);
        

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel the first client request
        q1.cancel(true);
        try {
            q1.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;
        assertCacheWaiting();

        // Cancel the second request
        q2.cancel(true);
        try {
            q2.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;

        assertCacheCanceled();

        // Completed the retrieve RM
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        }).get();

        // Validate that cache accepts the canceled request data
        assertCacheValidWithData(1);
    }

    @Test 
    public void cancelWhilePendingWithManyClientsTest() throws InterruptedException, ExecutionException {
        // Request data from cache 
        List<Query<Integer>> qList = new ArrayList<Query<Integer>>(); 
        for (int i = 0; i < 10; i++) {
            Query<Integer> q = new Query<Integer>() { 
                @Override
                protected void execute(DataRequestMonitor<Integer> rm) {
                    fTestCache.update(rm);
                }
            };
            fExecutor.execute(q);
            qList.add(q);
        }

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel some client requests
        int[] toCancel = new int[] { 0, 2, 5, 9};
        for (int i = 0; i < toCancel.length; i++) {
            
            // Cancel request and verify that its canceled
            Query<Integer> q = qList.get(toCancel[i]);
            q.cancel(true);
            try {
                q.get();
                Assert.fail("Expected a cancellation exception");
            } catch (CancellationException e) {} // Expected exception;
            qList.set(toCancel[i], null);
            
            assertCacheWaiting();
        }

        // Replace canceled requests with new ones
        for (int i = 0; i < toCancel.length; i++) {
            Query<Integer> q = new Query<Integer>() { 
                @Override
                protected void execute(DataRequestMonitor<Integer> rm) {
                    fTestCache.update(rm);
                }
            };
            fExecutor.execute(q);
            qList.set(toCancel[i], q);
            assertCacheWaiting();
        }

        // Now cancel all requests
        for (int i = 0; i < (qList.size() - 1); i++) {
            // Validate that cache is still waiting and is not canceled
            assertCacheWaiting();
            qList.get(i).cancel(true);
        }
        qList.get(qList.size() - 1).cancel(true);
        assertCacheCanceled();

        // Completed the retrieve RM
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        }).get();

        // Validate that cache accepts the canceled request data
        assertCacheValidWithData(1);
    }

    @Test 
    public void resetWhileValidTest() throws InterruptedException, ExecutionException {
        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataRequestMonitor<Integer> rm) {
                fTestCache.update(rm);
            }
        };
        fExecutor.execute(q);

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();
        
        // Complete the request
        fRetrieveRm.setData(1);
        fRetrieveRm.done();

        q.get();
        
        // Disable cache
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.reset();
            }
        }).get();
        
        // Check final state
        assertCacheResetWithoutData();
    }

    @Test 
    public void resetWithValueTest() throws InterruptedException, ExecutionException {
        // Disable the cache with a given value 
        fExecutor.submit(new DsfRunnable() {
            public void run() {
                fTestCache.reset(2, Status.OK_STATUS);
            }
        }).get();
        
        // Validate that cache is disabled without data.
        Assert.assertFalse(fTestCache.isValid());
        Assert.assertEquals(2, (int)fTestCache.getData());
        Assert.assertTrue(fTestCache.getStatus().isOK());
    }    
}
