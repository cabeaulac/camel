/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.jms.reply;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.camel.support.DefaultTimeoutMap;

/**
 * A {@link org.apache.camel.TimeoutMap} which is used to track reply messages which
 * has been timed out, and thus should trigger the waiting {@link org.apache.camel.Exchange} to
 * timeout as well.
 *
 * @version 
 */
public class CorrelationTimeoutMap extends DefaultTimeoutMap<String, ReplyHandler> {

    public CorrelationTimeoutMap(ScheduledExecutorService executor, long requestMapPollTimeMillis) {
        super(executor, requestMapPollTimeMillis);
    }

    public boolean onEviction(String key, ReplyHandler value) {
        // trigger timeout
        value.onTimeout(key);
        // return true to remove the element
        log.trace("Evicted correlationID: {}", key);
        return true;
    }

    @Override
    public void put(String key, ReplyHandler value, long timeoutMillis) {
        if (timeoutMillis <= 0) {
            // no timeout (must use Integer.MAX_VALUE)
            super.put(key, value, Integer.MAX_VALUE);
        } else {
            super.put(key, value, timeoutMillis);
        }
        log.trace("Added correlationID: {} to timeout after: {} millis", key, timeoutMillis);
    }

    @Override
    public ReplyHandler remove(String id) {
        ReplyHandler answer = super.remove(id);
        log.trace("Removed correlationID: {} -> {}", id, answer != null);
        return answer;
    }

}