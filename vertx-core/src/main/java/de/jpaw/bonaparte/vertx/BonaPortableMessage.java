/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.jpaw.bonaparte.vertx;

import java.util.concurrent.atomic.AtomicInteger;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.impl.BaseMessage;
import org.vertx.java.core.eventbus.impl.MessageFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.ByteArrayComposer;
import de.jpaw.bonaparte.core.ByteArrayParser;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.core.StaticMeta;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BonaPortableMessage extends BaseMessage<BonaPortable> {
    public static boolean doUsageStatistics = false;                            // avoid performance impact unless desired
    private static AtomicInteger cntSerializations = new AtomicInteger();       // just for usage statistics
    private static AtomicInteger cntDeserializations = new AtomicInteger();     // just for usage statistics
    public static int getSerializations() {
        return cntSerializations.get();
    }
    public static int getDeserializations() {
        return cntDeserializations.get();
    }
    
    private byte[] encoded = null;

    public BonaPortableMessage(boolean send, String address, BonaPortable body) {
        super(send, address, body);
    }

    public BonaPortableMessage(Buffer readBuff) {
        super(readBuff);
    }

    private void ensureEncodeFormAvailable() {
        if (encoded == null) {
            if (doUsageStatistics)
                cntSerializations.incrementAndGet();
            ByteArrayComposer bac = new ByteArrayComposer();
            bac.addField(StaticMeta.OUTER_BONAPORTABLE, body);
            encoded = bac.getBytes();
        }
    }

    @Override
    protected void readBody(int pos, Buffer readBuff) {
        boolean isNull = readBuff.getByte(pos) == (byte) 0;
        if (!isNull) {
            pos++;
            int strLength = readBuff.getInt(pos);
            pos += 4;
            byte[] bytes = readBuff.getBytes(pos, pos + strLength);
            ByteArrayParser bap = new ByteArrayParser(bytes, 0, -1);
            try {
                if (doUsageStatistics)
                    cntDeserializations.incrementAndGet();
                body = bap.readObject(StaticMeta.OUTER_BONAPORTABLE, BonaPortable.class);
            } catch (MessageParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void writeBody(Buffer buff) {
        if (body == null) {
            buff.appendByte((byte) 0);
        } else {
            ensureEncodeFormAvailable();
            buff.appendByte((byte) 1);
            buff.appendInt(encoded.length);
            buff.appendBytes(encoded);
        }
    }

    @Override
    protected int getBodyLength() {
        if (body == null) {
            return 1;
        } else {
            ensureEncodeFormAvailable();
            return 1 + 4 + encoded.length;
        }
    }

    @Override
    protected Message<BonaPortable> copy() {
        if (body.was$Frozen())
            return this;
        // body is mutable, return a copy
        try {
            return new BonaPortableMessage(send, address, body.ret$MutableClone(true, true));
        } catch (ObjectValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null; // FIXME
        }
    }

    @Override
    protected byte type() {
        return MessageFactory.TYPE_BONAPORTABLE_ASCII;
    }

}
