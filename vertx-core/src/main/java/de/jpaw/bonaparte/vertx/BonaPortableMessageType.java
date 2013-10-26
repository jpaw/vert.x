package de.jpaw.bonaparte.vertx;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.UserMessageType;
import org.vertx.java.core.eventbus.impl.BaseMessage;
import org.vertx.java.core.eventbus.impl.MessageFactory;

import de.jpaw.bonaparte.core.BonaPortable;

public class BonaPortableMessageType implements UserMessageType<BonaPortable> {

    @Override
    public String getId() {
        return BonaPortable.class.getName();
    }

    @Override
    public byte getDefaultNumericUserMessageType() {
        return MessageFactory.USER_MESSAGE_TYPE_FIRST;
    }

    @Override
    public BaseMessage<BonaPortable> createMessage(Buffer buff) {
        return new BonaPortableMessage(buff);
    }

}
